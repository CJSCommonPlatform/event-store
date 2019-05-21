package uk.gov.justice.services.eventsourcing.util.jee.timer;

import org.apache.commons.lang3.time.StopWatch;

public class StopWatchFactory {

    public StopWatch createStopWatch() {
        return new StopWatch();
    }
}
