package uk.gov.justice.services.eventsourcing.source.core.snapshot.async;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.DefaultSnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.async.event.SnapshotDeleterRequestEvent;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class SnapshotDeleterObserverTest {

    @Mock
    private Logger logger;
    @Mock
    private DefaultSnapshotService snapshotService;
    @InjectMocks
    private SnapshotDeleterObserver snapshotDeleterObserver;

    final SnapshotDeleterRequestEvent event = new SnapshotDeleterRequestEvent(UUID.randomUUID(),
            TestAggregate.class, 1L, new UtcClock().now());

    @Test
    void shouldDeleteSnapshot() {

        when(snapshotService.removeSnapshot(event.getStreamId(), event.getAggregateClass(),
                event.getPositionInStream(), event.getCreatedAt())).thenReturn(1);

        final int deleteCount = snapshotDeleterObserver.onDeleteReceived(event);

        assertThat(deleteCount, is(1));

        verify(logger).trace("About to delete snapshot {}", event);
        verify(logger).info("Successfully deleted in background snapshot {}", event);
        verifyNoMoreInteractions(logger, snapshotService);
    }

    @Test
    void shouldReturnZeroOnException() {

        final RuntimeException exception = new RuntimeException("Failed to delete");
        when(snapshotService.removeSnapshot(event.getStreamId(), event.getAggregateClass(),
                event.getPositionInStream(), event.getCreatedAt())).thenThrow(exception);

        final int deleteCount = snapshotDeleterObserver.onDeleteReceived(event);

        assertThat(deleteCount, is(0));

        verify(logger).trace("About to delete snapshot {}", event);
        verify(logger).error("Failed to delete snapshot %s".formatted(event), exception);
        verifyNoMoreInteractions(logger, snapshotService);
    }
}