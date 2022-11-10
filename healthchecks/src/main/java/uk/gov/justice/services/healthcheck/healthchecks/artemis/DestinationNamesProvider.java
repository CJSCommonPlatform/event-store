package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class DestinationNamesProvider {

    private static final List<String> DESTINATION_NAME_PATTERNS = List.of("%s.controller.command", "%s.handler.command", "%s.event");

    @Inject
    private JndiContextNameProvider jndiContextNameProvider;

    public List<String> getDestinationNames() {
        var contextName = jndiContextNameProvider.getContextName();
        return DESTINATION_NAME_PATTERNS.stream()
                .map(qp -> String.format(qp, contextName))
                .collect(Collectors.toList());
    }
}
