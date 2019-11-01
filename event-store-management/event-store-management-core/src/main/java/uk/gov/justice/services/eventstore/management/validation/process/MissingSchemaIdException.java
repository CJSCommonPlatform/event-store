package uk.gov.justice.services.eventstore.management.validation.process;

public class MissingSchemaIdException extends RuntimeException {

    public MissingSchemaIdException(final String message) {
        super(message);
    }
}
