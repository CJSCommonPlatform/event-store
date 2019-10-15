package uk.gov.justice.services.eventstore.management.rebuild.commands;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventstore.management.rebuild.process.RebuildProcessRunner;
import uk.gov.justice.services.jmx.api.command.RebuildCommand;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RebuildCommandHandlerTest {

    @Mock
    private RebuildProcessRunner rebuildProcessRunner;

    @InjectMocks
    private RebuildCommandHandler rebuildCommandHandler;

    @Test
    public void shouldFireRebuildEvent() throws Exception {

        final UUID commandId = randomUUID();
        final RebuildCommand rebuildCommand = new RebuildCommand();

        rebuildCommandHandler.doRebuild(rebuildCommand, commandId);

        verify(rebuildProcessRunner).runRebuild(commandId, rebuildCommand);
    }
}
