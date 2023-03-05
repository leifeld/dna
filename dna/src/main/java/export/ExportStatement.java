package export;


import model.Statement;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

/**
 * An extension of the Statement class, which also holds some document meta-
 * data and a hash map of the values (in addition to the array list of
 * values).
 */
public class ExportStatement extends Statement {
    private HashMap<String, Object> map;
    private String title, author, source, section, type;
    private LocalDateTime dateTime;

    /**
     * Create an export statement.
     *
     * @param statement The statement to be converted.
     * @param title The document title.
     * @param author The author.
     * @param source The source.
     * @param section The section.
     * @param type The type.
     * @param dateTime The date and time of the statement.
     */
    public ExportStatement(Statement statement, String title, String author,
                           String source, String section, String type,
                           LocalDateTime dateTime) {
        super(statement);
        this.title = title;
        this.author = author;
        this.source = source;
        this.section = section;
        this.type = type;
        this.dateTime = dateTime;
        this.map = new HashMap<String, Object>();
        // TODO: re-implement this
        /*
        for (Variable value : ExportStatement.this.getVariables()) {
            map.put(value.getVariableName(), value.getValue());
        }
        */
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
        this.dateTime = exportStatement.getDateTime();
        this.map = new HashMap<String, Object>();
        // TODO: re-implement this
        /*
        for (Variable value : exportStatement.getVariables()) {
            map.put(value.getVariableName(), value.getValue());
        }
        */
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

    /**
     * Get the date/time stored in the statement.
     *
     * @return The date/time as a {@link LocalDateTime} object.
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Get the date/time of the document in which the statement is located as seconds since 1 January 1970. Used in the
     * {@code dna_network} function in the rDNA R package because {@link java.time.LocalDateTime} objects cannot be
     * transferred directly to R using the rJava package.
     *
     * @return Date/time in seconds since 1 January 1970.
     */
    public long getDateTimeLong() {
        return this.getDateTime().toEpochSecond(ZoneOffset.UTC);
    }

    /**
     * Set the date/time of the corresponding document.
     *
     * @param dateTime The new date/time to set.
     */
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDocumentIdAsString() {
        return String.valueOf(this.getDocumentId());
    }
}