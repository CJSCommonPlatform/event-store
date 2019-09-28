package uk.gov.justice.services.eventstore.management.catchup.state;

import static uk.gov.justice.services.eventstore.management.events.catchup.CatchupType.INDEX_CATCHUP;

import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.events.catchup.CatchupType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class CatchupStateManager {

    private final Map<String, CatchupInProgress> eventCatchupsInProgress = new ConcurrentHashMap<>();
    private final Map<String, CatchupInProgress> indexCatchupsInProgress = new ConcurrentHashMap<>();

    public void clear(final CatchupType catchupType) {
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
