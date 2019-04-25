package uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.manager;

import static java.lang.String.format;

import uk.gov.justice.services.event.sourcing.subscription.catchup.EventCatchupException;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.ConsumeEventQueueBean;
import uk.gov.justice.services.event.sourcing.subscription.catchup.consumer.task.EventQueueConsumer;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A concurrent implementation of EventStreamConsumerManager and EventStreamConsumerListener.
 *
 * This uses the ManagedExecutorService for concurrency and Queues events according to the Stream
 * Id.
 *
 * When the add method is called
 */
public class ConcurrentEventStreamConsumerManager implements EventStreamConsumerManager, EventStreamConsumptionResolver {

    private static final Object EXCLUSIVE_LOCK = new Object();

    private final ConcurrentHashMap<UUID, Queue<JsonEnvelope>> allEventStreams = new ConcurrentHashMap<>();

    private final EventStreamsInProgressList eventStreamsInProgressList;
    private final ConsumeEventQueueBean consumeEventQueueBean;
    private final EventQueueConsumerFactory eventQueueConsumerFactory;

    public ConcurrentEventStreamConsumerManager(final EventStreamsInProgressList eventStreamsInProgressList,
                                                final ConsumeEventQueueBean consumeEventQueueBean,
                                                final EventQueueConsumerFactory eventQueueConsumerFactory) {
        this.eventStreamsInProgressList = eventStreamsInProgressList;
        this.consumeEventQueueBean = consumeEventQueueBean;
        this.eventQueueConsumerFactory = eventQueueConsumerFactory;
    }

    /**
     * A ConcurrentLinkedQueue is created for each Stream Id and added to a ConcurrentHashMap.  An
     * event is added to the Queue for a Stream Id.
     *
     * If the Queue is not currently being processed a new ConsumeEventQueueTask is created and
     * submitted to the ManagedExecutorService.  The Queue is then added to the
     * eventStreamsInProgress Queue.
     *
     * If the Queue is currently being processed no further action is taken, as the event will be
     * processed by the current ConsumeEventQueueTask.
     *
     * @param event - the JsonEnvelope to be consumed
     * @return The number of events added to the stream. Note this is always one and is used to
     * count the number of events consumed
     */
    @Override
    public int add(final JsonEnvelope event, final String subscriptionName) {

        final UUID streamId = event.metadata()
                .streamId()
                .orElseThrow(() -> missingStreamIdException(event.metadata().id()));

        final Queue<JsonEnvelope> events = allEventStreams.computeIfAbsent(streamId, id -> new ConcurrentLinkedQueue<>());

        synchronized (EXCLUSIVE_LOCK) {
            events.offer(event);

            if (notInProgress(events)) {
                createAndSubmitTaskFor(events, subscriptionName);
            }
        }

        return 1;
    }

    /**
     * When a ConsumeEventQueueTask has finished consuming an event Queue, the event Queue is
     * removed from the eventStreamsInProgress Queue
     *
     * @param finishedProcessingMessage - the message containing the Queue that has been consumed.
     */
    @Override
    public boolean isEventConsumptionComplete(final FinishedProcessingMessage finishedProcessingMessage) {

        synchronized (EXCLUSIVE_LOCK) {
            final Queue<JsonEnvelope> finishedProcessingMessageQueue = finishedProcessingMessage.getQueue();

            final boolean finishedProcessingMessageQueueEmpty = finishedProcessingMessageQueue.isEmpty();

            if (finishedProcessingMessageQueueEmpty) {
                eventStreamsInProgressList.remove(finishedProcessingMessageQueue);
            }

            return finishedProcessingMessageQueueEmpty;
        }
    }

    @Override
    public void waitForCompletion() {
        eventStreamsInProgressList.blockUntilEmpty();
    }

    private EventCatchupException missingStreamIdException(final UUID eventId) {
        return new EventCatchupException(format("Event with id '%s' has no streamId", eventId));
    }

    private boolean notInProgress(final Queue<JsonEnvelope> eventStream) {
        return !eventStreamsInProgressList.contains(eventStream);
    }

    private void createAndSubmitTaskFor(final Queue<JsonEnvelope> eventStream, final String subscriptionName) {

        eventStreamsInProgressList.add(eventStream);

        final EventQueueConsumer eventQueueConsumer = eventQueueConsumerFactory.create(this);
        consumeEventQueueBean.consume(eventStream, eventQueueConsumer, subscriptionName);
    }
}
