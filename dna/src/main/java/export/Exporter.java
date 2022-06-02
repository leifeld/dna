package export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import model.Statement;

public class Exporter {
	private ArrayList<Statement> statements;

	public Exporter(ArrayList<Statement> statements) {
		this.statements = statements;
	}

	public void filterStatements(
			String duplicateSetting,
			HashMap<String, ArrayList<String>> excludeValues,
			String var1Name,
			String var2Name,
			String qualifierName,
			boolean ignoreQualifier,
			boolean filterEmptyFields
			) {
		this.statements = this.statements
				.stream()
				.filter(s -> true) // TODO: implement filters
				.collect(Collectors.toCollection(ArrayList::new));
	}
}