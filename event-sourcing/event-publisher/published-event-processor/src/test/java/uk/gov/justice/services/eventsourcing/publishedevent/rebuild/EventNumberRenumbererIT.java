package uk.gov.justice.services.eventsourcing.publishedevent.rebuild;

import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.events.EventBuilder.eventBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;
import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.test.utils.events.EventStoreDataAccess;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.SequenceSetter;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventNumberRenumbererIT {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private Logger logger;

    @InjectMocks
    private EventNumberRenumberer eventNumberRenumberer;

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    private final EventStoreDataAccess eventStoreDataAccess = new EventStoreDataAccess(eventStoreDataSource);
    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();
    private final UtcClock clock = new UtcClock();

    @Before

    public void initialize() throws Exception {

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        databaseCleaner.cleanEventStoreTables("framework");
    }

    @Test
    public void shouldResetEventSequenceAndRenumberTheEvents() throws Exception {

        final SequenceSetter sequenceSetter = new SequenceSetter();

        sequenceSetter.setSequenceTo(11L, "event_sequence_seq", eventStoreDataSource);
        assertThat(sequenceSetter.getCurrentSequenceValue("event_sequence_seq", eventStoreDataSource), is(11L));


        eventStoreDataAccess.insertIntoEventLog(eventBuilder().withName("event 1").withTimestamp(clock.now()).build());
        eventStoreDataAccess.insertIntoEventLog(eventBuilder().withName("event 2").withTimestamp(clock.now()).build());
        eventStoreDataAccess.insertIntoEventLog(eventBuilder().withName("event 3").withTimestamp(clock.now()).build());
        eventStoreDataAccess.insertIntoEventLog(eventBuilder().withName("event 4").withTimestamp(clock.now()).build());
        eventStoreDataAccess.insertIntoEventLog(eventBuilder().withName("event 5").withTimestamp(clock.now()).build());

        final List<Event> allEvents = eventStoreDataAccess.findAllEvents();

        assertThat(allEvents.size(), is(5));

        assertThat(allEvents.get(0).getName(), is("event 1"));
        assertThat(allEvents.get(0).getEventNumber(), is(of(11L)));
        assertThat(allEvents.get(1).getName(), is("event 2"));
        assertThat(allEvents.get(1).getEventNumber(), is(of(12L)));
        assertThat(allEvents.get(2).getName(), is("event 3"));
        assertThat(allEvents.get(2).getEventNumber(), is(of(13L)));
        assertThat(allEvents.get(3).getName(), is("event 4"));
        assertThat(allEvents.get(3).getEventNumber(), is(of(14L)));
        assertThat(allEvents.get(4).getName(), is("event 5"));
        assertThat(allEvents.get(4).getEventNumber(), is(of(15L)));

        eventNumberRenumberer.renumberEventLogEventNumber();

        final List<Event> renumberedEvents = eventStoreDataAccess.findAllEvents();

        assertThat(renumberedEvents.get(0).getName(), is("event 1"));
        assertThat(renumberedEvents.get(0).getEventNumber(), is(of(1L)));
        assertThat(renumberedEvents.get(1).getName(), is("event 2"));
        assertThat(renumberedEvents.get(1).getEventNumber(), is(of(2L)));
        assertThat(renumberedEvents.get(2).getName(), is("event 3"));
        assertThat(renumberedEvents.get(2).getEventNumber(), is(of(3L)));
        assertThat(renumberedEvents.get(3).getName(), is("event 4"));
        assertThat(renumberedEvents.get(3).getEventNumber(), is(of(4L)));
        assertThat(renumberedEvents.get(4).getName(), is("event 5"));
        assertThat(renumberedEvents.get(4).getEventNumber(), is(of(5L)));

        assertThat(sequenceSetter.getCurrentSequenceValue("event_sequence_seq", eventStoreDataSource), is(5L));
    }
}
