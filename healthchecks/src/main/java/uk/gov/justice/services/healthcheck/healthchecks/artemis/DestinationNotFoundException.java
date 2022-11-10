package uk.gov.justice.services.healthcheck.healthchecks.artemis;

public class DestinationNotFoundException extends Exception {

    public DestinationNotFoundException(final String message) {
        super(message);
    }
}
