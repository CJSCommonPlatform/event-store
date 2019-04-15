package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinder;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventFinderFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JdbcEventSourceFactoryTest {

    @Mock
    private EventStreamManagerFactory eventStreamManagerFactory;

    @Mock
    private EventRepositoryFactory eventRepositoryFactory;

    @Mock
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Mock
    private PublishedEventFinderFactory publishedEventFinderFactory;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private JdbcEventSourceFactory jdbcEventSourceFactory;

    @Test
    public void shouldCreateJdbcBasedEventSource() throws Exception {

        final String jndiDataSourceName = "jndiDatasource";
        final String eventSourceName = "eventSourceName";

        final EventJdbcRepository eventJdbcRepository = mock(EventJdbcRepository.class);
        final EventStreamJdbcRepository eventStreamJdbcRepository = mock(EventStreamJdbcRepository.class);
        final EventRepository eventRepository = mock(EventRepository.class);
        final EventStreamManager eventStreamManager = mock(EventStreamManager.class);
        final DataSource dataSource = mock(DataSource.class);
        final PublishedEventFinder publishedEventFinder = mock(PublishedEventFinder.class);

        when(eventJdbcRepositoryFactory.eventJdbcRepository(dataSource)).thenReturn(eventJdbcRepository);
        when(eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(dataSource)).thenReturn(eventStreamJdbcRepository);
        when(publishedEventFinderFactory.create(dataSource)).thenReturn(publishedEventFinder);

        when(eventRepositoryFactory.eventRepository(
                eventJdbcRepository,
                eventStreamJdbcRepository,
                publishedEventFinder)).thenReturn(eventRepository);

        when(eventStreamManagerFactory.eventStreamManager(eventRepository, eventSourceName)).thenReturn(eventStreamManager);
        when(jdbcDataSourceProvider.getDataSource(jndiDataSourceName)).thenReturn(dataSource);

        final JdbcBasedEventSource jdbcBasedEventSource = jdbcEventSourceFactory.create(dataSource, eventSourceName);

        assertThat(getValueOfField(jdbcBasedEventSource, "eventStreamManager", EventStreamManager.class), is(eventStreamManager));
        assertThat(getValueOfField(jdbcBasedEventSource, "eventRepository", EventRepository.class), is(eventRepository));
        assertThat(getValueOfField(jdbcBasedEventSource, "eventSourceName", String.class), is(eventSourceName));
    }
}
