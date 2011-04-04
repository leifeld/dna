package dna;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class Export {
	
	private DnaContainer dc;
	private StatementContainer statCont;
	private StatementContainer sc;
	private String[] excludePersons;
	private String[] excludeOrganizations;
	private String[] excludeCategories;
	private String outfile, agreement, algorithm, format, twoModeType, oneModeType, via, networkName;
	private boolean includeIsolates, ignoreDuplicates, normalization;
	double windowSize, stepSize, soniaForwardDays, soniaBackwardDays, commetrixBackwardWindow;
	private boolean invertPersons, invertOrganizations, invertCategories;
	private boolean verbose;
	private double alpha, lambda;
	
	ArrayList<String> class1, class2, class3, s_agree, s_text;
	ArrayList<GregorianCalendar> s_date;
	DnaGraph graph;
	GregorianCalendar start, stop;
	
	//declarations for normalization procedures
	int sourceCounter;
	int targetCounter;
	String sourceLabel;
	String targetLabel;
	ArrayList<String> tabuSource = new ArrayList<String>();
	ArrayList<String> tabuTarget = new ArrayList<String>();
	
	//isolates lists
	ArrayList<String> persIsolates = new ArrayList<String>();
	ArrayList<String> orgIsolates = new ArrayList<String>();
	ArrayList<String> catIsolates = new ArrayList<String>();
	ArrayList<String> isolatesList = new ArrayList<String>();
	
	/**
	 * Constructor with statement container (for internal use, e.g. from the Dna main class)
	 * 
	 * @param statCont
	 * @param outfile
	 * @param excludePersons
	 * @param excludeOrganizations
	 * @param excludeCategories
	 * @param start
	 * @param stop
	 * @param agreement
	 * @param algorithm
	 * @param format
	 * @param twoModeType
	 * @param oneModeType
	 * @param via
	 * @param includeIsolates
	 * @param ignoreDuplicates
	 * @param normalization
	 * @param windowSize
	 * @param stepSize
	 * @param alpha
	 * @param lambda
	 * @param soniaForwardDays
	 * @param soniaBackwardDays
	 * @param networkName
	 * @param commetrixBackwardWindow
	 */
	public Export(
			StatementContainer statCont,
			String outfile,
			String[] excludePersons,
			String[] excludeOrganizations, 
			String[] excludeCategories,
			GregorianCalendar start,
			GregorianCalendar stop,
			String agreement, //yes, no, combined, conflict
			String algorithm, //affiliation, cooccurrence, timewindow, attenuation, dynamic, edgelist
			String format, //csvmatrix, csvlist, dl, graphml, sonia, commetrix
			String twoModeType, //oc, pc, po
			String oneModeType, //organizations, persons, categories
			String via, //organizations, persons, categories
			boolean includeIsolates,
			boolean ignoreDuplicates,
			boolean normalization,
			Double windowSize,
			Double stepSize,
			Double alpha,
			Double lambda,
			Double soniaForwardDays,
			Double soniaBackwardDays,
			String networkName,
			Double commetrixBackwardWindow
			) {
		this.statCont = statCont;
		this.outfile = outfile;
		this.excludePersons = excludePersons;
		this.excludeOrganizations = excludeOrganizations;
		this.excludeCategories = excludeCategories;
		this.start = start;
		this.stop = stop;
		this.agreement = agreement;
		this.algorithm = algorithm;
		this.format = format;
		this.twoModeType = twoModeType;
		this.oneModeType = oneModeType;
		this.via = via;
		this.includeIsolates = includeIsolates;
		this.ignoreDuplicates = ignoreDuplicates;
		this.normalization = normalization;
		this.windowSize = windowSize;
		this.stepSize = stepSize;
		this.alpha = alpha;
		this.lambda = lambda;
		this.soniaBackwardDays = soniaBackwardDays;
		this.soniaForwardDays = soniaForwardDays;
		this.networkName = networkName;
		this.commetrixBackwardWindow = commetrixBackwardWindow;
		this.invertPersons = false;
		this.invertOrganizations = false;
		this.invertCategories = false;
		this.verbose = true;
		
		sc = new StatementContainer();
		
		applyFilters();
		createGraph();
		exportControl();
	}
	
	/**
	 * Constructor with file name (for external calls, e.g. from R).
	 * 
	 * @param infile
	 * @param excludePersons
	 * @param excludeOrganizations
	 * @param excludeCategories
	 * @param startDate
	 * @param stopDate
	 * @param agreement
	 * @param algorithm
	 * @param twoModeType
	 * @param oneModeType
	 * @param via
	 * @param includeIsolates
	 * @param ignoreDuplicates
	 * @param normalization
	 * @param windowSize
	 * @param stepSize
	 * @param alpha
	 * @param lambda
	 * @param soniaForwardDays
	 * @param soniaBackwardDays
	 * @param networkName
	 * @param commetrixBackwardWindow
	 * @param invertPersons
	 * @param invertOrganizations
	 * @param invertCategories
	 * @param verbose
	 */
	public Export(
			String infile,
			String[] excludePersons,
			String[] excludeOrganizations,
			String[] excludeCategories,
			String startDate,
			String stopDate,
			String agreement,
			String algorithm,
			String twoModeType,
			String oneModeType,
			String via,
			boolean includeIsolates,
			boolean ignoreDuplicates,
			boolean normalization,
			double windowSize,
			double stepSize,
			double alpha,
			double lambda,
			double soniaForwardDays,
			double soniaBackwardDays,
			String networkName,
			double commetrixBackwardWindow,
			boolean invertPersons,
			boolean invertOrganizations,
			boolean invertCategories,
			boolean verbose
			) {
		if (excludePersons.length == 2 && excludePersons[0].equals("") && excludePersons[1].equals("")) {
			excludePersons = new String[0];
		}
		if (excludeOrganizations.length == 2 && excludeOrganizations[0].equals("") && excludeOrganizations[1].equals("")) {
			excludeOrganizations = new String[0];
		}
		if (excludeCategories.length == 2 && excludeCategories[0].equals("") && excludeCategories[1].equals("")) {
			excludeCategories = new String[0];
		}
		this.excludePersons = excludePersons;
		this.excludeOrganizations = excludeOrganizations;
		this.excludeCategories = excludeCategories;
		this.invertPersons = invertPersons;
		this.invertOrganizations = invertOrganizations;
		this.invertCategories = invertCategories;
		
		String[] startDateComponents = startDate.split("\\.");
		this.start = new GregorianCalendar((int)Integer.valueOf(startDateComponents[2]), (int)Integer.valueOf(startDateComponents[1]) - 1, (int)Integer.valueOf(startDateComponents[0]));
		String[] stopDateComponents = stopDate.split("\\.");
		this.stop = new GregorianCalendar((int)Integer.valueOf(stopDateComponents[2]), (int)Integer.valueOf(stopDateComponents[1]) - 1, (int)Integer.valueOf(stopDateComponents[0]));
		
		this.agreement = agreement;
		this.algorithm = algorithm;
		this.twoModeType = twoModeType;
		this.oneModeType = oneModeType;
		this.via = via;
		this.includeIsolates = includeIsolates;
		this.ignoreDuplicates = ignoreDuplicates;
		this.normalization = normalization;
		this.windowSize = windowSize;
		this.stepSize = stepSize;
		this.alpha = alpha;
		this.lambda = lambda;
		this.soniaBackwardDays = soniaBackwardDays;
		this.soniaForwardDays = soniaForwardDays;
		this.networkName = networkName;
		this.commetrixBackwardWindow = commetrixBackwardWindow;
		this.format = "csvmatrix"; //if this is not set, there will be a NullPointerException in the createGraph() method
		this.verbose = verbose;
		
		dc = new ParseLatest(infile, verbose).getDc();
		this.statCont = dc.sc;
		sc = new StatementContainer();
		
		applyFilters();
		createGraph();
	}
	
	/**
	 * Constructor for external actor attribute access.
	 * 
	 * @param infile
	 */
	public Export(String infile, boolean verbose) {
		dc = new ParseLatest(infile, verbose).getDc();
	}
	
	public void applyFilters() {
		
		String regex1 = "'|^[ ]+|[ ]+$|[ ]*;|;[ ]*"; //required for undesirable character filter
		String regex2 = "\\s+"; //required for undesirable character filter
		
		if (includeIsolates == true) {
			
			if (verbose == true) {
				System.out.print("Saving isolates in a separate list... ");
			}
			
			persIsolates = statCont.getPersonList();
			for (int i = 0; i < excludePersons.length; i++) {
				for (int j = 0; j < persIsolates.size(); j++) {
					if (persIsolates.get(j).equals(excludePersons[i])) {
						persIsolates.remove(j);
						continue;
					}
				}
			}

			orgIsolates = statCont.getOrganizationList();
			for (int i = 0; i < excludeOrganizations.length; i++) {
				for (int j = 0; j < orgIsolates.size(); j++) {
					if (orgIsolates.get(j).equals(excludeOrganizations[i])) {
						orgIsolates.remove(j);
						continue;
					}
				}
			}

			catIsolates = statCont.getCategoryList();
			for (int i = 0; i < excludeCategories.length; i++) {
				for (int j = 0; j < catIsolates.size(); j++) {
					if (catIsolates.get(j).equals(excludeCategories[i])) {
						catIsolates.remove(j);
						continue;
					}
				}
			}
			
			for (int i = 0; i < persIsolates.size(); i++) {
				persIsolates.set(i, persIsolates.get(i).replaceAll(regex1, "").replaceAll(regex2, " "));
			}
			for (int i = 0; i < orgIsolates.size(); i++) {
				orgIsolates.set(i, orgIsolates.get(i).replaceAll(regex1, "").replaceAll(regex2, " "));
			}
			for (int i = 0; i < catIsolates.size(); i++) {
				catIsolates.set(i, catIsolates.get(i).replaceAll(regex1, "").replaceAll(regex2, " "));
			}
			if (verbose == true) {
				System.out.println("done.");
			}
		}
		
		//apply filters
		if (verbose == true) {
			System.out.print("Applying filters... ");
		}
		
		for (int i = statCont.size() - 1; i >= 0; i--) {
			boolean exclude = false;
			boolean excludePerson = true;
			boolean excludeOrganization = true;
			boolean excludeCategory = true;
			boolean excludeOther = false;
			
			//exclude list filter
			if (invertPersons == true) {
				for (int j = 0; j < excludePersons.length; j++) {
					if (statCont.get(i).getPerson().equals(excludePersons[j])) {
						excludePerson = false;
					}
				}
			} else {
				for (int j = 0; j < excludePersons.length; j++) {
					if (statCont.get(i).getPerson().equals(excludePersons[j])) {
						exclude = true;
					}
				}
			}
			if (invertOrganizations == true) {
				for (int j = 0; j < excludeOrganizations.length; j++) {
					if (statCont.get(i).getOrganization().equals(excludeOrganizations[j])) {
						excludeOrganization = false;
					}
				}
			} else {
				for (int j = 0; j < excludeOrganizations.length; j++) {
					if (statCont.get(i).getOrganization().equals(excludeOrganizations[j])) {
						exclude = true;
					}
				}
			}
			if (invertCategories == true) {
				for (int j = 0; j < excludeCategories.length; j++) {
					if (statCont.get(i).getCategory().equals(excludeCategories[j])) {
						excludeCategory = false;
					}
				}
			} else {
				for (int j = 0; j < excludeCategories.length; j++) {
					if (statCont.get(i).getCategory().equals(excludeCategories[j])) {
						exclude = true;
					}
				}
			}
			
			//date filter
			GregorianCalendar currentDate = new GregorianCalendar();
			currentDate.setTime(statCont.get(i).getDate());
			if (currentDate.before(start) || currentDate.after(stop)) {
				excludeOther = true;
			}
			
			//agreement filter
			if ((statCont.get(i).getAgreement().equals("no") && agreement.equals("yes"))
					|| (statCont.get(i).getAgreement().equals("yes") && agreement.equals("no"))) {
				excludeOther = true;
			}
			
			if ( 
				(invertPersons == true && excludePerson == true) || 
				(invertOrganizations == true && excludeOrganization == true) || 
				(invertCategories == true && excludeCategory == true) ||
				excludeOther == true
			) {
				exclude = true;
			}
			
			//add statement and apply undesirable character filter
			if (exclude == false) {
				try {
					sc.addStatement(statCont.get(i), false);
					sc.get(sc.size()-1).setPerson(sc.get(sc.size()-1).getPerson().replaceAll(regex1, "").replaceAll(regex2, " "));
					sc.get(sc.size()-1).setOrganization(sc.get(sc.size()-1).getOrganization().replaceAll(regex1, "").replaceAll(regex2, " "));
					sc.get(sc.size()-1).setCategory(sc.get(sc.size()-1).getCategory().replaceAll(regex1, "").replaceAll(regex2, " "));
				} catch (DuplicateStatementIdException e) {
					System.err.println(e.getStackTrace());
				}
			}
		}
		
		if (verbose == true) {
			System.out.println("done.");
			System.out.println("Keeping " + sc.size() + " out of " + statCont.size() + " statements.");
		}
	}
	
	public void createGraph() {
		if (algorithm.equals("affiliation")) {
			if (format.equals("sonia")) {
				exportFilterSoniaAffiliation(outfile);
			} else {
				generateGraphAffiliation();
			}
		} else if (algorithm.equals("edgelist")) {
			if (format.equals("commetrix")) {
				exportFilterCommetrix(outfile);
			} else if (format.equals("sonia")) {
				exportFilterSonia(outfile);
			}
		} else if (algorithm.equals("cooccurrence")) {
			generateGraphCoOccurrence();
		} else if (algorithm.equals("timewindow")) {
			generateGraphTimeWindow();
		} else if (algorithm.equals("attenuation")) {
			generateGraphAttenuation();
		}
	}
	
	public void exportControl() {
		if (format.equals("csvmatrix")) {
			exportCsvMatrix(outfile);
		} else if (format.equals("dl")) {
			exportDlFullMatrix(outfile);
		} else if (format.equals("graphml")) {
			graphMl(outfile);
		} else if (format.equals("csvlist")) {
			exportCsvAffiliationList(outfile);
		}
	}
	
	/**
	 * Generate bipartite graph from lists of statements.
	 */
	private void generateGraphAffiliation() {
		
		//create graph
		graph = new DnaGraph();
		graph.removeAllEdges();
		graph.removeAllVertices();
		
		//create vertices
		int count = 0;
		class1 = new ArrayList<String>();
		class2 = new ArrayList<String>();
		s_agree = new ArrayList<String>();
		if (twoModeType.equals("oc")) {
			if (verbose == true) {
				System.out.print("Creating vertices for organizations... ");
			}
			class1 = sc.getStringList("o");
			for (int i = 0; i < sc.getOrganizationList().size(); i++) {
				if (!sc.getOrganizationList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getOrganizationList().get(i), "o"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < orgIsolates.size(); i++) {
					if (!graph.containsVertex(orgIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, orgIsolates.get(i), "o"));
						count++;
					}
				}
			}
			if (verbose == true) {
				System.out.println(graph.countVertexType("o") + " vertices were added.");
				System.out.print("Creating vertices for categories... ");
			}
			class2 = sc.getStringList("c");
			for (int i = 0; i < sc.getCategoryList().size(); i++) {
				if (!sc.getCategoryList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getCategoryList().get(i), "c"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < catIsolates.size(); i++) {
					if (!graph.containsVertex(catIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, catIsolates.get(i), "c"));
						count++;
					}
				}
			}
			if (verbose == true) {
				System.out.println(graph.countVertexType("c") + " vertices were added.");
			}
		} else if (twoModeType.equals("po")) {
			if (verbose == true) {
				System.out.print("Creating vertices for persons... ");
			}
			class1 = sc.getStringList("p");
			for (int i = 0; i < sc.getPersonList().size(); i++) {
				if (!sc.getPersonList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getPersonList().get(i), "p"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < persIsolates.size(); i++) {
					if (!graph.containsVertex(persIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, persIsolates.get(i), "p"));
						count++;
					}
				}
			}
			if (verbose == true) {
				System.out.println(graph.countVertexType("p") + " vertices were added.");
				System.out.print("Creating vertices for organizations... ");
			}
			class2 = sc.getStringList("o");
			for (int i = 0; i < sc.getOrganizationList().size(); i++) {
				if (!sc.getOrganizationList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getOrganizationList().get(i), "o"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < orgIsolates.size(); i++) {
					if (!graph.containsVertex(orgIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, orgIsolates.get(i), "o"));
						count++;
					}
				}
			}
			if (verbose == true) {
				System.out.println(graph.countVertexType("o") + " vertices were added.");
			}
		} else if (twoModeType.equals("pc")) {
			if (verbose == true) {
				System.out.print("Creating vertices for persons... ");
			}
			class1 = sc.getStringList("p");
			for (int i = 0; i < sc.getPersonList().size(); i++) {
				if (!sc.getPersonList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getPersonList().get(i), "p"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < persIsolates.size(); i++) {
					if (!graph.containsVertex(persIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, persIsolates.get(i), "p"));
						count++;
					}
				}
			}
			if (verbose == true) {
				System.out.println(graph.countVertexType("p") + " vertices were added.");
				System.out.print("Creating vertices for categories... ");
			}
			class2 = sc.getStringList("c");
			for (int i = 0; i < sc.getCategoryList().size(); i++) {
				if (!sc.getCategoryList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getCategoryList().get(i), "c"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < catIsolates.size(); i++) {
					if (!graph.containsVertex(catIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, catIsolates.get(i), "c"));
						count++;
					}
				}
			}
			if (verbose == true) {
				System.out.println(graph.countVertexType("c") + " vertices were added.");
			}
		}
		s_agree = sc.getStringList("a");

		//add bipartite edges
		if (verbose == true) {
			System.out.print("Adding bipartite edges... ");
		}
		count = 0;
		for (int i = 0; i < class1.size(); i++) {
			if (!class1.get(i).equals("") && !class2.get(i).equals("")) {
				int sourceId = graph.getVertex(class1.get(i)).getId();
				int targetId = graph.getVertex(class2.get(i)).getId();
				String agree = s_agree.get(i);
				if (ignoreDuplicates == false) { //duplicates are counted, so there can be no multiplexity
					int weight = 0;
					if (agree.equals("yes")) {
						weight = 1;
					} else if (agreement.equals("combined")) { //negative weight if combined is selected and agreement=no
						weight = -1;
					} else { //positive weight if combined is not selected but agreement=no
						weight = 1;
					} //the result will be a weighted, signed network; positive values indicate many agreements with statement, negative many disagreements
					if (graph.containsEdge(sourceId, targetId)) {
						graph.getEdge(sourceId, targetId).addToWeight(weight);
					} else {
						graph.addEdge(new DnaGraphEdge(count, weight, graph.getVertex(sourceId), graph.getVertex(targetId)));
						count++;
					}
				} else { //duplicates are ignored, relations are binary
					if (agreement.equals("combined")) { //if combined is selected, a multiplex network is created
						if (graph.containsEdge(sourceId, targetId)) {
							if ((graph.getEdge(sourceId, targetId).getWeight() == 1 && agree.equals("no")) || (graph.getEdge(sourceId, targetId).getWeight() == 2 && agree.equals("yes"))) {
								graph.getEdge(sourceId, targetId).setWeight(3); //edge weight = 3 means both
							}
						} else { //add new edge
							if (agree.equals("yes")) { //edge weight = 1 means agreement
								graph.addEdge(new DnaGraphEdge(count, 1, graph.getVertex(sourceId), graph.getVertex(targetId)));
								count++;
							} else { //edge weight = 2 means disagreement
								graph.addEdge(new DnaGraphEdge(count, 2, graph.getVertex(sourceId), graph.getVertex(targetId)));
								count++;
							}
						}
					} else { //yes or no is selected, so there will be no multiplexity
						if (graph.containsEdge(sourceId, targetId)) {
							//do nothing
						} else { //add new edge
							graph.addEdge(new DnaGraphEdge(count, 1, graph.getVertex(sourceId), graph.getVertex(targetId)));
							count++;
						}
					}
				}
			}
		}
		if (verbose == true) {
			System.out.println(graph.countEdges() + " edges were added. Mean edge weight: " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()));
		}
	}
	
	public void prepareSimpleGraph() {
		if (verbose == true) {
			System.out.print("Creating vertices... ");
		}
		
		//create graph
		graph = new DnaGraph();
		graph.removeAllEdges();
		graph.removeAllVertices();
		
		//create vertices and get lists from statementCollection
		class1 = new ArrayList<String>();
		int count = 1;
		if (oneModeType.equals("persons")) {
			class1 = sc.getStringList("p");
			for (int i = 0; i < sc.getPersonList().size(); i++) {
				if (!sc.getPersonList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getPersonList().get(i), "p"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < persIsolates.size(); i++) {
					if (!graph.containsVertex(persIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, persIsolates.get(i), "p"));
						count++;
					}
				}
				isolatesList = persIsolates;
			}
		} else if (oneModeType.equals("organizations")) {
			class1 = sc.getStringList("o");
			for (int i = 0; i < sc.getOrganizationList().size(); i++) {
				if (!sc.getOrganizationList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getOrganizationList().get(i), "o"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < orgIsolates.size(); i++) {
					if (!graph.containsVertex(orgIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, orgIsolates.get(i), "o"));
						count++;
					}
				}
				isolatesList = orgIsolates;
			}
		} else if (oneModeType.equals("categories")) {
			class1 = sc.getStringList("c");
			for (int i = 0; i < sc.getCategoryList().size(); i++) {
				if (!sc.getCategoryList().get(i).equals("")) {
					graph.addVertex(new DnaGraphVertex(count, sc.getCategoryList().get(i), "c"));
					count++;
				}
			}
			if (includeIsolates == true) {
				for (int i = 0; i < catIsolates.size(); i++) {
					if (!graph.containsVertex(catIsolates.get(i))) {
						graph.addVertex(new DnaGraphVertex(count, catIsolates.get(i), "c"));
						count++;
					}
				}
				isolatesList = catIsolates;
			}
		}
		
		if (via.equals("organizations")) {
			class3 = sc.getStringList("o");
		} else if (via.equals("persons")) {
			class3 = sc.getStringList("p");
		} else {
			class3 = sc.getStringList("c");
		}
		
		s_agree = sc.getStringList("a");
		s_text = sc.getStringList("t");
		s_date = sc.getDateList();
		if (verbose == true) {
			System.out.println(graph.countVertices() + " vertices were added.");
		}
	}
	
	/**
	 * Congruence networks
	 */
	public void generateGraphCoOccurrence() {
		
		if (verbose == true) {
			System.out.print("Creating graph and adding vertices... ");
		}
		
		graph = new DnaGraph();
		graph.removeAllEdges();
		graph.removeAllVertices();
		
		class1 = new ArrayList<String>();
		class2 = new ArrayList<String>();
		ArrayList<String> agList = new ArrayList<String>();
		if (oneModeType.equals("persons")) {
			if (includeIsolates == true) {
				class1 = persIsolates;
			} else {
				class1 = sc.getPersonList();
			}
			for (int i = 0; i < class1.size(); i++) {
				graph.addVertex(new DnaGraphVertex(i+1, class1.get(i), "p"));
			}
		} else if (oneModeType.equals("organizations")) {
			if (includeIsolates == true) {
				class1 = orgIsolates;
			} else {
				class1 = sc.getOrganizationList();
			}
			for (int i = 0; i < class1.size(); i++) {
				graph.addVertex(new DnaGraphVertex(i+1, class1.get(i), "o"));
			}
		} else {
			if (includeIsolates == true) {
				class1 = catIsolates;
			} else {
				class1 = sc.getCategoryList();
			}
			for (int i = 0; i < class1.size(); i++) {
				graph.addVertex(new DnaGraphVertex(i+1, class1.get(i), "c"));
			}
		}
		if (via.equals("persons")) {
			if (includeIsolates == true) {
				class2 = persIsolates;
			} else {
				class2 = sc.getPersonList();
			}
		} else if (via.equals("organizations")) {
			if (includeIsolates == true) {
				class2 = orgIsolates;
			} else {
				class2 = sc.getOrganizationList();
			}
		} else {
			if (includeIsolates == true) {
				class2 = catIsolates;
			} else {
				class2 = sc.getCategoryList();
			}
		}
		if (agreement.equals("yes")) {
			agList.add("yes");
		} else if (agreement.equals("no")) {
			agList.add("no");
		} else {
			agList.add("yes");
			agList.add("no");
		}
		
		double[][][] threeMode = new double[class1.size()][class2.size()][agList.size()];
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class2.size(); j++) {
				for (int k = 0; k < agList.size(); k++) {
					threeMode[i][j][k] = 0;
				}
			}
		}
		
		if (verbose == true) {
			System.out.println("done.");
			System.out.print("Converting statement list to three-dimensional array... ");
		}
		for (int i = 0; i < sc.size(); i ++) {
			int c1 = -1;
			int r = -1;
			int c2 = -1;
			
			for (int j = 0; j < class1.size(); j++) {
				if ( (sc.get(i).getCategory().equals(class1.get(j)) && oneModeType.equals("categories")) ||
						(sc.get(i).getPerson().equals(class1.get(j)) && oneModeType.equals("persons")) ||
						(sc.get(i).getOrganization().equals(class1.get(j)) && oneModeType.equals("organizations")) ) {
					c1 = j;
				}
			}
			for (int j = 0; j < class2.size(); j++) {
				if ( (sc.get(i).getCategory().equals(class2.get(j)) && via.equals("categories")) ||
						(sc.get(i).getPerson().equals(class2.get(j)) && via.equals("persons")) ||
						(sc.get(i).getOrganization().equals(class2.get(j)) && via.equals("organizations")) ) {
					c2 = j;
				}
			}
			for (int j = 0; j < agList.size(); j++) {
				if ( sc.get(i).getAgreement().equals(agList.get(j))) {
					r = j;
				}
			}
			
			if (c1 > -1 && c2 > -1 && r > -1) {
				threeMode[c1][c2][r] = threeMode[c1][c2][r] + 1;
			}
		}
		if (verbose == true) {
			System.out.println("done.");
			System.out.print("Computing edge weights... ");
		}

		double[][] results = new double[class1.size()][class1.size()];
		
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class1.size(); j++) {
				double frequency = 0;
				if (!class1.get(i).equals(class1.get(j))) {
					if (ignoreDuplicates == true) {
						if (agreement.equals("conflict")) {
							for (int k = 0; k < class2.size(); k++) {
								if ( (threeMode[i][k][0] > 0 && threeMode[j][k][1] > 0) || (threeMode[i][k][1] > 0 && threeMode[j][k][0] > 0) ) {
									frequency++;
								}
							}
						} else {
							for (int k = 0; k < class2.size(); k++) {
								for (int m = 0; m < agList.size(); m++) {
									if (threeMode[i][k][m] > 0 && threeMode[j][k][m] > 0) {
										frequency++;
									}
								}
							}
						}
					} else {
						if (agreement.equals("conflict")) {
							for (int k = 0; k < class2.size(); k++) {
								if ( (threeMode[i][k][0] > 0 && threeMode[j][k][1] > 0) || (threeMode[i][k][1] > 0 && threeMode[j][k][0] > 0) ) {
									frequency = frequency + (threeMode[i][k][0] * threeMode[j][k][1]) + (threeMode[i][k][1] * threeMode[j][k][0]);
								}
							}
						} else {
							for (int k = 0; k < class2.size(); k++) {
								for (int m = 0; m < agList.size(); m++) {
									if (threeMode[i][k][m] > 0 && threeMode[j][k][m] > 0) {
										frequency = frequency + (threeMode[i][k][m] * threeMode[j][k][m]);
									}
								}
							}
						}
					}
				}
				results[i][j] = frequency;
			}
		}
		if (verbose == true) {
			System.out.println("done.");
		}
		
		if (normalization == true) {
			
			if (verbose == true) {
				System.out.print("Normalizing congruence matrix... ");
			}
			
			//compute binary degrees in affiliation network
			double[] degrees = new double[class1.size()];
			for (int i = 0; i < class1.size(); i++) {
				double degree = 0;
				for (int j = 0; j < class2.size(); j++) {
					double bothR = 0;
					for (int k = 0; k < agList.size(); k++) {
						bothR = bothR + threeMode[i][j][k];
					}
					if (bothR > 0) {
						degree = degree + 1;
					}
				}
				degrees[i] = degree;
			}
			
			//normalize
			for (int i = 0; i < class1.size(); i++) {
				for (int j = 0; j < class1.size(); j++) {
					results[i][j] = results[i][j] / ((degrees[i] + degrees[j]) / 2);
				}
			}

			if (verbose == true) {
				System.out.println("done.");
			}
		}
		
		if (verbose == true) {
			System.out.print("Adding edges from edge-weight matrix to the graph... ");
		}
		
		int count = 1;
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class1.size(); j++) {
				if (results[i][j] > 0) {
					graph.addEdge(new DnaGraphEdge(count, results[i][j], graph.getVertex(i+1), graph.getVertex(j+1)));
					count++;
				}
			}
		}
		
		if (verbose == true) {
			System.out.println("done.");
			System.out.println(graph.countVertices() + " vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + ".");
		}
	}
	
	/**
	 * Time window algorithm
	 */
	public void generateGraphTimeWindow() {
		
		if (verbose == true) {
			System.out.print("Creating graph and adding vertices... ");
		}
		
		graph = new DnaGraph();
		graph.removeAllEdges();
		graph.removeAllVertices();
		
		//initialize lists which are the basis for a 3D array
		class1 = new ArrayList<String>();
		class2 = new ArrayList<String>();
		ArrayList<String> agList = new ArrayList<String>();
		if (oneModeType.equals("persons")) {
			if (includeIsolates == true) {
				class1 = persIsolates;
			} else {
				class1 = sc.getPersonList();
			}
			for (int i = 0; i < class1.size(); i++) {
				graph.addVertex(new DnaGraphVertex(i+1, class1.get(i), "p"));
			}
		} else if (oneModeType.equals("organizations")) {
			if (includeIsolates == true) {
				class1 = orgIsolates;
			} else {
				class1 = sc.getOrganizationList();
			}
			for (int i = 0; i < class1.size(); i++) {
				graph.addVertex(new DnaGraphVertex(i+1, class1.get(i), "o"));
			}
		} else {
			if (includeIsolates == true) {
				class1 = catIsolates;
			} else {
				class1 = sc.getCategoryList();
			}
			for (int i = 0; i < class1.size(); i++) {
				graph.addVertex(new DnaGraphVertex(i+1, class1.get(i), "c"));
			}
		}
		if (via.equals("persons")) {
			if (includeIsolates == true) {
				class2 = persIsolates;
			} else {
				class2 = sc.getPersonList();
			}
		} else if (via.equals("organizations")) {
			if (includeIsolates == true) {
				class2 = orgIsolates;
			} else {
				class2 = sc.getOrganizationList();
			}
		} else {
			if (includeIsolates == true) {
				class2 = catIsolates;
			} else {
				class2 = sc.getCategoryList();
			}
		}
		if (agreement.equals("yes")) {
			agList.add("yes");
		} else if (agreement.equals("no")) {
			agList.add("no");
		} else {
			agList.add("yes");
			agList.add("no");
		}

		double[][][] threeMode = new double[class1.size()][class2.size()][agList.size()];
		double[][] results = new double[class1.size()][class1.size()];
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class2.size(); j++) {
				results[i][j] = 0;
			}
		}
		
		//initialize counters for the time window
		int windowSizeInt = new Double(windowSize).intValue();;
		int stepSizeInt = new Double(stepSize).intValue();
		GregorianCalendar startOfPeriod = (GregorianCalendar)start.clone();
		startOfPeriod.add(Calendar.DATE, -windowSizeInt);
		GregorianCalendar endOfPeriod = (GregorianCalendar)start.clone();

		if (verbose == true) {
			System.out.println("done.");
			System.out.print("Starting to move a time window through the discourse... ");
		}
		
		//do this at every time step
		while (!startOfPeriod.after(stop)) {
			
			//reset the 3D array
			for (int i = 0; i < class1.size(); i++) {
				for (int j = 0; j < class2.size(); j++) {
					for (int k = 0; k < agList.size(); k++) {
						threeMode[i][j][k] = 0;
					}
				}
			}
			
			//extract those statements from the statement container which fall into the current time window
			StatementContainer twsc = new StatementContainer();
			for (int i = 0; i < sc.size(); i++) {
				GregorianCalendar scGC = new GregorianCalendar();
				scGC.setTime(sc.get(i).getDate());
				if (!startOfPeriod.after(scGC) && !scGC.after(endOfPeriod)) {
					try {
						twsc.addStatement(sc.get(i), false);
					} catch (DuplicateStatementIdException e) {
						e.printStackTrace();
					}
				}
			}
			sc.sort();
			
			//put the extracted statements as frequency counts into the 3D array
			for (int i = 0; i < twsc.size(); i ++) {
				int c1 = -1;
				int r = -1;
				int c2 = -1;
				
				for (int j = 0; j < class1.size(); j++) {
					if ( (twsc.get(i).getCategory().equals(class1.get(j)) && oneModeType.equals("categories")) ||
							(twsc.get(i).getPerson().equals(class1.get(j)) && oneModeType.equals("persons")) ||
							(twsc.get(i).getOrganization().equals(class1.get(j)) && oneModeType.equals("organizations")) ) {
						c1 = j;
					}
				}
				for (int j = 0; j < class2.size(); j++) {
					if ( (twsc.get(i).getCategory().equals(class2.get(j)) && via.equals("categories")) ||
							(twsc.get(i).getPerson().equals(class2.get(j)) && via.equals("persons")) ||
							(twsc.get(i).getOrganization().equals(class2.get(j)) && via.equals("organizations")) ) {
						c2 = j;
					}
				}
				for (int j = 0; j < agList.size(); j++) {
					if ( twsc.get(i).getAgreement().equals(agList.get(j))) {
						r = j;
					}
				}
				
				if (c1 > -1 && c2 > -1 && r > -1) {
					threeMode[c1][c2][r] = threeMode[c1][c2][r] + 1;
				}
			}
			
			//run through the 3D array, create congruence network edges (the frequency variable), and add them to the result matrix
			for (int i = 0; i < class1.size(); i++) {
				for (int j = 0; j < class1.size(); j++) {
					double frequency = 0;
					if (!class1.get(i).equals(class1.get(j))) {
						if (ignoreDuplicates == true) {
							if (agreement.equals("conflict")) {
								for (int k = 0; k < class2.size(); k++) {
									if ( (threeMode[i][k][0] > 0 && threeMode[j][k][1] > 0) || (threeMode[i][k][1] > 0 && threeMode[j][k][0] > 0) ) {
										frequency++;
									}
								}
							} else {
								for (int k = 0; k < class2.size(); k++) {
									for (int m = 0; m < agList.size(); m++) {
										if (threeMode[i][k][m] > 0 && threeMode[j][k][m] > 0) {
											frequency++;
										}
									}
								}
							}
						} else {
							if (agreement.equals("conflict")) {
								for (int k = 0; k < class2.size(); k++) {
									if ( (threeMode[i][k][0] > 0 && threeMode[j][k][1] > 0) || (threeMode[i][k][1] > 0 && threeMode[j][k][0] > 0) ) {
										frequency = frequency + (threeMode[i][k][0] * threeMode[j][k][1]) + (threeMode[i][k][1] * threeMode[j][k][0]);
									}
								}
							} else {
								for (int k = 0; k < class2.size(); k++) {
									for (int m = 0; m < agList.size(); m++) {
										if (threeMode[i][k][m] > 0 && threeMode[j][k][m] > 0) {
											frequency = frequency + (threeMode[i][k][m] * threeMode[j][k][m]);
										}
									}
								}
							}
						}
					}
					results[i][j] = results[i][j] + frequency;
				}
			}
			
			//move the time window forward
			startOfPeriod.add(Calendar.DATE, stepSizeInt);
			endOfPeriod.add(Calendar.DATE, stepSizeInt);
		}
		
		if (verbose == true) {
			System.out.println("done.");
		}
		
		//normalization
		if (normalization == true) {
			
			if (verbose == true) {
				System.out.print("Normalizing congruence matrix... ");
			}
			
			//reset the 3D array
			for (int i = 0; i < class1.size(); i++) {
				for (int j = 0; j < class2.size(); j++) {
					for (int k = 0; k < agList.size(); k++) {
						threeMode[i][j][k] = 0;
					}
				}
			}
			
			//fill the 3D array with complete affiliation data (not restricted to time window)
			for (int i = 0; i < sc.size(); i ++) {
				int c1 = -1;
				int r = -1;
				int c2 = -1;
				
				for (int j = 0; j < class1.size(); j++) {
					if ( (sc.get(i).getCategory().equals(class1.get(j)) && oneModeType.equals("categories")) ||
							(sc.get(i).getPerson().equals(class1.get(j)) && oneModeType.equals("persons")) ||
							(sc.get(i).getOrganization().equals(class1.get(j)) && oneModeType.equals("organizations")) ) {
						c1 = j;
					}
				}
				for (int j = 0; j < class2.size(); j++) {
					if ( (sc.get(i).getCategory().equals(class2.get(j)) && via.equals("categories")) ||
							(sc.get(i).getPerson().equals(class2.get(j)) && via.equals("persons")) ||
							(sc.get(i).getOrganization().equals(class2.get(j)) && via.equals("organizations")) ) {
						c2 = j;
					}
				}
				for (int j = 0; j < agList.size(); j++) {
					if ( sc.get(i).getAgreement().equals(agList.get(j))) {
						r = j;
					}
				}
				
				if (c1 > -1 && c2 > -1 && r > -1) {
					threeMode[c1][c2][r] = threeMode[c1][c2][r] + 1;
				}
			}
			
			//compute binary degrees in affiliation network
			double[] degrees = new double[class1.size()];
			for (int i = 0; i < class1.size(); i++) {
				double degree = 0;
				for (int j = 0; j < class2.size(); j++) {
					double bothR = 0;
					for (int k = 0; k < agList.size(); k++) {
						bothR = bothR + threeMode[i][j][k];
					}
					if (bothR > 0) {
						degree = degree + 1;
					}
				}
				degrees[i] = degree;
			}
			
			//compute number of time steps
			double dur = (stop.getTimeInMillis() - start.getTimeInMillis()) / (24*60*60*1000) + 1;
			dur = Math.abs(dur);
			double numTimeSteps = (dur + windowSize) / stepSize;
			
			//normalize
			for (int i = 0; i < class1.size(); i++) {
				for (int j = 0; j < class1.size(); j++) {
					results[i][j] = (alpha * results[i][j]) / ( numTimeSteps * ((degrees[i] + degrees[j]) / 2));
				}
			}

			if (verbose == true) {
				System.out.println("done.");
			}
		}

		if (verbose == true) {
			System.out.print("Adding edges from edge-weight matrix to the graph... ");
		}
		
		int count = 1;
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class1.size(); j++) {
				if (results[i][j] > 0) {
					graph.addEdge(new DnaGraphEdge(count, results[i][j], graph.getVertex(i+1), graph.getVertex(j+1)));
					count++;
				}
			}
		}
		
		if (verbose == true) {
			System.out.println("done.");
			System.out.println(graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + " were created.");
		}
	}
	
	/**
	 * Generate a simple graph using the attenuation algorithm.
	 */
	public void generateGraphAttenuation() {
		
		prepareSimpleGraph();
		
		ArrayList<String> concepts = sc.getCategoryList();
		
		if (verbose == true) {
			System.out.println("Total number of concepts: " + concepts.size());
		}
		
		ActorList actorList = new ActorList();
		for (int i = 0; i < class1.size(); i++) {
			if (!actorList.containsActor(class1.get(i)) && !class1.get(i).equals("")) {
				actorList.add(new Actor(class1.get(i)));
			}
		}
		
		if (verbose == true) {
			System.out.println("Total number of actors: " + actorList.size());
		}
		
		for (int i = 0; i < actorList.size(); i++) {
			for (int j = 0; j < concepts.size(); j++) {
				actorList.get(i).addConcept(new Concept(concepts.get(j)));
			}
		}
		
		double[][][] referrals = new double[actorList.size()][actorList.size()][2];
		for (int i = 0; i < actorList.size(); i++) {
			for (int j = 0; j < actorList.size(); j++) {
				for (int k = 0; k < 1; k++) {
					referrals[i][j][k] = 0;
				}
			}
		}
		int m;
		int n;
		long time;
		double days;
		String agree;
		
		for (int l = 0; l < 2; l++) {
			
			if (l == 0) {
				agree = "yes";
				if (verbose == true) {
					System.out.print("Computing positive edge weights... ");
				}
			} else {
				agree = "no";
				if (verbose == true) {
					System.out.print("Computing negative edge weights... ");
				}
			}
			
			for (int i = 0; i < actorList.size(); i++) {
				for (int j = 0; j < actorList.get(i).conceptList.size(); j++) {
					actorList.get(i).conceptList.get(j).dateList.clear();
				}
			}
			
			for (int i = 0; i < actorList.size(); i++) {
				for (int j = 0; j < actorList.get(i).conceptList.size(); j++) {
					for (int k = 0; k < class3.size(); k++) {
						if (actorList.get(i).getId().equals(class1.get(k)) && actorList.get(i).conceptList.get(j).getName().equals(class3.get(k)) && s_agree.get(k).equals(agree)) {
							actorList.get(i).conceptList.get(j).addDate(s_date.get(k));
						}
					}
				}	
			}
			
			for (int i = 0; i < actorList.size(); i++) {
				for (int j = 0; j < actorList.get(0).conceptList.size(); j++) {
					Collections.sort(actorList.get(i).conceptList.get(j).dateList);
				}
			}
			
			//the actual algorithm follows
			for (int i = 0; i < actorList.size(); i++) {
				for (int j = 0; j < actorList.size(); j++) {
					for (int k = 0; k < concepts.size(); k++) {
						if (i != j && actorList.get(i).conceptList.get(k).dateList.size() > 0 && actorList.get(j).conceptList.get(k).dateList.size() > 0) {
							m = 0;
							n = 0;
							while (m < actorList.get(i).conceptList.get(k).dateList.size()) {
								if (n + 1 >= actorList.get(j).conceptList.get(k).dateList.size()) {
									if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n))) {
										time = actorList.get(i).conceptList.get(k).dateList.get(m).getTime().getTime() - actorList.get(j).conceptList.get(k).dateList.get(n).getTime().getTime();
										days = (double)Math.round( (double)time / (24. * 60.*60.*1000.) );
										if (days == 0) {
											days = 1;
										}
										if (normalization == true) {
											referrals[i][j][l] = referrals[i][j][l] + ( Math.exp(days*lambda * (-1)) / actorList.get(i).conceptList.get(k).dateList.size() );
										} else {
											referrals[i][j][l] = referrals[i][j][l] + Math.exp(days*lambda * (-1)); //without normalization
										}
									}
									m++;
								} else if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n)) && ! actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n + 1))) {
									time = actorList.get(i).conceptList.get(k).dateList.get(m).getTime().getTime() - actorList.get(j).conceptList.get(k).dateList.get(n).getTime().getTime();
									days = (int)Math.round( (double)time / (24. * 60.*60.*1000.) );
									if (days == 0) {
										days = 1;
									}
									if (normalization == true) {
										referrals[i][j][l] = referrals[i][j][l] + ( Math.exp(days*lambda * (-1)) / actorList.get(i).conceptList.get(k).dateList.size());
									} else {
										referrals[i][j][l] = referrals[i][j][l] + Math.exp(days*lambda * (-1)); //without normalization
									}
									m++;
								} else if (actorList.get(i).conceptList.get(k).dateList.get(m).after(actorList.get(j).conceptList.get(k).dateList.get(n + 1))) {
									n++;
								} else {
									m++;
								}
							}
						}
					}
				}
				
			}
			if (verbose == true) {
				System.out.println("done.");
			}
		}
		
		if (verbose == true) {
			System.out.print("Assembling graph from edge-weight matrix... ");
		}
		int count = 0;
		if (agreement.equals("yes")) {
			for (int i = 0; i < referrals.length; i++) {
				for (int j = 0; j < referrals[0].length; j++) {
					if (referrals[i][j][0] > 0) {
						graph.addEdge(new DnaGraphEdge(count, referrals[i][j][0], graph.getVertex(actorList.get(i).getId()), graph.getVertex(actorList.get(j).getId())));
						count++;
					}
				}
			}
		} else if (agreement.equals("no")) {
			for (int i = 0; i < referrals.length; i++) {
				for (int j = 0; j < referrals[0].length; j++) {
					if (referrals[i][j][1] > 0) {
						graph.addEdge(new DnaGraphEdge(count, referrals[i][j][1], graph.getVertex(actorList.get(i).getId()), graph.getVertex(actorList.get(j).getId())));
						count++;
					}
				}
			}
		} else if (agreement.equals("combined")) {
			for (int i = 0; i < referrals.length; i++) {
				for (int j = 0; j < referrals[0].length; j++) {
					if (referrals[i][j][0] + referrals[i][j][1] > 0) {
						graph.addEdge(new DnaGraphEdge(count, (referrals[i][j][0] + referrals[i][j][1]), graph.getVertex(actorList.get(i).getId()), graph.getVertex(actorList.get(j).getId())));
						count++;
					}
				}
			}
		}
		if (verbose == true) {
			System.out.println("done.");
			System.out.println(graph.countVertices() + " vertices and " + graph.countEdges() + " edges with a mean edge weight of " + String.format(new Locale("en"), "%.2f", graph.getMeanWeight()) + ".");
		}
	}
	
	/**
	 * This class represents a list of actors. It is needed for the attenuation algorithm.
	 */
	@SuppressWarnings("serial")
	class ActorList extends ArrayList<Actor> {
		
		public boolean containsActor(String id) {
			boolean flag = false;
			for (int i = 0; i < this.size(); i++) {
				if (this.get(i).getId().equals(id)) {
					flag = true;
				}
			}
			return flag;
		}
		
		public Actor getActor(String id) {
			int count = -1;
			for (int i = 0; i < this.size(); i++) {
				if (this.get(i).getId().equals(id)) {
					count = i;
				}
			}
			return this.get(count);
		}
	}
	
	/**
	 * This class represents an actor. It is needed for the attenuation algorithm.
	 */
	class Actor {
		String id;
		ArrayList<Concept> conceptList;
		
		public Actor(String id) {
			this.id = id;
			conceptList = new ArrayList<Concept>();
		}
		
		public void addConcept(Concept concept) {
			conceptList.add(concept);
		}
		
		public boolean containsConcept(String id) {
			boolean flag = false;
			for (int i = 0; i < conceptList.size(); i++) {
				if (conceptList.get(i).getName().equals(id)) {
					flag = true;
				}
			}
			return flag;
		}
		
		public String getId() {
			return id;
		}
		
		public Concept getConcept(String id) {
			int count = -1;
			for (int i = 0; i < conceptList.size(); i++) {
				if (conceptList.get(i).getName().equals(id)) {
					count = i;
				}
			}
			return conceptList.get(count);
		}
	}
	
	/**
	 * This class represents a concept. It is needed for the attenuation algorithm.
	 */
	class Concept {
		ArrayList<GregorianCalendar> dateList;
		String name;
		
		public Concept(String name) {
			dateList = new ArrayList<GregorianCalendar>();
			this.name = name;
		}
		
		public void addDate(GregorianCalendar date) {
			dateList.add(date);
		}
		
		public String getName() {
			return name;
		}
	}
	
	/**
	 * This class represents a single edge and its duration for the SoNIA algorithm.
	 */
	public class SoniaSlice {
		
		double startTime;
		double endTime;
		int strength;
		
		public SoniaSlice(double start, double end, int strength) {
			this.startTime = start;
			this.endTime = end;
			this.strength = strength;
		}
		
	}
	
	/**
	 * This class represents a dyad and its edges for the SoNIA algorithm.
	 */
	public class SoniaDyad {
		
		ArrayList<SoniaSlice> slices = new ArrayList<SoniaSlice>();
		ArrayList<SoniaSlice> reducedSlices = new ArrayList<SoniaSlice>();
		
		public SoniaDyad addSlice(double start, double end) {
			slices.add(new SoniaSlice(start, end, 1));
			return this;
		}
		
		public double getNextPoint(double currentPoint) {
			ArrayList<Double> nextList = new ArrayList<Double>();
			for (int i = 0; i < slices.size(); i++) {
				nextList.add(slices.get(i).startTime);
				nextList.add(slices.get(i).endTime);
			}
			Collections.sort(nextList);
			double nextValue = nextList.get(nextList.size()-1);
			for (int i = nextList.size() - 1; i >= 0; i--) {
				if (nextList.get(i) > currentPoint) {
					nextValue = nextList.get(i);
				}
			}
			return nextValue;
		}
		
		public boolean isNextEndPoint(double currentPoint) {
			double temp = currentPoint;
			boolean end = true;
			try {
				temp = slices.get(0).endTime;
			} catch (NullPointerException npe) {}
			for (int i = 0; i < slices.size(); i++) {
				if (slices.get(i).startTime > currentPoint && slices.get(i).endTime <= temp) {
					temp = slices.get(i).startTime;
					end = false;
				} else if (slices.get(i).endTime > currentPoint && slices.get(i).endTime <= temp) {
					temp = slices.get(i).endTime;
					end = true;
				}
			}
			return end;
		}
		
		public int numberOfEndsAtPoint(double currentPoint) {
			int counter = 0;
			for (int i = 0; i < slices.size(); i++) {
				if (slices.get(i).endTime == currentPoint) {
					counter++;
				}
			}
			return counter;
		}
		
		public int numberOfStartsAtPoint(double currentPoint) {
			int counter = 0;
			for (int i = 0; i < slices.size(); i++) {
				if (slices.get(i).startTime == currentPoint) {
					counter++;
				}
			}
			return counter;
		}
		
		public boolean hasLaterPoint(double point) {
			for (int i = 0; i < slices.size(); i++) {
				if (slices.get(i).startTime > point || slices.get(i).endTime > point) {
					return true;
				}
			}
			return false;
		}
		
		public double getFirstPoint() {
			if (slices.size() > 0) {
				double p = slices.get(0).startTime;
				for (int i = 0; i < slices.size(); i++) {
					if (slices.get(i).startTime < p) {
						p = slices.get(i).startTime;
					}
				}
				return p;
			} else {
				return 0.0;
			}
		}
		
		public double getLastPoint() {
			if (slices.size() > 0) {
				double p = slices.get(0).endTime;
				for (int i = 0; i < slices.size(); i++) {
					if (slices.get(i).endTime > p) {
						p = slices.get(i).endTime;
					}
				}
				return p;
			} else {
				return 2099.0;
			}
		}
		
		public SoniaDyad reduceSlices() {
			
			reducedSlices.clear();
			double firstPoint = getFirstPoint();
			int strength = numberOfStartsAtPoint(getFirstPoint());
			double secondPoint;
			while (hasLaterPoint(firstPoint) == true) {
				secondPoint = getNextPoint(firstPoint);
				reducedSlices.add(new SoniaSlice(firstPoint, secondPoint, strength));
				strength = strength + numberOfStartsAtPoint(secondPoint) - numberOfEndsAtPoint(secondPoint);
				firstPoint = secondPoint;
			}
			for (int i = reducedSlices.size() - 1; i >= 0 ; i--) {
				if (reducedSlices.get(i).strength == 0) {
					reducedSlices.remove(i);
				}
			}
			return this;
		}
	}
	
	//convert a date into double
	public double timeToDouble(GregorianCalendar cal) {
		int days = 364;
		if (cal.isLeapYear(cal.get(Calendar.YEAR)) == true) {
			days = 365;
		}
		double dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		double year = cal.get(Calendar.YEAR);
		double date = year + (dayOfYear / days);
		return date;
	}
	
	/**
	 * SoNIA export filter.
	 * 
	 * @param name of the output file
	 */
	public void exportFilterSonia (String outfile) {
		
		prepareSimpleGraph();
		
		if (verbose == true) {
			System.out.print("Computing SoNIA graph... ");
		}
		
		ArrayList<String> actors = new ArrayList<String>();
		for (int i = 0; i < class1.size(); i++) {
			if (!actors.contains(class1.get(i)) && !class1.get(i).equals("")) {
				actors.add(class1.get(i));
			}
		}
		SoniaDyad[][] matrix = new SoniaDyad[actors.size()][actors.size()];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = new SoniaDyad();
			}
		}
		
		int days = new Double(soniaBackwardDays).intValue();
		double duration;
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class1.size(); j++) {
				if (class3.get(i).equals(class3.get(j))
						&& !class1.get(i).equals(class1.get(j))
						&& !class1.get(i).equals("")
						&& !class1.get(j).equals("")
						&& !class3.get(i).equals("")
						&& !class3.get(j).equals("")) {
					if ((agreement.equals("conflict") && !s_agree.get(i).equals(s_agree.get(j))) || (!agreement.equals("conflict") && s_agree.get(i).equals(s_agree.get(j)))) {
						duration = (s_date.get(i).getTimeInMillis() - s_date.get(j).getTimeInMillis()) / (24*60*60*1000) + 1;
						if (0 <= duration && duration <= days) {
							int firstIndex = 0;
							int secondIndex = 0;
							for (int k = 0; k < actors.size(); k++) {
								if (actors.get(k).equals(class1.get(i))) {
									firstIndex = k;
								} else if (actors.get(k).equals(class1.get(j))) {
									secondIndex = k;
								}
							}
							double startD = timeToDouble(s_date.get(i));
							boolean leap = s_date.get(i).isLeapYear(s_date.get(i).get(Calendar.YEAR));
							double leapDays;
							if (leap == true) {
								leapDays = 365.0;
							} else {
								leapDays = 364.0;
							}
							double forwardWindow = soniaForwardDays / leapDays;
							double endD = startD + forwardWindow;
							matrix[firstIndex][secondIndex] = matrix[firstIndex][secondIndex].addSlice(startD, endD);
						}
					}
				}
			}
		}
		int counter = 0;
		for (int i = 0; i < actors.size(); i++) {
			for (int j = 0; j < actors.size(); j++) {
				if (i != j) {
					matrix[i][j] = matrix[i][j].reduceSlices();
					counter = counter + matrix[i][j].reducedSlices.size();
				}
			}
		}
		if (verbose == true) {
			System.out.println("A dynamic graph with " + actors.size() + " vertices and " + counter + " weighted edges was generated.");
			
			System.out.print("Adjusting time line... ");
		}
		
		HashMap<String,Double> vertexStart = new HashMap<String,Double>();
		HashMap<String,Double> vertexEnd = new HashMap<String,Double>();
		for (int i = 0; i < s_date.size(); i++) {
			if (!vertexStart.containsKey(class1.get(i)) || timeToDouble(s_date.get(i)) < vertexStart.get(class1.get(i))) {
				vertexStart.put(class1.get(i), timeToDouble(s_date.get(i)));
			}
			if (!vertexEnd.containsKey(class1.get(i)) || timeToDouble(s_date.get(i)) > vertexEnd.get(class1.get(i))) {
				boolean leap = s_date.get(i).isLeapYear(s_date.get(i).get(Calendar.YEAR));
				double leapDays;
				if (leap == true) {
					leapDays = 365.0;
				} else {
					leapDays = 364.0;
				}
				double forwardWindow = soniaForwardDays / leapDays;
				double backwardWindow = soniaBackwardDays / leapDays;
				vertexEnd.put(class1.get(i), (timeToDouble(s_date.get(i)) + forwardWindow + backwardWindow));
			}
		}
		if (verbose == true) {
			System.out.println("done.");
		}
		
		try {
			if (verbose == true) {
				System.out.print("Writing data to disk... ");
			}
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("NodeId\tLabel\tStartTime\tEndTime\tNodeSize\tNodeShape\tColorName\tBorderWidth\tBorderColor");
			out.newLine();
			for (int i = 0; i < actors.size(); i++) {
				out.write((i + 1) + "\t" + actors.get(i) + "\t" + vertexStart.get(actors.get(i)) + "\t" + vertexEnd.get(actors.get(i)) + "\t15\tellipse\torange\t1.5\tblack");
				out.newLine();
			}
			out.write("FromId\tToId\tStartTime\tEndTime\tArcWeight\tArcWidth\tColorName");
			for (int i = 0; i < actors.size(); i++) {
				for (int j = 0; j < actors.size(); j++) {
					for (int k = 0; k < matrix[i][j].reducedSlices.size(); k++) {
						out.newLine();
						out.write((i + 1) + "\t" + (j + 1) + "\t" + matrix[i][j].reducedSlices.get(k).startTime + "\t" + matrix[i][j].reducedSlices.get(k).endTime + "\t" + matrix[i][j].reducedSlices.get(k).strength + "\t" + matrix[i][j].reducedSlices.get(k).strength + "\tblack");
					}
				}
			}
			out.close();
			if (verbose == true) {
				System.out.println("File has been exported to \"" + outfile + "\".");
			}
		} catch (IOException e) {
			System.err.println("Error while saving SoNIA file.");
		}
	}
	
	/**
	 * Commetrix SQL export filter.
	 * 
	 * @param name of the output file
	 */
	public void exportFilterCommetrix (String outfile) {
		if (networkName.equals("")) {
			networkName = "DNA_CMX";
		}
		String nodeName = "Name";
		String nodeNumber = "Number";
		String nodeNameDescription = nodeName;
		String nodeNumberDescription = nodeNumber;
		if (oneModeType.equals("persons")) {
			nodeNameDescription = "Name of the person.";
			nodeNumberDescription = "Person number.";
		} else if (oneModeType.equals("organizations")) {
			nodeNameDescription = "Name of the organization.";
			nodeNumberDescription = "Organization number.";
		} else if (oneModeType.equals("categories")) {
			nodeNameDescription = "Name of the category.";
			nodeNumberDescription = "Category number.";
		}
		
		prepareSimpleGraph();
		if (verbose == true) {
			System.out.print("Computing Commetrix graph... ");
		}
		int counter = 0;
		
		int days = new Double(commetrixBackwardWindow).intValue();
		double duration;
		for (int i = 0; i < class1.size(); i++) {
			for (int j = 0; j < class1.size(); j++) {
				if (class3.get(i).equals(class3.get(j))
						&& !class1.get(i).equals(class1.get(j))
						&& !class1.get(i).equals("")
						&& !class1.get(j).equals("")
						&& !class3.get(i).equals("")
						&& !class3.get(j).equals("")) {
					if ((agreement.equals("conflict") && !s_agree.get(i).equals(s_agree.get(j))) || (!agreement.equals("conflict") && s_agree.get(i).equals(s_agree.get(j)))) {
						duration = (s_date.get(i).getTimeInMillis() - s_date.get(j).getTimeInMillis()) / (24*60*60*1000) + 1;
						if (0 <= duration && duration <= days) {
							counter++;
							//graph.addEdge(new DnaGraphEdge(counter, 1, graph.getVertex(class1.get(i)), graph.getVertex(class1.get(j)), s_date.get(i), class3.get(i), s_text.get(i)));
							//substituted for an edge without text detail because memory consumption was too high in MySQL:
							graph.addEdge(new DnaGraphEdge(counter, 1, graph.getVertex(class1.get(i)), graph.getVertex(class1.get(j)), s_date.get(i), class3.get(i), "null"));
						}
					}
				}
			}
		}
		if (verbose == true) {
			System.out.println("A dynamic graph with " + graph.v.size() + " vertices and " + graph.e.size() + " binary edges was generated.");
		}
		
		try {
			if (verbose == true) {
				System.out.print("Writing data to disk... ");
			}
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("-- Commetrix SQL file.");
			out.newLine();
			out.write("-- Produced by Discourse Network Analyzer (DNA).");
			out.newLine();
			out.write("-- http://www.philipleifeld.de");
			out.newLine();
			out.newLine();
			out.newLine();
			out.newLine();
			out.write("-- Data for table `network`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `network` (`networkid`,`ElementTypeID`,`Name`,`Detail1`,`Detail2`,`Detail3`,`Detail4`,`Detail5`) VALUES ");
			out.newLine();
			out.write(" (1,0,'" + networkName + "','" + networkName + "',NULL,NULL,NULL,NULL);");
			out.newLine();
			out.newLine();
			out.newLine();
			out.write("-- Data for table `node`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `node` (`nodeID`,`networkid`,`AliasID`,`Detail1`,`Detail2`,`Detail3`,`Detail4`,`Detail5`) VALUES ");
			
			for (int i = 0; i < graph.v.size(); i++) {
				out.newLine();
				out.write(" (" + graph.v.get(i).getId() + ",1,0,'" + graph.v.get(i).getLabel() + "','" + graph.v.get(i).getId() + "','null','null','null')");
				if (i == graph.v.size() - 1) {
					out.write(";");
				} else {
					out.write(",");
				}
			}
			
			out.newLine();
			out.newLine();
			out.newLine();
			out.write("-- Data for table `linkevent`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `linkevent` (`linkeventID`,`networkid`,`LinkeventDate`,`Subject`,`Content`,`Detail1`,`Detail2`,`Detail3`,`Detail4`,`Detail5`) VALUES"); 
			
			SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			for (int i = 0; i < graph.e.size(); i++) {
				out.newLine();
				out.write(" (" + graph.e.get(i).getId() + ",1,'" + df.format(graph.e.get(i).getDate().getTime()) + "','" + graph.e.get(i).getCategory() + "','" + graph.e.get(i).getDetail() + "','null','null','null','null','null')");
				if (i == graph.e.size() - 1) {
					out.write(";");
				} else {
					out.write(",");
				}
			}
			
			out.newLine();
			out.newLine();
			out.newLine();
			out.write("-- Data for table `linkeventsender`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `linkeventsender` (`linkeventID`,`senderNodeID`,`networkid`) VALUES ");
			
			for (int i = 0; i < graph.e.size(); i++) {
				out.newLine();
				out.write(" (" + graph.e.get(i).getId() + "," + graph.e.get(i).getSource().getId() + ",1)"); 
				if (i == graph.e.size() - 1) {
					out.write(";");
				} else {
					out.write(",");
				}
			}
			
			out.newLine();
			out.newLine();
			out.newLine();
			out.write("-- Data for table `linkeventrecipient`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `linkeventrecipient` (`linkeventID`,`recipientNodeID`,`networkID`) VALUES ");
			
			for (int i = 0; i < graph.e.size(); i++) {
				out.newLine();
				out.write(" (" + graph.e.get(i).getId() + "," + graph.e.get(i).getTarget().getId() + ",1)"); 
				if (i == graph.e.size() - 1) {
					out.write(";");
				} else {
					out.write(",");
				}
			}
			
			out.newLine();
			out.newLine();
			
			out.newLine();
			out.write("-- Metadata for table `network`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `networkdetailconfig` (`detail`,`label`,`description`,`isReadOnly`,`networkid`,`isSize`,`isColor`,`isNumeric`) VALUES");
			out.newLine();
			out.write(" (1,'" + networkName + "','" + networkName + "',1,1,0,0,0);");
			out.newLine();
			out.newLine();
			out.write("-- Metadata for table `node`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `nodedetailconfig` (`detail`,`label`,`description`,`isReadOnly`,`isColor`,`isSize`,`isNumeric`,`networkid`) VALUES");
			out.newLine();
			out.write(" (1,'" + nodeName + "','" + nodeNameDescription +"',1,1,0,0,1),");
			out.newLine();
			out.write(" (2,'" + nodeNumber + "','" + nodeNumberDescription + "',1,1,0,1,1);");
			out.newLine();
			out.newLine();
			out.write("-- Metadata for table `linkevent`");
			out.newLine();
			out.newLine();
			out.write("INSERT INTO `linkeventdetailconfig` (`detail`,`label`,`description`,`isReadOnly`,`networkid`,`isSize`,`isColor`,`isNumeric`) VALUES");
			out.newLine();
			out.write(" (2,'numeric test','numeric test values',0,1,1,1,1),");
			out.newLine();
			out.write(" (3,'alphanum test','alphanumeric test values',0,1,0,1,0);");
			out.newLine();
			out.newLine();
			
			out.close();
			if (verbose == true) {
				System.out.println("File has been exported to \"" + outfile + "\".");
			}
		} catch (IOException e) {
			System.err.println("Error while saving Commetrix SQL file.");
		}
	}
	
	/**
	 * CSV affiliation list export filter.
	 * 
	 * @param name of the output file
	 */
	public void exportCsvAffiliationList (String outfile) {
		if (verbose == true) {
			System.out.print("Writing data to disk... ");
		}
		@SuppressWarnings("unused")
		int nc1 = 0;
		@SuppressWarnings("unused")
		int nc2 = 0;
		ArrayList<String> c1list;
		@SuppressWarnings("unused")
		ArrayList<String> c2list;
		if (twoModeType.equals("oc")) {
			nc1 = graph.countVertexType("o");
			nc2 = graph.countVertexType("c");
			if (includeIsolates == true) {
				c1list = orgIsolates;
				c2list = catIsolates;
			} else {
				c1list = sc.getOrganizationList();
				c2list = sc.getCategoryList();
			}
		} else if (twoModeType.equals("po")) {
			nc1 = graph.countVertexType("p");
			nc2 = graph.countVertexType("o");
			if (includeIsolates == true) {
				c1list = persIsolates;
				c2list = orgIsolates;
			} else {
				c1list = sc.getPersonList();
				c2list = sc.getOrganizationList();
			}
		} else {
			nc1 = graph.countVertexType("p");
			nc2 = graph.countVertexType("c");
			if (includeIsolates == true) {
				c1list = persIsolates;
				c2list = catIsolates;
			} else {
				c1list = sc.getPersonList();
				c2list = sc.getCategoryList();
			}
		}
		
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			for (int i = 0; i < c1list.size(); i++) {
				out.write(c1list.get(i));
				for (int j = 0; j < graph.e.size(); j++) {
					if (graph.e.get(j).getSource().getLabel().equals(c1list.get(i))) {
						out.write(";" + graph.e.get(j).getTarget().getLabel());
					}
				}
				out.write("\n");
			}
			out.close();
			if (verbose == true) {
				System.out.println("File has been exported to \"" + outfile + "\".");
			}
		} catch (IOException e) {
			System.err.println("Error while saving CSV matrix file.");
		}
	}
	
	/**
	 * CSV matrix export filter.
	 * 
	 * @param name of the output file
	 */
	public void exportCsvMatrix (String outfile) {
		if (verbose == true) {
			System.out.print("Writing data to disk... ");
		}
		if (!algorithm.equals("affiliation")) {
			int nc1 = graph.countVertices();
			double[][] csvmat = new double[nc1][nc1];
			
			if (includeIsolates == true) {
				class1 = isolatesList;
			}
			
			ArrayList<String> c1list = new ArrayList<String>();
			for (int i = 0; i < class1.size(); i++) {
				if (!c1list.contains(class1.get(i)) && !class1.get(i).equals("")) {
					c1list.add(class1.get(i));
				}
			}
			
			for (int e = 0; e < graph.e.size(); e++) {
				for (int i = 0; i < nc1; i++) {
					for (int j = 0; j < nc1; j++) {
						if (c1list.get(i).equals(graph.e.get(e).source.getLabel()) && c1list.get(j).equals(graph.e.get(e).target.getLabel())) {
							csvmat[i][j] = graph.e.get(e).getWeight();
						}
					}
				}
			}
			
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				out.write("\"Label\"");
				for (int i = 0; i < nc1; i++) {
					out.write(";\"" + c1list.get(i) + "\"");
				}
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
					for (int j = 0; j < nc1; j++) {
						out.write(";" + String.format(new Locale("en"), "%.6f", csvmat[i][j]));
					}
				}
				out.close();
				if (verbose == true) {
					System.out.println("File has been exported to \"" + outfile + "\".");
				}
			} catch (IOException e) {
				System.err.println("Error while saving CSV matrix file.");
			}
		} else {
			int nc1 = 0;
			int nc2 = 0;
			ArrayList<String> c1list;
			ArrayList<String> c2list;
			if (twoModeType.equals("oc")) {
				nc1 = graph.countVertexType("o");
				nc2 = graph.countVertexType("c");
				if (includeIsolates == true) {
					c1list = orgIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getOrganizationList();
					c2list = sc.getCategoryList();
				}
			} else if (twoModeType.equals("po")) {
				nc1 = graph.countVertexType("p");
				nc2 = graph.countVertexType("o");
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = orgIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getOrganizationList();
				}
			} else {
				nc1 = graph.countVertexType("p");
				nc2 = graph.countVertexType("c");
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getCategoryList();
				}
			}
			
			double[][] csvmat = new double[nc1][nc2];
			
			for (int k = 0; k < graph.countEdges(); k++) {
				for (int i = 0; i < nc1; i++) {
					for (int j = 0; j < nc2; j++) {
						DnaGraphVertex sv = graph.e.get(k).getSource();
						String sl = sv.label;
						DnaGraphVertex tv = graph.e.get(k).getTarget();
						String tl = tv.getLabel();
						if (c1list.get(i).equals(sl) && c2list.get(j).equals(tl)) {
							csvmat[i][j] = graph.e.get(k).getWeight();
						}
					}
				}
			}
			
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				out.write("\"Label\"");
				for (int i = 0; i < nc2; i++) {
					out.write(";\"" + c2list.get(i) + "\"");
				}
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
					for (int j = 0; j < nc2; j++) {
						out.write(";" + new Integer(new Double(csvmat[i][j]).intValue()));
					}
				}
				out.close();
				if (verbose == true) {
					System.out.println("File has been exported to \"" + outfile + "\".");
				}
			} catch (IOException e) {
				System.err.println("Error while saving CSV matrix file.");
			}
		}
	}
	
	/**
	 * DL fullmatrix export filter.
	 * 
	 * @param name of the output file
	 */
	private void exportDlFullMatrix (String outfile) {
		if (verbose == true) {
			System.out.print("Writing data to disk... ");
		}
		if (!algorithm.equals("affiliation")) {
			int nc1 = graph.countVertices();
			double[][] csvmat = new double[nc1][nc1];
			
			if (oneModeType.equals("categories")) {
				isolatesList = catIsolates;
			} else if (oneModeType.equals("persons")) {
				isolatesList = persIsolates;
			} else if (oneModeType.equals("organizations")) {
				isolatesList = orgIsolates;
			}
			
			if (includeIsolates == true) {
				class1 = isolatesList;
			}
			ArrayList<String> c1list = new ArrayList<String>();
			for (int i = 0; i < class1.size(); i++) {
				if (!c1list.contains(class1.get(i)) && !class1.get(i).equals("")) {
					c1list.add(class1.get(i));
				}
			}
			
			for (int e = 0; e < graph.e.size(); e++) {
				for (int i = 0; i < nc1; i++) {
					for (int j = 0; j < nc1; j++) {
						if (c1list.get(i).equals(graph.e.get(e).source.getLabel()) && c1list.get(j).equals(graph.e.get(e).target.getLabel())) {
							csvmat[i][j] = graph.e.get(e).getWeight();
						}
					}
				}
			}
			
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				out.write("DL");
				out.newLine();
				out.write("N=" + nc1);
				out.newLine();
				out.write("FORMAT = FULLMATRIX DIAGONAL PRESENT");
				out.newLine();
				out.write("ROW LABELS:");
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
				}
				out.newLine();
				out.write("ROW LABELS EMBEDDED");
				out.newLine();
				out.write("COLUMN LABELS:");
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
				}
				out.newLine();
				out.write("COLUMN LABELS EMBEDDED");
				out.newLine();
				out.write("DATA:");
				out.newLine();
				out.write("      ");
				for (int i = 0; i < nc1; i++) {
					out.write(" \"" + c1list.get(i) + "\"");
				}
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
					for (int j = 0; j < nc1; j++) {
						out.write(" " + String.format(new Locale("en"), "%.6f", csvmat[i][j]));
					}
				}
				out.close();
				if (verbose == true) {
					System.out.println("File has been exported to \"" + outfile + "\".");
				}
			} catch (IOException e) {
				System.err.println("Error while saving DL matrix file.");
			}
		} else {
			int nc1 = 0;
			int nc2 = 0;
			ArrayList<String> c1list;
			ArrayList<String> c2list;
			if (twoModeType.equals("oc")) {
				nc1 = graph.countVertexType("o");
				nc2 = graph.countVertexType("c");
				if (includeIsolates == true) {
					c1list = orgIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getOrganizationList();
					c2list = sc.getCategoryList();
				}
			} else if (twoModeType.equals("po")) {
				nc1 = graph.countVertexType("p");
				nc2 = graph.countVertexType("o");
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = orgIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getOrganizationList();
				}
			} else {
				nc1 = graph.countVertexType("p");
				nc2 = graph.countVertexType("c");
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getCategoryList();
				}
			}
			
			double[][] csvmat = new double[nc1][nc2];
			
			for (int k = 0; k < graph.countEdges(); k++) {
				for (int i = 0; i < nc1; i++) {
					for (int j = 0; j < nc2; j++) {
						DnaGraphVertex sv = graph.e.get(k).getSource();
						String sl = sv.label;
						DnaGraphVertex tv = graph.e.get(k).getTarget();
						String tl = tv.getLabel();
						if (c1list.get(i).equals(sl) && c2list.get(j).equals(tl)) {
							csvmat[i][j] = graph.e.get(k).getWeight();
						}
					}
				}
			}
			
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
				out.write("DL");
				out.newLine();
				out.write("NR=" + nc1 + ", NC=" + nc2);
				out.newLine();
				out.write("FORMAT = FULLMATRIX DIAGONAL PRESENT");
				out.newLine();
				out.write("ROW LABELS:");
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
				}
				out.newLine();
				out.write("ROW LABELS EMBEDDED");
				out.newLine();
				out.write("COLUMN LABELS:");
				for (int i = 0; i < nc2; i++) {
					out.newLine();
					out.write("\"" + c2list.get(i) + "\"");
				}
				out.newLine();
				out.write("COLUMN LABELS EMBEDDED");
				out.newLine();
				out.write("DATA:");
				out.newLine();
				out.write("      ");
				for (int i = 0; i < nc2; i++) {
					out.write(" \"" + c2list.get(i) + "\"");
				}
				out.newLine();
				for (int i = 0; i < nc1; i++) {
					out.newLine();
					out.write("\"" + c1list.get(i) + "\"");
					for (int j = 0; j < nc2; j++) {
						out.write(" " + new Integer(new Double(csvmat[i][j]).intValue()));
					}
				}
				out.close();
				if (verbose == true) {
					System.out.println("File has been exported to \"" + outfile + "\".");
				}
			} catch (IOException e) {
				System.err.println("Error while saving DL matrix file.");
			}
		}
	}
	
	public String colToHex(Color col) {
		String r = Integer.toHexString(col.getRed());
		String g = Integer.toHexString(col.getGreen());
		String b = Integer.toHexString(col.getBlue());
		if (r.equals("0")) {
			r = "00";
		}
		if (g.equals("0")) {
			g = "00";
		}
		if (b.equals("0")) {
			b = "00";
		}
		String hex = "#" + r + g + b;
		return hex;
	}
	
	/**
	 * Export filter for graphML files.
	 * 
	 * @param name of the output file
	 */
	private void graphMl(String outfile) {
		if (verbose == true) {
			System.out.print("Writing data to disk... ");
		}
		
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
		Document document = new Document(graphml);
		
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
		
		if ((algorithm.equals("affiliation")) || (! algorithm.equals("affiliation") && oneModeType.equals("organizations")) || (! algorithm.equals("affiliation") && oneModeType.equals("persons"))) {
			Element keyType = new Element("key", xmlns);
			keyType.setAttribute(new Attribute("id", "type"));
			keyType.setAttribute(new Attribute("for", "node"));
			keyType.setAttribute(new Attribute("attr.name", "type"));
			keyType.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyType);
			
			Element keyAlias = new Element("key", xmlns);
			keyAlias.setAttribute(new Attribute("id", "alias"));
			keyAlias.setAttribute(new Attribute("for", "node"));
			keyAlias.setAttribute(new Attribute("attr.name", "alias"));
			keyAlias.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyAlias);
			
			Element keyNote = new Element("key", xmlns);
			keyNote.setAttribute(new Attribute("id", "note"));
			keyNote.setAttribute(new Attribute("for", "node"));
			keyNote.setAttribute(new Attribute("attr.name", "note"));
			keyNote.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyNote);
		}
		
		Element keyClass = new Element("key", xmlns);
		keyClass.setAttribute(new Attribute("id", "class"));
		keyClass.setAttribute(new Attribute("for", "node"));
		keyClass.setAttribute(new Attribute("attr.name", "class"));
		keyClass.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyClass);
		
		if (algorithm.equals("affiliation") && ignoreDuplicates == true && ! twoModeType.equals("po")) {
			Element keyAttribute = new Element("key", xmlns);
			keyAttribute.setAttribute(new Attribute("id", "agreement"));
			keyAttribute.setAttribute(new Attribute("for", "edge"));
			keyAttribute.setAttribute(new Attribute("attr.name", "agreement"));
			keyAttribute.setAttribute(new Attribute("attr.type", "string"));
			graphml.addContent(keyAttribute);
		} else {
			Element keyWeight = new Element("key", xmlns);
			keyWeight.setAttribute(new Attribute("id", "weight"));
			keyWeight.setAttribute(new Attribute("for", "edge"));
			keyWeight.setAttribute(new Attribute("attr.name", "weight"));
			keyWeight.setAttribute(new Attribute("attr.type", "double"));
			graphml.addContent(keyWeight);
		}
		
		Element graphElement = new Element("graph", xmlns);
		if (algorithm.equals("affiliation") || algorithm.equals("attenuation")) {
			graphElement.setAttribute(new Attribute("edgedefault", "directed"));
		} else {
			graphElement.setAttribute(new Attribute("edgedefault", "undirected"));
		}
		
		graphElement.setAttribute(new Attribute("id", "G"));
		graphElement.setAttribute(new Attribute("parse.edges", String.valueOf(graph.countEdges())));
		graphElement.setAttribute(new Attribute("parse.nodes", String.valueOf(graph.countVertices())));
		graphElement.setAttribute(new Attribute("parse.order", "free"));
		Element properties = new Element("data", xmlns);
		properties.setAttribute(new Attribute("key", "prop"));
		Element labelAttribute = new Element("labelAttribute", visone);
		if (algorithm.equals("affiliation") && ignoreDuplicates == true && ! twoModeType.equals("po")) {
			labelAttribute.setAttribute("edgeLabel", "agreement");
		} else {
			labelAttribute.setAttribute("edgeLabel", "weight");
		}
		labelAttribute.setAttribute("nodeLabel", "id");
		properties.addContent(labelAttribute);
		graphElement.addContent(properties);
		
		Comment nodes = new Comment(" nodes ");
		graphElement.addContent(nodes);
		
		for (int i = 0; i < graph.getVertices().size(); i++) {
			Element node = new Element("node", xmlns);
			node.setAttribute(new Attribute("id", "v" + String.valueOf(graph.getVertices().get(i).getId())));
			
			Element id = new Element("data", xmlns);
			id.setAttribute(new Attribute("key", "id"));
			id.setText(graph.getVertices().get(i).getLabel());
			node.addContent(id);
			
			String org = graph.getVertices().get(i).getLabel();
			String hex = "#000000";
			Color col;
			
			if (graph.getVertices().get(i).getType().equals("o") || 
					graph.getVertices().get(i).getType().equals("p") ||
					(! algorithm.equals("affiliation") && oneModeType.equals("organizations")) ||
					(! algorithm.equals("affiliation") && oneModeType.equals("persons"))
			) {
				
				Element type = new Element("data", xmlns);
				type.setAttribute(new Attribute("key", "type"));
				
				Element alias = new Element("data", xmlns);
				alias.setAttribute(new Attribute("key", "alias"));
				
				Element note = new Element("data", xmlns);
				note.setAttribute(new Attribute("key", "note"));
				
				String t,a,n;
				
				if (graph.getVertices().get(i).getType().equals("o") || (! algorithm.equals("affiliation") && oneModeType.equals("organizations"))) {
					col = Dna.mainProgram.om.getColor(org);
					hex = colToHex(col);
					t = Dna.mainProgram.om.getActor(org).getType();
					a = Dna.mainProgram.om.getActor(org).getAlias();
					n = Dna.mainProgram.om.getActor(org).getNote();
				} else {
					col = Dna.mainProgram.pm.getColor(org);
					hex = colToHex(col);
					t = Dna.mainProgram.pm.getActor(org).getType();
					a = Dna.mainProgram.pm.getActor(org).getAlias();
					n = Dna.mainProgram.pm.getActor(org).getNote();
				}
				
				type.setText(t);
				node.addContent(type);
				alias.setText(a);
				node.addContent(alias);
				note.setText(n);
				node.addContent(note);
			}
			
			Element vClass = new Element("data", xmlns);
			vClass.setAttribute(new Attribute("key", "class"));
			if (graph.getVertices().get(i).getType().equals("c") || (! algorithm.equals("affiliation") && oneModeType.equals("categories"))) {
				vClass.setText("concept");
			} else if (graph.getVertices().get(i).getType().equals("p") || (! algorithm.equals("affiliation") && oneModeType.equals("persons"))) {
				vClass.setText("person");
			} else if (graph.getVertices().get(i).getType().equals("o") || (! algorithm.equals("affiliation") && oneModeType.equals("organizations"))) {
				vClass.setText("organization");
			}
			node.addContent(vClass);
			
			Element vis = new Element("data", xmlns);
			vis.setAttribute(new Attribute("key", "d0"));
			Element visoneShapeNode = new Element("shapeNode", visone);
			Element yShapeNode = new Element("ShapeNode", yNs);
			Element geometry = new Element("Geometry", yNs);
			geometry.setAttribute(new Attribute("height", "20.0"));
			geometry.setAttribute(new Attribute("width", "20.0"));
			geometry.setAttribute(new Attribute("x", String.valueOf(Math.random()*800)));
			geometry.setAttribute(new Attribute("y", String.valueOf(Math.random()*600)));
			yShapeNode.addContent(geometry);
			Element fill = new Element("Fill", yNs);
			if (graph.getVertices().get(i).getType().equals("o") || (! algorithm.equals("affiliation") && oneModeType.equals("organizations"))) {
				fill.setAttribute(new Attribute("color", hex));
			} else if (graph.getVertices().get(i).getType().equals("c") || (! algorithm.equals("affiliation") && oneModeType.equals("categories"))) {
				fill.setAttribute(new Attribute("color", "#3399FF")); //light blue
			} else if (graph.getVertices().get(i).getType().equals("p") || (! algorithm.equals("affiliation") && oneModeType.equals("persons"))) {
				fill.setAttribute(new Attribute("color", hex));
			}
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
			nodeLabel.setText(graph.getVertices().get(i).getLabel());
			yShapeNode.addContent(nodeLabel);
			
			Element shape = new Element("Shape", yNs);
			if (graph.getVertices().get(i).getType().equals("o") || (! algorithm.equals("affiliation") && oneModeType.equals("organizations"))) {
				shape.setAttribute(new Attribute("type", "ellipse"));
			} else if (graph.getVertices().get(i).getType().equals("c") || (! algorithm.equals("affiliation") && oneModeType.equals("categories"))) {
				shape.setAttribute(new Attribute("type", "roundrectangle"));
			} else if (graph.getVertices().get(i).getType().equals("p") || (! algorithm.equals("affiliation") && oneModeType.equals("persons"))) {
				shape.setAttribute(new Attribute("type", "diamond"));
			}
			yShapeNode.addContent(shape);
			visoneShapeNode.addContent(yShapeNode);
			vis.addContent(visoneShapeNode);
			node.addContent(vis);
			
			graphElement.addContent(node);
		}
		
		Comment edges = new Comment(" edges ");
		graphElement.addContent(edges);
		
		for (int i = 0; i < graph.getEdges().size(); i++) {
			Element edge = new Element("edge", xmlns);
			edge.setAttribute(new Attribute("source", "v" + String.valueOf(graph.getEdges().get(i).getSource().getId())));
			edge.setAttribute(new Attribute("target", "v" + String.valueOf(graph.getEdges().get(i).getTarget().getId())));
			if (algorithm.equals("affiliation") && ignoreDuplicates == true && ! twoModeType.equals("po")) {
				Element agree = new Element("data", xmlns);
				agree.setAttribute(new Attribute("key", "agreement"));
				if (agreement.equals("yes")) {
					agree.setText("yes");
				} else if (agreement.equals("no")) {
					agree.setText("yes");
				} else if (agreement.equals("combined")) {
					if (graph.getEdges().get(i).getWeight() == 1.0) {
						agree.setText("yes");
					} else if (graph.getEdges().get(i).getWeight() == 2.0) {
						agree.setText("no");
					} else if (graph.getEdges().get(i).getWeight() == 3.0) {
						agree.setText("mixed");
					}
				}
				edge.addContent(agree);
			} else {
				Element weight = new Element("data", xmlns);
				weight.setAttribute(new Attribute("key", "weight"));
				weight.setText(String.valueOf(graph.getEdges().get(i).getWeight()));
				edge.addContent(weight);
			}
			
			Element visEdge = new Element("data", xmlns);
			visEdge.setAttribute("key", "e0");
			Element visPolyLineEdge = new Element("polyLineEdge", visone);
			Element yPolyLineEdge = new Element("PolyLineEdge", yNs);
			if (algorithm.equals("attenuation")) {
				Element yArrows = new Element("Arrows", yNs);
				yArrows.setAttribute("source", "none");
				yArrows.setAttribute("target", "StandardArrow");
				yPolyLineEdge.addContent(yArrows);
			}
			Element yLineStyle = new Element("LineStyle", yNs);
			if (algorithm.equals("affiliation") && ignoreDuplicates == true && ! twoModeType.equals("po")) {
				if (agreement.equals("yes")) {
					yLineStyle.setAttribute("color", "#00ff00");
				} else if (agreement.equals("no")) {
					yLineStyle.setAttribute("color", "#ff0000");
				} else if (agreement.equals("combined")) {
					if (graph.getEdges().get(i).getWeight() == 1.0) {
						yLineStyle.setAttribute("color", "#00ff00");
					} else if (graph.getEdges().get(i).getWeight() == 2.0) {
						yLineStyle.setAttribute("color", "#ff0000");
					} else if (graph.getEdges().get(i).getWeight() == 3.0) {
						yLineStyle.setAttribute("color", "#0000ff");
					}
				}
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
		
		graphml.addContent(graphElement);
		
		File dnaFile = new File (outfile);
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
			System.err.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			JOptionPane.showMessageDialog(dna.Dna.mainProgram, "Error while saving the file!\n" + e.getStackTrace());
		}
		if (verbose == true) {
			System.out.println("The file \"" + outfile + "\" has been saved.");
		}
	}
	
	/**
	 * SoNIA affiliation export filter.
	 * 
	 * @param name of the output file
	 */
	public void exportFilterSoniaAffiliation (String outfile) {
		
		prepareSimpleGraph();
		
		if (verbose == true) {
			System.out.print("Computing SoNIA graph... ");
		}
		
		//create list of actors
		ArrayList<String> actors = new ArrayList<String>();
		for (int i = 0; i < class1.size(); i++) {
			if (!actors.contains(class1.get(i)) && !class1.get(i).equals("")) {
				actors.add(class1.get(i));
			}
		}
		
		//create list of categories
		ArrayList<String> categories = new ArrayList<String>();
		for (int i = 0; i < class3.size(); i++) {
			if (!categories.contains(class3.get(i)) && !class3.get(i).equals("")) {
				categories.add(class3.get(i));
			}
		}
		
		//create matrices for Sonia slices
		SoniaDyad[][] matrixYes = new SoniaDyad[actors.size()][categories.size()];
		for (int i = 0; i < matrixYes.length; i++) {
			for (int j = 0; j < matrixYes[0].length; j++) {
				matrixYes[i][j] = new SoniaDyad();
			}
		}
		SoniaDyad[][] matrixNo = new SoniaDyad[actors.size()][categories.size()];
		for (int i = 0; i < matrixNo.length; i++) {
			for (int j = 0; j < matrixNo[0].length; j++) {
				matrixNo[i][j] = new SoniaDyad();
			}
		}
		SoniaDyad[][] matrixCombined = new SoniaDyad[actors.size()][categories.size()];
		for (int i = 0; i < matrixCombined.length; i++) {
			for (int j = 0; j < matrixCombined[0].length; j++) {
				matrixCombined[i][j] = new SoniaDyad();
			}
		}
		
		//fill matrices
		for (int i = 0; i < class1.size(); i++) {
			
			//calculate size of the time window/expiration date of the edge
			double startD = timeToDouble(s_date.get(i));
			boolean leap = s_date.get(i).isLeapYear(s_date.get(i).get(Calendar.YEAR));
			double leapDays;
			if (leap == true) {
				leapDays = 365.0;
			} else {
				leapDays = 364.0;
			}
			double forwardWindow = soniaForwardDays / leapDays;
			double endD = startD + forwardWindow;
			
			//find out which matrix cell to use
			int firstIndex = 0;
			int secondIndex = 0;
			for (int j = 0; j < actors.size(); j++) {
				if (actors.get(j).equals(class1.get(i))) {
					firstIndex = j;
				}
			}
			for (int j = 0; j < categories.size(); j++) {
				if (categories.get(j).equals(class3.get(i))) {
					secondIndex = j;
				}
			}
			
			//add the edge with its start and end point to the matrix
			if (s_agree.get(i).equals("yes")) {
				matrixYes[firstIndex][secondIndex] = matrixYes[firstIndex][secondIndex].addSlice(startD, endD);
			} else {
				matrixNo[firstIndex][secondIndex] = matrixNo[firstIndex][secondIndex].addSlice(startD, endD);
			}
		}
		
		//compute reduced slices/edges for both matrices
		int counterYes = 0;
		for (int i = 0; i < actors.size(); i++) {
			for (int j = 0; j < categories.size(); j++) {
				matrixYes[i][j] = matrixYes[i][j].reduceSlices();
				counterYes = counterYes + matrixYes[i][j].reducedSlices.size();
			}
		}
		int counterNo = 0;
		for (int i = 0; i < actors.size(); i++) {
			for (int j = 0; j < categories.size(); j++) {
				matrixNo[i][j] = matrixNo[i][j].reduceSlices();
				counterNo = counterNo + matrixNo[i][j].reducedSlices.size();
			}
		}
		if (verbose == true) {
			System.out.println("A dynamic graph with " + actors.size() + " vertices and " + counterYes + " positive and " + counterNo + " negative weighted edges was generated.");
		}
		
		/*
		//determine entry and exit of actors
		if (verbose == true) {
			System.out.print("Adjusting time line... ");
		}
		HashMap<String,Double> vertexStart = new HashMap<String,Double>();
		HashMap<String,Double> vertexEnd = new HashMap<String,Double>();
		for (int i = 0; i < s_date.size(); i++) {
			if (!vertexStart.containsKey(class1.get(i)) || timeToDouble(s_date.get(i)) < vertexStart.get(class1.get(i))) {
				vertexStart.put(class1.get(i), timeToDouble(s_date.get(i)));
			}
			if (!vertexEnd.containsKey(class1.get(i)) || timeToDouble(s_date.get(i)) > vertexEnd.get(class1.get(i))) {
				boolean leap = s_date.get(i).isLeapYear(s_date.get(i).get(Calendar.YEAR));
				double leapDays;
				if (leap == true) {
					leapDays = 365.0;
				} else {
					leapDays = 364.0;
				}
				double forwardWindow = forwardDays / leapDays;
				double backwardWindow = (Double)soniaPanel.backwardWindow.getValue() / leapDays;
				vertexEnd.put(class1.get(i), (timeToDouble(s_date.get(i)) + forwardWindow + backwardWindow));
			}
		}
		if (verbose == true) {
			System.out.println("done.");
		}
		*/
		
		//set entry and exit of categories
		GregorianCalendar d1 = s_date.get(0);
		GregorianCalendar d2 = s_date.get(0);
		for (int i = 1; i < s_date.size(); i++) {
			if (s_date.get(i).before(d1)) {
				d1 = s_date.get(i);
			}
			if (s_date.get(i).after(d2)) {
				d2 = s_date.get(i);
			}
		}
		double d1d = timeToDouble(d1);
		double d2d = timeToDouble(d2);
		
		//combine the two matrices?
		
		//write to file
		try {
			if (verbose == true) {
				System.out.print("Writing data to disk... ");
			}
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("NodeId\tLabel\tStartTime\tEndTime\tNodeSize\tNodeShape\tColorName\tBorderWidth\tBorderColor");
			out.newLine();
			for (int i = 0; i < actors.size(); i++) {
				//out.write((i + 1) + "\t" + actors.get(i) + "\t" + vertexStart.get(actors.get(i)) + "\t" + vertexEnd.get(actors.get(i)) + "\t15\tellipse\torange\t1.5\tblack");
				out.write((i + 1) + "\t" + actors.get(i) + "\t" + d1d + "\t" + d2d + "\t15\tellipse\torange\t1.5\tblack");
				out.newLine();
			}
			for (int i = 0; i < categories.size(); i++) {
				out.write((i + 1 + actors.size()) + "\t" + categories.get(i) + "\t" + d1d + "\t" + d2d + "\t20\tsquare\tblue\t1.5\tblack");
				out.newLine();
			}
			out.write("FromId\tToId\tStartTime\tEndTime\tArcWeight\tArcWidth\tColorName");
			for (int i = 0; i < actors.size(); i++) {
				for (int j = 0; j < categories.size(); j++) {
					for (int k = 0; k < matrixYes[i][j].reducedSlices.size(); k++) {
						out.newLine();
						out.write((i + 1) + 
								"\t" + 
								(j + 1 + actors.size()) + 
								"\t" + 
								matrixYes[i][j].reducedSlices.get(k).startTime + 
								"\t" + 
								matrixYes[i][j].reducedSlices.get(k).endTime + 
								"\t" + 
								matrixYes[i][j].reducedSlices.get(k).strength + 
								"\t" + 
								matrixYes[i][j].reducedSlices.get(k).strength + 
								"\tblue");
					}
				}
			}
			for (int i = 0; i < actors.size(); i++) {
				for (int j = 0; j < categories.size(); j++) {
					for (int k = 0; k < matrixNo[i][j].reducedSlices.size(); k++) {
						out.newLine();
						out.write(
								(i + 1) + 
								"\t" + 
								(j + 1 + actors.size()) + 
								"\t" + 
								matrixNo[i][j].reducedSlices.get(k).startTime + 
								"\t" + 
								matrixNo[i][j].reducedSlices.get(k).endTime + 
								"\t" + 
								matrixNo[i][j].reducedSlices.get(k).strength + 
								"\t" + 
								matrixNo[i][j].reducedSlices.get(k).strength + 
								"\tred"
						);
					}
				}
			}
			out.close();
			if (verbose == true) {
				System.out.println("File has been exported to \"" + outfile + "\".");
			}
		} catch (IOException e) {
			System.err.println("Error while saving SoNIA file.");
		}
	}
	
	public double[][] matrixObject () {
		if (verbose == true) {
			System.out.println("Creating matrix object... ");
		}
		double[][] csvmat;
		if (!algorithm.equals("affiliation")) {
			int nc1 = graph.countVertices();
			csvmat = new double[nc1][nc1];
			if (oneModeType.equals("categories")) {
				isolatesList = catIsolates;
			} else if (oneModeType.equals("persons")) {
				isolatesList = persIsolates;
			} else if (oneModeType.equals("organizations")) {
				isolatesList = orgIsolates;
			}
			if (includeIsolates == true) {
				class1 = isolatesList;
			}
			ArrayList<String> c1list = new ArrayList<String>();
			for (int i = 0; i < class1.size(); i++) {
				if (!c1list.contains(class1.get(i)) && !class1.get(i).equals("")) {
					c1list.add(class1.get(i));
				}
			}
			for (int e = 0; e < graph.e.size(); e++) {
				for (int i = 0; i < nc1; i++) {
					for (int j = 0; j < nc1; j++) {
						if (c1list.get(i).equals(graph.e.get(e).source.getLabel()) && c1list.get(j).equals(graph.e.get(e).target.getLabel())) {
							csvmat[i][j] = graph.e.get(e).getWeight();
						}
					}
				}
			}
		} else {
			int nc1 = 0;
			int nc2 = 0;
			ArrayList<String> c1list;
			ArrayList<String> c2list;
			if (twoModeType.equals("oc")) {
				nc1 = graph.countVertexType("o");
				nc2 = graph.countVertexType("c");
				if (includeIsolates == true) {
					c1list = orgIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getOrganizationList();
					c2list = sc.getCategoryList();
				}
			} else if (twoModeType.equals("po")) {
				nc1 = graph.countVertexType("p");
				nc2 = graph.countVertexType("o");
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = orgIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getOrganizationList();
				}
			} else {
				nc1 = graph.countVertexType("p");
				nc2 = graph.countVertexType("c");
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getCategoryList();
				}
			}
			
			csvmat = new double[nc1][nc2];
			
			for (int k = 0; k < graph.countEdges(); k++) {
				for (int i = 0; i < nc1; i++) {
					for (int j = 0; j < nc2; j++) {
						DnaGraphVertex sv = graph.e.get(k).getSource();
						String sl = sv.label;
						DnaGraphVertex tv = graph.e.get(k).getTarget();
						String tl = tv.getLabel();
						if (c1list.get(i).equals(sl) && c2list.get(j).equals(tl)) {
							csvmat[i][j] = graph.e.get(k).getWeight();
						}
					}
				}
			}
		}
		return csvmat;
	}
	
	public String[] getMatrixLabels(boolean row) {
		String[] rowLabels;
		String[] colLabels;
		if (!algorithm.equals("affiliation")) {
			if (includeIsolates == true) {
				class1 = isolatesList;
			}
			
			ArrayList<String> c1list = new ArrayList<String>();
			for (int i = 0; i < class1.size(); i++) {
				if (!c1list.contains(class1.get(i)) && !class1.get(i).equals("")) {
					c1list.add(class1.get(i));
				}
			}
			rowLabels = new String[c1list.size()];
			for (int i = 0; i < c1list.size(); i++) {
				rowLabels[i] = c1list.get(i);
			}
			colLabels = rowLabels;
		} else {
			ArrayList<String> c1list;
			ArrayList<String> c2list;
			if (twoModeType.equals("oc")) {
				if (includeIsolates == true) {
					c1list = orgIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getOrganizationList();
					c2list = sc.getCategoryList();
				}
			} else if (twoModeType.equals("po")) {
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = orgIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getOrganizationList();
				}
			} else {
				if (includeIsolates == true) {
					c1list = persIsolates;
					c2list = catIsolates;
				} else {
					c1list = sc.getPersonList();
					c2list = sc.getCategoryList();
				}
			}
			rowLabels = new String[c1list.size()];
			for (int i = 0; i < c1list.size(); i++) {
				rowLabels[i] = c1list.get(i);
			}
			colLabels = new String[c2list.size()];
			for (int i = 0; i < c2list.size(); i++) {
				colLabels[i] = c2list.get(i);
			}
		}
		if (row == true) {
			return rowLabels;
		} else {
			return colLabels;
		}
	}
	
	public String[] exportAttributes(boolean org, double column) {
		String[] list;
		if (column == 4 && org == true) {
			list = new String[dc.oc.getRowCount()];
			for (int i = 0; i < list.length; i++) {
				String type = (String) dc.oc.getValueAt(i, 1);
				Color color = Color.white;
				for (int j = 0; j < dc.ot.size(); j++) {
					if (((RegexTerm)dc.ot.get(j)).getPattern().equals(type)) {
						color = ((RegexTerm)dc.ot.get(j)).getColor();
					}
				}
				int rgb = color.getRGB();
				String rgbString = Integer.toHexString(rgb);
				rgbString = "#" + rgbString.substring(2, rgbString.length());
				list[i] = rgbString;
			}
		} else if (column == 4 && org == false) {
			list = new String[dc.pc.getRowCount()];
			for (int i = 0; i < list.length; i++) {
				String type = (String) dc.oc.getValueAt(i, 1);
				Color color = Color.white;
				for (int j = 0; j < dc.pt.size(); j++) {
					if (((RegexTerm)dc.pt.get(j)).getPattern().equals(type)) {
						color = ((RegexTerm)dc.pt.get(j)).getColor();
					}
				}
				int rgb = color.getRGB();
				String rgbString = Integer.toHexString(rgb);
				rgbString = "#" + rgbString.substring(2, rgbString.length());
				list[i] = rgbString;
			}
		} else if (org == true) {
			list = new String[dc.oc.getRowCount()];
			for (int i = 0; i < list.length; i++) {
				list[i] = (String) dc.oc.getValueAt(i, new Double(column).intValue());
			}
		} else {
			list = new String[dc.pc.getRowCount()];
			for (int i = 0; i < list.length; i++) {
				list[i] = (String) dc.pc.getValueAt(i, new Double(column).intValue());
			}
		}
		return list;
	}
	
	public String[] getCategories() {
		ArrayList<String> cat =  dc.getSc().getCategoryList();
		String[] categories = new String[cat.size()];
		for (int i = 0; i < cat.size(); i++) {
			categories[i] = cat.get(i);
		}
		return categories;
	}
	
}