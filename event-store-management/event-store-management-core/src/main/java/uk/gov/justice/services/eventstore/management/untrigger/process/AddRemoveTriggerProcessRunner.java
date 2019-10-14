package uk.gov.justice.services.eventstore.management.untrigger.process;

import static java.lang.String.format;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jmx.api.command.AddTriggerCommand;
import uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.slf4j.Logger;

public class AddRemoveTriggerProcessRunner {

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    public void addTriggerToEventLogTable(final UUID commandId, final AddTriggerCommand addTriggerCommand) {

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                addTriggerCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                "Add trigger to event log table process started"
        ));

        try {
            eventLogTriggerManipulator.addTriggerToEventLogTable();

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    addTriggerCommand,
                    COMMAND_COMPLETE,
                    clock.now(),
                    "Add trigger to event log table process complete"
            ));
        } catch (final Exception e) {

            final String message = format("Add trigger to event log table process failed: %s: %s", e.getClass().getSimpleName(), e.getMessage());

            logger.error(message, e);
            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    addTriggerCommand,
                    COMMAND_FAILED,
                    clock.now(),
                    message
            ));
        }
    }

    public void removeTriggerFromEventLogTable(final UUID commandId, final RemoveTriggerCommand removeTriggerCommand) {

        systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                commandId,
                removeTriggerCommand,
                COMMAND_IN_PROGRESS,
                clock.now(),
                "Remove trigger from event log table process started"
        ));

        try {
            eventLogTriggerManipulator.removeTriggerFromEventLogTable();

            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    removeTriggerCommand,
                    COMMAND_COMPLETE,
                    clock.now(),
                    "Remove trigger from event log table process complete"
            ));
        } catch (final Exception e) {
            final String message = format("Remove trigger from event log table process failed: %s: %s", e.getClass().getSimpleName(), e.getMessage());
            logger.error(message,e);
            systemCommandStateChangedEventFirer.fire(new SystemCommandStateChangedEvent(
                    commandId,
                    removeTriggerCommand,
                    COMMAND_FAILED,
                    clock.now(),
                    message
            ));
        }
    }
}
