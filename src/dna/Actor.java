package dna;

class Actor implements Comparable<Actor> {
	
	String name, type, alias, note;
	boolean appearsInDataSet;
	
	public Actor(String name, String type, String alias, String note, boolean appearsInDataSet) {
		this.name = name;
		this.type = type;
		this.alias = alias;
		this.note = note;
		this.appearsInDataSet = appearsInDataSet;
	}
	
	public Actor(String name, boolean appearsInDataSet) {
		this.name = name;
		this.appearsInDataSet = appearsInDataSet;
	}

	public boolean appearsInDataSet() {
		return appearsInDataSet;
	}

	public void setAppearsInDataSet(boolean appearsInDataSet) {
		this.appearsInDataSet = appearsInDataSet;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public int compareTo(Actor o) {
		if (this.getName().compareTo(o.getName()) < 0) {
			return -1;
		} else if (this.getName().compareTo(o.getName()) > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	//necessary for sorting purposes
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (getClass() != o.getClass()) return false;
		return compareTo((Actor) o) == 0;
	}
}