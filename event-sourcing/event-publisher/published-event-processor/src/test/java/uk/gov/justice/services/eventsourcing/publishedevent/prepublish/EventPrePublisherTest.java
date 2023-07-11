package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.publishedevent.jdbc.PublishedEventQueries;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.PublishedEventException;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.messaging.Metadata;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventPrePublisherTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Mock
    private PrePublishRepository prePublishRepository;

    @Mock
    private PublishedEventQueries publishedEventQueries;

    @Mock
    private UtcClock clock;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PublishedEventFactory publishedEventFactory;

    @InjectMocks
    private EventPrePublisher eventPrePublisher;

    @Test
    public void shouldAddSequenceNumberIntoTheEventMetadataAndSetItForPublishing() throws Exception {

        final UUID eventId = randomUUID();
        final Metadata originalMetadata = mock(Metadata.class);
        final Metadata updatedMetadata = mock(Metadata.class);
        final JsonObject metadataJsonObject = mock(JsonObject.class);
        final String updatedMetadataString = "updated metadata";

        final long eventNumber = 982L;
        final long previousEventNumber = 981L;

        final ZonedDateTime now = new UtcClock().now();

        final Event event = mock(Event.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final DataSource dataSource = mock(DataSource.class);

        when(event.getId()).thenReturn(eventId);
        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(prePublishRepository.getEventNumber(eventId, dataSource)).thenReturn(eventNumber);
        when(prePublishRepository.getPreviousEventNumber(eventNumber, dataSource)).thenReturn(previousEventNumber);
        when(clock.now()).thenReturn(now);
        when(eventConverter.metadataOf(event)).thenReturn(originalMetadata);

        when(metadataEventNumberUpdater.updateMetadataJson(
                originalMetadata,
                previousEventNumber,
                eventNumber)).thenReturn(updatedMetadata);
        when(publishedEventFactory.create(event, updatedMetadata, eventNumber, previousEventNumber)).thenReturn(publishedEvent);

        eventPrePublisher.prePublish(event);

        final InOrder inOrder = inOrder(publishedEventQueries, prePublishRepository);
        inOrder.verify(publishedEventQueries).insertPublishedEvent(publishedEvent, dataSource);
        inOrder.verify(prePublishRepository).addToPublishQueueTable(eventId, now, dataSource);
    }

    @Test
    public void shouldThrowAPublishQueueExceptionIfAnSQLExceptionIsThrown() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID eventId = fromString("5dd46779-07a6-4772-b5e8-e9d280708269");

        final Event event = mock(Event.class);
        final DataSource dataSource = mock(DataSource.class);

        when(event.getId()).thenReturn(eventId);
        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(dataSource);
        when(prePublishRepository.getEventNumber(eventId, dataSource)).thenThrow(sqlException);

        try {
            eventPrePublisher.prePublish(event);
            fail();
        } catch (final PublishedEventException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to insert event_number into metadata in event_log table for event id 5dd46779-07a6-4772-b5e8-e9d280708269"));
        }
    }
}
