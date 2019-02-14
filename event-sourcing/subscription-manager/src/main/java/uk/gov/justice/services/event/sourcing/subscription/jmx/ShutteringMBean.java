package uk.gov.justice.services.event.sourcing.subscription.jmx;

import javax.management.MXBean;

@MXBean
public interface ShutteringMBean {
    void doShutteringRequested();
    void doUnshutteringRequested();
}
