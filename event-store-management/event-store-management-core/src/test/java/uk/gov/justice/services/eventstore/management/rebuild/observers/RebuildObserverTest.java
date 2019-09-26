package uk.gov.justice.services.eventstore.management.rebuild.observers;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;
import uk.gov.justice.services.eventstore.management.events.rebuild.RebuildCompleteEvent;
import uk.gov.justice.services.eventstore.management.events.rebuild.RebuildRequestedEvent;
import uk.gov.justice.services.eventstore.management.logging.MdcLogger;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import javax.enterprise.event.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class RebuildObserverTest {

    @Mock
    private PublishedEventRebuilder publishedEventRebuilder;

    @Mock
    private Event<RebuildCompleteEvent> rebuildCompletedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private MdcLogger mdcLogger;

    @Mock
    private Logger logger;

    @InjectMocks
    private RebuildObserver rebuildObserver;

    private Consumer<Runnable> testConsumer = Runnable::run;

    @Test
    public void shouldRunRebuild() throws Exception {

        final ZonedDateTime rebuildStartedAt = of(2019, 5, 24, 12, 0, 0, 0, UTC);
        final ZonedDateTime rebuildRequestedAt = rebuildStartedAt.minusSeconds(1);
        final ZonedDateTime rebuildCompletedAt = rebuildStartedAt.plusSeconds(1);

        final RebuildCommand target = new RebuildCommand();

        final RebuildRequestedEvent rebuildRequestedEvent = new RebuildRequestedEvent(
                rebuildRequestedAt,
                target);

        when(mdcLogger.mdcLoggerConsumer()).thenReturn(testConsumer);
        when(clock.now()).thenReturn(rebuildStartedAt, rebuildCompletedAt);

        rebuildObserver.onRebuildRequested(rebuildRequestedEvent);

        final InOrder inOrder = inOrder(logger, publishedEventRebuilder, rebuildCompletedEventFirer);

        inOrder.verify(logger).info("Rebuild requested by 'REBUILD' command at Fri May 24 11:59:59 Z 2019");
        inOrder.verify(logger).info("Rebuild for 'REBUILD' command started at Fri May 24 12:00:00 Z 2019");
        inOrder.verify(publishedEventRebuilder).rebuild();
        inOrder.verify(logger).info("Rebuild for 'REBUILD' command completed at Fri May 24 12:00:01 Z 2019");
        inOrder.verify(logger).info("Rebuild took 1000 milliseconds");
        inOrder.verify(rebuildCompletedEventFirer).fire(new RebuildCompleteEvent(target, rebuildCompletedAt));
    }
}
