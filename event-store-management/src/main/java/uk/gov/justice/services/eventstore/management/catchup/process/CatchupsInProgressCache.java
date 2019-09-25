package uk.gov.justice.services.eventstore.management.catchup.process;

import static uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType.INDEX_CATCHUP;

import uk.gov.justice.services.eventstore.management.catchup.commands.CatchupType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatchupsInProgressCache {

    private final Map<String, CatchupInProgress> eventCatchupsInProgress = new ConcurrentHashMap<>();
    private final Map<String, CatchupInProgress> indexCatchupsInProgress = new ConcurrentHashMap<>();

    public void removeAll(final CatchupType catchupType) {
        getCache(catchupType).clear();
    }

    public void addCatchupInProgress(final CatchupInProgress catchupInProgress, final CatchupType catchupType) {
        getCache(catchupType).put(catchupInProgress.getSubscriptionName(), catchupInProgress);
    }

    public CatchupInProgress removeCatchupInProgress(final String subscriptionName, final CatchupType catchupType) {
        return getCache(catchupType).remove(subscriptionName);
    }

    public boolean isCatchupInProgress(final String subscriptionName, final CatchupType catchupType) {
       return getCache(catchupType).containsKey(subscriptionName);
    }

    public List<CatchupInProgress> getAllCatchupsInProgress(final CatchupType catchupType) {
        return new ArrayList<>(getCache(catchupType).values());
    }

    public boolean noCatchupsInProgress(final CatchupType catchupType) {
        return getCache(catchupType).isEmpty();
    }

    private Map<String, CatchupInProgress> getCache(final CatchupType catchupType) {
        if (catchupType == INDEX_CATCHUP) {
            return indexCatchupsInProgress;
        }

        return eventCatchupsInProgress;
    }
}
