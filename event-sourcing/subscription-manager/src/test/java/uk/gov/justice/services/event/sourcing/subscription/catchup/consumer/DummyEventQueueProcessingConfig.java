package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer;

import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.DefaultEventQueueProcessingConfig;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager.EventQueueProcessingConfig;

import org.apache.openejb.testing.Default;

@Default
public class DummyEventQueueProcessingConfig implements EventQueueProcessingConfig {

    @Override
    public int getMaxTotalEventsInProcess() {
        return 100;
    }
}
