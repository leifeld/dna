package dna;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class TimeSeriesExporter {
	
	String outfile;
	StatementContainer sc;
	boolean persons, onePerRow;
	String timeUnit, ignoreDuplicates;
	Date start, stop;
	String[] includedPersons, includedOrganizations, includedCategories;
	
	String[] actors;
	int timeSteps;
	int[][] matrix;
	
	public TimeSeriesExporter(
			StatementContainer sc, 
			String outfile, 
			boolean persons, 
			String timeUnit, 
			String ignoreDuplicates, 
			boolean onePerRow,
			Date start, 
			Date stop, 
			String[] includedPersons, 
			String[] includedOrganizations, 
			String[] includedCategories
			) {
		this.sc = sc;
		this.outfile = outfile;
		this.persons = persons;
		this.timeUnit = timeUnit;
		this.ignoreDuplicates = ignoreDuplicates;
		this.onePerRow = onePerRow;
		this.start = start;
		this.stop = stop;
		this.includedCategories = includedCategories;
		this.includedOrganizations = includedOrganizations;
		this.includedPersons = includedPersons;
		
		calculate();
		csv();
	}
	
	public TimeSeriesExporter(
			String infile, 
			boolean persons,
			String timeUnit,
			String ignoreDuplicates,
			boolean onePerRow,
			String startDate,
			String stopDate,
			String[] includedPersons,
			String[] includedOrganizations,
			String[] includedCategories
			) {
		
		this.persons = persons;
		this.timeUnit = timeUnit;
		this.ignoreDuplicates = ignoreDuplicates;
		this.onePerRow = onePerRow;
		
		//infile
		DnaContainer dc = new ParseLatest(infile).getDc();
		this.sc = dc.sc;
		
		//start date
		GregorianCalendar startGC;
		if (startDate.equals("first")) {
			try {
				startGC = sc.getFirstDate();
			} catch (NullPointerException npe) {
				startGC = new GregorianCalendar(1900, 1, 1);
			}
		} else {
			String[] startDateComponents = startDate.split("\\.");
			startGC = new GregorianCalendar((int)Integer.valueOf(startDateComponents[2]), (int)Integer.valueOf(startDateComponents[1]) - 1, (int)Integer.valueOf(startDateComponents[0]));
		}
		this.start = startGC.getTime();
		
		//stop date
		GregorianCalendar stopGC;
		if (stopDate.equals("last")) {
			try {
				stopGC = sc.getLastDate();
			} catch (NullPointerException npe) {
				stopGC = new GregorianCalendar(1900, 1, 1);
			}
		} else {
			String[] stopDateComponents = stopDate.split("\\.");
			stopGC = new GregorianCalendar((int)Integer.valueOf(stopDateComponents[2]), (int)Integer.valueOf(stopDateComponents[1]) - 1, (int)Integer.valueOf(stopDateComponents[0]));
		}
		this.stop = stopGC.getTime();
		
		//includedPersons
		if (includedPersons[0].equals("all")) {
			ArrayList<String> pl = dc.sc.getPersonList();
			this.includedPersons = new String[pl.size()];
			for (int i = 0; i < pl.size(); i++) {
				this.includedPersons[i] = pl.get(i);
			}
		} else {
			this.includedPersons = includedPersons;
		}
		
		//includedOrganizations
		if (includedOrganizations[0].equals("all")) {
			ArrayList<String> ol = dc.sc.getOrganizationList();
			this.includedOrganizations = new String[ol.size()];
			for (int i = 0; i < ol.size(); i++) {
				this.includedOrganizations[i] = ol.get(i);
			}
		} else {
			this.includedOrganizations = includedOrganizations;
		}
		
		//includedCategories
		if (includedCategories[0].equals("all")) {
			ArrayList<String> cl = dc.sc.getCategoryList();
			this.includedCategories = new String[cl.size()];
			for (int i = 0; i < cl.size(); i++) {
				this.includedCategories[i] = cl.get(i);
			}
		} else {
			this.includedCategories = includedCategories;
		}
		
		//do the calculations
		calculate();
	}
	
	public boolean isActorIncluded(String actor) {
		if (persons == true) {
			for (int i = 0; i < includedPersons.length; i++) {
				if (includedPersons[i].equals(actor)) {
					return true;
				}
			}
		} else {
			for (int i = 0; i < includedOrganizations.length; i++) {
				if (includedOrganizations[i].equals(actor)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isCategoryIncluded(String category) {
		for (int i = 0; i < includedCategories.length; i++) {
			if (includedCategories[i].equals(category)) {
				return true;
			}
		}
		return false;
	}
	
	public int numTimeSteps(Date date1, Date date2) {
		GregorianCalendar cal1 = new GregorianCalendar();
		cal1.setTime(date1);
		GregorianCalendar cal2 = new GregorianCalendar();
		cal2.setTime(date2);
		int diff = 0;
		if (timeUnit.equals("month")) {
			int y = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
		    int m = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
		    diff = y*12 + m;
		} else if (timeUnit.equals("year")) {
			int y = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
		    diff = y;
		} else {
			diff = 0;
		}
		return diff;
	}
	
	public void calculate() {
		
		//actor and date filter
		for (int i = sc.size() - 1; i >= 0; i--) {
			String actor;
			if (persons == true) {
				actor = sc.get(i).getPerson(); 
			} else {
				actor = sc.get(i).getOrganization();
			}
			if ( sc.get(i).getDate().before(start)
					|| sc.get(i).getDate().after(stop)
					|| isActorIncluded(actor) == false
					|| isCategoryIncluded(sc.get(i).getCategory()) == false ) {
				sc.removeStatement(sc.get(i).getId());
			}
		}
		
		//duplicate filter
		ArrayList<Integer> duplicates = new ArrayList<Integer>();
		for (int i = 0; i < sc.size(); i++) {
			GregorianCalendar iCal = new GregorianCalendar();
			iCal.setTime(sc.get(i).getDate());
			int iMonth = iCal.get(Calendar.MONTH);
			int iYear = iCal.get(Calendar.YEAR);
			for (int j = 0; j < sc.size(); j++) {
				GregorianCalendar jCal = new GregorianCalendar();
				jCal.setTime(sc.get(j).getDate());
				int jMonth = jCal.get(Calendar.MONTH);
				int jYear = jCal.get(Calendar.YEAR);
				if (	((sc.get(j).getArticleTitle().equals(sc.get(i).getArticleTitle())
							&& ignoreDuplicates.equals("article"))
							|| (ignoreDuplicates.equals("month")
							&& (iYear == jYear
							&& iMonth == jMonth)))
						&& sc.get(j).getCategory().equals(sc.get(i).getCategory())
						&& ((sc.get(j).getPerson().equals(sc.get(i).getPerson())
							&& persons == true)
							|| (sc.get(j).getOrganization().equals(sc.get(i).getOrganization())
							&& persons == false))
						&& !duplicates.contains(j)
						&& !duplicates.contains(i)
						&& i != j
						&& sc.get(i).getAgreement().equals(sc.get(j).getAgreement())) {
					duplicates.add(i);
				}
			}
		}
		for (int i = sc.size() - 1; i >= 0; i--) {
			if (duplicates.contains(i)) {
				sc.removeStatement(sc.get(i).getId());
			}
		}
		System.out.println("Number of omitted duplicates: " + duplicates.size() + ". Statements left: " + sc.size());
		
		//count actors, compile actor list, and put their indices in a hash map
		Object[] actorsO;
		if (persons == true) {
			actorsO = includedPersons;
		} else {
			actorsO = includedOrganizations;
		}
		actors = new String[actorsO.length];
		for (int i = 0; i < actorsO.length; i++) {
			actors[i] = actorsO[i].toString();
		}
		HashMap<String, Integer> actorIndices = new HashMap<String, Integer>();
		for (int i = 0; i < actors.length; i++) {
			actorIndices.put(actors[i], i);
		}
		
		//create null matrix
		timeSteps = numTimeSteps(start, stop) + 1;
		matrix = new int[actors.length][timeSteps];
		for (int i = 0; i < actors.length; i++) {
			for (int j = 0; j < timeSteps; j++) {
				matrix[i][j] = 0;
			}
		}
		
		//fill the actual matrix with frequency values
		for (int i = 0; i < sc.size(); i++) {
			int y = numTimeSteps(start, sc.get(i).getDate());
			String actor;
			if (persons == true) {
				actor = sc.get(i).getPerson(); 
			} else {
				actor = sc.get(i).getOrganization();
			}
			int x = actorIndices.get(actor);
			matrix[x][y] = matrix[x][y] + 1;
		}
	}
	
	private void csv() {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("Actor");
			
			//write column header
			GregorianCalendar cal1 = new GregorianCalendar();
			cal1.setTime(start);
			String currentDateString;
			int y = cal1.get(Calendar.YEAR);
		    int m = cal1.get(Calendar.MONTH);
			for (int i = 0; i < timeSteps; i++) {
				if (timeUnit.equals("month")) {
				    if (m < 12) {
				    	m++;
				    } else {
				    	m = 1;
				    	y++;
				    }
				    currentDateString = new Integer(y).toString() + "-" + new Integer(m).toString();
				} else if (timeUnit.equals("year")) {
					y++;
					currentDateString = new Integer(y - 1).toString();
				} else {
					currentDateString = "frequency";
				}
				out.write(";" + currentDateString);
			}
			
			//write matrix contents
			if (onePerRow == true) {
				for (int i = 0; i < matrix.length; i++) {
					out.newLine();
					out.write(actors[i]);
					for (int j = 0; j < matrix[i].length; j++) {
						out.write(";" + matrix[i][j]);
					}
				}
			} else {
				out.newLine();
				out.write("set of " + actors.length + " actors");
				for (int i = 0; i < matrix[0].length; i++) {
					int count = 0;
					for (int j = 0; j < matrix.length; j++) {
						count = count + matrix[j][i];
					}
					out.write(";" + count);
				}
			}
			
			out.close();
			System.out.println("Statistics file has been exported to \"" + outfile + "\".");
		} catch (IOException e) {
			System.out.println("Error while saving CSV statistics file.");
		}
	}
	
	public int[][] getMatrixObject() {
		if (onePerRow == true) {
			return matrix;
		} else {
			int[][] singleRowMatrix = new int[1][matrix[0].length];
			for (int i = 0; i < matrix[0].length; i++) {
				int count = 0;
				for (int j = 0; j < matrix.length; j++) {
					count = count + matrix[j][i];
				}
				singleRowMatrix[1][i] = count;
			}
			return singleRowMatrix;
		}
	}
	
	public String[] getRowLabels() {
		String[] rowLabels;
		if (onePerRow == true) {
			rowLabels = actors;
		} else {
			rowLabels = new String[1];
			rowLabels[0] = "set of " + actors.length + " actors";
		}
		return rowLabels;
	}
	
	public String[] getColumnLabels() {
		String[] columnLabels = new String[timeSteps];
		GregorianCalendar cal1 = new GregorianCalendar();
		cal1.setTime(start);
		String currentDateString;
		int y = cal1.get(Calendar.YEAR);
	    int m = cal1.get(Calendar.MONTH);
		for (int i = 0; i < timeSteps; i++) {
			if (timeUnit.equals("month")) {
			    if (m < 12) {
			    	m++;
			    } else {
			    	m = 1;
			    	y++;
			    }
			    currentDateString = new Integer(y).toString() + "-" + new Integer(m).toString();
			} else if (timeUnit.equals("year")) {
				y++;
				currentDateString = new Integer(y - 1).toString();
			} else {
				currentDateString = "frequency";
			}
			columnLabels[i] = currentDateString;
		}
		return columnLabels;
	}
}
