package uk.gov.justice.services.eventsourcing.publishing;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventsourcing.EventDeQueuer.PUBLISH_TABLE_NAME;

import uk.gov.justice.services.eventsourcing.EventDeQueuer;
import uk.gov.justice.services.eventsourcing.EventFetcher;
import uk.gov.justice.services.eventsourcing.EventFetchingException;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinkedEventDeQueuerAndPublisherTest {

    @Mock
    private EventDeQueuer eventDeQueuer;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private EventFetcher eventFetcher;

    @InjectMocks
    private LinkedEventDeQueuerAndPublisher linkedEventDeQueuerAndPublisher;

    @Test
    public void shouldPublishLinkedEventIfFound() throws Exception {

        final UUID eventId = UUID.randomUUID();
        final String eventName = "event-name";

        final LinkedEvent linkedEvent = mock(LinkedEvent.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(eventDeQueuer.popNextEventId(PUBLISH_TABLE_NAME)).thenReturn(of(eventId));

        when(eventFetcher.getLinkedEvent(eventId)).thenReturn(of(linkedEvent));
        when(eventConverter.envelopeOf(linkedEvent)).thenReturn(jsonEnvelope);
        when(linkedEvent.getName()).thenReturn(eventName);

        assertThat(linkedEventDeQueuerAndPublisher.deQueueAndPublish(), is(true));

        verify(eventPublisher).publish(jsonEnvelope);
    }

    @Test
    public void shouldNotPublishIfNoLinkedEventsAreFoundOnQueue() throws Exception {

        when(eventDeQueuer.popNextEventId(PUBLISH_TABLE_NAME)).thenReturn(empty());

        assertThat(linkedEventDeQueuerAndPublisher.deQueueAndPublish(), is(false));

        verifyZeroInteractions(eventConverter);
        verifyZeroInteractions(eventPublisher);
    }

    @Test
    public void shouldThrowExceptionIfNoLinkedEventFoundInLinkedEventTable() throws Exception {

        final UUID eventId = UUID.randomUUID();

        when(eventDeQueuer.popNextEventId(PUBLISH_TABLE_NAME)).thenReturn(of(eventId));
        when(eventFetcher.getLinkedEvent(eventId)).thenReturn(empty());

        try {
            linkedEventDeQueuerAndPublisher.deQueueAndPublish();
            fail();
        } catch (final EventFetchingException expected) {
            assertThat(expected.getMessage(), is("Failed to find LinkedEvent with id '" + eventId + "'"));
        }

        verifyZeroInteractions(eventConverter);
        verifyZeroInteractions(eventPublisher);
    }
}
