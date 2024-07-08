package uk.gov.justice.services.eventsourcing.jdbc.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.SettableEventStoreDataSourceProvider;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.common.converter.ZonedDateTimes.toSqlTimestamp;

@ExtendWith(MockitoExtension.class)
public class SnapshotJdbcRepositoryJdbcIT {

    private static final String FETCH_ALL_SNAPSHOTS_QUERY = "SELECT * FROM snapshot";
    private static final String REMOVE_ALL_SNAPSHOTS_SQL = "DELETE FROM snapshot";
    private static final String FIND_CREATED_TIME_BY_VERSION_ID = "SELECT created_at FROM snapshot where stream_id = ? and version_id = ?";
    private static final Long VERSION_ID = 5L;
    private static final Class<RecordingAggregate> TYPE = RecordingAggregate.class;
    private static final Class<DifferentAggregate> OTHER_TYPE = DifferentAggregate.class;
    private static final byte[] AGGREGATE = "Any String you want".getBytes();

    @SuppressWarnings("unused")
    @Spy
    private SettableEventStoreDataSourceProvider eventStoreDataSourceProvider = new SettableEventStoreDataSourceProvider();

    @SuppressWarnings("unused")
    @Mock
    private Logger logger;

    @Mock
    private UtcClock clock;

    @InjectMocks
    private SnapshotJdbcRepository snapshotJdbcRepository;

    private final ZonedDateTime now = new UtcClock().now();

    @BeforeEach
    public void setupDatabaseConnection() throws Exception {
        eventStoreDataSourceProvider.setDataSource(new FrameworkTestDataSourceFactory().createEventStoreDataSource());
        removeAllSnapshots();
    }

    @Test
    public void shouldStoreAndRetrieveSnapshot() throws Exception {
        when(clock.now()).thenReturn(now);

        final UUID streamId = randomUUID();
        final AggregateSnapshot aggregateSnapshot = createSnapshot(streamId, VERSION_ID, TYPE, AGGREGATE);

        final boolean snapshotStored = snapshotJdbcRepository.storeSnapshot(aggregateSnapshot);

        assertTrue(snapshotStored);
        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot)));
        final Optional<Timestamp> createdTime = findSnapshotCreatedAt(streamId, VERSION_ID);
        assertTrue(createdTime.isPresent());
        assertThat(createdTime.get(), is(toSqlTimestamp(now)));
    }

    @Test
    public void shouldIgnoreFailureOnStoreAndLogError() throws Exception {
        when(clock.now()).thenReturn(now);
        final UUID streamId = randomUUID();
        final AggregateSnapshot aggregateSnapshot = createSnapshot(streamId, VERSION_ID, TYPE, AGGREGATE);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot);

        final boolean snapshotStored = snapshotJdbcRepository.storeSnapshot(aggregateSnapshot);

        assertFalse(snapshotStored);
        verify(logger).error(eq("Error while storing a snapshot for {} at version {}"), eq(streamId), eq( VERSION_ID), ArgumentMatchers.any(Throwable.class));
    }

    @Test
    public void shouldRetrieveLatestSnapshot() {
        when(clock.now()).thenReturn(now);

        final UUID streamId = randomUUID();

        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(streamId, VERSION_ID + 1, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot2 = createSnapshot(streamId, VERSION_ID + 2, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot3 = createSnapshot(streamId, VERSION_ID + 3, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot4 = createSnapshot(streamId, VERSION_ID + 4, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot5 = createSnapshot(streamId, VERSION_ID + 5, TYPE, AGGREGATE);

        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot1);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot2);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot3);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot4);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot5);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot5)));
    }

    @Test
    public void shouldRetrieveLatestSnapshotWithCorrectType() {
        when(clock.now()).thenReturn(now);

        final UUID streamId = randomUUID();

        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(streamId, VERSION_ID + 1, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot2 = createSnapshot(streamId, VERSION_ID + 2, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot3 = createSnapshot(streamId, VERSION_ID + 3, OTHER_TYPE, AGGREGATE);

        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot1);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot2);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot3);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot2)));
    }

    @Test
    public void shouldRemoveAllSnapshots() {
        when(clock.now()).thenReturn(now);

        final UUID streamId = randomUUID();

        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(streamId, VERSION_ID + 1, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot2 = createSnapshot(streamId, VERSION_ID + 2, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot3 = createSnapshot(streamId, VERSION_ID + 3, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot4 = createSnapshot(streamId, VERSION_ID + 4, TYPE, AGGREGATE);
        final AggregateSnapshot aggregateSnapshot5 = createSnapshot(streamId, VERSION_ID + 6, TYPE, AGGREGATE);

        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot1);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot2);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot3);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot4);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot5);

        snapshotJdbcRepository.removeAllSnapshots(streamId, TYPE);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshots = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshots, notNullValue());
        assertThat(snapshots.isPresent(), is(false));
    }

    @Test
    public void shouldRemoveOlderSnapshotsThanGivenSnapshot() throws Exception {
        when(clock.now()).thenReturn(now);
        final UUID streamId = randomUUID();
        final UUID otherStreamId = randomUUID();

        final AggregateSnapshot snapshot1 = createSnapshot(streamId, 1L, TYPE, AGGREGATE);
        final AggregateSnapshot snapshot2 = createSnapshot(streamId, 2L, TYPE, AGGREGATE);
        final AggregateSnapshot snapshot3 = createSnapshot(streamId, 4L, TYPE, AGGREGATE);
        final AggregateSnapshot snapshot4 = createSnapshot(streamId, 6L, TYPE, AGGREGATE);
        final AggregateSnapshot snapshot5 = createSnapshot(otherStreamId, 1L, OTHER_TYPE, AGGREGATE);

        snapshotJdbcRepository.storeSnapshot(snapshot1);
        snapshotJdbcRepository.storeSnapshot(snapshot2);
        snapshotJdbcRepository.storeSnapshot(snapshot3);
        snapshotJdbcRepository.storeSnapshot(snapshot4);
        snapshotJdbcRepository.storeSnapshot(snapshot5);

        snapshotJdbcRepository.removeAllSnapshotsOlderThan(snapshot3);

        final List<AggregateSnapshot> fetchedSnapshots = fetchAllSnapshotsFromDb();
        assertThat(fetchedSnapshots.size(), is(3));
        assertThat(fetchedSnapshots, hasItems(snapshot3, snapshot4, snapshot5));
    }

    @Test
    public void shouldLogErrorAndIgnoreAnyFailuresWhileRemovingOldSnapshots() throws Exception {
        //There is no easy way to reproduce sqlexception and had to use chain of mocks
        final UUID streamId = randomUUID();
        final SQLException sqlException = new SQLException();
        final DataSource mockDatasource = mock(DataSource.class);
        final Connection mockConnection = mock(Connection.class);
        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(mockDatasource);
        when(mockDatasource.getConnection()).thenReturn(mockConnection);
        doThrow(sqlException).when(mockConnection).prepareStatement(any());

        snapshotJdbcRepository.removeAllSnapshotsOlderThan(createSnapshot(streamId, 1L, TYPE, AGGREGATE));

        verify(logger).error("Exception while removing old snapshots %s of stream %s, version_id less than 1".formatted(TYPE.getName(), streamId), sqlException);
    }


    @Test
    public void shouldReturnOptionalNullIfNoSnapshotAvailable() {

        final UUID streamId = randomUUID();
        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot.isPresent(), is(false));
    }

    @Test
    public void shouldRetrieveOptionalNullIfOnlySnapshotsOfDifferentTypesAvailable() {
        when(clock.now()).thenReturn(now);
        final UUID streamId = randomUUID();
        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(streamId, VERSION_ID, OTHER_TYPE, AGGREGATE);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot1);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot.isPresent(), is(false));

    }

    private void removeAllSnapshots() throws Exception  {
        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(REMOVE_ALL_SNAPSHOTS_SQL)) {
            preparedStatement.executeUpdate();
        }
    }

    private Optional<Timestamp> findSnapshotCreatedAt(UUID streamId, Long versionId) throws Exception {
        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection();
             final PreparedStatement ps = connection.prepareStatement(FIND_CREATED_TIME_BY_VERSION_ID)) {
             ps.setObject(1, streamId);
             ps.setLong(2, versionId);
             try(final ResultSet rs = ps.executeQuery()) {
                 return rs.next() ? Optional.of(rs.getTimestamp("created_at")) : Optional.empty();
             }
        }
    }

    private List<AggregateSnapshot> fetchAllSnapshotsFromDb() throws SQLException {
        final List<AggregateSnapshot> fetchedSnapshots = new ArrayList<>();
        try (final Connection connection = eventStoreDataSourceProvider.getDefaultDataSource().getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(FETCH_ALL_SNAPSHOTS_QUERY)) {
             try(final ResultSet rs = preparedStatement.executeQuery()) {
                 while(rs.next()) {
                     fetchedSnapshots.add(snapshotJdbcRepository.entityFrom(rs));
                 }
             }
        }

        return fetchedSnapshots;
    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> AggregateSnapshot createSnapshot(final UUID streamId, final Long sequenceId, Class<T> type, byte[] aggregate) {
        return new AggregateSnapshot(streamId, sequenceId, type, aggregate);
    }

    public class RecordingAggregate implements Aggregate {
        final List<Object> recordedEvents = new ArrayList<>();

        @Override
        public Object apply(Object event) {
            recordedEvents.add(event);
            return event;
        }
    }

    public class DifferentAggregate implements Aggregate {
        @Override
        public Object apply(Object event) {
            return null;
        }
    }
}
