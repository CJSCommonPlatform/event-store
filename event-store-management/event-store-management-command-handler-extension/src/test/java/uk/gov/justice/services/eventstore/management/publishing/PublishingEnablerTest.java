package uk.gov.justice.services.eventstore.management.publishing;

import static org.mockito.Mockito.inOrder;

import uk.gov.justice.services.eventsourcing.publishedevent.prepublish.PrePublisherTimerConfig;
import uk.gov.justice.services.eventsourcing.publishedevent.publishing.PublisherTimerConfig;
import uk.gov.justice.services.eventstore.management.commands.DisablePublishingCommand;
import uk.gov.justice.services.eventstore.management.commands.EnablePublishingCommand;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class PublishingEnablerTest {

    @Mock
    private PublisherTimerConfig publishTimerConfig;

    @Mock
    private PrePublisherTimerConfig prePublisherTimerConfig;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishingEnabler publishingEnabler;

    @Test
    public void shouldEnablePublishingIfThePublishingCommandIsEnablePublishing() throws Exception {

        final EnablePublishingCommand enablePublishingCommand = new EnablePublishingCommand();

        publishingEnabler.enableOrDisable(enablePublishingCommand);

        final InOrder inOrder = inOrder(prePublisherTimerConfig, publishTimerConfig, logger);

        inOrder.verify(prePublisherTimerConfig).setDisabled(false);
        inOrder.verify(publishTimerConfig).setDisabled(false);
        inOrder.verify(logger).info("Publishing of events enabled");
    }

    @Test
    public void shouldDisablePublishingIfThePublishingCommandIsDisablePublishing() throws Exception {

        final DisablePublishingCommand disablePublishingCommand = new DisablePublishingCommand();

        publishingEnabler.enableOrDisable(disablePublishingCommand);

        final InOrder inOrder = inOrder(prePublisherTimerConfig, publishTimerConfig, logger);

        inOrder.verify(prePublisherTimerConfig).setDisabled(true);
        inOrder.verify(publishTimerConfig).setDisabled(true);
        inOrder.verify(logger).info("Publishing of events disabled");
    }
}
