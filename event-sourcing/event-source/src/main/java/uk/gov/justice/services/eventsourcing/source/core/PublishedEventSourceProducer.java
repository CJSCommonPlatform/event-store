package uk.gov.justice.services.eventsourcing.source.core;

import static java.lang.String.format;

import uk.gov.justice.services.cdi.QualifierAnnotationExtractor;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;
import uk.gov.justice.services.eventsourcing.source.core.annotation.EventSourceName;
import uk.gov.justice.subscription.domain.eventsource.EventSourceDefinition;
import uk.gov.justice.subscription.domain.eventsource.Location;
import uk.gov.justice.subscription.registry.EventSourceDefinitionRegistry;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@ApplicationScoped
@Default
@Priority(200)
public class PublishedEventSourceProducer {

    @Inject
    private EventSourceDefinitionRegistry eventSourceDefinitionRegistry;

    @Inject
    private JdbcPublishedEventSourceFactory jdbcPublishedEventSourceFactory;

    @Inject
    private QualifierAnnotationExtractor qualifierAnnotationExtractor;

    /**
     * Backwards compatible support for Unnamed PublishedEventSource injection points. Uses the
     * injected container JNDI name to lookup the EventSource
     *
     * @return {@link PublishedEventSource}
     */
    @Produces
    public PublishedEventSource publishedEventSource() {
        return createEventSourceFrom(eventSourceDefinitionRegistry.getDefaultEventSourceDefinition());
    }

    /**
     * Support for Named PublishedEventSource injection points.  Annotate injection point with
     * <pre>
     *   &#64;EventSourceName("name")
     * </pre>
     *
     *  @param injectionPoint the injection point for the EventSource
     *  @return {@link PublishedEventSource}
     */
    @Produces
    @EventSourceName
    public PublishedEventSource publishedEventSource(final InjectionPoint injectionPoint) {

        final String eventSourceName = qualifierAnnotationExtractor.getFrom(injectionPoint, EventSourceName.class).value();
        final Optional<EventSourceDefinition> eventSourceDefinition = eventSourceDefinitionRegistry.getEventSourceDefinitionFor(eventSourceName);

        return eventSourceDefinition
                .map(this::createEventSourceFrom)
                .orElseThrow(() -> new CreationException(format("Failed to find EventSource named '%s' in event-sources.yaml", eventSourceName)));
    }

    private PublishedEventSource createEventSourceFrom(final EventSourceDefinition eventSourceDefinition) {
        final Location location = eventSourceDefinition.getLocation();
        final Optional<String> dataSourceOptional = location.getDataSource();

        return dataSourceOptional
                .map(dataSource -> jdbcPublishedEventSourceFactory.create(dataSource))
                .orElseThrow(() -> new CreationException(
                        format("No DataSource specified for EventSource '%s' specified in event-sources.yaml", eventSourceDefinition.getName())
                ));
    }
}
