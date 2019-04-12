package uk.gov.justice.services.eventsourcing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import uk.gov.justice.services.eventsourcing.publishing.helpers.EventFactory;
import uk.gov.justice.services.eventsourcing.publishing.helpers.TestEventInserter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
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
    public void shouldFetchPublishedEventById() throws Exception {

        final PublishedEvent publishedEvent = eventFactory.createPublishedEvent(randomUUID(),"example.published-event", 1L, 1L, 0L);

        testEventInserter.insertIntoPublishedEvent(publishedEvent);

        try (final Connection connection = dataSource.getConnection()) {
            final Optional<PublishedEvent> publishedEventOptional = eventFetcherRepository.getPublishedEvent(publishedEvent.getId(), connection);

            if (publishedEventOptional.isPresent()) {
                assertThat(publishedEventOptional.get(), is(publishedEvent));
            } else {
                fail();
            }
        }
    }

    @Test
    public void shouldReturnEmptyIfNoPublishedEventFound() throws Exception {

        final UUID unknownId = randomUUID();

        try (final Connection connection = dataSource.getConnection()) {
            assertThat(eventFetcherRepository.getPublishedEvent(unknownId, connection).isPresent(), is(false));
        }
    }
}
