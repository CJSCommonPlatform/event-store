package uk.gov.justice.services.test.utils.events;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;
import static uk.gov.justice.services.test.utils.events.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEvent;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.TableCleaner;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventInserterIT {

    @Spy
    private DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

    @InjectMocks
    private TestEventInserter testEventInserter;
    
    @Test
    public void shouldCreateAnEntryInTheEventLogTableUsingAnEvent() throws Exception {

        new TableCleaner().clean("event_log", eventStoreDataSource);

        assertThat(testEventInserter.findAllEvents(), is(emptyList()));

        final Event event_1 = eventBuilder().withName("event 1").build();
        final Event event_2 = eventBuilder().withName("event 1").build();
        final Event event_3 = eventBuilder().withName("event 1").build();

        testEventInserter.insertIntoEventLog(event_1);
        testEventInserter.insertIntoEventLog(event_2);
        testEventInserter.insertIntoEventLog(event_3);

        final List<Event> events = testEventInserter.findAllEvents();

        assertThat(events.size(), is(3));

        assertThat(events.get(0), is(event_1));
        assertThat(events.get(1), is(event_2));
        assertThat(events.get(2), is(event_3));
    }

    @Test
    public void shouldInsertIntoEventLogTableUsingParameters() throws Exception {

        new TableCleaner().clean("event_log", eventStoreDataSource);

        assertThat(testEventInserter.findAllEvents(), is(emptyList()));

        final Event event_1 = eventBuilder().withName("event 1").build();
        final Event event_2 = eventBuilder().withName("event 1").build();
        final Event event_3 = eventBuilder().withName("event 1").build();

        testEventInserter.insertIntoEventLog(
                event_1.getId(),
                event_1.getStreamId(),
                event_1.getSequenceId(),
                event_1.getCreatedAt(),
                event_1.getName(),
                event_1.getPayload(),
                event_1.getMetadata()
        );
        testEventInserter.insertIntoEventLog(
                event_2.getId(),
                event_2.getStreamId(),
                event_2.getSequenceId(),
                event_2.getCreatedAt(),
                event_2.getName(),
                event_2.getPayload(),
                event_2.getMetadata()
        );
        testEventInserter.insertIntoEventLog(
                event_3.getId(),
                event_3.getStreamId(),
                event_3.getSequenceId(),
                event_3.getCreatedAt(),
                event_3.getName(),
                event_3.getPayload(),
                event_3.getMetadata()
        );


        final List<Event> events = testEventInserter.findAllEvents();

        assertThat(events.size(), is(3));

        assertThat(events.get(0), is(event_1));
        assertThat(events.get(1), is(event_2));
        assertThat(events.get(2), is(event_3));
    }

    @Test
    public void shouldInsertAndGetPublishedEvents() throws Exception {

        new TableCleaner().clean("published_event", eventStoreDataSource);

        assertThat(testEventInserter.findAllPublishedEvents(), is(emptyList()));

        final PublishedEvent publishedEvent_1 = publishedEventBuilder()
                .withName("published event 1")
                .withPreviousEventNumber(0)
                .withEventNumber(1)
                .build();
        final PublishedEvent publishedEvent_2 = publishedEventBuilder()
                .withName("published event 2")
                .withPreviousEventNumber(1)
                .withEventNumber(2)
                .build();
        final PublishedEvent publishedEvent_3 = publishedEventBuilder()
                .withName("published event 3")
                .withPreviousEventNumber(2)
                .withEventNumber(3)
                .build();

        testEventInserter.insertIntoPublishedEvent(publishedEvent_1);
        testEventInserter.insertIntoPublishedEvent(publishedEvent_2);
        testEventInserter.insertIntoPublishedEvent(publishedEvent_3);

        final List<PublishedEvent> publishedEvents = testEventInserter.findAllPublishedEvents();

        assertThat(publishedEvents.size(), is(3));
        assertThat(publishedEvents.get(0), is(publishedEvent_1));
        assertThat(publishedEvents.get(1), is(publishedEvent_2));
        assertThat(publishedEvents.get(2), is(publishedEvent_3));
    }
}
