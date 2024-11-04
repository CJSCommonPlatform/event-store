package uk.gov.justice.services.eventstore.management.aggregate.snapshot.regeneration.commands;

import static java.lang.String.format;
import static java.util.stream.Stream.empty;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.core.aggregate.SnapshotAwareAggregateService;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class RegenerateAggregateSnapshotBean {

    static final Stream<JsonEnvelope> EMPTY_JSON_ENVELOPE_STREAM_TO_FORCE_SNAPSHOT_GENERATION = empty();

    @Inject
    private SnapshotAwareAggregateService snapshotAwareAggregateService;

    @Inject
    private EventSource eventSource;

    @Inject
    private AggregateClassProvider aggregateClassProvider;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private AggregateSnapshotGenerationCommandConfiguration aggregateSnapshotGenerationCommandConfiguration;

    @Inject
    private Logger logger;

    public void runAggregateSnapshotRegeneration(final UUID streamId, final String aggregateClassName) {

        try {
            final int transactionTimoutSeconds = aggregateSnapshotGenerationCommandConfiguration.getTransactionTimoutSeconds();
            userTransaction.setTransactionTimeout(transactionTimoutSeconds);
            userTransaction.begin();

            logger.info(format("Hydrating aggregate '%s' for streamId '%s'", aggregateClassName, streamId));

            final Class<? extends Aggregate> aggregateClass = aggregateClassProvider.toClass(aggregateClassName);
            final EventStream eventStream = eventSource.getStreamById(streamId);
            final Aggregate aggregate = snapshotAwareAggregateService.get(eventStream, aggregateClass);
            // to save, let's append
            eventStream.append(EMPTY_JSON_ENVELOPE_STREAM_TO_FORCE_SNAPSHOT_GENERATION);

            logger.info(format("'%s' hydrated with all events for streamId '%s'", aggregate.getClass().getName(), streamId));

            userTransaction.commit();

        } catch (final Exception e) {
            try {
                userTransaction.rollback();
            } catch (final SystemException ignored) {}

            throw new AggregateSnapshotGenerationFailedException(format("Snapshot generation failed for '%s': streamId '%s'", aggregateClassName, streamId), e);
        }
    }

}
