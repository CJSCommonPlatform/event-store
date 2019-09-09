package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.GlobalValue;

import javax.inject.Inject;

public class PublisherTimerConfig {

    @Inject
    @GlobalValue(key = "event.dequer.start.wait.milliseconds", defaultValue = "7000")
    String timerStartWaitMilliseconds;

    @Inject
    @GlobalValue(key = "event.dequer.timer.interval.milliseconds", defaultValue = "500")
    String timerIntervalMilliseconds;

    @Inject
    @GlobalValue(key = "event.dequer.timer.max.runtime.milliseconds", defaultValue = "450")
    String timerMaxRuntimeMilliseconds;

    public long getTimerStartWaitMilliseconds() {
        return parseLong(timerStartWaitMilliseconds);
    }

    public long getTimerIntervalMilliseconds() {
        return parseLong(timerIntervalMilliseconds);
    }

    public long getTimerMaxRuntimeMilliseconds() {
        return parseLong(timerMaxRuntimeMilliseconds);
    }
}
