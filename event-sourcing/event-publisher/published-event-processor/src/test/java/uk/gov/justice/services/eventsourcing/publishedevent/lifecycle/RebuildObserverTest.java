package uk.gov.justice.services.eventsourcing.publishedevent.lifecycle;

import static java.time.ZoneOffset.UTC;
import static java.time.ZonedDateTime.of;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.lifecycle.events.rebuild.RebuildRequestedEvent;
import uk.gov.justice.services.core.lifecycle.events.rebuild.RebuildStartedEvent;
import uk.gov.justice.services.eventsourcing.publishedevent.rebuild.PublishedEventRebuilder;

import java.time.ZonedDateTime;

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
    private Event<RebuildStartedEvent> rebuildStartedEventFirer;

    @Mock
    private UtcClock clock;

    @Mock
    private Logger logger;

    @InjectMocks
    private RebuildObserver rebuildObserver;

    @Test
    public void shouldRunRebuild() throws Exception {

        final ZonedDateTime rebuildStartedAt = of(2019, 5, 24, 12, 0, 0, 0, UTC);
        final ZonedDateTime rebuildRequestedAt = rebuildStartedAt.minusSeconds(1);
        final ZonedDateTime rebuildCompletedAt = rebuildStartedAt.plusSeconds(1);

        final RebuildRequestedEvent rebuildRequestedEvent = new RebuildRequestedEvent(
                getClass().getSimpleName(),
                rebuildRequestedAt);

        when(clock.now()).thenReturn(rebuildStartedAt, rebuildCompletedAt);

        rebuildObserver.onRebuildRequested(rebuildRequestedEvent);

        final InOrder inOrder = inOrder(logger, rebuildStartedEventFirer, publishedEventRebuilder);

        inOrder.verify(logger).info("Rebuild requested at Fri May 24 11:59:59 Z 2019");
        inOrder.verify(logger).info("Rebuild started at Fri May 24 12:00:00 Z 2019");
        inOrder.verify(rebuildStartedEventFirer).fire(new RebuildStartedEvent(rebuildStartedAt));
        inOrder.verify(publishedEventRebuilder).rebuild();
        inOrder.verify(logger).info("Rebuild completed at Fri May 24 12:00:01 Z 2019");
        inOrder.verify(logger).info("Rebuild took 1000 milliseconds");

    }
}
