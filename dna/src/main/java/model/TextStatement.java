package model;

import java.awt.*;
import java.time.LocalDateTime;

public class TextStatement extends Statement {
    private Color coderColor;
    private Color statementTypeColor;

    public TextStatement(int id, int start, int stop, int statementTypeId, int coderId, int documentId, LocalDateTime dateTime, Color coderColor, Color statementTypeColor) {
        super(id, start, stop, statementTypeId, coderId, documentId, dateTime);
        this.coderColor = coderColor;
        this.statementTypeColor = statementTypeColor;
    }

    public TextStatement(TextStatement textStatement) {
        super(textStatement);
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
}