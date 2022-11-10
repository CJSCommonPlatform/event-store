package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import java.util.List;

public class DestinationNotFoundException extends Exception {

    private final List<String> destinationNames;

    public DestinationNotFoundException(List<String> destinationNames, Exception cause) {
        super(cause);
        this.destinationNames = destinationNames;
    }

    public String formattedDestinationNames() {
        return String.join(", ", this.destinationNames);
    }
}
