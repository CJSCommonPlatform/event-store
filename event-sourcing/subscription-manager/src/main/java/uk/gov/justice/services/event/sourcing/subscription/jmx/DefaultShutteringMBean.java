package uk.gov.justice.services.event.sourcing.subscription.jmx;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.ApplicationStateController;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.shuttering.events.ShutteringRequestedEvent;
import uk.gov.justice.services.event.sourcing.subscription.lifecycle.shuttering.events.UnshutteringRequestedEvent;

import javax.inject.Inject;


public class DefaultShutteringMBean implements ShutteringMBean {

    @Inject
    private ApplicationStateController applicationStateController;

    @Inject
    private UtcClock clock;


    public void doShutteringRequested() {
        final ShutteringRequestedEvent shutteringRequestedEvent = new ShutteringRequestedEvent(this, clock.now());
        applicationStateController.fireShutteringRequested(shutteringRequestedEvent);
    }

    public void doUnshutteringRequested() {
        final UnshutteringRequestedEvent unshutteringRequestedEvent = new UnshutteringRequestedEvent(this, clock.now());
        applicationStateController.fireUnshutteringRequested(unshutteringRequestedEvent);
    }

}
