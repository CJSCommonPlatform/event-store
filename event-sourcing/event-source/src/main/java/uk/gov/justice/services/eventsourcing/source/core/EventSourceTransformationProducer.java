package uk.gov.justice.services.eventsourcing.source.core;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class EventSourceTransformationProducer {

    @Inject
    private EventStreamManager eventStreamManager;

    @Produces
    public EventSourceTransformation eventSourceTransformation() {

        return new DefaultEventSourceTransformation(eventStreamManager);
    }
}
