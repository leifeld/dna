package model;

import java.util.Objects;

public class RoleValue extends Variable {
    private int roleVariableLinkId, roleId, statementTypeId;
    private String roleName;
    private Object value;

    public RoleValue(int variableId, String variableName, String dataType, Object value, int roleVariableLinkId, int roleId, String roleName, int statementTypeId) {
        super(variableId, variableName, dataType);
        this.value = value;
        this.roleVariableLinkId = roleVariableLinkId;
        this.roleId = roleId;
        this.roleName = roleName;
        this.statementTypeId = statementTypeId;
    }

    /**
     * Copy constructor. Creates a deep copy of an existing role value object.
     *
     * @param roleValue An existing role value object that needs to be duplicated.
     */
    public RoleValue(RoleValue roleValue) {
        super(roleValue.getVariableId(), roleValue.getVariableName(), roleValue.getDataType());
        this.value = roleValue.getValue();
        this.roleVariableLinkId = roleValue.getRoleVariableLinkId();
        this.roleId = roleValue.getRoleId();
        this.roleName = roleValue.getRoleName();
        this.statementTypeId = roleValue.getStatementTypeId();
    }

    public int getRoleVariableLinkId() {
        return roleVariableLinkId;
    }

    public void setRoleVariableLinkId(int roleVariableLinkId) {
        this.roleVariableLinkId = roleVariableLinkId;
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

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        RoleValue v = (RoleValue) o;
        // field comparison
        return Objects.equals(this.getVariableId(), v.getVariableId())
                && Objects.equals(this.getVariableName(), v.getVariableName())
                && Objects.equals(this.getDataType(), v.getDataType())
                && Objects.equals(this.getValue(), v.getValue())
                && Objects.equals(this.getRoleVariableLinkId(), v.getRoleVariableLinkId())
                && Objects.equals(this.getRoleId(), v.getRoleId())
                && Objects.equals(this.getRoleName(), v.getRoleName())
                && Objects.equals(this.getStatementTypeId(), v.getStatementTypeId());
    }
}
