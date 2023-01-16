package uk.gov.justice.services.eventsourcing.publishedevent.publish;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventRepository;
import uk.gov.justice.services.eventsourcing.publisher.jms.EventPublisher;
import uk.gov.justice.services.eventsourcing.repository.jdbc.PublishQueueRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedPublishQueuesDataAccessAndPublisherTest {

    @Mock
    private PublishQueueRepository publishQueueRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PublishedEventRepository publishedEventRepository;

    @InjectMocks
    private PublishedEventDeQueuerAndPublisher publishedEventDeQueuerAndPublisher;

    @Test
    public void shouldPublishPublishedEventIfFound() throws Exception {

        final UUID eventId = UUID.randomUUID();
        final String eventName = "event-name";

        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final JsonEnvelope jsonEnvelope = mock(JsonEnvelope.class);

        when(publishQueueRepository.popNextEventId()).thenReturn(of(eventId));

        when(publishedEventRepository.getPublishedEvent(eventId)).thenReturn(of(publishedEvent));
        when(eventConverter.envelopeOf(publishedEvent)).thenReturn(jsonEnvelope);

        assertThat(publishedEventDeQueuerAndPublisher.deQueueAndPublish(), is(true));

        verify(eventPublisher).publish(jsonEnvelope);
    }

    @Test
    public void shouldNotPublishIfNoPublishedEventsAreFoundOnQueue() throws Exception {

        when(publishQueueRepository.popNextEventId()).thenReturn(empty());

        assertThat(publishedEventDeQueuerAndPublisher.deQueueAndPublish(), is(false));

        verifyNoInteractions(eventConverter);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    public void shouldThrowExceptionIfNoPublishedEventFoundInPublishedEventTable() throws Exception {

        final UUID eventId = UUID.randomUUID();

        when(publishQueueRepository.popNextEventId()).thenReturn(of(eventId));
        when(publishedEventRepository.getPublishedEvent(eventId)).thenReturn(empty());

        try {
            publishedEventDeQueuerAndPublisher.deQueueAndPublish();
            fail();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getMessage(), is("Failed to find PublishedEvent with id '" + eventId + "'"));
        }

        verifyNoInteractions(eventConverter);
        verifyNoInteractions(eventPublisher);
    }
}
