package uk.gov.justice.services.eventsourcing.repository.jdbc.datasource;

public class MissingEventStoreDataSourceName extends RuntimeException {

    public MissingEventStoreDataSourceName(final String message) {
        super(message);
    }
}
