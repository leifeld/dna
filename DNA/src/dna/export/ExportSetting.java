package dna.export;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import dna.dataStructures.StatementType;

public class ExportSetting {
	String networkType;  // [oneMode, twoMode, eventList]
	StatementType statementType;
	Date startDate, stopDate;
	String var1;  // first variable (e.g., organization)
	String var2;  // second variable (e.g., concept)
	String qualifier;  // name of the agreeement qualifier variable (e.g., agreement)
	String agreementPattern;  // [ignore, congruence, conflict, subtract]
	HashMap<String, ArrayList<String>> excludeValues;
	String aggregationRule; // [across date range, per document, per calendar year, per time window]
	String exportFormat; // [csv, dl, graphml]
	String normalization;  // [coocurrence, average, jaccard, cosine]
	boolean countDuplicates;
	boolean includeIsolates;
	
	public ExportSetting(StatementType statementType, Date startDate, Date stopDate, String var1, String var2) {
		networkType = "oneMode";
		qualifier = null;
		agreementPattern = "congruence";
		excludeValues = new HashMap<String, ArrayList<String>>();
		aggregationRule = "across date range";
		exportFormat = "graphml";
		normalization = "cooccurrence";
		countDuplicates = false;
		includeIsolates = false;
		
		this.statementType = statementType;
		this.startDate = startDate;
		this.stopDate = stopDate;
		this.var1 = var1;
		this.var2 = var2;
	}
	
	/**
	 * @return the networkType
	 */
	public String getNetworkType() {
		return networkType;
	}

	/**
	 * @param networkType the networkType to set
	 */
	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	/**
	 * @return the statementType
	 */
	public StatementType getStatementType() {
		return statementType;
	}

	/**
	 * @param statementType the statementType to set
	 */
	public void setStatementType(StatementType statementType) {
		this.statementType = statementType;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the stopDate
	 */
	public Date getStopDate() {
		return stopDate;
	}

	/**
	 * @param stopDate the stopDate to set
	 */
	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}

	/**
	 * @return the var1
	 */
	public String getVar1() {
		return var1;
	}

	/**
	 * @param var1 the var1 to set
	 */
	public void setVar1(String var1) {
		this.var1 = var1;
	}

	/**
	 * @return the var2
	 */
	public String getVar2() {
		return var2;
	}

	/**
	 * @param var2 the var2 to set
	 */
	public void setVar2(String var2) {
		this.var2 = var2;
	}

	/**
	 * @return the qualifier
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * @param qualifier the qualifier to set
	 */
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	/**
	 * @return the agreementPattern
	 */
	public String getAgreementPattern() {
		return agreementPattern;
	}

	/**
	 * @param agreementPattern the agreementPattern to set
	 */
	public void setAgreementPattern(String agreementPattern) {
		this.agreementPattern = agreementPattern;
	}

	/**
	 * @return the excludeValues
	 */
	public HashMap<String, ArrayList<String>> getExcludeValues() {
		return excludeValues;
	}

	/**
	 * @param excludeValues the excludeValues to set
	 */
	public void setExcludeValues(HashMap<String, ArrayList<String>> excludeValues) {
		this.excludeValues = excludeValues;
	}

	/**
	 * @return the aggregationRule
	 */
	public String getAggregationRule() {
		return aggregationRule;
	}

	/**
	 * @param aggregationRule the aggregationRule to set
	 */
	public void setAggregationRule(String aggregationRule) {
		this.aggregationRule = aggregationRule;
	}

	/**
	 * @return the exportFormat
	 */
	public String getExportFormat() {
		return exportFormat;
	}

	/**
	 * @param exportFormat the exportFormat to set
	 */
	public void setExportFormat(String exportFormat) {
		this.exportFormat = exportFormat;
	}

	/**
	 * @return the normalization
	 */
	public String getNormalization() {
		return normalization;
	}

	/**
	 * @param normalization the normalization to set
	 */
	public void setNormalization(String normalization) {
		this.normalization = normalization;
	}

	/**
	 * @return the countDuplicates
	 */
	public boolean isCountDuplicates() {
		return countDuplicates;
	}

	/**
	 * @param countDuplicates the countDuplicates to set
	 */
	public void setCountDuplicates(boolean countDuplicates) {
		this.countDuplicates = countDuplicates;
	}

	/**
	 * @return the includeIsolates
	 */
	public boolean isIncludeIsolates() {
		return includeIsolates;
	}

	/**
	 * @param includeIsolates the includeIsolates to set
	 */
	public void setIncludeIsolates(boolean includeIsolates) {
		this.includeIsolates = includeIsolates;
	}
}