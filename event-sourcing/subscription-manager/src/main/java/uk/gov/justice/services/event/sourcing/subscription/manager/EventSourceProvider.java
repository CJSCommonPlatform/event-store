package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.EventSourceNameQualifier;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class EventSourceProvider {

    @Inject
    @Any
    Instance<EventSource> eventSourceInstance;

    public EventSource getEventSource(final String eventSourceName) {

        return eventSourceInstance
                .select(new EventSourceNameQualifier(eventSourceName))
                .get();
    }
}
