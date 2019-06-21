package uk.gov.justice.services.eventstore.management.indexer.process;

import java.util.Objects;
import java.util.concurrent.Callable;

public class EventIndexerCatchupTask implements Callable<Boolean> {

    private final IndexerCatchupContext indexerCatchupContext;
    private final EventIndexerCatchupProcessorBean eventIndexerCatchupProcessorBean;

    public EventIndexerCatchupTask(
            final IndexerCatchupContext indexerCatchupContext,
            final EventIndexerCatchupProcessorBean eventIndexerCatchupProcessorBean) {
        this.indexerCatchupContext = indexerCatchupContext;
        this.eventIndexerCatchupProcessorBean = eventIndexerCatchupProcessorBean;
    }


    @Override
    public Boolean call() {
        eventIndexerCatchupProcessorBean.performEventIndexerCatchup(indexerCatchupContext);

        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof EventIndexerCatchupTask)) return false;
        final EventIndexerCatchupTask that = (EventIndexerCatchupTask) o;
        return Objects.equals(indexerCatchupContext, that.indexerCatchupContext) &&
                Objects.equals(eventIndexerCatchupProcessorBean, that.eventIndexerCatchupProcessorBean);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexerCatchupContext, eventIndexerCatchupProcessorBean);
    }
}
