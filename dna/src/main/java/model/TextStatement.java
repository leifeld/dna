package model;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TextStatement extends Statement {
    private Color coderColor;
    private Color statementTypeColor;
    private LocalDateTime dateTime;

    public TextStatement(int id, int start, int stop, int statementTypeId, int coderId, int documentId, LocalDateTime dateTime, Color coderColor, Color statementTypeColor) {
        super(id, start, stop, statementTypeId, coderId, documentId);
        this.dateTime = dateTime;
        this.coderColor = coderColor;
        this.statementTypeColor = statementTypeColor;
    }

    public TextStatement(TextStatement textStatement) {
        super(textStatement);
        this.dateTime = textStatement.getDateTime();
        this.coderColor = textStatement.getCoderColor();
        this.statementTypeColor = textStatement.getStatementTypeColor();
    }

    public Color getCoderColor() {
        return coderColor;
    }

    public void setCoderColor(Color coderColor) {
        this.coderColor = coderColor;
    }

    public Color getStatementTypeColor() {
        return this.statementTypeColor;
    }

    public void setStatementTypeColor(Color statementTypeColor) {
        this.statementTypeColor = statementTypeColor;
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
}