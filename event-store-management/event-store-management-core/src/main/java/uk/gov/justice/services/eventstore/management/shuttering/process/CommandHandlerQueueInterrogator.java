package uk.gov.justice.services.eventstore.management.shuttering.process;

import uk.gov.justice.services.common.polling.MultiIteratingPoller;
import uk.gov.justice.services.common.polling.MultiIteratingPollerFactory;

import javax.inject.Inject;

public class CommandHandlerQueueInterrogator {

    @Inject
    private CommandHandlerQueueSupplierFactory commandHandlerQueueSupplierFactory;

    @Inject
    private MultiIteratingPollerFactory multiIteratingPollerFactory;

    public boolean pollUntilEmptyHandlerQueue() {

        final MultiIteratingPoller multiIteratingPoller = multiIteratingPollerFactory.create(5, 1000L, 3, 500L);

        return multiIteratingPoller.pollUntilTrue(commandHandlerQueueSupplierFactory.isCommandHandlerQueueEmpty());
    }
}
