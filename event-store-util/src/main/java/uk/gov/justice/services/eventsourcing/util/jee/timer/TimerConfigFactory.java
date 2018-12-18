package uk.gov.justice.services.eventsourcing.util.jee.timer;

import javax.ejb.TimerConfig;

public class TimerConfigFactory {

    public TimerConfig createNew() {
        return new TimerConfig();
    }
}
