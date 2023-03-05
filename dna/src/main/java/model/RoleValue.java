package model;

public class RoleValue extends Variable {
    private int roleVariableLinkId, dataId, roleId, statementTypeId;
    private String roleName;
    private Object value;

    public RoleValue(int variableId, String variableName, String dataType, Object value, int roleVariableLinkId, int dataId, int roleId, String roleName, int statementTypeId) {
        super(variableId, variableName, dataType);
        this.value = value;
        this.roleVariableLinkId = roleVariableLinkId;
        this.dataId = dataId;
        this.roleId = roleId;
        this.roleName = roleName;
        this.statementTypeId = statementTypeId;
    }

    public int getRoleVariableLinkId() {
        return roleVariableLinkId;
    }

    public void setRoleVariableLinkId(int roleVariableLinkId) {
        this.roleVariableLinkId = roleVariableLinkId;
    }

    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getStatementTypeId() {
        return statementTypeId;
    }

    public void setStatementTypeId(int statementTypeId) {
        this.statementTypeId = statementTypeId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
