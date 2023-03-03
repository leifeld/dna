package model;

public class RoleVariableLink {
    int id, roleId, variableId;

    public RoleVariableLink(int id, int roleId, int variableId) {
        this.id = id;
        this.roleId = roleId;
        this.variableId = variableId;
    }

    public RoleVariableLink(RoleVariableLink roleVariableLink) {
        this.id = roleVariableLink.getId();
        this.roleId = roleVariableLink.getRoleId();
        this.variableId = roleVariableLink.getVariableId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getVariableId() {
        return variableId;
    }

    public void setVariableId(int variableId) {
        this.variableId = variableId;
    }

    /**
     * Is this role variable link equal to another object?
     *
     * @param o An object for comparison.
     * @return  A boolean indicator of whether the other role variable link is identical.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass())	return false;
        RoleVariableLink l = (RoleVariableLink) o;
        if (this.id != l.getId()) {
            return false;
        }
        if (this.roleId != l.getRoleId()) {
            return false;
        }
        if (this.variableId != l.getVariableId()) {
            return false;
        }
        return true;
    }
}