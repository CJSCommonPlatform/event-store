package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PublishedEventFinderIT {

    private PublishedEventInserter publishedEventInserter = new PublishedEventInserter();
    private DataSource dataSource;

    private PublishedEventFinder publishedEventFinder;

    @Before
    public void initialize() throws Exception {
        dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
        new DatabaseCleaner().cleanEventStoreTables("framework");

        final JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();
        final PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();
        publishedEventFinder = new PublishedEventFinder(
                jdbcResultSetStreamer,
                preparedStatementWrapperFactory,
                dataSource);
    }

    @After
    public void after() throws SQLException {
        dataSource.getConnection().close();
    }

    @Test
    public void shouldGetEventsSinceEventNumber() throws Exception {

        final PublishedEvent event_1 = publishedEventBuilder().withPreviousEventNumber(0).withEventNumber(1).build();
        final PublishedEvent event_2 = publishedEventBuilder().withPreviousEventNumber(1).withEventNumber(2).build();
        final PublishedEvent event_3 = publishedEventBuilder().withPreviousEventNumber(2).withEventNumber(3).build();
        final PublishedEvent event_4 = publishedEventBuilder().withPreviousEventNumber(3).withEventNumber(4).build();
        final PublishedEvent event_5 = publishedEventBuilder().withPreviousEventNumber(4).withEventNumber(5).build();

        final Connection connection = dataSource.getConnection();

        publishedEventInserter.insertPublishedEvent(event_1, connection);
        publishedEventInserter.insertPublishedEvent(event_2, connection);
        publishedEventInserter.insertPublishedEvent(event_3, connection);
        publishedEventInserter.insertPublishedEvent(event_4, connection);
        publishedEventInserter.insertPublishedEvent(event_5, connection);


        final List<PublishedEvent> publishedEvents = publishedEventFinder.findEventsSince(3)
                .collect(toList());

        assertThat(publishedEvents.size(), is(2));

        assertThat(publishedEvents.get(0).getId(), is(event_4.getId()));
        assertThat(publishedEvents.get(1).getId(), is(event_5.getId()));
    }
}
