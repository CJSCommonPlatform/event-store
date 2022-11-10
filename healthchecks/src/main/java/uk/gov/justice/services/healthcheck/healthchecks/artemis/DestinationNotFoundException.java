package uk.gov.justice.services.healthcheck.healthchecks.artemis;

public class DestinationNotFoundException extends Exception {

    public DestinationNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
