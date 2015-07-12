package dna;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.DefaultListModel;


public class NetworkExporterObject {

	private StatementType st;
	private String networkType; //One-mode network - Two-mode network - Event-List network
	private String var1mode; // Card 2 - first mode (rows) person
	private String var2mode; // Card 2 - second mode (columns) organization
	private String agreeVar; // Agreement qualifier 
	private ArrayList<String> agreeValList; // Values restricted for agreement qualifier
	private ArrayList<String> exclude1List; // First mode
	private ArrayList<String> exclude2List; // Second mode
	private String agreementPattern; // [Congruence, conflict, subtract, separate]
	private String agregationRule; // [whole date range, per document, per calendar year, per time window]
	private String exportFormat; // [.graphml, .dl, .csv]
	private Date startDate;
	private Date endDate;
	private ArrayList<String> values;
	private ArrayList<String> valuesVar1; // All values from var1mode (rows) without repetitions
	private ArrayList<String> valuesVar2; // All values from var2mode (colums) without repetitions
	private HashMap<String, ArrayList<String>> filterVariables; // All values from var2mode (colums) without repetitions

	
	public DefaultListModel<String> getValuesVar1() {
		DefaultListModel<String> listData = new DefaultListModel<String>();
		if (valuesVar1.size() != 0)
		{
			for (int i=0; i< valuesVar1.size(); i++)
			  listData.addElement(valuesVar1.get(i));		  
		}
		return listData;
	}

	public void setValuesVar1(String[] valuesVar1) {
		
		ArrayList<String> listValues = new ArrayList<String>();
		
		for(int i=0; i<valuesVar1.length; i++)
		{
			if (!listValues.contains(valuesVar1[i]))
				listValues.add(valuesVar1[i]);
		}
		this.valuesVar1 = listValues;
	}

	public DefaultListModel<String> getValuesVar2() {
		
		DefaultListModel<String> listData = new DefaultListModel<String>();
		if (valuesVar2.size() != 0)
		{
			for (int i=0; i< valuesVar2.size(); i++)
			  listData.addElement(valuesVar2.get(i));
		}
		return listData;
	}

	public void setValuesVar2(String[] valuesVar2) {
ArrayList<String> listValues = new ArrayList<String>();
		
		for(int i=0; i<valuesVar2.length; i++)
		{
			if (!listValues.contains(valuesVar2[i]))
				listValues.add(valuesVar2[i]);
		}
		this.valuesVar2 = listValues;
	}

	/**
	 * Creates a {@link NetworkExporterObject} given a {@link StatementType}
	 * @param st - {@link StatementType}
	 */
	public NetworkExporterObject(StatementType st) {
		super();
		this.st = st;
		this.networkType = "oneMode";
		this.var1mode = "";
		this.var2mode = "";
		this.agreeVar = "";
		this.agreeValList = new ArrayList<String>();
		this.agreementPattern = "";
		this.exclude1List = new ArrayList<String>();
		this.exclude2List = new ArrayList<String>();
		this.agregationRule = "whole date range";
		this.startDate = null;
		this.endDate = null;
		this.exportFormat = ".csv";
		this.values = new ArrayList<String>();
		this.valuesVar1 = new ArrayList<String>();
		this.valuesVar2 = new ArrayList<String>();
		this.filterVariables = new HashMap<String, ArrayList<String>>();
	}

	/**
	 * Creates a {@link NetworkExporterObject} 
	 */
	public NetworkExporterObject() {
		this.st = null;
		this.networkType = "oneMode";
		this.var1mode = "";
		this.var2mode = "";
		this.agreeVar = "";
		this.agreeValList =new ArrayList<String>();
		this.agreementPattern = "";
		this.exclude1List = new ArrayList<String>();
		this.exclude2List = new ArrayList<String>();
		this.agregationRule = "whole date range";
		this.startDate = null;
		this.endDate = null;
		this.exportFormat = ".csv";
		this.values = new ArrayList<String>();
		this.valuesVar1 = new ArrayList<String>();
		this.valuesVar2 = new ArrayList<String>();
		this.filterVariables = new HashMap<String, ArrayList<String>>();
	}
	
	public HashMap<String, ArrayList<String>> getFilterVariables(){
		return filterVariables;
	}

	public void setValues(String[] values) {
		ArrayList<String> listValues = new ArrayList<String>();
		
		for(int i=0; i<values.length; i++)
		{
			if (!listValues.contains(values[i]))
				listValues.add(values[i]);
		}
		
		this.values = listValues;
	}
	
	public void setVarVal(ArrayList<String> values,String variable) {
			this.filterVariables.put(variable, values);
	}

	public StatementType getSt() {
		return st;
	}


	public void setSt(StatementType st) {
		this.st = st;
	}


	public String getNetworkType() {
		return networkType;
	}


	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}


	public String getVar1mode() {
		return var1mode;
	}


	public void setVar1mode(String var1mode) {
		this.var1mode = var1mode;
	}


	public String getVar2mode() {
		return var2mode;
	}


	public void setVar2mode(String var2mode) {
		this.var2mode = var2mode;
	}


	public String getAgreementPattern() {
		return agreementPattern;
	}


	public void setAgreementPattern(String agreementPattern) {
		this.agreementPattern = agreementPattern;
	}


	public ArrayList<String> getExclude1List() {
		return exclude1List;
	}

	public void setExclude1List(ArrayList<String> exclude1List) {
		this.exclude1List = exclude1List;
	}

	public ArrayList<String> getExclude2List() {
		return exclude2List;
	}

	public void setExclude2List(ArrayList<String> exclude2List) {
		this.exclude2List = exclude2List;
	}

	public String getAgregationRule() {
		return agregationRule;
	}

	public void setAgregationRule(String agregationRule) {
		this.agregationRule = agregationRule;
	}
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startSpinner) {
		this.startDate = startSpinner;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endSpinner) {
		this.endDate = endSpinner;
	}

	public String getExportFormat() {
		return exportFormat;
	}

	public void setExportFormat(String exportFormat) {
		this.exportFormat = exportFormat;
	}
	
	public String getAgreeVar() {
		return agreeVar;
	}

	public void setAgreeVar(String agreeVar) {
		this.agreeVar = agreeVar;
	}

	public int[] getAgreeValList() {
		int[] values = new int[agreeValList.size()];
		for (int i=0; i<agreeValList.size(); i++)
			values[i]=Integer.parseInt(agreeValList.get(i));		
		return values;
	}

	public void setAgreeValList(ArrayList<String> agreeValList) {
					
			this.agreeValList = agreeValList;
	}

	@Override
	public String toString() {
		if(startDate!=null)
		return "NetworkExporterObject [st=" + st.getLabel() + ", networkType="
				+ networkType + ", var1mode=" + var1mode + ", var2mode="
				+ var2mode + ", agreementPattern=" + agreementPattern
				+ ", agregationRule=" + agregationRule + ", exportFormat="
				+ exportFormat + ", startDate="+ startDate.toString() + ", endDate="
				+ endDate.toString() + "]";
		else
			return "";
	}

	/**
	 * This function returns a {@link DefaultListModel} with the variables of the statementTipe selected to fill a JList.
	 * @return		{@link DefaultListModel<String>} with variables of the the statementTipe selected.
	 */
	public DefaultListModel<String> getVariablesList ()
	{
			LinkedHashMap<String, String> variables = st.getVariables();
			Iterator<String> it = variables.keySet().iterator();
			DefaultListModel<String> listData = new DefaultListModel<String>();
			while (it.hasNext())
			{
			  listData.addElement(it.next());		  
			}
			
			return listData;
	}
	
	/**
	 * This function returns a {@link DefaultListModel} with boolean or integer variables of the statementTipe selected to fill a JList.
	 * @return		{@link DefaultListModel<String>} with Boolean/int variables of the the statementTipe selected.
	 */
	public DefaultListModel<String> getBoolVariablesList ()
	{
			LinkedHashMap<String, String> variables = st.getVariables();
			Iterator<String> it = variables.keySet().iterator();
			//Iterator<String> itValue = variables.values().iterator();
			DefaultListModel<String> listData = new DefaultListModel<String>();

			
			while (it.hasNext())
			{
				String key= it.next();			
				String type = variables.get(key);
				if (type.equals("boolean")||type.equals("integer"))
					listData.addElement(key);		  
			}
			
			return listData;
	}
	
	/**
	 * This function returns a {@link DefaultListModel} with the values of a given Variable of a statementTipe, selected to fill a JList.
	 * @return		{@link DefaultListModel<String>} with variables of the the statementTipe selected.
	 */
	public DefaultListModel<String> getVarVal (String variable)
	{
		DefaultListModel<String> listData = new DefaultListModel<String>();	
		ArrayList<String> values = new ArrayList<String>();
		
		values = filterVariables.get(variable);
			if (values != null)
			{
				for (int i=0; i< values.size(); i++)
				  listData.addElement(values.get(i));		  
			}
			return listData;
	}
	
	/**
	 * This function returns a {@link DefaultListModel} with the values of a Variable of a statementTipe, selected to fill a JList.
	 * @return		{@link DefaultListModel<String>} with variables of the the statementTipe selected.
	 */
	public DefaultListModel<String> getValuesList ()
	{
		DefaultListModel<String> listData = new DefaultListModel<String>();
			if (values.size() != 0)
			{
				for (int i=0; i< values.size(); i++)
				  listData.addElement(values.get(i));		  
			}
			return listData;
	}
	
	/**
	 * @author Ele
	 * This function returns a boolean value to set some options enable.
	 * @return		false: if the networkType is a "eventList" or twoMode network; 
	 * 		 		true: for oneMode Network type
	 */
	public boolean getEnable()
	{
		if (this.networkType.equals("oneMode"))
			return true;
		else
			return false;
	}
	
}
