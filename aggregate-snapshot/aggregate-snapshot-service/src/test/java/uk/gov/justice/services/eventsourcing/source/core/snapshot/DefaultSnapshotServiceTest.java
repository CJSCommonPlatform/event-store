package uk.gov.justice.services.eventsourcing.source.core.snapshot;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.aggregate.NoSerializableTestAggregate;
import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.domain.snapshot.AggregateSnapshot;
import uk.gov.justice.domain.snapshot.VersionedAggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;
import uk.gov.justice.services.eventsourcing.jdbc.snapshot.SnapshotRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class DefaultSnapshotServiceTest {

    private static final UUID STREAM_ID = randomUUID();

    @Mock
    private SnapshotRepository snapshotRepository;

    @Mock
    private SnapshotStrategy snapshotStrategy;

    @Mock
    private Logger logger;

    @Captor
    private ArgumentCaptor<AggregateSnapshot<TestAggregate>> snapshotArgumentCaptor;

    @InjectMocks
    private DefaultSnapshotService snapshotService;


    @Test
    public void shouldCreateAggregateWhenLatestVersionRequested() throws AggregateChangeDetectedException {
        final Optional<AggregateSnapshot<TestAggregate>> aggregateSnapshot = Optional.empty();
        when(snapshotRepository.getLatestSnapshot(STREAM_ID, TestAggregate.class)).thenReturn(aggregateSnapshot);

        final Optional<VersionedAggregate<TestAggregate>> versionedAggregate = snapshotService.getLatestVersionedAggregate(STREAM_ID, TestAggregate.class);

        assertThat(versionedAggregate, notNullValue());
        assertThat(versionedAggregate.isPresent(), is(false));
    }

    @Test
    public void shouldRemoveAllSnapshots() {

        snapshotService.removeAllSnapshots(STREAM_ID, TestAggregate.class);
        verify(snapshotRepository).removeAllSnapshots(STREAM_ID, TestAggregate.class);
    }

    @Test
    public void shouldCreateAndRemoveOldSnapshotsOnSuccessfulCreateIfStrategyMandatesCreation() {
        final TestAggregate aggregate = new TestAggregate();
        final Long currentSnapshotVersion = 0l;
        final Long currentAggregateVersionId = 26l;
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, TestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(true);
        when(snapshotRepository.storeSnapshot(any(AggregateSnapshot.class))).thenReturn(true);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);

        verify(snapshotRepository, times(1)).storeSnapshot(snapshotArgumentCaptor.capture());
        assertThat(snapshotArgumentCaptor.getValue(), notNullValue());
        assertThat(snapshotArgumentCaptor.getValue().getPositionInStream(), is(currentAggregateVersionId));

        verify(snapshotRepository, times(1)).removeAllSnapshotsOlderThan(argThat(actual -> {
            assertThat(actual.getPositionInStream(), is(currentAggregateVersionId));
            assertThat(actual.getStreamId(), is(STREAM_ID));
            return true;
        }));
    }

    @Test
    public void shouldNotRemoveOldSnapshotsWhenSnapshotStoreFails() {
        final TestAggregate aggregate = new TestAggregate();
        final Long currentSnapshotVersion = 0l;
        final Long currentAggregateVersionId = 26l;
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, TestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(true);
        when(snapshotRepository.storeSnapshot(any(AggregateSnapshot.class))).thenReturn(false);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);

        verify(snapshotRepository, never()).removeAllSnapshotsOlderThan(any());
    }

    @Test
    public void shouldNotCreateAggregateIfStrategyDoesNotMandatesCreation() {
        final Long currentSnapshotVersion = 0l;
        final Long currentAggregateVersionId = 26l;
        final TestAggregate aggregate = new TestAggregate();
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, TestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(false);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);
        verify(snapshotRepository, never()).storeSnapshot(any(AggregateSnapshot.class));
        verify(snapshotRepository, never()).removeAllSnapshotsOlderThan(any(AggregateSnapshot.class));
    }


    @Test
    public void shouldNotCreateSnapshotWhenStrategyMandatesCreationButFailsSerialization() {
        final NoSerializableTestAggregate aggregate = new NoSerializableTestAggregate();
        final Long currentSnapshotVersion = 16l;
        final Long currentAggregateVersionId = 36l;
        when(snapshotRepository.getLatestSnapshotVersion(STREAM_ID, NoSerializableTestAggregate.class)).thenReturn(currentSnapshotVersion);
        when(snapshotStrategy.shouldCreateSnapshot(currentAggregateVersionId, currentSnapshotVersion)).thenReturn(true);

        snapshotService.attemptAggregateStore(STREAM_ID, currentAggregateVersionId, aggregate);

        verify(snapshotRepository, never()).storeSnapshot(any(AggregateSnapshot.class));
        verify(snapshotRepository, never()).removeAllSnapshotsOlderThan(any(AggregateSnapshot.class));
    }
}
