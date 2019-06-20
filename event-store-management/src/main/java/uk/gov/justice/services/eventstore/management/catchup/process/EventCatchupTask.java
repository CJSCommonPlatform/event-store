package uk.gov.justice.services.eventstore.management.catchup.process;

import java.util.Objects;
import java.util.concurrent.Callable;

public class EventCatchupTask implements Callable<Boolean> {

    private final CatchupContext catchupContext;
    private final EventCatchupProcessorBean eventCatchupProcessorBean;

    public EventCatchupTask(
            final CatchupContext catchupContext,
            final EventCatchupProcessorBean eventCatchupProcessorBean) {
        this.catchupContext = catchupContext;
        this.eventCatchupProcessorBean = eventCatchupProcessorBean;
    }


    @Override
    public Boolean call() {
        eventCatchupProcessorBean.performEventCatchup(catchupContext);

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventCatchupTask)) return false;
        final EventCatchupTask that = (EventCatchupTask) o;
        return Objects.equals(catchupContext, that.catchupContext) &&
                Objects.equals(eventCatchupProcessorBean, that.eventCatchupProcessorBean);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catchupContext, eventCatchupProcessorBean);
    }
}
