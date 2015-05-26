package dna;

import java.util.ArrayList;
import java.util.Date;

public class EventExporter {

	public EventExporter() {
		//TODO: write GUI elements to define variables
		String statementType = "DNAStatement";
		String sourceVariable = "organization";
		String targetVariable = "category";
		String typeVariable = "agreement";
		String weightVariable = null;
		ArrayList<StatementEvent> evlist = getStatementEventList(statementType, 
				sourceVariable, targetVariable, typeVariable, weightVariable);
		//TODO: convert calendaric time axis into numbers; remove weekends
		//TODO: export to CSV file and other formats
	}

	// a class for relational events
	public class StatementEvent {
		
		String source, target, type;
		Date time;
		double weight;
		
		public StatementEvent(String source, String target, String type, 
				Date time, double weight) {
			this.source = source;
			this.target = target;
			this.type = type;
			this.time = time;
			this.weight = weight;
		}
		
		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Date getTime() {
			return time;
		}

		public void setTime(Date time) {
			this.time = time;
		}

		public double getWeight() {
			return weight;
		}
		
		public void setWeight(double weight) {
			this.weight = weight;
		}
	}
	
	// a function which provides a list of events for relational event modeling
	public ArrayList<StatementEvent> getStatementEventList(
			String statementType, String sourceVariable, String targetVariable, 
			String typeVariable, String weightVariable) {
		
		ArrayList<SidebarStatement> s = Dna.dna.db.getStatementsByType(
				statementType);
		ArrayList<StatementEvent> sev = new ArrayList<StatementEvent>();
		for (int i = 0; i < s.size(); i++) {
			Date d = s.get(i).getDate();
			int statementId = s.get(i).statementId;
			String source = Dna.dna.db.getVariableStringEntry(statementId, 
					sourceVariable);
			String target = Dna.dna.db.getVariableStringEntry(statementId, 
					targetVariable);
			String typeType = Dna.dna.db.getDataType(typeVariable, 
					statementType);
			String type = "";
			if (typeType.equals("integer")) {
				type = Integer.toString(Dna.dna.db.getVariableIntEntry(
						statementId, typeVariable));
			} else if (typeType.equals("boolean")) {
				int b = Dna.dna.db.getVariableIntEntry(statementId, 
						typeVariable);
				if (b == 1) {
					type = "positive";
				} else if (b == 0) {
					type = "negative";
				}
			} else {
				type = Dna.dna.db.getVariableStringEntry(statementId, 
						typeVariable);
			}
			double weight = 0;
			if (weightVariable == null) {
				weight = 1; 
			} else {
				String weightType = Dna.dna.db.getDataType(weightVariable, 
						statementType);
				if (weightType.equals("boolean") || weightType.equals(
						"integer")) {
					weight = Dna.dna.db.getVariableIntEntry(statementId, 
							weightVariable);
				} else {
					System.err.println("Weight variable is a text variable");
				}
			}
			StatementEvent se = new StatementEvent(source, target, type, d, 
					weight);
			sev.add(se);
		}
		return sev;
	}
}