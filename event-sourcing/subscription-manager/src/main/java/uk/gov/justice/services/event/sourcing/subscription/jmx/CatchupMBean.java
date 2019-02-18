package uk.gov.justice.services.event.sourcing.subscription.jmx;

import javax.management.MXBean;

@MXBean
public interface CatchupMBean {
    void doCatchupRequested();
}
