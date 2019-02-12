package uk.gov.justice.services.event.sourcing.subscription.lifecycle.catchup;

import uk.gov.justice.services.event.sourcing.subscription.lifecycle.catchup.events.CatchupRequestedEvent;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.catchup.events.CatchupStartedEvent;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.catchup.events.CatchupCompletedEvent;

public interface CatchupListener {

    default void catchupRequested(@SuppressWarnings("unused") final CatchupRequestedEvent catchupRequestedEvent) {}
    default void catchupStarted(@SuppressWarnings("unused") final CatchupStartedEvent catchupStartedEvent) {}
    default void catchupCompleted(@SuppressWarnings("unused") final CatchupCompletedEvent catchupCompletedEvent) {}
}
