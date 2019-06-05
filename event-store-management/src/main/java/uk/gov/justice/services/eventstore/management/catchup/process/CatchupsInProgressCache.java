package uk.gov.justice.services.eventstore.management.catchup.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CatchupsInProgressCache {

    private final Map<String, CatchupInProgress> catchupsInProgress = new ConcurrentHashMap<>();

    public void removeAll() {
        catchupsInProgress.clear();
    }

    public void addCatchupInProgress(final CatchupInProgress catchupInProgress) {
        catchupsInProgress.put(catchupInProgress.getSubscriptionName(), catchupInProgress);
    }

    public CatchupInProgress removeCatchupInProgress(final String subscriptionName) {
        return catchupsInProgress.remove(subscriptionName);
    }

    public boolean isCatchupInProgress(final String subscriptionName) {
       return catchupsInProgress.containsKey(subscriptionName);
    }

    public List<CatchupInProgress> getAllCatchupsInProgress() {
        return new ArrayList<>(catchupsInProgress.values());
    }

    public boolean noCatchupsInProgress() {
        return catchupsInProgress.isEmpty();
    }
}
