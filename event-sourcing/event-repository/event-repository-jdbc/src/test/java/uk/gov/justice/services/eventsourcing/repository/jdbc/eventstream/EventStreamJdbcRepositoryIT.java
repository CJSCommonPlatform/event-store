package uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream;


import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.repository.jdbc.exception.InvalidPositionException;
import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.SettableEventStoreDataSourceProvider;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventStreamJdbcRepositoryIT {

    private static final String FRAMEWORK_CONTEXT_NAME = "framework";

    private DataSource dataSource;

    @SuppressWarnings("unused")
    @Spy
    private JdbcResultSetStreamer jdbcResultSetStreamer = new JdbcResultSetStreamer();

    @SuppressWarnings("unused")
    @Spy
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @SuppressWarnings("unused")
    @Spy
    private SettableEventStoreDataSourceProvider eventStoreDefaultDataSourceProvider = new SettableEventStoreDataSourceProvider();

    @SuppressWarnings("unused")
    @Spy
    private UtcClock clock = new UtcClock();

    @InjectMocks
    private EventStreamJdbcRepository jdbcRepository;

    @BeforeEach
    public void initialize() throws Exception {

        dataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();

        eventStoreDefaultDataSourceProvider.setDataSource(dataSource);

        new DatabaseCleaner().cleanEventStoreTables(FRAMEWORK_CONTEXT_NAME);
    }

    @AfterEach
    public void after() throws SQLException {
        dataSource.getConnection().close();
    }

    @Test
    public void shouldStoreEventStreamUsingInsert() throws InvalidPositionException {
        jdbcRepository.insert(randomUUID());
        jdbcRepository.insert(randomUUID());
        jdbcRepository.insert(randomUUID());

        final Stream<EventStream> streamOfStreams = jdbcRepository.findAll();
        assertThat(streamOfStreams.count(), equalTo(3L));
    }

    @Test
    public void shouldNotThrowExceptionOnDuplicateStreamId() throws InvalidPositionException {
        final UUID streamID = randomUUID();
        jdbcRepository.insert(streamID);
        jdbcRepository.insert(streamID);
    }

    @Test
    public void shouldMarkStreamAsInactive() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId);

        final Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());
        assertTrue(eventStream.get().isActive());

        jdbcRepository.markActive(streamId, false);

        assertFalse(jdbcRepository.findAll().findFirst().get().isActive());
    }

    @Test
    public void shouldFindActiveStreams() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId, false);

        assertThat((int) jdbcRepository.findAll().count(), is(1));
        assertThat((int) jdbcRepository.findActive().count(), is(0));

        jdbcRepository.markActive(streamId, true);
        assertThat((int) jdbcRepository.findActive().count(), is(1));
    }

    @Test
    public void shouldDeleteStream() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId);

        final Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());

        jdbcRepository.delete(streamId);

        assertFalse(jdbcRepository.findAll().findFirst().isPresent());
    }

    @Test
    public void shouldInsertNewStreamAsInactive() {
        final UUID streamId = randomUUID();
        jdbcRepository.insert(streamId, false);

        final Optional<EventStream> eventStream = jdbcRepository.findAll().findFirst();

        assertTrue(eventStream.isPresent());
        assertFalse(eventStream.get().isActive());
    }
}
