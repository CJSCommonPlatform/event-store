package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import java.util.List;

public interface DestinationNamesProvider {
    List<String> getDestinationNames();
}
