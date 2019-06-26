package uk.gov.justice.services.eventstore.management.indexer.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexerCatchupsInProgressCache {

    private final Map<String, IndexerCatchupInProgress> catchupsInProgress = new ConcurrentHashMap<>();

    public void removeAll() {
        catchupsInProgress.clear();
    }

    public void addCatchupInProgress(final IndexerCatchupInProgress catchupInProgress) {
        catchupsInProgress.put(catchupInProgress.getSubscriptionName(), catchupInProgress);
    }

    public IndexerCatchupInProgress removeCatchupInProgress(final String subscriptionName) {
        return catchupsInProgress.remove(subscriptionName);
    }

    public boolean isCatchupInProgress(final String subscriptionName) {
       return catchupsInProgress.containsKey(subscriptionName);
    }

    public List<IndexerCatchupInProgress> getAllCatchupsInProgress() {
        return new ArrayList<>(catchupsInProgress.values());
    }

    public boolean noCatchupsInProgress() {
        return catchupsInProgress.isEmpty();
    }
}
