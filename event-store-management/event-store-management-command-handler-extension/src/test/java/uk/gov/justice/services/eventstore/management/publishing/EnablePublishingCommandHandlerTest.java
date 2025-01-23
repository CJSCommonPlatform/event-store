package uk.gov.justice.services.eventstore.management.publishing;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.withNoCommandParameters;

import uk.gov.justice.services.eventstore.management.commands.DisablePublishingCommand;
import uk.gov.justice.services.eventstore.management.commands.EnablePublishingCommand;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class EnablePublishingCommandHandlerTest {

    @Mock
    private EnablePublishingProcessor enablePublishingProcessor;

    @InjectMocks
    private EnablePublishingCommandHandler enablePublishingCommandHandler;

    @Test
    public void shouldEnablePublishing() throws Exception {

        final EnablePublishingCommand enablePublishingCommand = new EnablePublishingCommand();
        final UUID commandId = randomUUID();

        enablePublishingCommandHandler.enablePublishing(enablePublishingCommand, commandId, withNoCommandParameters());

        verify(enablePublishingProcessor).enableDisable(enablePublishingCommand, commandId);
    }

    @Test
    public void shouldDisablePublishing() throws Exception {

        final DisablePublishingCommand disablePublishingCommand = new DisablePublishingCommand();
        final UUID commandId = randomUUID();

        enablePublishingCommandHandler.disablePublishing(disablePublishingCommand, commandId, withNoCommandParameters());

        verify(enablePublishingProcessor).enableDisable(disablePublishingCommand, commandId);
    }
}
