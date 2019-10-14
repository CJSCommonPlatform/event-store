package uk.gov.justice.services.eventstore.management.untrigger.commands;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventstore.management.untrigger.process.AddRemoveTriggerProcessRunner;
import uk.gov.justice.services.jmx.api.command.AddTriggerCommand;
import uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class AddRemoveTriggerCommandHandlerTest {

    @Mock
    private AddRemoveTriggerProcessRunner addRemoveTriggerProcessRunner;

    @Mock
    private Logger logger;

    @InjectMocks
    private AddRemoveTriggerCommandHandler addRemoveTriggerCommandHandler;

    @Test
    public void shouldCallTheAddEventLogTriggerProcess() throws Exception {

        final UUID commandId = randomUUID();
        final AddTriggerCommand addTriggerCommand = new AddTriggerCommand();

        addRemoveTriggerCommandHandler.addTriggerToEventLogTable(addTriggerCommand, commandId);

        verify(logger).info("Received command ADD_TRIGGER");
        verify(addRemoveTriggerProcessRunner).addTriggerToEventLogTable(commandId, addTriggerCommand);
    }

    @Test
    public void shouldCallTheRemoveEventLogTriggerProcess() throws Exception {

        final UUID commandId = randomUUID();
        final RemoveTriggerCommand removeTriggerCommand = new RemoveTriggerCommand();

        addRemoveTriggerCommandHandler.removeTriggerFromEventLogTable(removeTriggerCommand, commandId);

        verify(logger).info("Received command REMOVE_TRIGGER");
        verify(addRemoveTriggerProcessRunner).removeTriggerFromEventLogTable(commandId, removeTriggerCommand);
    }
}
