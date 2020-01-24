package uk.gov.justice.services.eventstore.management.validation.process;

public class MissingSchemaException extends RuntimeException {
    public MissingSchemaException(final String message) {
        super(message);
    }
}
