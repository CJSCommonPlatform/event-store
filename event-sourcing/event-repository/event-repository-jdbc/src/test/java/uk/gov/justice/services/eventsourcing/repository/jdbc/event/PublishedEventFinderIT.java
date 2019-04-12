package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.PublishedEventBuilder.publishedEventBuilder;

import uk.gov.justice.services.jdbc.persistence.JdbcRepositoryHelper;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.TestEventStoreDataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventFinderIT {

    private PublishedEventJdbcRepository publishedEventJdbcRepository = new PublishedEventJdbcRepository();
    private DataSource dataSource;

    @Spy
    @SuppressWarnings("unused")
    private JdbcRepositoryHelper jdbcRepositoryHelper = new JdbcRepositoryHelper();

    @InjectMocks
    private PublishedEventFinder publishedEventFinder;

    @Before
    public void initialize() throws Exception {
        dataSource = new TestEventStoreDataSourceFactory().createDataSource("frameworkeventstore");
        new DatabaseCleaner().cleanEventStoreTables("framework");
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

        publishedEventJdbcRepository.insertPublishedEvent(event_1, connection);
        publishedEventJdbcRepository.insertPublishedEvent(event_2, connection);
        publishedEventJdbcRepository.insertPublishedEvent(event_3, connection);
        publishedEventJdbcRepository.insertPublishedEvent(event_4, connection);
        publishedEventJdbcRepository.insertPublishedEvent(event_5, connection);


        final List<PublishedEvent> publishedEvents = publishedEventFinder.findEventsSince(3, dataSource)
                .collect(toList());

        assertThat(publishedEvents.size(), is(2));

        assertThat(publishedEvents.get(0).getId(), is(event_4.getId()));
        assertThat(publishedEvents.get(1).getId(), is(event_5.getId()));
    }
}
