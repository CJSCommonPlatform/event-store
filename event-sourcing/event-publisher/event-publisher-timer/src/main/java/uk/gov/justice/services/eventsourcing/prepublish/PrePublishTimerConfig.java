package uk.gov.justice.services.eventsourcing.prepublish;

import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.GlobalValue;

import javax.inject.Inject;

public class PrePublishTimerConfig {

    @Inject
    @GlobalValue(key = "pre.publish.start.wait.milliseconds", defaultValue = "7250")
    String timerStartWaitMilliseconds;

    @Inject
    @GlobalValue(key = "pre.publish.timer.interval.milliseconds", defaultValue = "500")
    String timerIntervalMilliseconds;

    public long getTimerStartWaitMilliseconds() {
        return parseLong(timerStartWaitMilliseconds);
    }

    public long getTimerIntervalMilliseconds() {
        return parseLong(timerIntervalMilliseconds);
    }
}
