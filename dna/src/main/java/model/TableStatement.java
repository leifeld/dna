package model;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class TableStatement extends Statement {
    private String text, coderName, statementTypeLabel;
    private Color coderColor, statementTypeColor;
    private LocalDateTime dateTime;
    private ArrayList<RoleValue> roleValues;

    public TableStatement(int id, int start, int stop, int statementTypeId, int coderId, int documentId, LocalDateTime dateTime, String text, String coderName, Color coderColor, String statementTypeLabel, Color statementTypeColor, ArrayList<RoleValue> roleValues) {
        super(id, start, stop, statementTypeId, coderId, documentId);
        this.dateTime = dateTime;
        this.text = text;
        this.coderName = coderName;
        this.coderColor = coderColor;
        this.statementTypeColor = statementTypeColor;
        this.statementTypeLabel = statementTypeLabel;
        this.roleValues = roleValues;
    }

    public TableStatement(TableStatement tableStatement) {
        super(tableStatement);
        this.dateTime = tableStatement.getDateTime();
        this.text = tableStatement.getText();
        this.coderName = tableStatement.getCoderName();
        this.coderColor = tableStatement.getCoderColor();
        this.statementTypeColor = tableStatement.getStatementTypeColor();
        this.statementTypeLabel = tableStatement.getStatementTypeLabel();
        this.roleValues = tableStatement.getRoleValues();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCoderName() {
        return coderName;
    }

    public void setCoderName(String coderName) {
        this.coderName = coderName;
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

    public String getStatementTypeLabel() {
        return statementTypeLabel;
    }

    public void setStatementTypeLabel(String statementTypeLabel) {
        this.statementTypeLabel = statementTypeLabel;
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

    public ArrayList<RoleValue> getRoleValues() {
        return roleValues;
    }

    public void setRoleValues(ArrayList<RoleValue> roleValues) {
        this.roleValues = roleValues;
    }
}