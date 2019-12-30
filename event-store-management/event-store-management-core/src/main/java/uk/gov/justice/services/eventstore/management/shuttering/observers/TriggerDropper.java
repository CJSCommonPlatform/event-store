package uk.gov.justice.services.eventstore.management.shuttering.observers;

import static java.lang.String.format;
import static uk.gov.justice.services.management.suspension.api.SuspensionResult.suspensionFailed;
import static uk.gov.justice.services.management.suspension.api.SuspensionResult.suspensionSucceeded;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.management.suspension.api.Suspendable;
import uk.gov.justice.services.management.suspension.api.SuspensionResult;
import uk.gov.justice.services.management.suspension.commands.SuspensionCommand;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

public class TriggerDropper implements Suspendable {

    @Inject
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Inject
    private Logger logger;

    @Override
    public boolean shouldSuspend() {
        return true;
    }

    @Override
    public boolean shouldUnsuspend() {
        return true;
    }

    @Override
    public SuspensionResult suspend(final UUID commandId, final SuspensionCommand suspensionCommand) {

        try {
            eventLogTriggerManipulator.removeTriggerFromEventLogTable();

            final String message = "Trigger successfully removed from event_log table";
            logger.info(message);

            return suspensionSucceeded(
                    getName(),
                    commandId,
                    message,
                    suspensionCommand
            );
        } catch (final Exception e) {
            final String message = format("Failed to remove trigger from event_log table: %s:%s", e.getClass().getSimpleName(), e.getMessage());
            logger.error(message, e);

            return suspensionFailed(
                    getName(),
                    commandId,
                    message,
                    suspensionCommand,
                    e
            );
        }
    }

    @Override
    public SuspensionResult unsuspend(final UUID commandId, final SuspensionCommand suspensionCommand) {
        try {
            eventLogTriggerManipulator.addTriggerToEventLogTable();

            final String message = "Trigger successfully added to event_log table";
            logger.info(message);

            return suspensionSucceeded(
                    getName(),
                    commandId,
                    message,
                    suspensionCommand
            );
        } catch (final Exception e) {
            final String message = format("Failed to add trigger to event_log table: %s:%s", e.getClass().getSimpleName(), e.getMessage());
            logger.error(message, e);

            return suspensionFailed(
                    getName(),
                    commandId,
                    message,
                    suspensionCommand,
                    e
            );
        }
    }
}
