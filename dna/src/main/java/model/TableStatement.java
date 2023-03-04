package model;

import java.awt.*;
import java.time.LocalDateTime;

public class TableStatement extends Statement {
    private String text;
    private String coderName;
    private Color coderColor, statementTypeColor;

    public TableStatement(int id, int start, int stop, int statementTypeId, int coderId, int documentId, LocalDateTime dateTime, String text, String coderName, Color coderColor, Color statementTypeColor) {
        super(id, start, stop, statementTypeId, coderId, documentId, dateTime);
        this.text = text;
        this.coderName = coderName;
        this.coderColor = coderColor;
    }

    public TableStatement(TableStatement tableStatement) {
        super(tableStatement);
        this.text = tableStatement.getText();
        this.coderName = tableStatement.getCoderName();
        this.coderColor = tableStatement.getCoderColor();
        this.statementTypeColor = tableStatement.getStatementTypeColor();
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
}