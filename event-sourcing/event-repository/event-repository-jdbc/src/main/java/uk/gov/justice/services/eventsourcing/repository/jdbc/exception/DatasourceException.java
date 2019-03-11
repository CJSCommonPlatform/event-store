package uk.gov.justice.services.eventsourcing.repository.jdbc.exception;


/**
 * Exception thrown when a request tries to create an Event JDBC Repository without datasource
 */
public class DatasourceException extends Exception {

    private static final long serialVersionUID = 5934757852541630746L;

    public DatasourceException(String message) {
        super(message);
    }

}
