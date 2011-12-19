package dna;

import java.util.ArrayList;

public class DnaContainer {
	
	ArticleContainer ac;
	StatementContainer sc;
	String currentFileName;
	ArrayList<RegexTerm> regexTerms;
	ActorContainer pc;
	ActorContainer oc;
	RegexListModel pt;
	RegexListModel ot;
	
	public DnaContainer() {
		currentFileName = new String("");
		ac = new ArticleContainer();
		sc = new StatementContainer();
		pc = new ActorContainer();
		oc = new ActorContainer();
		pt = new RegexListModel();
		ot = new RegexListModel();
		regexTerms = new ArrayList<RegexTerm>();
	}
	
	public void clear() {
		ac.clear();
		sc.clear();
		currentFileName = "";
		regexTerms.clear();
		pc.actorList.clear();
		oc.actorList.clear();
		pt.removeAllElements();
		ot.removeAllElements();
	}
	
	public RegexListModel getPt() {
		return pt;
	}

	public void setPt(RegexListModel pt) {
		this.pt = pt;
	}

	public RegexListModel getOt() {
		return ot;
	}

	public void setOt(RegexListModel ot) {
		this.ot = ot;
	}
	
	public ArticleContainer getAc() {
		return ac;
	}

	public void setAc(ArticleContainer ac) {
		this.ac = ac;
	}

	public StatementContainer getSc() {
		return sc;
	}

	public void setSc(StatementContainer sc) {
		this.sc = sc;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

	public void setCurrentFileName(String currentFileName) {
		this.currentFileName = currentFileName;
	}
	
	public ArrayList<RegexTerm> getRegexTerms() {
		return regexTerms;
	}

	public void setRegexTerms(ArrayList<RegexTerm> regexTerms) {
		this.regexTerms = regexTerms;
	}
	
	public ActorContainer getPc() {
		return pc;
	}

	public void setPc(ActorContainer pc) {
		this.pc = pc;
	}

	public ActorContainer getOc() {
		return oc;
	}

	public void setOc(ActorContainer oc) {
		this.oc = oc;
	}
}