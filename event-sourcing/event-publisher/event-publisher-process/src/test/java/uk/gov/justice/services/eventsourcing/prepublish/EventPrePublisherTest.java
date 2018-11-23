package uk.gov.justice.services.eventsourcing.prepublish;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.PublishQueueException;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventPrePublisherTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Mock
    private MetadataSequenceNumberUpdater metadataSequenceNumberUpdater;

    @Mock
    private PrePublishRepository prePublishRepository;

    @Mock
    private UtcClock clock;

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private EventPrePublisher eventPrePublisher;

    @Test
    public void shouldAddSequenceNumberIntoTheEventMetadataAndSetItForPublishing() throws Exception {

        final UUID eventId = randomUUID();
        final Metadata originalMetadata = mock(Metadata.class);
        final Metadata updatedMetadata = mock(Metadata.class);
        final JsonObject metadataJsonObject = mock(JsonObject.class);
        final String updatedMetadataString = "updated metadata";

        final long sequenceNumber = 982L;
        final long previousSequenceNumber = 981L;

        final ZonedDateTime now = new UtcClock().now();

        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);

        when(event.getId()).thenReturn(eventId);
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);
        when(prePublishRepository.getSequenceNumber(eventId, connection)).thenReturn(sequenceNumber);
        when(prePublishRepository.getPreviousSequenceNumber(sequenceNumber, connection)).thenReturn(previousSequenceNumber);
        when(clock.now()).thenReturn(now);
        when(eventConverter.metadataOf(event)).thenReturn(originalMetadata);

        when(metadataSequenceNumberUpdater.updateMetadataJson(
                originalMetadata,
                previousSequenceNumber,
                sequenceNumber)).thenReturn(updatedMetadata);

        when(updatedMetadata.asJsonObject()).thenReturn(metadataJsonObject);
        when(metadataJsonObject.toString()).thenReturn(updatedMetadataString);

        eventPrePublisher.prePublish(event);

        final InOrder inOrder = inOrder(prePublishRepository);
        inOrder.verify(prePublishRepository).updateMetadata(eventId, updatedMetadataString, connection);
        inOrder.verify(prePublishRepository).addToPublishQueueTable(eventId, now, connection);
    }

    @Test
    public void shouldThrowAPublishQueueExceptionIfAnSQLExceptionIsThrown() throws Exception {

        final SQLException sqlException = new SQLException("Ooops");

        final UUID eventId = fromString("5dd46779-07a6-4772-b5e8-e9d280708269");

        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);

        when(event.getId()).thenReturn(eventId);
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);
        when(prePublishRepository.getSequenceNumber(eventId, connection)).thenThrow(sqlException);

        try {
            eventPrePublisher.prePublish(event);
            fail();
        } catch (final PublishQueueException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Failed to insert event_number into metadata in event_log table for event id 5dd46779-07a6-4772-b5e8-e9d280708269"));
        }

    }
}
