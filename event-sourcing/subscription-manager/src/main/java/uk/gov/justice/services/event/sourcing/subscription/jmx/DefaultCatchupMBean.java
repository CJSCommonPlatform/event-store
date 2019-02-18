package uk.gov.justice.services.event.sourcing.subscription.jmx;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.ApplicationStateController;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.catchup.events.CatchupRequestedEvent;

import javax.inject.Inject;

public class DefaultCatchupMBean implements CatchupMBean {

    @Inject
    private ApplicationStateController applicationStateController;

    @Inject
    private UtcClock clock;

    @Override
    public void doCatchupRequested() {
        final CatchupRequestedEvent catchupRequestedEvent = new CatchupRequestedEvent(this, clock.now());
        applicationStateController.fireCatchupRequested(catchupRequestedEvent);
    }
}
