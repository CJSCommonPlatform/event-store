package uk.gov.justice.services.eventstore.management.validation.process;

import static java.lang.String.format;
import static uk.gov.justice.services.eventstore.management.CommandResult.failure;
import static uk.gov.justice.services.eventstore.management.CommandResult.success;

import uk.gov.justice.services.eventstore.management.CommandResult;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class EventValidationProcess {

    @Inject
    private EventValidator eventValidator;

    public CommandResult validateAllPublishedEvents(final UUID commandId) {

        final List<ValidationError> validationErrors = eventValidator.findErrors();

        if (validationErrors.isEmpty()) {
            return success(
                    commandId,
                    "All PublishedEvents successfully passed schema validation"
            );
        }

        return failure(
                commandId,
                format("%d PublishedEvent(s) failed schema validation. Please see server logs for errors", validationErrors.size())
        );

    }
}
