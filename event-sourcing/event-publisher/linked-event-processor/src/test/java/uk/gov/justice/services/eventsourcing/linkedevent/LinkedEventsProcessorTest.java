package uk.gov.justice.services.eventsourcing.linkedevent;

import static java.util.UUID.randomUUID;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEventJdbcRepository;
import uk.gov.justice.subscription.registry.SubscriptionDataSourceProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LinkedEventsProcessorTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    private SubscriptionDataSourceProvider subscriptionDataSourceProvider;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private LinkedEventProcessor linkedEventProcessor;

    @Mock
    private LinkedEventJdbcRepository linkedEventJdbcRepository;

    @InjectMocks
    private LinkedEventsProcessor linkedEventsProcessor;

    @Test
    public void shouldCreateLinkedEvents() throws SQLException {
        final Connection connection = mock(Connection.class);
        when(subscriptionDataSourceProvider.getEventStoreDataSource().getConnection()).thenReturn(connection);

        final Event event1 = mock(Event.class);
        final Event event2 = mock(Event.class);
        final Event event3 = mock(Event.class);

        final UUID streamID1 = randomUUID();
        final Stream<Event> eventIds = Stream.of(event1, event2, event3);

        when(eventJdbcRepository.findByStreamIdOrderByPositionAsc(streamID1)).thenReturn(eventIds);

        linkedEventsProcessor.populateLinkedEvents(streamID1, eventJdbcRepository);

        verify(linkedEventProcessor).createLinkedEvent(event1);
        verify(linkedEventProcessor).createLinkedEvent(event2);
        verify(linkedEventProcessor).createLinkedEvent(event3);
    }
}
