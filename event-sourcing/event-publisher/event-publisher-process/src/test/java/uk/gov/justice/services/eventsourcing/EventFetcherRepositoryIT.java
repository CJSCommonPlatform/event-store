package uk.gov.justice.services.eventsourcing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.eventsourcing.publishing.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEvent;
import uk.gov.justice.services.test.utils.core.eventsource.EventStoreInitializer;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

public class EventFetcherRepositoryIT {

    private final DataSource dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final EventFactory eventFactory = new EventFactory();
    private final TestEventInserter testEventInserter = new TestEventInserter();

    private final EventFetcherRepository eventFetcherRepository = new EventFetcherRepository();

    @Before
    public void initDatabase() throws Exception {
        new EventStoreInitializer().initializeEventStore(dataSource);
    }

    @Test
    public void shouldFetchAnEventById() throws Exception {

        final Event event = eventFactory.createEvent("example.first-event", 1L);

        testEventInserter.insertIntoEventLog(event);

        try (final Connection connection = dataSource.getConnection()) {
            final Optional<Event> eventOptional = eventFetcherRepository.getEvent(event.getId(), connection);

            if (eventOptional.isPresent()) {
                assertThat(eventOptional.get(), is(event));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldReturnEmptyIfNoEventFound() throws Exception {

        final UUID unknownId = randomUUID();
        
        try (final Connection connection = dataSource.getConnection()) {
            assertThat(eventFetcherRepository.getEvent(unknownId, connection).isPresent(), is(false));
        }
    }

    @Test
    public void shouldFetchLinkedEventById() throws Exception {

        final LinkedEvent linkedEvent = eventFactory.createLinkedEvent(randomUUID(),"example.linked-event", 1L, 1L, 0L);

        testEventInserter.insertIntoLinkedEvent(linkedEvent);

        try (final Connection connection = dataSource.getConnection()) {
            final Optional<LinkedEvent> linkedEventOptional = eventFetcherRepository.getLinkedEvent(linkedEvent.getId(), connection);

            if (linkedEventOptional.isPresent()) {
                assertThat(linkedEventOptional.get(), is(linkedEvent));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldReturnEmptyIfNoLinkedEventFound() throws Exception {

        final UUID unknownId = randomUUID();

        try (final Connection connection = dataSource.getConnection()) {
            assertThat(eventFetcherRepository.getLinkedEvent(unknownId, connection).isPresent(), is(false));
        }
    }
}
