package uk.gov.justice.services.eventstore.management.catchup.state;

import uk.gov.justice.services.eventstore.management.catchup.process.CatchupFor;
import uk.gov.justice.services.eventstore.management.catchup.process.CatchupInProgress;
import uk.gov.justice.services.eventstore.management.commands.CatchupCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class CatchupStateManager {

    private final Map<CatchupFor, CatchupInProgress> eventCatchupsInProgress = new ConcurrentHashMap<>();
    private final Map<CatchupFor, CatchupInProgress> indexCatchupsInProgress = new ConcurrentHashMap<>();

    public void clear(final CatchupCommand catchupCommand) {
        getCache(catchupCommand).clear();
    }

    public void addCatchupInProgress(final CatchupInProgress catchupInProgress, final CatchupCommand catchupCommand) {

        getCache(catchupCommand).put(catchupInProgress.getCatchupFor(), catchupInProgress);
    }

    public CatchupInProgress removeCatchupInProgress(final CatchupFor catchupFor, final CatchupCommand catchupCommand) {
        return getCache(catchupCommand).remove(catchupFor);
    }

    public boolean isCatchupInProgress(final CatchupFor catchupFor, final CatchupCommand catchupCommand) {
        return getCache(catchupCommand).containsKey(catchupFor);
    }

    public List<CatchupInProgress> getAllCatchupsInProgress(final CatchupCommand catchupCommand) {
        return new ArrayList<>(getCache(catchupCommand).values());
    }

    public boolean noCatchupsInProgress(final CatchupCommand catchupType) {
        return getCache(catchupType).isEmpty();
    }

    private Map<CatchupFor, CatchupInProgress> getCache(final CatchupCommand catchupCommand) {
        if (catchupCommand.isEventCatchup()) {
            return eventCatchupsInProgress;
        }

        return indexCatchupsInProgress;
    }
}
