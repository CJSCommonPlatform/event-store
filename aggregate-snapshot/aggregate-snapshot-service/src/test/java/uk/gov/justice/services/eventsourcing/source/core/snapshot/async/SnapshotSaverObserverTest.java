package uk.gov.justice.services.eventsourcing.source.core.snapshot.async;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.domain.aggregate.TestAggregate;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.DefaultSnapshotService;
import uk.gov.justice.services.eventsourcing.source.core.snapshot.async.event.SnapshotSaverRequestEvent;

import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class SnapshotSaverObserverTest {

    @Mock
    private Logger logger;
    @Mock
    private DefaultSnapshotService snapshotService;
    @InjectMocks
    private SnapshotSaverObserver snapshotSaverObserver;

    final TestAggregate testAggregate = new TestAggregate();
    final SnapshotSaverRequestEvent event = new SnapshotSaverRequestEvent(UUID.randomUUID(), 1L, testAggregate);

    @Test
    void shouldSave() {
        when(snapshotService.storeAggregateSimply(event.getStreamId(), event.getPositionInStream(), event.getAggregate())).thenReturn(true);

        final boolean savedOK = snapshotSaverObserver.onSaveReceived(event);

        assertThat(savedOK, Matchers.is(true));
        verify(logger).trace("About to save snapshot {}", event);
        verify(logger).info("Successfully saved in background snapshot {}", event);

        verifyNoMoreInteractions(logger, snapshotService);
    }

    @Test
    void shouldReturnFalseOnException() {
        final RuntimeException exception = new RuntimeException("Failed save snapshot");
        when(snapshotService.storeAggregateSimply(event.getStreamId(), event.getPositionInStream(), event.getAggregate())).thenThrow(exception);

        final boolean savedOK = snapshotSaverObserver.onSaveReceived(event);

        assertThat(savedOK, Matchers.is(false));
        verify(logger).trace("About to save snapshot {}", event);
        verify(logger).error("Failed to save snapshot %s".formatted(event), exception);

        verifyNoMoreInteractions(logger, snapshotService);
    }
}