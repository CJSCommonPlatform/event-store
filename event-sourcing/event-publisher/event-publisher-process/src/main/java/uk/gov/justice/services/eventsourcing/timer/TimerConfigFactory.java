package uk.gov.justice.services.eventsourcing.timer;

import javax.ejb.TimerConfig;

public class TimerConfigFactory {

    public TimerConfig createNew() {
        return new TimerConfig();
    }
}
