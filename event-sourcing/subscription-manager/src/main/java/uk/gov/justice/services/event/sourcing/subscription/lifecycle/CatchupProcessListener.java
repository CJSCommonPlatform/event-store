package uk.gov.justice.services.event.sourcing.subscription.lifecycle;

public interface CatchupProcessListener {

    default void onCatchupStarted(@SuppressWarnings("unused") final CatchupStartedEvent catchupStartedEvent) {}

    default void onCatchupCompleted(@SuppressWarnings("unused") final CatchupCompletedEvent catchupCompletedEvent) {}
}
