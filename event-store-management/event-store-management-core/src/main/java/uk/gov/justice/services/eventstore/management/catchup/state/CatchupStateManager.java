package uk.gov.justice.services.eventstore.management.catchup.state;

import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.jmx.api.command.CatchupCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class CatchupStateManager {

    private final Map<String, CatchupInProgress> eventCatchupsInProgress = new ConcurrentHashMap<>();
    private final Map<String, CatchupInProgress> indexCatchupsInProgress = new ConcurrentHashMap<>();

    public void clear(final CatchupCommand catchupCommand) {
        getCache(catchupCommand).clear();
    }

    public void addCatchupInProgress(final CatchupInProgress catchupInProgress, final CatchupCommand catchupCommand) {
        getCache(catchupCommand).put(catchupInProgress.getSubscriptionName(), catchupInProgress);
    }

    public CatchupInProgress removeCatchupInProgress(final String subscriptionName, final CatchupCommand catchupCommand) {
        return getCache(catchupCommand).remove(subscriptionName);
    }

    public boolean isCatchupInProgress(final String subscriptionName, final CatchupCommand catchupCommand) {
        return getCache(catchupCommand).containsKey(subscriptionName);
    }

    public List<CatchupInProgress> getAllCatchupsInProgress(final CatchupCommand catchupCommand) {
        return new ArrayList<>(getCache(catchupCommand).values());
    }

    public boolean noCatchupsInProgress(final CatchupCommand catchupType) {
        return getCache(catchupType).isEmpty();
    }

    private Map<String, CatchupInProgress> getCache(final CatchupCommand catchupCommand) {
        if (catchupCommand.isEventCatchup()) {
            return eventCatchupsInProgress;
        }

        return indexCatchupsInProgress;
    }
}
