package uk.gov.justice.services.healthcheck.healthchecks.artemis;

public class DestinationNotFoundException extends Exception {

    public DestinationNotFoundException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
