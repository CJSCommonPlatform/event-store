package uk.gov.justice.services.event.sourcing.subscription.catchup;

import static java.lang.Boolean.valueOf;

import uk.gov.justice.services.common.configuration.Value;

import javax.inject.Inject;

public class EventCatchupConfig {

    @Inject
    @Value(key = "event.catchup.enabled", defaultValue = "false")
    String eventCatchupEnabled;

    public boolean isEventCatchupEnabled() {
        return valueOf(eventCatchupEnabled);
    }
}


