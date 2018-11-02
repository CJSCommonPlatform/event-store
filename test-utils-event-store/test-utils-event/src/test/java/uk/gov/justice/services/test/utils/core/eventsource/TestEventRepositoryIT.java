package uk.gov.justice.services.test.utils.core.eventsource;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

public class TestEventRepositoryIT {

    @Test
    public void shouldGetAConnectionToTheEventStore() throws Exception {

        final TestEventRepository testEventRepository = TestEventRepository.forContext("framework");

        try (final Connection connection = testEventRepository.getDataSource().getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from event_log")) {
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    assertThat(resultSet, is(notNullValue()));
                }
            }
        }
    }

    @Test
    public void shouldBeInstantiatedUsingADatasource() throws Exception {

        final String url = "jdbc:postgresql://localhost/frameworkeventstore";
        final String driverName = "org.postgresql.Driver";
        final String username = "framework";
        final String password = "framework";

        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        final TestEventRepository testEventRepository = new TestEventRepository(dataSource);

        assertThat(testEventRepository.getDataSource(), is(sameInstance(dataSource)));
    }

    @Test
    public void shouldInsertAndFindEvents() throws Exception {

        cleanDatabase();

        final UUID steamId_1 = randomUUID();
        final UUID steamId_2 = randomUUID();
        final UUID steamId_3 = randomUUID();

        final Event event_1 = anEvent(1L, steamId_1);
        final Event event_2 = anEvent(2L, steamId_2);
        final Event event_3 = anEvent(3L, steamId_3);

        final TestEventRepository testEventRepository = TestEventRepository.forContext("framework");

        testEventRepository.insert(event_1);
        testEventRepository.insert(event_2);
        testEventRepository.insert(event_3);

        final List<Event> events = testEventRepository.allEvents();

        assertThat(events.size(), is(3));

        assertThat(events, hasItem(event_1));
        assertThat(events, hasItem(event_2));
        assertThat(events, hasItem(event_3));
    }

    @Test
    public void shouldFindEventsByStreamId() throws Exception {

        cleanDatabase();

        final UUID steamId_1 = randomUUID();
        final UUID steamId_2 = randomUUID();

        final Event event_1 = anEvent(1L, steamId_1);
        final Event event_2 = anEvent(2L, steamId_2);
        final Event event_3 = anEvent(3L, steamId_2);

        final TestEventRepository testEventRepository = TestEventRepository.forContext("framework");

        testEventRepository.insert(event_1);
        testEventRepository.insert(event_2);
        testEventRepository.insert(event_3);

        final List<Event> events = testEventRepository.eventsOfStreamId(steamId_2);

        assertThat(events.size(), is(2));

        assertThat(events, hasItem(event_2));
        assertThat(events, hasItem(event_3));
    }

    private Event anEvent(final Long sequenceId, final UUID steamId) {
        return new Event(
                randomUUID(),
                steamId,
                sequenceId,
                "event.name_" + sequenceId,
                "metadata " + sequenceId,
                "payload " + sequenceId,
                new UtcClock().now()
        );
    }

    private void cleanDatabase() throws Exception {
        final TestEventRepository testEventRepository = TestEventRepository.forContext("framework");

        try(final Connection connection = testEventRepository.getDataSource().getConnection()) {
            try(final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM event_log")) {
                preparedStatement.executeUpdate();
            }
        }
    }
}
