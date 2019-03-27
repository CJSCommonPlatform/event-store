package uk.gov.justice.services.event.sourcing.subscription.startup.util;

import uk.gov.justice.services.event.sourcing.subscription.startup.manager.ConcurrentEventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamConsumerManager;
import uk.gov.justice.services.event.sourcing.subscription.startup.manager.EventStreamsInProgressList;
import uk.gov.justice.services.event.sourcing.subscription.startup.task.ConsumeEventQueueTaskFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

@Singleton
public class TestCatchupBean {

    @Resource
    ManagedExecutorService managedExecutorService;

    @Inject
    DummyTransactionalEventProcessor transactionalEventProcessor;

    @Inject
    EventStreamsInProgressList eventStreamsInProgressList;

    @Inject
    Logger logger;

    public void run(final StopWatch stopWatch) {
        logger.info("running!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        final int numberOfStreams = 10;
        final int numberOfEventsToCreate = 600;
        final int numberOfUniqueEventNames = 10;

        transactionalEventProcessor.setExpectedNumberOfEvents(numberOfEventsToCreate);

        final EventFactory eventFactory = new EventFactory(numberOfStreams, numberOfUniqueEventNames);
        final Stream<JsonEnvelope> eventStream = eventFactory.generateEvents(numberOfEventsToCreate).stream();

        final EventStreamConsumerManager eventStreamConsumerManager = new ConcurrentEventStreamConsumerManager(
                managedExecutorService,
                new ConsumeEventQueueTaskFactory(transactionalEventProcessor),
                eventStreamsInProgressList);

        stopWatch.start();
        final int totalEventsProcessed = eventStream.mapToInt(event -> {
            eventStreamConsumerManager.add(event);
            return 1;
        }).sum();


        eventStreamConsumerManager.waitForCompletion();

        logger.info("Total events processed: " + totalEventsProcessed);
    }
}
