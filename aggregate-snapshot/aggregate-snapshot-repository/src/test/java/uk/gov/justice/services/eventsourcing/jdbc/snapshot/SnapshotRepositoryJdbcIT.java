package uk.gov.justice.services.eventsourcing.jdbc.snapshot;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.services.test.utils.persistence.FrameworkTestDataSourceFactory;
import uk.gov.justice.services.test.utils.persistence.SettableEventStoreDataSourceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class SnapshotRepositoryJdbcIT {

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

    @InjectMocks
    private SnapshotJdbcRepository snapshotJdbcRepository;

    @BeforeEach
    public void setupDatabaseConnection() throws Exception {
        eventStoreDataSourceProvider.setDataSource(new FrameworkTestDataSourceFactory().createEventStoreDataSource());
    }

    @Test
    public void shouldStoreAndRetrieveSnapshot() {

        final UUID streamId = randomUUID();
        final AggregateSnapshot aggregateSnapshot = createSnapshot(streamId, VERSION_ID, TYPE, AGGREGATE);

        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot, notNullValue());
        assertThat(snapshot, is(Optional.of(aggregateSnapshot)));
    }

    @Test
    public void shouldRetrieveLatestSnapshot() {

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
    public void shouldReturnOptionalNullIfNoSnapshotAvailable() {

        final UUID streamId = randomUUID();
        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot.isPresent(), is(false));
    }

    @Test
    public void shouldRetrieveOptionalNullIfOnlySnapshotsOfDifferentTypesAvailable() {
        final UUID streamId = randomUUID();
        final AggregateSnapshot aggregateSnapshot1 = createSnapshot(streamId, VERSION_ID, OTHER_TYPE, AGGREGATE);
        snapshotJdbcRepository.storeSnapshot(aggregateSnapshot1);

        final Optional<AggregateSnapshot<RecordingAggregate>> snapshot = snapshotJdbcRepository.getLatestSnapshot(streamId, TYPE);

        assertThat(snapshot.isPresent(), is(false));

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
