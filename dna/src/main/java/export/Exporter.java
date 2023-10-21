package export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import dna.Dna;
import logger.LogEvent;
import logger.Logger;
import me.tongfei.progressbar.ProgressBar;
import model.*;
import model.Color;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.ojalgo.array.DenseArray;
import org.ojalgo.array.Primitive64Array;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.matrix.decomposition.Eigenvalue;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Exporter class. This class contains functions for filtering statement array
 * lists, creating network matrices, and writing networks to files.
 */
public class Exporter {
	private StatementType statementType;
	private String networkType, qualifierAggregation;
	private Role role1, role2, qualifier;
	private String normalization, duplicates, timeWindow;
	private boolean role1Document, role2Document, qualifierDocument, isolates;
	private LocalDateTime startDateTime, stopDateTime;
	private int windowSize;
	private HashMap<String, ArrayList<String>> excludeValues;
	private ArrayList<String> excludeAuthors, excludeSources, excludeSections, excludeTypes;
	private boolean invertValues, invertAuthors, invertSources, invertSections, invertTypes;
	private String fileFormat, outfile;

	/**
	 * Holds all documents.
	 */
	private ArrayList<TableDocument> documents;
	/**
	 * Holds a mapping of document IDs to indices in the {@link #documents} array list.
	 */
	private HashMap<Integer, Integer> docMap;
	/**
	 * Holds all statements.
	 */
	private ArrayList<ExportEvent> originalEvents;
	/**
	 * Holds the statements that remain after filtering by date, exclude filter, duplicates etc.
	 */
	private ArrayList<ExportEvent> filteredEvents;
	/**
	 * Holds the resulting matrices. Can have size 1.
	 */
	private ArrayList<Matrix> matrixResults;
	/**
	 * Holds the resulting backbone result.
	 */
	//private BackboneResult backboneResult = null;

	// objects for backbone algorithm
	private ArrayList<Double> temperatureLog, acceptanceProbabilityLog, penalizedBackboneLossLog, acceptanceRatioLastHundredIterationsLog;
	private ArrayList<Integer> acceptedLog, proposedBackboneSizeLog, acceptedBackboneSizeLog, finalBackboneSizeLog;
	private String[] fullConcepts;
	private String selectedAction;
	private ArrayList<String> actionList, currentBackboneList, currentRedundantList, candidateBackboneList, candidateRedundantList, finalBackboneList, finalRedundantList;
	private ArrayList<ExportStatement> currentStatementList, candidateStatementList, finalStatementList; // declare candidate statement list at t
	private Matrix fullMatrix, currentMatrix, candidateMatrix, finalMatrix; // candidate matrix at the respective t, Y^{B^*_t}
	private boolean accept;
	private double p, temperature, acceptance, r, oldLoss, newLoss, finalLoss, log;
	private double[] eigenvaluesFull, eigenvaluesCurrent, eigenvaluesCandidate, eigenvaluesFinal;
	private int T, t;

	/**
	 * <p>Create a new Exporter class instance, holding an array list of export
	 * statements (i.e., statements with added document information and a hash
	 * map for easier access to variable values.
	 * 
	 * @param networkType The type of network to be exported. Valid values are:
	 *   <ul>
	 *     <li>{@code "twomode"} (to create a two-mode network)</li>
	 *     <li>{@code "onemode"} (to create a one-mode network)</li>
	 *     <li>{@code "eventlist"} (to create an event list)</li>
	 *   </ul>
	 * @param statementType The statement type.
	 * @param role1 The name of the first role, for example {@code
	 *   "organization"}. In addition to the roles defined in the statement
	 *   type, the document variables {@code author}, {@code source}, {@code
	 *   section}, {@code type}, {@code id}, and {@code title} are valid. If
	 *   document-level roles are used, this must be declared using the
	 *   {@code role1Document} argument.
	 * @param role2 The name of the second variable, for example {@code
	 *   "concept"}. In addition to the variables defined in the statement type,
	 *   the document variables {@code author}, {@code source}, {@code section},
	 *   {@code type}, {@code id}, and {@code title} are valid. If
	 *   document-level variables are used, this must be declared using the
	 *   {@code variable2Document} argument.
	 * @param qualifier The qualifier role, for example {@code
	 *  "agreement"}.
	 * @param qualifierAggregation The way in which the qualifier variable is
	 *   used to aggregate ties in the network.<br/>
	 *   Valid values if the {@code networkType} argument equals {@code
	 *   "onemode"} are:
	 *   <ul>
	 *     <li>{@code "ignore"} (for ignoring the qualifier variable)</li>
	 *     <li>{@code "congruence"} (for recording a network tie only if both
	 *       nodes have the same qualifier value in the binary case or for
	 *       recording the similarity between the two nodes on the qualifier
	 *       variable in the integer case)</li>
	 *     <li>{@code "conflict"} (for recording a network tie only if both
	 *       nodes have a different qualifier value in the binary case or for
	 *       recording the distance between the two nodes on the qualifier
	 *       variable in the integer case)</li>
	 *     <li>{@code "subtract"} (for subtracting the conflict tie value from
	 *       the congruence tie value in each dyad)</li>
	 *     <li>{@code "congruence & conflict"} (only applicable to time window
	 *       networks: add both a congruence and a conflict network to the time
	 *       window list of networks at each time step)</li>
	 *   </ul>
	 *   Valid values if the {@code networkType} argument equals {@code
	 *   "twomode"} are:
	 *   <ul>
	 *     <li>{@code "ignore"} (for ignoring the qualifier variable)</li>
	 *     <li>{@code "combine"} (for creating multiplex combinations, e.g.,
	 *       {@code 1} for positive, {@code 2} for negative, and {@code 3} for
	 *       mixed)</li>
	 *     <li>{@code "subtract"} (for subtracting negative from positive
	 *       ties)</li>
	 *   </ul>
	 *   The argument is ignored if {@code networkType} equals {@code
	 *   "eventlist"}.
	 * @param normalization Normalization of edge weights. Valid settings for
	 *   <em>one-mode</em> networks are:
     *   <ul>
	 *     <li>{@code "no"} (for switching off normalization)</li>
	 *     <li>{@code "average"} (for average activity normalization)</li>
	 *     <li>{@code "jaccard"} (for Jaccard coefficient normalization)</li>
	 *     <li>{@code "cosine"} (for cosine similarity normalization)</li>
	 *   </ul>
	 *   Valid settings for <em>two-mode</em> networks are:
	 *   <ul>
	 *     <li>{@code "no"} (for switching off normalization)</li>
	 *     <li>{@code "activity"} (for activity normalization)</li>
	 *     <li>{@code "prominence"} (for prominence normalization)</li>
	 *   </ul>
	 * @param isolates Should all nodes of the respective variable be included
	 *    in the network matrix ({@code true}), or should only those nodes be
	 *    included that are active in the current time period and are not
	 *    excluded ({@code false})?
	 * @param duplicates Setting for excluding duplicate statements before
	 *   network construction. Valid values are:
	 *   <ul>
	 *     <li>{@code "include"} (for including all statements in network
	 *       construction)</li>
	 *     <li>{@code "document"} (for counting only one identical statement per
	 *       document)</li>
	 *     <li>{@code "week"} (for counting only one identical statement per
	 *       calendar week as defined in the UK locale, i.e., Monday to Sunday)
	 *       </li>
	 *     <li>{@code "month"} (for counting only one identical statement per
	 *       calendar month)</li>
	 *     <li>{@code "year"} (for counting only one identical statement per
	 *       calendar year)</li>
	 *     <li>{@code "acrossrange"} (for counting only one identical statement
	 *       across the whole time range)</li>
	 *   </ul>
	 * @param startDateTime The start date and time for network construction.
	 *   All statements before this specified date/time will be excluded.
	 * @param stopDateTime The stop date and time for network construction.
	 *   All statements after this specified date/time will be excluded.
	 * @param timeWindow If any of the time units is selected, a moving time
	 *    window will be imposed, and only the statements falling within the
	 *    time period defined by the window will be used to create the network.
	 *    The time window will then be moved forward by one time unit at a time,
	 *    and a new network with the new time boundaries will be created. This
	 *    is repeated until the end of the overall time span is reached. All
	 *    time windows will be saved as separate network matrices in a list. The
	 *    duration of each time window is defined by the {@code windowsize}
	 *    argument. For example, this could be used to create a time window of
	 *    six months that moves forward by one month each time, thus creating
	 *    time windows that overlap by five months. If {@code "events"} is used
	 *    instead of a natural time unit, the time window will comprise exactly
	 *    as many statements as defined in the {@code windowsize} argument.
	 *    However, if the start or end statement falls on a date and time where
	 *    multiple events happen, those additional events that occur
	 *    simultaneously are included because there is no other way to decide
	 *    which of the statements should be selected. Therefore the window size
	 *    is sometimes extended when the start or end point of a time window is
	 *    ambiguous in event time. Valid argument values are:
	 *   <ul>
	 *     <li>{@code "no"} (no time window will be used)</li>
	 *     <li>{@code "events"} (time window length = number of statements)</li>
	 *     <li>{@code "seconds"} (number of seconds)</li>
	 *     <li>{@code "minutes"} (number of minutes)</li>
	 *     <li>{@code "hours"} (number of hours)</li>
	 *     <li>{@code "days"} (number of days)</li>
	 *     <li>{@code "weeks"} (number of calendar weeks)</li>
	 *     <li>{@code "months"} (number of calendar months)</li>
	 *     <li>{@code "years"} (number of calendar years)</li>
	 *   </ul>
	 *   @param windowSize The number of time units of which a moving time
	 *     window is comprised. This can be the number of statement events, the
	 *     number of days etc., as defined in the {@code timeWindow} argument.
	 *   @param excludeValues A hash map that contains values which should be
	 *     excluded during network construction. The hash map is indexed by
	 *     variable name (for example, {@code "organization"} as the key, and
	 *     the corresponding value is an array list of values to exclude, for
	 *     example {@code "org A"} or {@code "org B"}. This is irrespective of
	 *     whether these values appear in {@code variable1}, {@code variable2},
	 *     or the {@code qualifier} variable. Note that only variables at the
	 *     statement level can be used here. There are separate arguments for
	 *     excluding statements nested in documents with certain meta-data.
	 *   @param excludeAuthors An array of authors. If a statement is nested in
	 *     a document where one of these authors is set in the {@code author}
	 *     meta-data field, the statement is excluded from network construction.
	 *   @param excludeSources An array of sources. If a statement is nested in
	 *     a document where one of these sources is set in the {@code source}
	 *     meta-data field, the statement is excluded from network construction.
	 *   @param excludeSections An array of sections. If a statement is nested
	 *     in a document where one of these sections is set in the {@code
	 *     section} meta-data field, the statement is excluded from network
	 *     construction.
	 *   @param excludeTypes An array of types. If a statement is nested in a
	 *     document where one of these types is set in the {@code type}
	 *     meta-data field, the statement is excluded from network construction.
	 *   @param invertValues Indicates whether the entries provided by the
	 *     {@code excludeValues} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertAuthors Indicates whether the values provided by the
	 *     {@code excludeAuthors} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertSources Indicates whether the values provided by the
	 *     {@code excludeSources} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertSections Indicates whether the values provided by the
	 *     {@code excludeSections} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param invertTypes Indicates whether the values provided by the
	 *     {@code excludeTypes} argument should be excluded from network
	 *     construction ({@code false}) or if they should be the only values
	 *     that should be included during network construction ({@code true}).
	 *   @param fileFormat The file format specification for saving the
	 *     resulting network(s) to a file instead of returning an object. Valid
	 *     values are:
	 *     <ul>
	 *       <li>{@code "csv"} (for network matrices or event lists)</li>
	 *       <li>{@code "dl"} (for UCINET DL full-matrix files)</li>
	 *       <li>{@code "graphml"} (for visone {@code .graphml} files; this
	 *         specification is also compatible with time windows)</li>
	 *     </ul>
	 *   @param outfile The file name for saving the network.
	 */
	public Exporter(
			String networkType,
			StatementType statementType,
			Role role1,
			Role role2,
			Role qualifier,
			String qualifierAggregation,
			String normalization,
			boolean isolates,
			String duplicates,
			LocalDateTime startDateTime,
			LocalDateTime stopDateTime,
			String timeWindow,
			int windowSize,
			HashMap<String, ArrayList<String>> excludeValues,
			ArrayList<String> excludeAuthors,
			ArrayList<String> excludeSources,
			ArrayList<String> excludeSections,
			ArrayList<String> excludeTypes,
			boolean invertValues,
			boolean invertAuthors,
			boolean invertSources,
			boolean invertSections,
			boolean invertTypes,
			String fileFormat,
			String outfile) {

		// create a list of document variables for easier if-condition checking below
		ArrayList<String> documentVariables = new ArrayList<String>();
		documentVariables.add("author");
		documentVariables.add("source");
		documentVariables.add("section");
		documentVariables.add("type");
		documentVariables.add("id");
		documentVariables.add("title");

		// check network type
		// valid input: 'eventlist', 'twomode', or 'onemode'
		this.networkType = networkType;
		this.networkType = this.networkType.toLowerCase();
		if (!this.networkType.equals("eventlist") && !this.networkType.equals("twomode") && !this.networkType.equals("onemode")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Network type setting invalid.",
					"When exporting a network, the network type was set to be \"" + networkType + "\", but the only valid options are \"onemode\", \"twomode\", and \"eventlist\". Using the default value \"twomode\" in this case.");
			Dna.logger.log(le);
			this.networkType = "twomode";
		}

		// check statement type
		this.statementType = statementType;

		// check role1, role1Document, role2, and role22Document
		this.role1Document = role1Document;
		if (this.role1Document && !documentVariables.contains(role1.getRoleName())) {
			this.role1Document = false;
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Role 1 is not a document-level variable.",
					"When exporting a network, Role 1 was set to be a document-level variable, but \"" + role1.getRoleName() + "\" does not exist as a document-level variable. Trying to interpret it as a statement-level role instead.");
			Dna.logger.log(le);
		}

		this.role2Document = role2Document;
		if (this.role2Document && !documentVariables.contains(role2)) {
			this.role2Document = false;
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Role 2 is not a document-level variable.",
					"When exporting a network, Role 2 was set to be a document-level variable, but \"" + role2.getRoleName() + "\" does not exist as a document-level variable. Trying to interpret it as a statement-level role instead.");
			Dna.logger.log(le);
		}

		this.role1 = role1;
		this.role2 = role2;

		// check qualifier, qualifierDocument, and qualifierAggregation
		this.qualifierDocument = qualifierDocument;
		if (qualifier == null && this.qualifierDocument) {
			this.qualifierDocument = false;
		} else if (qualifier != null && this.qualifierDocument && !documentVariables.contains(qualifier)) {
			this.qualifierDocument = false;
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Qualifier variable is not a document-level variable.",
					"When exporting a network, the qualifier variable was set to be a document-level variable, but \"" + qualifier + "\" does not exist as a document-level variable. Trying to interpret it as a statement-level variable instead.");
			Dna.logger.log(le);
		}

		this.qualifierAggregation = qualifierAggregation.toLowerCase();
		this.qualifier = qualifier;

		if (!this.qualifierAggregation.equals("ignore") &&
				!this.qualifierAggregation.equals("subtract") &&
				!this.qualifierAggregation.equals("combine") &&
				!this.qualifierAggregation.equals("congruence") &&
				!this.qualifierAggregation.equals("conflict") &&
				!(this.qualifierAggregation.equals("congruence & conflict") && timeWindow.equals("events"))) {
			this.qualifierAggregation = "ignore";
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Qualifier aggregation setting invalid.",
					"When exporting a network, the qualifier aggregation setting was \"" + qualifierAggregation + "\". The only valid settings are \"ignore\", \"combine\", \"congruence\", and \"conflict\", depending on other settings. Using \"ignore\" now.");
			Dna.logger.log(le);
		}
		if (this.qualifierAggregation.equals("combine") && !this.networkType.equals("twomode")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Qualifier aggregation incompatible with network type.",
					"When exporting a network, the qualifier aggregation setting was \"combine\", but this setting is only compatible with two-mode networks. Using \"ignore\" now.");
			Dna.logger.log(le);
			this.qualifierAggregation = "ignore";
		}
		if ((this.qualifierAggregation.equals("congruence") || this.qualifierAggregation.equals("conflict")) && !this.networkType.equals("onemode")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Qualifier aggregation incompatible with network type.",
					"When exporting a network, the qualifier aggregation setting was \"" + this.qualifierAggregation + "\", but this setting is only compatible with one-mode networks. Using \"ignore\" now.");
			Dna.logger.log(le);
			this.qualifierAggregation = "ignore";
		}
		if (this.qualifier == null && !this.qualifierAggregation.equals("ignore")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Qualifier aggregation incompatible with qualifier variable.",
					"When exporting a network, the qualifier aggregation setting was \"" + this.qualifierAggregation + "\", but no qualifier variable was selected. Using \"ignore\" now.");
			Dna.logger.log(le);
			this.qualifierAggregation = "ignore";
		}

		// check normalization (valid values: 'no', 'activity', 'prominence', 'average', 'jaccard', or 'cosine')
		this.normalization = normalization.toLowerCase();
		if (!this.normalization.equals("no") &&
				!this.normalization.equals("activity") &&
				!this.normalization.equals("prominence") &&
				!this.normalization.equals("average") &&
				!this.normalization.equals("jaccard") &&
				!this.normalization.equals("cosine")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Normalization setting invalid.",
					"When exporting a network, normalization was set to \"" + normalization + "\", which is invalid. The only valid values are \"no\", \"activity\", \"prominence\", \"average\", \"jaccard\", and \"cosine\". Using the default value \"no\" in this case.");
			Dna.logger.log(le);
			this.normalization = "no";
		}
		if ((this.normalization.equals("activity") || this.normalization.equals("prominence")) && !this.networkType.equals("twomode")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Normalization setting invalid.",
					"When exporting a network, normalization was set to \"" + normalization + "\", which is only possible with two-mode networks. Using the default value \"no\" in this case.");
			Dna.logger.log(le);
			this.normalization = "no";
		}
		if ((this.normalization.equals("average") || this.normalization.equals("jaccard") || this.normalization.equals("cosine")) && !this.networkType.equals("onemode")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Normalization setting invalid.",
					"When exporting a network, normalization was set to \"" + normalization + "\", which is only possible with one-mode networks. Using the default value \"no\" in this case.");
			Dna.logger.log(le);
			this.normalization = "no";
		}

		// isolates setting
		this.isolates = isolates;

		// check duplicates setting (valid settings: 'include', 'document', 'week', 'month', 'year', or 'acrossrange')
		this.duplicates = duplicates.toLowerCase();
		if (!this.duplicates.equals("include") &&
				!this.duplicates.equals("document") &&
				!this.duplicates.equals("week") &&
				!this.duplicates.equals("month") &&
				!this.duplicates.equals("year") &&
				!this.duplicates.equals("acrossrange")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Duplicates setting invalid.",
					"When exporting a network, the duplicates setting was \"" + duplicates + "\", which is invalid. The only valid values are \"include\", \"document\", \"week\", \"month\", \"year\", and \"acrossrange\". Using the default value \"include\" in this case.");
			Dna.logger.log(le);
			this.duplicates = "include";
		}

		// check time window arguments
		this.timeWindow = timeWindow;
		if (this.timeWindow == null) {
			this.timeWindow = "no";
		} else if (!this.timeWindow.equals("no") &&
				!this.timeWindow.equals("seconds") &&
				!this.timeWindow.equals("minutes") &&
				!this.timeWindow.equals("hours") &&
				!this.timeWindow.equals("days") &&
				!this.timeWindow.equals("weeks") &&
				!this.timeWindow.equals("months") &&
				!this.timeWindow.equals("years") &&
				!this.timeWindow.equals("events")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Time window setting invalid.",
					"When exporting a network, the time window setting was \"" + this.timeWindow + "\", which is invalid. The only valid values are \"no\", \"seconds\", \"minutes\", \"hours\", \"days\", \"weeks\", \"months\", \"years\", and \"events\". Using the default value \"no\" in this case.");
			Dna.logger.log(le);
			this.timeWindow = "no";
		}
		this.windowSize = windowSize;
		if (this.windowSize < 1 && !this.timeWindow.equals("no")) {
			LogEvent le = new LogEvent(Logger.WARNING,
					"Exporter: Time window size invalid.",
					"When exporting a network, the time window size was " + this.windowSize + ", which is invalid in combination with a time window setting other than \"no\". Using the minimum value of 1 in this case.");
			Dna.logger.log(le);
			this.windowSize = 1;
		}

		// check file export format and file name arguments
		if (fileFormat != null) {
			this.fileFormat = fileFormat.toLowerCase();
			if (!this.fileFormat.equals("csv") && !this.fileFormat.equals("dl") && !this.fileFormat.equals("graphml")) {
				LogEvent le = new LogEvent(Logger.WARNING,
						"Exporter: File format invalid.",
						"When exporting a network, the file format setting was " + this.fileFormat + ", but \"csv\", \"dl\", and \"graphml\" are the only valid settings. Using \"graphml\" in this case.");
				Dna.logger.log(le);
				this.fileFormat = "graphml";
			}
		} else {
			this.fileFormat = null;
		}
		this.outfile = outfile;
		if (this.outfile != null) {
			if (this.fileFormat.equals("graphml") && !this.outfile.toLowerCase().endsWith(".graphml")) {
				this.outfile = this.outfile + ".graphml";
			} else if (this.fileFormat.equals("csv") && !this.outfile.toLowerCase().endsWith(".csv")) {
				this.outfile = this.outfile + ".csv";
			} else if (this.fileFormat.equals("dl") && !this.outfile.toLowerCase().endsWith(".dl")) {
				this.outfile = this.outfile + ".dl";
			}
		}

		// remaining arguments
		this.startDateTime = startDateTime;
		this.stopDateTime = stopDateTime;
		this.excludeValues = excludeValues;
		this.invertValues = invertValues;
		this.excludeAuthors = excludeAuthors;
		this.invertAuthors = invertAuthors;
		this.excludeSources = excludeSources;
		this.invertSources = invertSources;
		this.excludeSections = excludeSections;
		this.invertSections = invertSections;
		this.excludeTypes = excludeTypes;
		this.invertTypes = invertTypes;
	}

	private class ExportEvent {
		LocalDateTime dateTime;
		String variable1;
		String variable2;
		int qualifier;
		int documentId;

		public ExportEvent(String variable1, String variable2, int qualifier, int documentId, LocalDateTime dateTime) {
			this.variable1 = variable1;
			this.variable2 = variable2;
			this.qualifier = qualifier;
			this.documentId = documentId;
			this.dateTime = dateTime;
		}

		public ExportEvent(ExportEvent other) {
			this.variable1 = other.variable1;
			this.variable2 = other.variable2;
			this.qualifier = other.qualifier;
			this.documentId = other.documentId;
			this.dateTime = other.dateTime;
		}

		public LocalDateTime getDateTime() {
			return dateTime;
		}

		public void setDateTime(LocalDateTime dateTime) {
			this.dateTime = dateTime;
		}

		public String getVariable1() {
			return variable1;
		}

		public void setVariable1(String variable1) {
			this.variable1 = variable1;
		}

		public String getVariable2() {
			return variable2;
		}

		public void setVariable2(String variable2) {
			this.variable2 = variable2;
		}

		public int getQualifier() {
			return qualifier;
		}

		public void setQualifier(int qualifier) {
			this.qualifier = qualifier;
		}

		public int getDocumentId() {
			return documentId;
		}

		public void setDocumentId(int documentId) {
			this.documentId = documentId;
		}
	}

	/**
	 * Load statements and documents from the database and pre-process them.
	 */
	public void loadData() {

		// get documents and create document hash map for quick lookup
		this.documents = Dna.sql.getTableDocuments(new int[0]);
		Collections.sort(documents);
		this.docMap = new HashMap<Integer, Integer>(); // document ID to index in this.documents
		for (int i = 0; i < documents.size(); i++) {
			docMap.put(documents.get(i).getId(), i);
		}

		// get original statements
		ArrayList<TableStatement> statements = Dna.sql.getTableStatements(new int[0],
				this.statementType.getId(),
				this.startDateTime,
				this.stopDateTime,
				this.excludeAuthors,
				this.invertAuthors,
				this.excludeSources,
				this.invertSources,
				this.excludeSections,
				this.invertSections,
				this.excludeTypes,
				this.invertTypes);

		// filter excluded values
		TableStatement s;
		boolean contains;
		for (int i = statements.size() - 1; i >= 0; i--) {
			boolean select = true;
			s = statements.get(i);
			for (int j = 0; j < s.getRoleValues().size(); j++) {
				if (this.excludeValues.containsKey(s.getRoleValues().get(j).getRoleName()) && this.excludeValues.get(s.getRoleValues().get(j).getRoleName()).contains(s.getRoleValues().get(j).toString())) {
					contains = true;
				} else {
					contains = false;
				}
				if (contains && !this.invertValues || !contains && this.invertValues) {
					select = false;
				}
			}
			if (!select) {
				statements.remove(i);
			}
		}

		// create event list
		this.originalEvents = new ArrayList<>();
		RoleValue rv;
		for (int i = 0; i < statements.size(); i++) {
			String var1 = "";
			String var2 = "";
			Integer qual = null;
			for (int j = 0; j < statements.get(i).getRoleValues().size(); j++) {
				rv = statements.get(i).getRoleValues().get(j);
				if (rv.getRoleId() == role1.getId()) {
					var1 = rv.toString();
				}
				if (rv.getRoleId() == role2.getId()) {
					var2 = rv.toString();
				}
				if (rv.getRoleId() == this.qualifier.getId()) {
					qual = (Integer) rv.getValue();
				}
			}
			if (!var1.equals("") && !var2.equals("") && qual != null) {
				this.originalEvents.add(new ExportEvent(var1, var2, qual, statements.get(i).getDocumentId(), statements.get(i).getDateTime()));
			}
		}

		if (this.originalEvents.size() == 0) {
			Dna.logger.log(
					new LogEvent(Logger.WARNING,
							"No statements found.",
							"When processing data for export, no statements were found in the database in the time period under scrutiny and given any document-level exclusion filters.")
			);
		}
	}

	public class ExportEventComparator implements Comparator<ExportEvent> {
		@Override
		public int compare(ExportEvent event1, ExportEvent event2) {
			int dateComparison = event1.getDateTime().compareTo(event2.getDateTime());
			if (dateComparison != 0) {
				return dateComparison;
			} else {
				return Integer.compare(event1.getDocumentId(), event2.getDocumentId());
			}
		}
	}

	public void filterExportEvents() {
		try (ProgressBar pb = new ProgressBar("Filtering export events...", this.originalEvents.size())) {
			pb.stepTo(0);

			// create a deep copy of the original statements and sort
			this.filteredEvents = new ArrayList<>();
			for (int i = 0; i < this.originalEvents.size(); i++) {
				this.filteredEvents.add(new ExportEvent(this.originalEvents.get(i)));
			}
			Collections.sort(this.filteredEvents, new ExportEventComparator());

			// process and exclude events
			ExportEvent s;
			ArrayList<ExportEvent> al = new ArrayList<>();
			String previousVar1 = null;
			String previousVar2 = null;
			String previousQualifier = null;
			LocalDateTime cal, calPrevious;
			int year, month, week, yearPrevious, monthPrevious, weekPrevious;
			for (int i = 0; i < this.filteredEvents.size(); i++) {
				s = this.filteredEvents.get(i);
				boolean select = true;

				// check for duplicates
				cal = s.getDateTime();
				year = cal.getYear();
				month = cal.getMonthValue();
				@SuppressWarnings("static-access")
				WeekFields weekFields = WeekFields.of(Locale.UK.getDefault()); // use UK definition of calendar weeks
				week = cal.get(weekFields.weekOfWeekBasedYear());
				if (!this.duplicates.equals("include")) {
					for (int j = al.size() - 1; j >= 0; j--) {
						previousVar1 = al.get(j).getVariable1();
						previousVar2 = al.get(j).getVariable2();
						previousQualifier = String.valueOf(al.get(j).getQualifier());
						calPrevious = al.get(j).getDateTime();
						yearPrevious = calPrevious.getYear();
						monthPrevious = calPrevious.getMonthValue();
						@SuppressWarnings("static-access")
						WeekFields weekFieldsPrevious = WeekFields.of(Locale.UK.getDefault()); // use UK definition of calendar weeks
						weekPrevious = calPrevious.get(weekFieldsPrevious.weekOfWeekBasedYear());
						if (((al.get(j).getDocumentId() == s.getDocumentId() && duplicates.equals("document"))
								|| duplicates.equals("acrossrange")
								|| (duplicates.equals("year") && year == yearPrevious)
								|| (duplicates.equals("month") && month == monthPrevious)
								|| (duplicates.equals("week") && week == weekPrevious) )
								&& this.filteredEvents.get(i).getVariable1().equals(previousVar1)
								&& this.filteredEvents.get(i).getVariable2().equals(previousVar2)
								&& (this.qualifierAggregation.equals("ignore") || String.valueOf(this.filteredEvents.get(i).getQualifier()).equals(previousQualifier))) {
							select = false;
							break;
						}
					}
				}

				// add only if the statement passed all checks
				if (select) {
					al.add(s);
				}

				pb.stepTo(i + 1);
			}
			this.filteredEvents = al;
			pb.stepTo(this.originalEvents.size());
		}
	}

	/**
	 * Count how often a value is used across the range of filtered statements.
	 * 
	 * @param variableValues String array of all values of a certain variable in
	 *   a set of statements.
	 * @param uniqueNames String array of unique values of the same variable
	 *   across all statements.
	 * @return {@link int} array of value frequencies for each unique value in
	 *   same order as {@code uniqueNames}.
	 */
	private int[] countFrequencies(String[] variableValues, String[] uniqueNames) {
		int[] frequencies = new int[uniqueNames.length];
		for (int i = 0; i < uniqueNames.length; i++) {
			for (int j = 0; j < variableValues.length; j++) {
				if (uniqueNames[i].equals(variableValues[j])) {
					frequencies[i] = frequencies[i] + 1;
				}
			}
		}
		return frequencies;
	}

	/**
	 * Lexical ranking of a binary vector.
	 *
	 * Examples:
	 *
	 * [0, 0] -> 0
	 * [0, 1] -> 1
	 * [1, 0] -> 2
	 * [1, 1] -> 3
	 * [0, 0, 1, 0, 1, 0] -> 10
	 *
	 * This bijection is used to map combinations of qualifier values into edge
	 * weights in the resulting network matrix.
	 *
	 * Source: <a href="https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf">https://cw.fel.cvut.cz/wiki/_media/courses/b4m33pal/pal06.pdf</a>.
	 *
	 * @param binaryVector A binary int array of arbitrary length, indicating
	 *   which qualifier values are used in the dataset
	 * @return An integer
	 */
	private int lexRank(int[] binaryVector) {
		int n = binaryVector.length;
		int r = 0;
		for (int i = 0; i < n; i++) {
			if (binaryVector[i] > 0) {
				r = r + (int) Math.pow(2, n - i - 1);
			}
		}
		return r;
	}

	private double[][][] createArray(ArrayList<ExportEvent> processedEvents) {

		// unique values
		int[] qualifier = new int[] {0, 1};
		String[] names1 = processedEvents.stream().map(p -> p.getVariable1()).distinct().sorted().toArray(String[]::new);
		String[] names2 = processedEvents.stream().map(p -> p.getVariable2()).distinct().sorted().toArray(String[]::new);
		
		// create and populate array
		double[][][] array = new double[names1.length][names2.length][qualifier.length]; // 3D array: rows x cols x qualifier value
		for (int i = 0; i < processedEvents.size(); i++) {
			String n1 = processedEvents.get(i).getVariable1(); // retrieve first value from statement
			String n2 = processedEvents.get(i).getVariable2(); // retrieve second value from statement
			int q = processedEvents.get(i).getQualifier();
			
			// find out which matrix row corresponds to the first value
			int row = -1;
			for (int j = 0; j < names1.length; j++) {
				if (names1[j].equals(n1)) {
					row = j;
					break;
				}
			}
			
			// find out which matrix column corresponds to the second value
			int col = -1;
			for (int j = 0; j < names2.length; j++) {
				if (names2[j].equals(n2)) {
					col = j;
					break;
				}
			}
			
			// find out which qualifier level corresponds to the qualifier value
			int qual = 0;
			for (int j = 0; j < qualifier.length; j++) {
				if (qualifier[j] == q) {
					qual = j;
					break;
				}
			}

			// add match to matrix (note that duplicates were dealt with at the statement filter stage)
			array[row][col][qual] = array[row][col][qual] + 1.0;
		}
		
		return array;
	}

	/**
	 * Compute the results. Choose the right method based on the settings.
	 */
	public void computeResults() {
		if (networkType.equals("onemode")) {
			computeOneModeMatrix();
		} else if (networkType.equals("twomode")) {
			computeTwoModeMatrix();
		}
	}

	/**
	 * Wrapper method to compute one-mode network matrix with class settings and save within class.
	 */
	private void computeOneModeMatrix() {
		ArrayList<Matrix> matrices = new ArrayList<Matrix>();
		matrices.add(this.computeOneModeMatrix(this.filteredEvents,	this.qualifierAggregation, this.startDateTime, this.stopDateTime));
		this.matrixResults = matrices;
	}

	Matrix computeOneModeMatrix(ArrayList<ExportEvent> processedEvents, String aggregation, LocalDateTime start, LocalDateTime stop) {

		// unique values
		int[] qualifier = new int[] {0, 1};
		String[] names1 = this.filteredEvents.stream().map(p -> p.getVariable1()).distinct().sorted().toArray(String[]::new);
		String[] names2 = this.filteredEvents.stream().map(p -> p.getVariable2()).distinct().sorted().toArray(String[]::new);

		if (processedEvents.size() == 0) {
			double[][] m = new double[names1.length][names1.length];
			Matrix mt = new Matrix(m, names1, names1, true, start, stop);
			return mt;
		}

		double[][][] array = createArray(processedEvents);
		double[][] mat1 = new double[names1.length][names1.length];  // square matrix for results

		double i1count = 0.0;
		double i2count = 0.0;
		for (int i1 = 0; i1 < names1.length; i1++) {
			for (int i2 = 0; i2 < names1.length; i2++) {
				if (i1 != i2) {
					for (int j = 0; j < names2.length; j++) {
						// "ignore": sum up i1 and i2 independently over levels of k, then multiply.
						// In the binary case, this amounts to counting how often each concept is used and then multiplying frequencies.
						if (aggregation.equals("ignore")) {
							i1count = 0.0;
							i2count = 0.0;
							for (int k = 0; k < qualifier.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
							mat1[i1][i2] = mat1[i1][i2] + i1count * i2count;
						} else {
							for (int k1 = 0; k1 < qualifier.length; k1++) {
								for (int k2 = 0; k2 < qualifier.length; k2++) {
									if (aggregation.equals("congruence")) {
										// "congruence": sum up proximity of i1 and i2 per level of k, weighted by joint usage.
										// In the binary case, this reduces to the sum of weighted matches per level of k
										mat1[i1][i2] = mat1[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * (1.0 - ((Math.abs(qualifier[k1] - qualifier[k2])))));
									} else if (aggregation.equals("conflict")) {
										// "conflict": same as congruence, but distance instead of proximity
										mat1[i1][i2] = mat1[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * ((Math.abs(qualifier[k1] - qualifier[k2]))));
									} else if (aggregation.equals("subtract")) {
										// "subtract": congruence - conflict
										mat1[i1][i2] = mat1[i1][i2] + (array[i1][j][k1] * array[i2][j][k2] * (1.0 - ((Math.abs(qualifier[k1] - qualifier[k2])))));
										mat1[i1][i2] = mat1[i1][i2] - (array[i1][j][k1] * array[i2][j][k2] * ((Math.abs(qualifier[k1] - qualifier[k2]))));
									}
								}
							}
						}
					}

					// normalization
					double norm = 1.0;
					if (this.normalization.equals("no")) {
						norm = 1.0;
					} else if (this.normalization.equals("average")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifier.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = (i1count + i2count) / 2;
					} else if (this.normalization.equals("jaccard")) {
						double m10 = 0.0;
						double m01 = 0.0;
						double m11 = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifier.length; k++) {
								if (array[i2][j][k] == 0) {
									m10 = m10 + array[i1][j][k];
								}
								if (array[i1][j][k] == 0) {
									m01 = m01 + array[i2][j][k];
								}
								if (array[i1][j][k] > 0 && array[i2][j][k] > 0) {
									m11 = m11 + (array[i1][j][k] * array[i2][j][k]);
								}
							}
						}
						norm = m01 + m10 + m11;
					} else if (this.normalization.equals("cosine")) {
						i1count = 0.0;
						i2count = 0.0;
						for (int j = 0; j < names2.length; j++) {
							for (int k = 0; k < qualifier.length; k++) {
								i1count = i1count + array[i1][j][k];
								i2count = i2count + array[i2][j][k];
							}
						}
						norm = Math.sqrt(i1count * i1count) * Math.sqrt(i2count * i2count);
					}
					if (norm == 0) {
						mat1[i1][i2] = 0;
					} else {
						mat1[i1][i2] = mat1[i1][i2] / norm;
					}
				}
			}
		}

		// does the matrix contain only integer values? (i.e., no normalization and boolean or short text qualifier)
		boolean integerBoolean;
		if (this.normalization.equals("no")) {
			integerBoolean = true;
		} else {
			integerBoolean = false;
		}

		Matrix matrix = new Matrix(mat1, names1, names1, integerBoolean, start, stop);
		matrix.setNumStatements(this.filteredEvents.size());
		return matrix;
	}

	/**
	 * Wrapper method to compute two-mode network matrix with class settings.
	 */
	public void computeTwoModeMatrix() {
		ArrayList<Matrix> matrices = new ArrayList<Matrix>();
		matrices.add(this.computeTwoModeMatrix(this.filteredEvents,	this.startDateTime, this.stopDateTime));
		this.matrixResults = matrices;
	}

	/**
	 * Create a two-mode network {@link Matrix}.
	 *
	 * @param processedEvents Usually the filtered list of export
	 *   statements, but it can be a more processed list of export statements,
	 *   for example for use in constructing a time window sequence of networks.
	 * @param start Start date/time.
	 * @param stop End date/time.
	 * @return {@link Matrix Matrix} object containing a two-mode network matrix.
	 */
	private Matrix computeTwoModeMatrix(ArrayList<ExportEvent> processedEvents, LocalDateTime start, LocalDateTime stop) {

		// unique values
		int[] qualifier = new int[] {0, 1};
		String[] names1 = this.filteredEvents.stream().map(p -> p.getVariable1()).distinct().sorted().toArray(String[]::new);
		String[] names2 = this.filteredEvents.stream().map(p -> p.getVariable2()).distinct().sorted().toArray(String[]::new);

		if (processedEvents.size() == 0) {
			double[][] m = new double[names1.length][names2.length];
			Matrix mt = new Matrix(m, names1, names2, true, start, stop);
			return mt;
		}

		double[][][] array = createArray(processedEvents);

		// combine levels of the qualifier variable conditional on qualifier aggregation option
		double[][] mat = new double[names1.length][names2.length];  // initialized with zeros
		HashMap<Integer, ArrayList> combinations = new HashMap<Integer, ArrayList>();
		for (int i = 0; i < names1.length; i++) {
			for (int j = 0; j < names2.length; j++) {
				if (this.qualifierAggregation.equals("combine")) { // combine
					double[] vec = array[i][j]; // may be weighted, so create a second, binary vector vec2
					int[] vec2 = new int[vec.length];
					ArrayList qualVal;
					qualVal = new ArrayList<Integer>();
					for (int k = 0; k < vec.length; k++) {
						if (vec[k] > 0) {
							vec2[k] = 1;
						}
						qualVal.add(qualifier[k]);
					}
					int lr = lexRank(vec2);
					mat[i][j] = lr; // compute lexical rank, i.e., map the combination of values to a single integer
					combinations.put(lr, qualVal); // the bijection needs to be stored for later reporting
				} else {
					for (int k = 0; k < qualifier.length; k++) {
						if (this.qualifierAggregation.equals("ignore")) { // ignore
							mat[i][j] = mat[i][j] + array[i][j][k]; // duplicates were already filtered out in the statement filter, so just add
						} else if (this.qualifierAggregation.equals("subtract")) { // subtract
							if (qualifier[k] == 0) { // zero category: subtract number of times this happens from edge weight
								mat[i][j] = mat[i][j] - array[i][j][k];
							} else if (qualifier[k] > 0) { // one category: add number of times this happens to edge weight
								mat[i][j] = mat[i][j] + array[i][j][k];
							}
						}
					}
				}
			}
		}

		// report combinations if necessary
		if (combinations.size() > 0) {
			String s = "";
			Iterator<Integer> keyIterator = combinations.keySet().iterator();
			while (keyIterator.hasNext()){
				Integer key = (Integer) keyIterator.next();
				ArrayList<Integer> values = combinations.get(key);
				s = "An edge weight of " + key + " maps onto combination: ";
				for (int i = 0; i < values.size(); i++) {
					s = s + values.get(i) + " ";
				}
				s = s + "\n";
			}
			LogEvent l = new LogEvent(Logger.MESSAGE,
					"Qualifier \"combine\" option combinatorial mapping established.",
					s);
			Dna.logger.log(l);
		}

		// normalization
		boolean integerBoolean = false;
		if (this.normalization.equals("no")) {
			integerBoolean = true;
		} else if (this.normalization.equals("activity")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names1.length; i++) {
				currentDenominator = 0.0;
				if (qualifierAggregation.equals("ignore")) { // iterate through columns of matrix and sum weighted values
					for (int j = 0; j < names2.length; j++) {
						currentDenominator = currentDenominator + mat[i][j];
					}
				} else if (qualifierAggregation.equals("combine")) { // iterate through columns of matrix and count how many are larger than one
					LogEvent l = new LogEvent(Logger.WARNING,
							"Normalization and \"combine\" yield uninterpretable results.",
							"When exporting a network, the use of normalization and the qualifier setting \"combine\" were used together. These settings together yield results that cannot be interpreted in a meaningful way.");
					Dna.logger.log(l);
					for (int j = 0; j < names2.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (qualifierAggregation.equals("subtract")) { // iterate through array and sum for different levels
					for (int j = 0; j < names2.length; j++) {
						for (int k = 0; k < qualifier.length; k++) {
							currentDenominator = currentDenominator + array[i][j][k];
						}
					}
				}
				for (int j = 0; j < names2.length; j++) { // divide all values by current denominator
					if (currentDenominator == 0) {
						mat[i][j] = 0;
					} else {
						mat[i][j] = mat[i][j] / currentDenominator;
					}
				}
			}
		} else if (this.normalization.equals("prominence")) {
			integerBoolean = false;
			double currentDenominator;
			for (int i = 0; i < names2.length; i++) {
				currentDenominator = 0.0;
				if (this.qualifierAggregation.equals("ignore")) { // iterate through rows of matrix and sum weighted values
					for (int j = 0; j < names1.length; j++) {
						currentDenominator = currentDenominator + mat[j][i];
					}
				} else if (this.qualifierAggregation.equals("combine")) { // iterate through rows of matrix and count how many are larger than one
					LogEvent l = new LogEvent(Logger.WARNING,
							"Normalization and \"combine\" yield uninterpretable results.",
							"When exporting a network, the use of normalization and the qualifier setting \"combine\" were used together. These settings together yield results that cannot be interpreted in a meaningful way.");
					Dna.logger.log(l);
					for (int j = 0; j < names1.length; j++) {
						if (mat[i][j] > 0.0) {
							currentDenominator = currentDenominator + 1.0;
						}
					}
				} else if (this.qualifierAggregation.equals("subtract")) { // iterate through array and sum for different levels
					for (int j = 0; j < names1.length; j++) {
						for (int k = 0; k < qualifier.length; k++) {
							currentDenominator = currentDenominator + array[j][i][k];
						}
					}
				}
				for (int j = 0; j < names1.length; j++) { // divide all values by current denominator
					if (currentDenominator == 0) {
						mat[j][i] = 0;
					} else {
						mat[j][i] = mat[j][i] / currentDenominator;
					}
				}
			}
		}

		// create Matrix object and return
		Matrix matrix = new Matrix(mat, names1, names2, integerBoolean, start, stop); // assemble the Matrix object with labels
		matrix.setNumStatements(this.filteredEvents.size());
		return matrix;
	}

	/**
	 * Write results to file.
	 */
	public void exportToFile() {
		if (fileFormat.equals("csv")) {
			exportCSV();
		} else if (fileFormat.equals("dl")) {
			exportDL();
		} else if (fileFormat.equals("graphml")) {
			exportGraphml();
		}
	}

	/**
	 * Export {@link Matrix Matrix} to a CSV matrix file.
	 */
	private void exportCSV() {
		try (ProgressBar pb = new ProgressBar("Exporting networks...", this.matrixResults.size())) {
			pb.stepTo(0);
			String filename2;
			String filename1 = this.outfile.substring(0, this.outfile.length() - 4);
			String filename3 = this.outfile.substring(this.outfile.length() - 4, this.outfile.length());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			for (int k = 0; k < this.matrixResults.size(); k++) {
				// assemble file name for time window networks if necessary
				String filename = this.outfile;
				if (this.matrixResults.size() > 1) {
					filename2 = " " + String.format("%0" + String.valueOf(this.matrixResults.size()).length() + "d", k + 1) + " " + this.matrixResults.get(k).getDateTime().format(formatter);
					filename = filename1 + filename2 + filename3;
				}

				// get current data
				String[] rn = this.matrixResults.get(k).getRowNames();
				String[] cn = this.matrixResults.get(k).getColumnNames();
				int nr = rn.length;
				int nc = cn.length;
				double[][] mat = this.matrixResults.get(k).getMatrix();

				// export
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
					out.write("\"\"");
					for (int i = 0; i < nc; i++) {
						out.write(";\"" + cn[i].replaceAll("\"", "'") + "\"");
					}
					for (int i = 0; i < nr; i++) {
						out.newLine();
						out.write("\"" + rn[i].replaceAll("\"", "'") + "\"");
						for (int j = 0; j < nc; j++) {
							if (this.matrixResults.get(k).getInteger()) {
								out.write(";" + (int) mat[i][j]);
							} else {
								out.write(";" + String.format(new Locale("en"), "%.6f", mat[i][j])); // six decimal places
							}
						}
					}
					out.close();
				} catch (IOException e) {
					LogEvent l = new LogEvent(Logger.ERROR,
							"Error while saving matrix as CSV file.",
							"Tried to save a matrix to CSV file \"" + this.outfile + "\", but an error occurred. See stack trace.",
							e);
					Dna.logger.log(l);
				}
				pb.stepTo(k + 1);
			}
			pb.stepTo(this.matrixResults.size());
		}
	}

	/**
	 * Export network to a DL fullmatrix file for the software UCINET.
	 */
	private void exportDL() {
		try (ProgressBar pb = new ProgressBar("Exporting networks...", this.matrixResults.size())) {
			pb.stepTo(0);
			String filename2;
			String filename1 = this.outfile.substring(0, this.outfile.length() - 4);
			String filename3 = this.outfile.substring(this.outfile.length() - 4, this.outfile.length());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			for (int k = 0; k < this.matrixResults.size(); k++) {
				// assemble file name for time window networks if necessary
				String filename = this.outfile;
				if (this.matrixResults.size() > 1) {
					filename2 = " " + String.format("%0" + String.valueOf(this.matrixResults.size()).length() + "d", k + 1) + " " + this.matrixResults.get(k).getDateTime().format(formatter);
					filename = filename1 + filename2 + filename3;
				}

				// get current data
				String[] rn = this.matrixResults.get(k).getRowNames();
				String[] cn = this.matrixResults.get(k).getColumnNames();
				int nr = rn.length;
				int nc = cn.length;
				double[][] mat = this.matrixResults.get(k).getMatrix();

				// export
				try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF8"));
					out.write("dl ");
					if (this.networkType.equals("onemode")) {
						out.write("n = " + nr);
					} else if (this.networkType.equals("twomode")) {
						out.write("nr = " + nr + ", nc = " + nc);
					}
					out.write(", format = fullmatrix");
					out.newLine();
					if (this.networkType.equals("twomode")) {
						out.write("row labels:");
					} else {
						out.write("labels:");
					}
					for (int i = 0; i < nr; i++) {
						out.newLine();
						out.write("\"" + rn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
					}
					if (this.networkType.equals("twomode")) {
						out.newLine();
						out.write("col labels:");
						for (int i = 0; i < nc; i++) {
							out.newLine();
							out.write("\"" + cn[i].replaceAll("\"", "'").replaceAll("'", "") + "\"");
						}
					}
					out.newLine();
					out.write("data:");
					for (int i = 0; i < nr; i++) {
						out.newLine();
						for (int j = 0; j < nc; j++) {
							if (this.matrixResults.get(k).getInteger()) {
								out.write(" " + (int) mat[i][j]);
							} else {
								out.write(" " + String.format(new Locale("en"), "%.6f", mat[i][j]));
							}
						}
					}
					out.close();
				} catch (IOException e) {
					LogEvent l = new LogEvent(Logger.ERROR,
							"Error while saving DL fullmatrix file.",
							"Tried to save a matrix to DL fullmatrix file \"" + this.outfile + "\", but an error occurred. See stack trace.",
							e);
					Dna.logger.log(l);
				}
				pb.stepTo(k + 1);
			}
			pb.stepTo(this.matrixResults.size());
		}
	}

	/**
	 * Export filter for graphML files.
	 */
	private void exportGraphml() {
		try (ProgressBar pb = new ProgressBar("Exporting networks...", this.matrixResults.size())) {
			pb.stepTo(0);

			// set up file name components for time window (in case this will be required later)
			String filename2;
			String filename1 = this.outfile.substring(0, this.outfile.length() - 4);
			String filename3 = this.outfile.substring(this.outfile.length() - 4, this.outfile.length());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			// get variable IDs for variable 1 and variable 2
			int variable1Id = role1.getId();
			int variable2Id = role2.getId();

			// get attribute variable names for variable 1 and variable 2
			ArrayList<String> attributeVariables1 = Dna.sql.getAttributeVariables(variable1Id);
			ArrayList<String> attributeVariables2 = Dna.sql.getAttributeVariables(variable2Id);

			// get entities with attribute values for variable 1 and variable 2 and save in hash maps
			ArrayList<RoleVariableLink> rvl = Dna.sql.getRoleVariableLinks(this.statementType.getId());
			ArrayList<Integer> var1IDs = new ArrayList<Integer>();
			ArrayList<Integer> var2IDs = new ArrayList<Integer>();
			for (int i = 0; i < rvl.size(); i++) {
				if (rvl.get(i).getRoleId() == Exporter.this.role1.getId()) {
					var1IDs.add(rvl.get(i).getVariableId());
				}
				if (rvl.get(i).getRoleId() == Exporter.this.role2.getId()) {
					var2IDs.add(rvl.get(i).getVariableId());
				}
			}
			ArrayList<Entity> entities = Dna.sql.getEntities(this.statementType.getId());
			HashMap<String, Entity> entityMap1 = new HashMap<>();
			HashMap<String, Entity> entityMap2 = new HashMap<>();
			for (int i = 0; i < entities.size(); i++) {
				if (var1IDs.contains(entities.get(i).getVariableId())) {
					entityMap1.put(entities.get(i).getValue(), entities.get(i));
				}
				if (var2IDs.contains(entities.get(i).getVariableId())) {
					entityMap2.put(entities.get(i).getValue(), entities.get(i));
				}
			}

			Matrix m;
			for (int k = 0; k < this.matrixResults.size(); k++) {
				m = this.matrixResults.get(k);

				// assemble file name for time window networks if necessary
				String filename = this.outfile;
				if (this.matrixResults.size() > 1) {
					filename2 = " " + String.format("%0" + String.valueOf(this.matrixResults.size()).length() + "d", k + 1) + " " + m.getDateTime().format(formatter);
					filename = filename1 + filename2 + filename3;
				}

				// frequencies
				String[] values1 = this.filteredEvents.stream().map(e -> e.getVariable1()).toArray(String[]::new);
				String[] values2 = this.filteredEvents.stream().map(e -> e.getVariable2()).toArray(String[]::new);
				int[] frequencies1 = this.countFrequencies(values1, m.getRowNames());
				int[] frequencies2 = this.countFrequencies(values2, m.getColumnNames());

				// join names, frequencies, and variable names into long arrays for both modes
				String[] rn = this.matrixResults.get(k).getRowNames();
				String[] cn = this.matrixResults.get(k).getColumnNames();
				String[] names;
				String[] variables;
				int[] frequencies;
				if (this.networkType.equals("twomode")) {
					names = new String[rn.length + cn.length];
					variables = new String[names.length];
					frequencies = new int[names.length];
				} else {
					names = new String[rn.length];
					variables = new String[rn.length];
					frequencies = new int[rn.length];
				}
				for (int i = 0; i < rn.length; i++) {
					names[i] = rn[i];
					variables[i] = this.role1.getRoleName();
					frequencies[i] = frequencies1[i];
				}
				if (this.networkType.equals("twomode")) {
					for (int i = 0; i < cn.length; i++) {
						names[i + rn.length] = cn[i];
						variables[i + rn.length] = this.role2.getRoleName();
						frequencies[i + rn.length] = frequencies2[i];
					}
				}

				// get id and color arrays
				int[] id = Arrays.stream(rn).mapToInt(s -> entityMap1.get(s).getId()).toArray();
				String[] color = Arrays.stream(rn).map(s -> {
					Color col = entityMap1.get(s).getColor();
					return String.format("#%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue());
				}).toArray(String[]::new);
				if (networkType.equals("twomode")) {
					id = IntStream.concat(IntStream.of(id), Arrays.stream(cn).mapToInt(s -> entityMap2.get(s).getId())).toArray();
					color = Stream.concat(Stream.of(color), Arrays.stream(cn).map(s -> {
						Color col = entityMap2.get(s).getColor();
						return String.format("#%02X%02X%02X", col.getRed(), col.getGreen(), col.getBlue());
					})).toArray(String[]::new);
				}

				// set up graph structure
				Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
				Element graphml = new Element("graphml", xmlns);
				Namespace visone = Namespace.getNamespace("visone", "http://visone.info/xmlns");
				graphml.addNamespaceDeclaration(visone);
				Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
				graphml.addNamespaceDeclaration(xsi);
				Namespace yNs = Namespace.getNamespace("y", "http://www.yworks.com/xml/graphml");
				graphml.addNamespaceDeclaration(yNs);
				Attribute attSchema = new Attribute("schemaLocation", "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd ", xsi);
				graphml.setAttribute(attSchema);
				org.jdom.Document document = new org.jdom.Document(graphml);

				Comment dataSchema = new Comment(" data schema ");
				graphml.addContent(dataSchema);

				Element keyVisoneNode = new Element("key", xmlns);
				keyVisoneNode.setAttribute(new Attribute("for", "node"));
				keyVisoneNode.setAttribute(new Attribute("id", "d0"));
				keyVisoneNode.setAttribute(new Attribute("yfiles.type", "nodegraphics"));
				graphml.addContent(keyVisoneNode);

				Element keyVisoneEdge = new Element("key", xmlns);
				keyVisoneEdge.setAttribute(new Attribute("for", "edge"));
				keyVisoneEdge.setAttribute(new Attribute("id", "e0"));
				keyVisoneEdge.setAttribute(new Attribute("yfiles.type", "edgegraphics"));
				graphml.addContent(keyVisoneEdge);

				Element keyVisoneGraph = new Element("key", xmlns);
				keyVisoneGraph.setAttribute(new Attribute("for", "graph"));
				keyVisoneGraph.setAttribute(new Attribute("id", "prop"));
				keyVisoneGraph.setAttribute(new Attribute("visone.type", "properties"));
				graphml.addContent(keyVisoneGraph);

				Element keyId = new Element("key", xmlns);
				keyId.setAttribute(new Attribute("id", "id"));
				keyId.setAttribute(new Attribute("for", "node"));
				keyId.setAttribute(new Attribute("attr.name", "id"));
				keyId.setAttribute(new Attribute("attr.type", "string"));
				graphml.addContent(keyId);

				Element keyName = new Element("key", xmlns);
				keyName.setAttribute(new Attribute("id", "name"));
				keyName.setAttribute(new Attribute("for", "node"));
				keyName.setAttribute(new Attribute("attr.name", "name"));
				keyName.setAttribute(new Attribute("attr.type", "string"));
				graphml.addContent(keyName);

				ArrayList<String> addedAttributes = new ArrayList<String>();
				attributeVariables1.stream().forEach(v -> {
					Element keyAttribute = new Element("key", xmlns);
					keyAttribute.setAttribute(new Attribute("id", v));
					keyAttribute.setAttribute(new Attribute("for", "node"));
					keyAttribute.setAttribute(new Attribute("attr.name", v));
					keyAttribute.setAttribute(new Attribute("attr.type", "string"));
					graphml.addContent(keyAttribute);
					addedAttributes.add(v);
				});
				if (this.networkType.equals("twomode")) {
					attributeVariables2.stream().forEach(v -> {
						if (!addedAttributes.contains(v)) {
							Element keyAttribute = new Element("key", xmlns);
							keyAttribute.setAttribute(new Attribute("id", v));
							keyAttribute.setAttribute(new Attribute("for", "node"));
							keyAttribute.setAttribute(new Attribute("attr.name", v));
							keyAttribute.setAttribute(new Attribute("attr.type", "string"));
							graphml.addContent(keyAttribute);
							addedAttributes.add(v);
						}
					});
				}

				Element keyVariable = new Element("key", xmlns);
				keyVariable.setAttribute(new Attribute("id", "variable"));
				keyVariable.setAttribute(new Attribute("for", "node"));
				keyVariable.setAttribute(new Attribute("attr.name", "variable"));
				keyVariable.setAttribute(new Attribute("attr.type", "string"));
				graphml.addContent(keyVariable);

				Element keyFrequency = new Element("key", xmlns);
				keyFrequency.setAttribute(new Attribute("id", "frequency"));
				keyFrequency.setAttribute(new Attribute("for", "node"));
				keyFrequency.setAttribute(new Attribute("attr.name", "frequency"));
				keyFrequency.setAttribute(new Attribute("attr.type", "int"));
				graphml.addContent(keyFrequency);

				Element keyWeight = new Element("key", xmlns);
				keyWeight.setAttribute(new Attribute("id", "weight"));
				keyWeight.setAttribute(new Attribute("for", "edge"));
				keyWeight.setAttribute(new Attribute("attr.name", "weight"));
				keyWeight.setAttribute(new Attribute("attr.type", "double"));
				graphml.addContent(keyWeight);

				Element graphElement = new Element("graph", xmlns);
				graphElement.setAttribute(new Attribute("edgedefault", "undirected"));

				graphElement.setAttribute(new Attribute("id", "DNA"));
				int numEdges = rn.length * cn.length;
				if (this.networkType.equals("onemode")) {
					numEdges = (numEdges - rn.length) / 2;
				}
				int numNodes = rn.length;
				if (this.networkType.equals("twomode")) {
					numNodes = numNodes + cn.length;
				}
				graphElement.setAttribute(new Attribute("parse.edges", String.valueOf(numEdges)));
				graphElement.setAttribute(new Attribute("parse.nodes", String.valueOf(numNodes)));
				graphElement.setAttribute(new Attribute("parse.order", "free"));
				Element properties = new Element("data", xmlns);
				properties.setAttribute(new Attribute("key", "prop"));
				Element labelAttribute = new Element("labelAttribute", visone);
				labelAttribute.setAttribute("edgeLabel", "weight");
				labelAttribute.setAttribute("nodeLabel", "name");
				properties.addContent(labelAttribute);
				graphElement.addContent(properties);

				// add nodes
				Comment nodes = new Comment(" nodes ");
				graphElement.addContent(nodes);

				for (int i = 0; i < names.length; i++) {
					Element node = new Element("node", xmlns);
					node.setAttribute(new Attribute("id", "n" + id[i]));

					Element idElement = new Element("data", xmlns);
					idElement.setAttribute(new Attribute("key", "id"));
					idElement.setText(String.valueOf(id[i]));
					node.addContent(idElement);

					Element nameElement = new Element("data", xmlns);
					nameElement.setAttribute(new Attribute("key", "name"));
					nameElement.setText(names[i]);
					node.addContent(nameElement);

					for (int j = 0; j < attributeVariables1.size(); j++) {
						if (i < rn.length) { // first mode: rows
							Element element = new Element("data", xmlns);
							element.setAttribute(new Attribute("key", attributeVariables1.get(j)));
							element.setText(entityMap1.get(names[i]).getAttributeValues().get(attributeVariables1.get(j)));
							node.addContent(element);
						}
					}
					if (this.networkType.equals("twomode")) {
						for (int j = 0; j < attributeVariables2.size(); j++) {
							if (i >= rn.length) { // second mode: columns
								Element element = new Element("data", xmlns);
								element.setAttribute(new Attribute("key", attributeVariables2.get(j)));
								element.setText(entityMap2.get(names[i]).getAttributeValues().get(attributeVariables2.get(j)));
								node.addContent(element);
							}
						}
					}

					Element variableElement = new Element("data", xmlns);
					variableElement.setAttribute(new Attribute("key", "variable"));
					variableElement.setText(variables[i]);
					node.addContent(variableElement);

					Element frequency = new Element("data", xmlns);
					frequency.setAttribute(new Attribute("key", "frequency"));
					frequency.setText(String.valueOf(frequencies[i]));
					node.addContent(frequency);

					Element vis = new Element("data", xmlns);
					vis.setAttribute(new Attribute("key", "d0"));
					Element visoneShapeNode = new Element("shapeNode", visone);
					Element yShapeNode = new Element("ShapeNode", yNs);
					Element geometry = new Element("Geometry", yNs);
					geometry.setAttribute(new Attribute("height", "20.0"));
					geometry.setAttribute(new Attribute("width", "20.0"));
					geometry.setAttribute(new Attribute("x", String.valueOf(Math.random() * 800)));
					geometry.setAttribute(new Attribute("y", String.valueOf(Math.random() * 600)));
					yShapeNode.addContent(geometry);
					Element fill = new Element("Fill", yNs);
					fill.setAttribute(new Attribute("color", color[i]));

					fill.setAttribute(new Attribute("transparent", "false"));
					yShapeNode.addContent(fill);
					Element borderStyle = new Element("BorderStyle", yNs);
					borderStyle.setAttribute(new Attribute("color", "#000000"));
					borderStyle.setAttribute(new Attribute("type", "line"));
					borderStyle.setAttribute(new Attribute("width", "1.0"));
					yShapeNode.addContent(borderStyle);

					Element nodeLabel = new Element("NodeLabel", yNs);
					nodeLabel.setAttribute(new Attribute("alignment", "center"));
					nodeLabel.setAttribute(new Attribute("autoSizePolicy", "content"));
					nodeLabel.setAttribute(new Attribute("backgroundColor", "#FFFFFF"));
					nodeLabel.setAttribute(new Attribute("fontFamily", "Dialog"));
					nodeLabel.setAttribute(new Attribute("fontSize", "12"));
					nodeLabel.setAttribute(new Attribute("fontStyle", "plain"));
					nodeLabel.setAttribute(new Attribute("hasLineColor", "false"));
					nodeLabel.setAttribute(new Attribute("height", "19.0"));
					nodeLabel.setAttribute(new Attribute("modelName", "eight_pos"));
					nodeLabel.setAttribute(new Attribute("modelPosition", "n"));
					nodeLabel.setAttribute(new Attribute("textColor", "#000000"));
					nodeLabel.setAttribute(new Attribute("visible", "true"));
					nodeLabel.setText(names[i]);
					yShapeNode.addContent(nodeLabel);

					Element shape = new Element("Shape", yNs);
					if (i < rn.length) {
						shape.setAttribute(new Attribute("type", "ellipse"));
					} else {
						shape.setAttribute(new Attribute("type", "roundrectangle"));
					}
					yShapeNode.addContent(shape);
					visoneShapeNode.addContent(yShapeNode);
					vis.addContent(visoneShapeNode);
					node.addContent(vis);

					graphElement.addContent(node);
				}

				// add edges
				Comment edges = new Comment(" edges ");
				graphElement.addContent(edges);
				for (int i = 0; i < rn.length; i++) {
					for (int j = 0; j < cn.length; j++) {
						if (m.getMatrix()[i][j] != 0.0 && (this.networkType.equals("twomode") || (this.networkType.equals("onemode") && i < j))) {  // only lower triangle is used for one-mode networks
							Element edge = new Element("edge", xmlns);

							int currentId = id[i];
							edge.setAttribute(new Attribute("source", "n" + String.valueOf(currentId)));
							if (this.networkType.equals("twomode")) {
								currentId = id[j + rn.length];
							} else {
								currentId = id[j];
							}
							edge.setAttribute(new Attribute("target", "n" + String.valueOf(currentId)));

							Element weight = new Element("data", xmlns);
							weight.setAttribute(new Attribute("key", "weight"));
							weight.setText(String.valueOf(m.getMatrix()[i][j]));
							edge.addContent(weight);

							Element visEdge = new Element("data", xmlns);
							visEdge.setAttribute("key", "e0");
							Element visPolyLineEdge = new Element("polyLineEdge", visone);
							Element yPolyLineEdge = new Element("PolyLineEdge", yNs);

							Element yLineStyle = new Element("LineStyle", yNs);
							if (qualifierAggregation.equals("combine")) {
								if (m.getMatrix()[i][j] == 1.0) {
									yLineStyle.setAttribute("color", "#00ff00");
								} else if (m.getMatrix()[i][j] == 2.0) {
									yLineStyle.setAttribute("color", "#ff0000");
								} else if (m.getMatrix()[i][j] == 3.0) {
									yLineStyle.setAttribute("color", "#0000ff");
								}
							} else if (qualifierAggregation.equals("subtract")) {
								if (m.getMatrix()[i][j] < 0) {
									yLineStyle.setAttribute("color", "#ff0000");
								} else if (m.getMatrix()[i][j] > 0) {
									yLineStyle.setAttribute("color", "#00ff00");
								}
							} else if (qualifierAggregation.equals("conflict")) {
								yLineStyle.setAttribute("color", "#ff0000");
							} else if (qualifierAggregation.equals("congruence")) {
								yLineStyle.setAttribute("color", "#00ff00");
							} else {
								yLineStyle.setAttribute("color", "#000000");
							}
							yLineStyle.setAttribute(new Attribute("type", "line"));
							yLineStyle.setAttribute(new Attribute("width", "2.0"));
							yPolyLineEdge.addContent(yLineStyle);
							visPolyLineEdge.addContent(yPolyLineEdge);
							visEdge.addContent(visPolyLineEdge);
							edge.addContent(visEdge);

							graphElement.addContent(edge);
						}
					}
				}

				graphml.addContent(graphElement);

				// write to file
				File dnaFile = new File(filename);
				try {
					FileOutputStream outStream = new FileOutputStream(dnaFile);
					XMLOutputter outToFile = new XMLOutputter();
					Format format = Format.getPrettyFormat();
					format.setEncoding("utf-8");
					outToFile.setFormat(format);
					outToFile.output(document, outStream);
					outStream.flush();
					outStream.close();
				} catch (IOException e) {
					LogEvent l = new LogEvent(Logger.ERROR,
							"Error while saving visone graphml file.",
							"Tried to save a matrix to graphml file \"" + dnaFile + "\", but an error occurred. See stack trace.",
							e);
					Dna.logger.log(l);
				}
				pb.stepTo(k + 1);
			}
			pb.stepTo(this.matrixResults.size());
		}
	}
}