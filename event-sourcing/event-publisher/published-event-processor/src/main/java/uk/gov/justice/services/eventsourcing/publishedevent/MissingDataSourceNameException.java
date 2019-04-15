package uk.gov.justice.services.eventsourcing.publishedevent;

public class MissingDataSourceNameException extends RuntimeException {
    public MissingDataSourceNameException(final String message) {
        super(message);
    }
}
