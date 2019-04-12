package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.SnapshotService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SnapshotAwareEventSourceFactoryTest {
    private static final String EVENT_SOURCE_NAME = "eventSourceName";

    @Mock
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Mock
    private EventRepositoryFactory eventRepositoryFactory;

    @Mock
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Mock
    private SnapshotService snapshotService;

    @Mock
    private EventConverter eventConverter;

    @Mock
    private PublishedEventFinder publishedEventFinder;

    @InjectMocks
    private SnapshotAwareEventSourceFactory snapshotAwareEventSourceFactory;

    @Test
    public void shouldCreateSnapshotAwareEventSource() throws Exception {

        final String jndiDatasource = "jndiDatasource";
        final String eventSourceName = "eventSourceName";

        final EventJdbcRepository eventJdbcRepository = mock(EventJdbcRepository.class);
        final EventStreamJdbcRepository eventStreamJdbcRepository = mock(EventStreamJdbcRepository.class);
        final EventRepository eventRepository = mock(EventRepository.class);
        final EventStreamManager eventStreamManager = mock(EventStreamManager.class);

        when(eventJdbcRepositoryFactory.eventJdbcRepository(jndiDatasource)).thenReturn(eventJdbcRepository);
        when(eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(jndiDatasource)).thenReturn(eventStreamJdbcRepository);

        when(eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                publishedEventFinder)).thenReturn(eventRepository);

        when(eventStreamManagerFactory.eventStreamManager(eventRepository, EVENT_SOURCE_NAME)).thenReturn(eventStreamManager);

        final EventSource eventSource = snapshotAwareEventSourceFactory.create(jndiDatasource, eventSourceName);

        assertThat(eventSource, is(instanceOf(SnapshotAwareEventSource.class)));


        assertThat(getValueOfField(eventSource, "eventStreamManager", EventStreamManager.class), is(eventStreamManager));
        assertThat(getValueOfField(eventSource, "eventRepository", EventRepository.class), is(eventRepository));
        assertThat(getValueOfField(eventSource, "snapshotService", SnapshotService.class), is(snapshotService));
        assertThat(getValueOfField(eventSource, "eventConverter", EventConverter.class), is(eventConverter));
        assertThat(getValueOfField(eventSource, "name", String.class), is(eventSourceName));
    }
}
