package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import uk.gov.justice.services.messaging.jms.DestinationProvider;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.List;

public class JmsDestinationsVerifier {

    @Inject
    private DestinationNamesProvider destinationNamesProvider;

    @Inject
    private DestinationProvider destinationProvider;

    public void verify(Session session) throws DestinationNotFoundException {
        List<String> destinationNames = destinationNamesProvider.getDestinationNames();

        try{
            for (final String destinationName : destinationNames) {
                verifyThatDestinationExist(session, destinationName);
            }
        } catch(Exception e) {
            throw new DestinationNotFoundException(e);
        }
    }

    private void verifyThatDestinationExist(Session session, String destinationName) throws JMSException {
        var destination = destinationProvider.getDestination(destinationName);
        var consumer = session.createConsumer(destination);
        consumer.close();
    }
}
