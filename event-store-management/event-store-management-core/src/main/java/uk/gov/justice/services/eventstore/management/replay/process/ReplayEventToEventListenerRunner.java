package uk.gov.justice.services.eventstore.management.replay.process;

import uk.gov.justice.services.common.util.UtcClock;

import javax.inject.Inject;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

public class ReplayEventToEventListenerRunner {

    @Inject
    private EventSourceNameFinder eventSourceNameFinder;

    @Inject
    private ReplayEventToEventListenerProcessorBean replayEventToEventListenerProcessorBean;

    @Inject
    private UtcClock clock;

    public void run(final UUID commandId, final UUID commandRuntimeId) {
        final String eventSourceName = eventSourceNameFinder.getEventSourceNameOfEventListener();

        final ReplayEventContext replayEventContext = new ReplayEventContext(commandId,
                commandRuntimeId, eventSourceName, EVENT_LISTENER);

        replayEventToEventListenerProcessorBean.perform(replayEventContext);
    }
}
