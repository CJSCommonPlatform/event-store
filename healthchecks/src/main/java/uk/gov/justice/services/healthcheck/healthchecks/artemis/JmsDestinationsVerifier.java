package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import uk.gov.justice.services.messaging.jms.DestinationProvider;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

public class JmsDestinationsVerifier {

    @Inject
    private DestinationNamesProvider destinationNamesProvider;

    @Inject
    private DestinationProvider destinationProvider;

    public void verify(Session session) throws DestinationNotFoundException {
        final var destinationNames = destinationNamesProvider.getDestinationNames();

        final List<String> failedDestinationNames = new ArrayList<>();
        Exception cause = null;
        for (final String destinationName : destinationNames) {
            try {
                verifyThatDestinationExist(session, destinationName);
            } catch (Exception e) {
                failedDestinationNames.add(destinationName);
                cause = e;
            }
        }

        if(failedDestinationNames.size() != 0) {
            throw new DestinationNotFoundException(failedDestinationNames, cause);
        }
    }

    private void verifyThatDestinationExist(final Session session, final String destinationName) throws JMSException {
        var destination = destinationProvider.getDestination(destinationName);

        try (final MessageConsumer consumer = session.createConsumer(destination)) {
        }
    }
}
