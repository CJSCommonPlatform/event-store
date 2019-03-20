package uk.gov.justice.services.eventsourcing.linkedevent;

import static java.util.UUID.randomUUID;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.prepublish.LinkedEventFactory;
import uk.gov.justice.services.eventsourcing.prepublish.MetadataEventNumberUpdater;
import uk.gov.justice.services.eventsourcing.prepublish.PrePublishRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinkedEventProcessorTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Mock
    private MetadataEventNumberUpdater metadataEventNumberUpdater;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PrePublishRepository prePublishRepository;

    @Mock
    private LinkedEventFactory linkedEventFactory;

    @Mock
    private LinkedEventJdbcRepository linkedEventJdbcRepository;

    @InjectMocks
    private LinkedEventProcessor linkedEventProcessor;

    @Test
    public void shouldCreateLinkedEvent() throws Exception {
        final Connection connection = mock(Connection.class);
        final Event event = mock(Event.class);
        final Metadata metadata = mock(Metadata.class);

        final UUID eventId = randomUUID();
        final Long eventNumber = 10l;
        final Long previousEventNumber = 9l;
        when(eventConverter.metadataOf(event)).thenReturn(metadata);
        when(event.getId()).thenReturn(eventId);
        when(event.getEventNumber()).thenReturn(Optional.of(eventNumber));
        when(prePublishRepository.getEventNumber(eventId, connection)).thenReturn(eventNumber);
        when(prePublishRepository.getPreviousEventNumber(eventNumber, connection)).thenReturn(previousEventNumber);
        when(metadataEventNumberUpdater.updateMetadataJson(metadata, previousEventNumber, eventNumber)).thenReturn(metadata);
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);

        linkedEventProcessor.createLinkedEvent(event);
        verify(linkedEventFactory).create(event, metadata, eventNumber, previousEventNumber);
    }
}
