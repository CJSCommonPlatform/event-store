package uk.gov.justice.services.eventsourcing.publisher.jms;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.jms.JmsEnvelopeSender;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class JmsEventPublisherTest {

    private static final String DESTINATION_NAME = "destinationName";

    private static final String EVENT_NAME = "test.event.listener";

    @Mock
    private Logger logger;

    @Mock
    private JmsEnvelopeSender jmsEnvelopeSender;

    @Mock
    private EventDestinationResolver eventDestinationResolver;

    @InjectMocks
    private JmsEventPublisher jmsEventPublisher;

    @Test
    public void shouldPublishEnvelope() {

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataBuilder()
                        .withName(EVENT_NAME)
                        .withId(randomUUID()),
                createObjectBuilder().build()
        );

        when(eventDestinationResolver.destinationNameOf(EVENT_NAME)).thenReturn(DESTINATION_NAME);

        jmsEventPublisher.publish(envelope);

        verify(jmsEnvelopeSender).send(envelope, DESTINATION_NAME);
        verify(logger).trace("Publishing event {} to {}", EVENT_NAME, DESTINATION_NAME);
    }

}
