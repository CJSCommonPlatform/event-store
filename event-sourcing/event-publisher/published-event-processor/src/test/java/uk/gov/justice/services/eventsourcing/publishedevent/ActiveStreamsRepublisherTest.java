package uk.gov.justice.services.eventsourcing.publishedevent;

import static java.time.ZonedDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStream;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepositoryFactory;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.DatasourceException;
import uk.gov.justice.services.jdbc.persistence.JdbcDataSourceProvider;
import uk.gov.justice.subscription.domain.eventsource.DefaultEventSourceDefinitionFactory;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveStreamsRepublisherTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    private DefaultEventSourceDefinitionFactory defaultEventSourceDefinitionFactory;

    @Mock
    private EventStreamJdbcRepositoryFactory eventStreamJdbcRepositoryFactory;

    @Mock
    private EventJdbcRepositoryFactory eventJdbcRepositoryFactory;

    @Mock
    private EventStreamJdbcRepository eventStreamJdbcRepository;

    @Mock
    private EventJdbcRepository eventJdbcRepository;

    @Mock
    private PublishedEventsProcessor publishedEventsProcessors;

    @Mock
    private JdbcDataSourceProvider jdbcDataSourceProvider;

    @InjectMocks
    private ActiveStreamsRepublisher activeStreamsRepublisher;

    @Test
    public void shouldProcessActiveStreams() throws SQLException, DatasourceException {

        final String defaultDataSourceJndiName = "defaultDataSourceJndiName";

        final UUID streamId1 = randomUUID();
        final UUID streamId2 = randomUUID();
        final UUID streamId3 = randomUUID();

        final Stream<EventStream> eventStreams = Stream.of(
                new EventStream(streamId1, 1L, true, now()),
                new EventStream(streamId2, 1L, true, now()),
                new EventStream(streamId3, 1L, true, now()));

        final DataSource defaultDataSource = mock(DataSource.class);

        when(defaultEventSourceDefinitionFactory.createDefaultEventSource().getLocation().getDataSource()).thenReturn(of(defaultDataSourceJndiName));
        when(jdbcDataSourceProvider.getDataSource(defaultDataSourceJndiName)).thenReturn(defaultDataSource);

        when(eventJdbcRepositoryFactory.eventJdbcRepository(defaultDataSource)).thenReturn(eventJdbcRepository);
        when(eventStreamJdbcRepositoryFactory.eventStreamJdbcRepository(defaultDataSource)).thenReturn(eventStreamJdbcRepository);

        when(eventStreamJdbcRepository.findActive()).thenReturn(eventStreams);

        activeStreamsRepublisher.populatePublishedEvents();

        verify(publishedEventsProcessors).populatePublishedEvents(streamId1, eventJdbcRepository);
        verify(publishedEventsProcessors).populatePublishedEvents(streamId2, eventJdbcRepository);
        verify(publishedEventsProcessors).populatePublishedEvents(streamId3, eventJdbcRepository);
    }

    @Test
    public void shouldThrowExceptionIfNoDefaultDataSourceNameFound() throws Exception {

        when(defaultEventSourceDefinitionFactory.createDefaultEventSource().getLocation().getDataSource()).thenReturn(empty());

        try {
            activeStreamsRepublisher.populatePublishedEvents();
            fail();
        } catch (final MissingDataSourceNameException expected) {
            assertThat(expected.getMessage(), is("Unable to create DataSource. Default DataSource name not found in event source definition."));
        }
    }
}
