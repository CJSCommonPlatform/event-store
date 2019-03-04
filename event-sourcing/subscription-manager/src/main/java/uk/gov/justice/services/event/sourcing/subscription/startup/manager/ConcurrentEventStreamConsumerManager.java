package uk.gov.justice.services.event.sourcing.subscription.startup.manager;

import static java.lang.String.format;

import uk.gov.justice.services.event.sourcing.subscription.startup.EventCatchupException;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.EventStreamConsumptionResolver;
import uk.gov.justice.services.event.sourcing.subscription.startup.listener.FinishedProcessingMessage;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueTask;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueTaskFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.enterprise.concurrent.ManagedExecutorService;

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
    private final ConcurrentLinkedQueue<Queue<JsonEnvelope>> eventStreamsInProgress = new ConcurrentLinkedQueue<>();

    private final ManagedExecutorService managedExecutorService;
    private final ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory;

    public ConcurrentEventStreamConsumerManager(final ManagedExecutorService managedExecutorService,
                                                final ConsumeEventQueueTaskFactory consumeEventQueueTaskFactory) {
        this.managedExecutorService = managedExecutorService;
        this.consumeEventQueueTaskFactory = consumeEventQueueTaskFactory;
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
     */
    @Override
    public void add(final JsonEnvelope event) {

        final UUID streamId = event.metadata()
                .streamId()
                .orElseThrow(() -> missingStreamIdException(event.metadata().id()));

        final Queue<JsonEnvelope> events = allEventStreams.computeIfAbsent(streamId, id -> new ConcurrentLinkedQueue<>());

        synchronized (EXCLUSIVE_LOCK) {
            events.offer(event);

            if (notInProgress(events)) {
                createAndSubmitTaskFor(events);
            }
        }
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

            if(finishedProcessingMessageQueueEmpty) {
                eventStreamsInProgress.remove(finishedProcessingMessageQueue);

            }

            return finishedProcessingMessageQueueEmpty;
        }
    }

    private EventCatchupException missingStreamIdException(final UUID eventId) {
        return new EventCatchupException(format("Event with id '%s' has no streamId", eventId));
    }

    private boolean notInProgress(final Queue<JsonEnvelope> events) {
        return !eventStreamsInProgress.contains(events);
    }

    private void createAndSubmitTaskFor(final Queue<JsonEnvelope> events) {

        eventStreamsInProgress.add(events);

        final ConsumeEventQueueTask consumeEventQueueTask = consumeEventQueueTaskFactory.createWith(events, this);

        managedExecutorService.submit(consumeEventQueueTask);
    }
}
