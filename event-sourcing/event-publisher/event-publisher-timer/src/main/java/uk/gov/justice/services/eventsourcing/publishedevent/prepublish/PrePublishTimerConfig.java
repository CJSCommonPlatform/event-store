package uk.gov.justice.services.eventsourcing.publishedevent.prepublish;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import uk.gov.justice.services.common.configuration.GlobalValue;

import javax.inject.Inject;

public class PrePublishTimerConfig {

    @Inject
    @GlobalValue(key = "pre.publish.start.wait.milliseconds", defaultValue = "7250")
    private String timerStartWaitMilliseconds;

    @Inject
    @GlobalValue(key = "pre.publish.timer.interval.milliseconds", defaultValue = "500")
    private String timerIntervalMilliseconds;

    @Inject
    @GlobalValue(key = "pre.publish.max.events.published.per.iteration", defaultValue = "1000")
    private String maxEventsPublishedPerIteration;

    public long getTimerStartWaitMilliseconds() {
        return parseLong(timerStartWaitMilliseconds);
    }

    public long getTimerIntervalMilliseconds() {
        return parseLong(timerIntervalMilliseconds);
    }

    public int getMaxEventsPublishedPerIteration() {
        return parseInt(maxEventsPublishedPerIteration);
    }
}
