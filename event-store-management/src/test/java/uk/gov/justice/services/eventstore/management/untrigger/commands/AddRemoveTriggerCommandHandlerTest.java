package uk.gov.justice.services.eventstore.management.untrigger.commands;

import static java.lang.String.format;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import uk.gov.justice.services.eventstore.management.untrigger.process.EventLogTriggerManipulator;
import uk.gov.justice.services.jmx.api.command.AddTriggerCommand;
import uk.gov.justice.services.jmx.api.command.RemoveTriggerCommand;

import javax.inject.Inject;

@RunWith(MockitoJUnitRunner.class)
public class AddRemoveTriggerCommandHandlerTest {

    @Mock
    private EventLogTriggerManipulator eventLogTriggerManipulator;

    @Mock
    private Logger logger;

    @InjectMocks
    private AddRemoveTriggerCommandHandler addRemoveTriggerCommandHandler;

    @Test
    public void shouldCallTheAddEventLogTriggerProcess() throws Exception {

        final AddTriggerCommand addTriggerCommand = new AddTriggerCommand();

        addRemoveTriggerCommandHandler.addTriggerToEventLogTable(addTriggerCommand);

        verify(logger).info("Received command ADD_TRIGGER");
        verify(eventLogTriggerManipulator).addTriggerToEventLogTable();
    }

    @Test
    public void shouldCallTheRemoveEventLogTriggerProcess() throws Exception {

        final RemoveTriggerCommand removeTriggerCommand = new RemoveTriggerCommand();

        addRemoveTriggerCommandHandler.removeTriggerFromEventLogTable(removeTriggerCommand);

        verify(logger).info("Received command REMOVE_TRIGGER");
        verify(eventLogTriggerManipulator).removeTriggerFromEventLogTable();
    }
}
