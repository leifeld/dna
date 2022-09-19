package model;


import java.util.HashMap;

/**
 * An extension of the Statement class, which also holds some document meta-
 * data and a hash map of the values (in addition to the array list of
 * values).
 */
public class ExportStatement extends Statement {
    private HashMap<String, Object> map;
    private String title, author, source, section, type;

    /**
     * Create an export statement.
     *
     * @param statement The statement to be converted.
     * @param title The document title.
     * @param author The author.
     * @param source The source.
     * @param section The section.
     * @param type The type.
     */
    public ExportStatement(Statement statement, String title, String author,
                           String source, String section, String type) {
        super(statement);
        this.title = title;
        this.author = author;
        this.source = source;
        this.section = section;
        this.type = type;
        this.map = new HashMap<String, Object>();
        for (Value value : ExportStatement.this.getValues()) {
            map.put(value.getKey(), value.getValue());
        }
    }

    /**
     * Copy constructor to create a deep copy of an export statement.
     *
     * @param exportStatement An existing export statement.
     */
    public ExportStatement(ExportStatement exportStatement) {
        super(exportStatement);
        this.title = exportStatement.getTitle();
        this.author = exportStatement.getAuthor();
        this.source = exportStatement.getSource();
        this.section = exportStatement.getSection();
        this.type = exportStatement.getType();
        this.map = new HashMap<String, Object>();
        for (Value value : exportStatement.getValues()) {
            map.put(value.getKey(), value.getValue());
        }
    }

    public Object get(String key) {
        return this.map.get(key);
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getSource() {
        return this.source;
    }

    public String getSection() {
        return this.section;
    }

    public String getType() {
        return this.type;
    }

    public String getDocumentIdAsString() {
        return String.valueOf(this.getDocumentId());
    }
}