package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.prepublish.PublishedEventFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventJdbcRepository;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventProcessorTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Mock
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PrePublishRepository prePublishRepository;

    @Mock
    private PublishedEventFactory publishedEventFactory;

    @Mock
    private PublishedEventJdbcRepository publishedEventJdbcRepository;

    @InjectMocks
    private PublishedEventProcessor publishedEventProcessor;

    @Test
    public void shouldCreatePublishedEvent() throws Exception {
        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final Metadata metadata = mock(Metadata.class, "metadata");
        final Metadata updatedMetadata = mock(Metadata.class, "updatedMetadata");

        final UUID eventId = randomUUID();
        final long eventNumber = 10l;
        final Long previousEventNumber = 9l;
        when(eventConverter.metadataOf(event)).thenReturn(metadata);
        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(Optional.of(eventNumber));
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);
        when(prePublishRepository.getPreviousEventNumber(eventNumber, connection)).thenReturn(previousEventNumber);
        when(metadataEventNumberUpdater.updateMetadataJson(metadata, previousEventNumber, eventNumber)).thenReturn(updatedMetadata);
        when(publishedEventFactory.create(event, updatedMetadata, eventNumber, previousEventNumber)).thenReturn(publishedEvent);

        publishedEventProcessor.createPublishedEvent(event);

        verify(publishedEventJdbcRepository).insertPublishedEvent(publishedEvent, connection);
    }

    @Test
    public void shouldThrowExceptionIfGettingPreviousEventNumberFails() throws Exception {

        final SQLException sqlException = new SQLException("(Ooops");
        final UUID eventId = fromString("df7958f4-ce50-45a1-a8ee-501d55475e69");

        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        final Metadata metadata = mock(Metadata.class, "metadata");

        final long eventNumber = 10l;
        when(eventConverter.metadataOf(event)).thenReturn(metadata);
        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(Optional.of(eventNumber));
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);
        when(prePublishRepository.getPreviousEventNumber(eventNumber, connection)).thenThrow(sqlException);

        try {
            publishedEventProcessor.createPublishedEvent(event);
            fail();
        } catch (final PublishedEventSQLException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Unable to get previous event number for event with id 'df7958f4-ce50-45a1-a8ee-501d55475e69'"));
        }

        verifyZeroInteractions(publishedEventJdbcRepository);
    }

    @Test
    public void shouldThrowExceptionIfInsertingPublishedEventFails() throws Exception {
        final SQLException sqlException = new SQLException("(Ooops");
        final UUID eventId = fromString("df7958f4-ce50-45a1-a8ee-501d55475e69");

        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        final PublishedEvent publishedEvent = mock(PublishedEvent.class);
        final Metadata metadata = mock(Metadata.class, "metadata");
        final Metadata updatedMetadata = mock(Metadata.class, "updatedMetadata");

        final long previousEventNumber = 9l;
        final long eventNumber = 10l;
        when(eventConverter.metadataOf(event)).thenReturn(metadata);
        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(Optional.of(eventNumber));
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);
        when(prePublishRepository.getPreviousEventNumber(eventNumber, connection)).thenReturn(previousEventNumber);
        when(metadataEventNumberUpdater.updateMetadataJson(metadata, previousEventNumber, eventNumber)).thenReturn(updatedMetadata);
        when(publishedEventFactory.create(event, updatedMetadata, eventNumber, previousEventNumber)).thenReturn(publishedEvent);

        doThrow(sqlException).when(publishedEventJdbcRepository).insertPublishedEvent(publishedEvent, connection);

        try {
            publishedEventProcessor.createPublishedEvent(event);
            fail();
        } catch (final PublishedEventSQLException expected) {
            assertThat(expected.getCause(), is(sqlException));
            assertThat(expected.getMessage(), is("Unable to insert PublishedEvent with id 'df7958f4-ce50-45a1-a8ee-501d55475e69'"));
        }
    }
}
