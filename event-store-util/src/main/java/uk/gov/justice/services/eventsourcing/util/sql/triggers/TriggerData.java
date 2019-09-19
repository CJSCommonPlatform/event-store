package uk.gov.justice.services.eventsourcing.util.sql.triggers;

import java.util.Objects;

public class TriggerData {

    private final String tableName;
    private final String triggerName;
    private final String manipulationType;
    private final String action;
    private final String timing;

    public TriggerData(
            final String tableName,
            final String triggerName,
            final String manipulationType,
            final String action,
            final String timing) {
        this.tableName = tableName;
        this.triggerName = triggerName;
        this.manipulationType = manipulationType;
        this.action = action;
        this.timing = timing;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public String getManipulationType() {
        return manipulationType;
    }

    public String getAction() {
        return action;
    }

    public String getTiming() {
        return timing;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TriggerData)) return false;
        final TriggerData that = (TriggerData) o;
        return Objects.equals(tableName, that.tableName) &&
                Objects.equals(triggerName, that.triggerName) &&
                Objects.equals(manipulationType, that.manipulationType) &&
                Objects.equals(action, that.action) &&
                Objects.equals(timing, that.timing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, triggerName, manipulationType, action, timing);
    }

    @Override
    public String toString() {
        return "TriggerData{" +
                "tableName='" + tableName + '\'' +
                ", triggerName='" + triggerName + '\'' +
                ", manipulationType='" + manipulationType + '\'' +
                ", action='" + action + '\'' +
                ", timing='" + timing + '\'' +
                '}';
    }
}
