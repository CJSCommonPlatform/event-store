package uk.gov.justice.services.event.sourcing.subscription.lifecycle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CatchupController {

    private final List<CatchupProcessListener> catchupProcessListeners = new CopyOnWriteArrayList<>();

    public void addCatchupProcessListener(final CatchupProcessListener catchupProcessListener) {
        catchupProcessListeners.add(catchupProcessListener);
    }

    public void removeCatchupProcessListener(final CatchupProcessListener catchupProcessListener) {
        catchupProcessListeners.remove(catchupProcessListener);
    }

    public void fireCatchupStarted(final CatchupStartedEvent catchupStartedEvent) {
        catchupProcessListeners.forEach(catchupProcessListener -> catchupProcessListener.onCatchupStarted(catchupStartedEvent));
    }

    public void fireCatchupCompleted(final CatchupCompletedEvent catchupCompletedEvent) {
        catchupProcessListeners.forEach(catchupProcessListener -> catchupProcessListener.onCatchupCompleted(catchupCompletedEvent));
    }
}
