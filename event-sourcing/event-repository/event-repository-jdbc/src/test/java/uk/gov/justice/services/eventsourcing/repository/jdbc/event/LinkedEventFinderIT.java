package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.eventsourcing.repository.jdbc.event.LinkedEventBuilder.linkedEventBuilder;

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
public class LinkedEventFinderIT {

    private LinkedEventJdbcRepository linkedEventJdbcRepository = new LinkedEventJdbcRepository();
    private DataSource dataSource;

    @Spy
    @SuppressWarnings("unused")
    private JdbcRepositoryHelper jdbcRepositoryHelper = new JdbcRepositoryHelper();

    @InjectMocks
    private LinkedEventFinder linkedEventFinder;

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

        final LinkedEvent event_1 = linkedEventBuilder().withPreviousEventNumber(0).withEventNumber(1).build();
        final LinkedEvent event_2 = linkedEventBuilder().withPreviousEventNumber(1).withEventNumber(2).build();
        final LinkedEvent event_3 = linkedEventBuilder().withPreviousEventNumber(2).withEventNumber(3).build();
        final LinkedEvent event_4 = linkedEventBuilder().withPreviousEventNumber(3).withEventNumber(4).build();
        final LinkedEvent event_5 = linkedEventBuilder().withPreviousEventNumber(4).withEventNumber(5).build();

        final Connection connection = dataSource.getConnection();

        linkedEventJdbcRepository.insertLinkedEvent(event_1, connection);
        linkedEventJdbcRepository.insertLinkedEvent(event_2, connection);
        linkedEventJdbcRepository.insertLinkedEvent(event_3, connection);
        linkedEventJdbcRepository.insertLinkedEvent(event_4, connection);
        linkedEventJdbcRepository.insertLinkedEvent(event_5, connection);


        final List<LinkedEvent> linkedEvents = linkedEventFinder.findEventsSince(3, dataSource)
                .collect(toList());

        assertThat(linkedEvents.size(), is(2));

        assertThat(linkedEvents.get(0).getId(), is(event_4.getId()));
        assertThat(linkedEvents.get(1).getId(), is(event_5.getId()));
    }
}
