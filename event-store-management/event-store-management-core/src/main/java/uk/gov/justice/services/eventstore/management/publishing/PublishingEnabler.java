package uk.gov.justice.services.eventstore.management.publishing;

import static uk.gov.justice.services.jmx.api.command.EnablePublishingCommand.ENABLE_PUBLISHING;

import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.PrePublisherTimerConfig;
import uk.gov.justice.services.eventsourcing.publishedevent.publishing.PublisherTimerConfig;
import uk.gov.justice.services.jmx.api.command.PublishingCommand;

import javax.inject.Inject;

import org.slf4j.Logger;

public class PublishingEnabler {

    @Inject
    private PublisherTimerConfig publishTimerConfig;

    @Inject
    private PrePublisherTimerConfig prePublisherTimerConfig;

    @Inject
    private Logger logger;

    public void enableOrDisable(final PublishingCommand publishingCommand) {

        if (publishingCommand.getName().equals(ENABLE_PUBLISHING)) {
            enable();
        } else {
            disable();
        }
    }

    private void enable() {
        prePublisherTimerConfig.setDisabled(false);
        publishTimerConfig.setDisabled(false);

        logger.info("Publishing of events enabled");
    }

    private void disable() {
        prePublisherTimerConfig.setDisabled(true);
        publishTimerConfig.setDisabled(true);

        logger.info("Publishing of events disabled");
    }
}
