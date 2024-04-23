package uk.gov.justice.services.eventstore.management.replay.process;

import java.util.Objects;
import java.util.UUID;

public class ReplayEventContext {

    private final UUID commandId;
    private final UUID commandRuntimeId;
    private final String eventSourceName;
    private final String componentName;

    public ReplayEventContext(
            final UUID commandId,
            final UUID commandRuntimeId,
            final String eventSourceName,
            final String componentName) {
        this.commandId = commandId;
        this.commandRuntimeId = commandRuntimeId;
        this.eventSourceName = eventSourceName;
        this.componentName = componentName;
    }

    public UUID getCommandId() {
        return commandId;
    }
    public UUID getCommandRuntimeId() {
        return commandRuntimeId;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getEventSourceName() {
        return eventSourceName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ReplayEventContext)) return false;
        final ReplayEventContext that = (ReplayEventContext) o;
        return Objects.equals(commandId, that.commandId) &&
                Objects.equals(commandRuntimeId, that.commandRuntimeId) &&
                Objects.equals(componentName, that.componentName) &&
                Objects.equals(eventSourceName, that.eventSourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandId, commandRuntimeId, componentName, eventSourceName);
    }

    @Override
    public String toString() {
        return "ReplayEventContext{" +
                "commandId=" + commandId +
                ", commandRuntimeId=" + commandRuntimeId +
                ", componentName=" + componentName +
                ", eventSourceName=" + eventSourceName +
                '}';
    }
}
