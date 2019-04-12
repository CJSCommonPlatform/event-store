package uk.gov.justice.services.event.sourcing.subscription.manager;

import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.EventSourceNameQualifier;
import uk.gov.justice.services.eventsourcing.source.core.PublishedEventSource;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class PublishedEventSourceProvider {

    @Inject
    @Any
    Instance<PublishedEventSource> publishedEventSources;

    public PublishedEventSource getPublishedEventSource(final String eventSourceName) {

        return publishedEventSources
                .select(new EventSourceNameQualifier(eventSourceName))
                .get();
    }
}
