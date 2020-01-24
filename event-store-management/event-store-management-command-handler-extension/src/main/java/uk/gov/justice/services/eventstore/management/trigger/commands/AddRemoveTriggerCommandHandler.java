package uk.gov.justice.services.eventstore.management.trigger.commands;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.commands.AddTriggerCommand.ADD_TRIGGER;
import static uk.gov.justice.services.eventstore.management.commands.RemoveTriggerCommand.REMOVE_TRIGGER;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_COMPLETE;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_FAILED;
import static uk.gov.justice.services.jmx.api.domain.CommandState.COMMAND_IN_PROGRESS;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.eventstore.management.commands.AddTriggerCommand;
import uk.gov.justice.services.eventstore.management.commands.RemoveTriggerCommand;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;
import uk.gov.justice.services.jmx.state.events.SystemCommandStateChangedEvent;

import java.util.UUID;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;

@Interceptors(MdcLoggerInterceptor.class)
public class AddRemoveTriggerCommandHandler {

    @Inject
    private Event<SystemCommandStateChangedEvent> systemCommandStateChangedEventFirer;

    @Inject
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Inject
    private UtcClock clock;

    @Inject
    private Logger logger;

    @HandlesSystemCommand(ADD_TRIGGER)
    public void addTriggerToEventLogTable(final AddTriggerCommand addTriggerCommand, final UUID commandId) {

        logger.info(format("Received command %s", addTriggerCommand.getName()));

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

    @HandlesSystemCommand(REMOVE_TRIGGER)
    public void removeTriggerFromEventLogTable(final RemoveTriggerCommand removeTriggerCommand, final UUID commandId) {

        logger.info(format("Received command %s", removeTriggerCommand.getName()));

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
            logger.error(message, e);
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
