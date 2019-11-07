package uk.gov.justice.services.eventstore.management.validation.process;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;
import org.slf4j.Logger;

public class SingleEventValidator {

    @Inject
    private SchemaProvider schemaProvider;

    @Inject
    private JsonStringConverter jsonStringConverter;

    @Inject
    private Logger logger;

    public Optional<ValidationError> validate(final PublishedEvent publishedEvent) {

        final String eventName = publishedEvent.getName();
        final UUID eventId = publishedEvent.getId();

        final String payload = publishedEvent.getPayload();
        final JSONObject jsonObject = jsonStringConverter.asJsonObject(payload);

        final Schema schema = schemaProvider.getForEvent(eventName);

        try {
            schema.validate(jsonObject);
        } catch (final ValidationException e) {
            final String message = format(
                    "Event '%s' with id '%s' failed to validate against schema: %s",
                    eventName,
                    eventId,
                    e.getAllMessages());

            logger.error(message);

            return of(new ValidationError(
                    eventName,
                    eventId,
                    message
            ));
        }

        return empty();
    }
}
