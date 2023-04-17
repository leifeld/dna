package model;

import java.awt.*;

public class Role {
    private int id, statementTypeId, position, numMin, numMax, numDefault, defaultVariableId;
    private String roleName;
    private Color color;

    public Role(int id, String roleName, Color color, int statementTypeId, int position, int numMin, int numMax, int numDefault, int defaultVariableId) {
        this.id = id;
        this.roleName = roleName;
        this.color = color;
        this.statementTypeId = statementTypeId;
        this.position = position;
        this.numMin = numMin;
        this.numMax = numMax;
        this.numDefault = numDefault;
        this.defaultVariableId = defaultVariableId;
    }

    public Role(int id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    public int getId() {
        return id;
    }

    public int getStatementTypeId() {
        return statementTypeId;
    }

    public void setStatementTypeId(int statementTypeId) {
        this.statementTypeId = statementTypeId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getNumMin() {
        return numMin;
    }

    public void setNumMin(int numMin) {
        this.numMin = numMin;
    }

    public int getNumMax() {
        return numMax;
    }

    public void setNumMax(int numMax) {
        this.numMax = numMax;
    }

    public int getNumDefault() {
        return numDefault;
    }

    public void setNumDefault(int numDefault) {
        this.numDefault = numDefault;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getDefaultVariableId() {
        return defaultVariableId;
    }

    public void setDefaultVariableId(int defaultVariableId) {
        this.defaultVariableId = defaultVariableId;
    }

    /**
     * Is this role equal to another object?
     *
     * @param o An object for comparison.
     * @return  A boolean indicator of whether the other role is identical.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass())	return false;
        Role r = (Role) o;
        if (this.id != r.getId()) {
            return false;
        }
        if ((this.roleName == null) != (r.getRoleName() == null)) {
            return false;
        }
        if (!this.roleName.equals(r.getRoleName())) {
            return false;
        }
        if ((this.color == null) != (r.getColor() == null)) {
            return false;
        }
        if (!this.color.equals(r.getColor())) {
            return false;
        }
        if (this.statementTypeId != r.getStatementTypeId()) {
            return false;
        }
        if (this.position != r.getPosition()) {
            return false;
        }
        if (this.numMin != r.getNumMin()) {
            return false;
        }
        if (this.numMax != r.getNumMax()) {
            return false;
        }
        if (this.numDefault != r.getNumDefault()) {
            return false;
        }
        return true;
    }
}