package uk.gov.justice.services.eventsourcing.jdbc.snapshot;


import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Snapshot repository.
 */
public interface SnapshotRepository {

    /**
     * Store snapshot.
     *
     * @param aggregateSnapshot the aggregate snapshot
     * @return true on successful save, otherwise false
     */
    boolean storeSnapshot(final AggregateSnapshot aggregateSnapshot);

    /**
     * Gets latest snapshot.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the latest snapshot
     */
    <T extends Aggregate> Optional<AggregateSnapshot<T>> getLatestSnapshot(final UUID streamId, final Class<T> clazz);


    /**
     * Remove all snapshots.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     */
    <T extends Aggregate> void removeAllSnapshots(final UUID streamId, final Class<T> clazz);


    /**
     * Remove all snapshots older than a given date.
     *
     * @param <T>       the type parameter
     * @param streamId  the stream id
     * @param clazz     the clazz
     * @param createdAt
     */
    <T extends Aggregate> int removeSnapshots(final UUID streamId, final Class<T> clazz, final long positionInStream, final ZonedDateTime createdAt);


    /**
     * Remove all snapshots older than given aggregateSnapshot
     *
     * @param aggregateSnapshot instance of Aggregate Snapshot
     */
    <T extends Aggregate> void removeAllSnapshotsOlderThan(final AggregateSnapshot aggregateSnapshot);

    /**
     * Gets latest snapshot version.
     *
     * @param <T>      the type parameter
     * @param streamId the stream id
     * @param clazz    the clazz
     * @return the latest snapshot version
     */
    <T extends Aggregate> long getLatestSnapshotVersion(final UUID streamId, final Class<T> clazz);
}
