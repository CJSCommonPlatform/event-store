package uk.gov.justice.services.event.buffer.core.repository.streambuffer;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class EventBufferJdbcRepositoryIT {

    public static final String EVENT_LISTENER = "event_listener";
    private EventBufferJdbcRepository eventBufferJdbcRepository;

    @Before
    public void initDatabase() throws Exception {
        final DataSource dataSource = new FrameworkTestDataSourceFactory().createViewStoreDataSource();
        eventBufferJdbcRepository = new EventBufferJdbcRepository(
                new JdbcResultSetStreamer(),
                new PreparedStatementWrapperFactory(),
                dataSource,
                mock(Logger.class));

        new DatabaseCleaner().cleanViewStoreTables("framework", "stream_buffer", "stream_status");
    }

    @Test
    public void shouldInsertAndReturnStreamOfEvents() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();
        final String source = "source";

        final String component = EVENT_LISTENER;
        eventBufferJdbcRepository.insert(new EventBufferEvent(id1, 2L, "eventVersion_2", source, component));
        eventBufferJdbcRepository.insert(new EventBufferEvent(id1, 1L, "eventVersion_1", source, component));
        eventBufferJdbcRepository.insert(new EventBufferEvent(id1, 3L, "eventVersion_3", source, component));
        eventBufferJdbcRepository.insert(new EventBufferEvent(id2, 1L, "eventVersion_1", source, component));

        final List<EventBufferEvent> events = eventBufferJdbcRepository.findStreamByIdSourceAndComponent(id1, source, component)
                .collect(toList());

        assertThat(events, hasSize(3));

        assertThat(events.get(0).getStreamId(), is(id1));
        assertThat(events.get(0).getPosition(), is(1L));
        assertThat(events.get(0).getEvent(), is("eventVersion_1"));
        assertThat(events.get(0).getSource(), is(source));

        assertThat(events.get(1).getStreamId(), is(id1));
        assertThat(events.get(1).getPosition(), is(2L));
        assertThat(events.get(1).getEvent(), is("eventVersion_2"));
        assertThat(events.get(1).getSource(), is(source));

        assertThat(events.get(2).getStreamId(), is(id1));
        assertThat(events.get(2).getPosition(), is(3L));
        assertThat(events.get(2).getEvent(), is("eventVersion_3"));
        assertThat(events.get(2).getSource(), is(source));
    }

    @Test
    public void shouldNotReturnEventsIfTheyHaveADifferentSource() {
        final UUID id1 = randomUUID();
        final UUID id2 = randomUUID();
        final String source = "source";
        final String component = EVENT_LISTENER;

        eventBufferJdbcRepository.insert(new EventBufferEvent(id1, 2L, "eventVersion_2", "a-different-source", component));
        eventBufferJdbcRepository.insert(new EventBufferEvent(id1, 1L, "eventVersion_1", source, component));
        eventBufferJdbcRepository.insert(new EventBufferEvent(id1, 3L, "eventVersion_3", source, component));
        eventBufferJdbcRepository.insert(new EventBufferEvent(id2, 1L, "eventVersion_1", source, component));

        final List<EventBufferEvent> events = eventBufferJdbcRepository.findStreamByIdSourceAndComponent(id1, source, component)
                .collect(toList());

        assertThat(events, hasSize(2));

        assertThat(events.get(0).getStreamId(), is(id1));
        assertThat(events.get(0).getPosition(), is(1L));
        assertThat(events.get(0).getEvent(), is("eventVersion_1"));
        assertThat(events.get(0).getSource(), is(source));

        assertThat(events.get(1).getStreamId(), is(id1));
        assertThat(events.get(1).getPosition(), is(3L));
        assertThat(events.get(1).getEvent(), is("eventVersion_3"));
        assertThat(events.get(1).getSource(), is(source));
    }

    @Test
    public void shouldRemoveFromBuffer() {
        final UUID id1 = randomUUID();
        final String source = "someOtherSource";
        final String component = EVENT_LISTENER;
        final EventBufferEvent eventBufferEvent = new EventBufferEvent(id1, 2L, "someOtherEvent", source, component);

        eventBufferJdbcRepository.insert(eventBufferEvent);

        assertThat(eventBufferJdbcRepository.findStreamByIdSourceAndComponent(id1, source, component).collect(toList()), hasItem(eventBufferEvent));

        eventBufferJdbcRepository.remove(eventBufferEvent);

        assertThat(eventBufferJdbcRepository.findStreamByIdSourceAndComponent(id1, source, component).collect(toList()), empty());
    }
}
