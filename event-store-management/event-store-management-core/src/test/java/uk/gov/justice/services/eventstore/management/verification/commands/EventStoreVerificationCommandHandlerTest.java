package uk.gov.justice.services.eventstore.management.verification.commands;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import uk.gov.justice.services.eventstore.management.commands.VerifyCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.VerifyRebuildCommand;
import uk.gov.justice.services.eventstore.management.verification.process.EventStoreVerification;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStoreVerificationCommandHandlerTest {

    @Mock
    private EventStoreVerification eventStoreVerification;

    @InjectMocks
    private EventStoreVerificationCommandHandler eventStoreVerificationCommandHandler;

    @Test
    public void shouldRunVerificationForRebuild() throws Exception {

        final UUID commandId = randomUUID();
        final VerifyRebuildCommand verifyRebuildCommand = new VerifyRebuildCommand();

        eventStoreVerificationCommandHandler.verifyRebuild(verifyRebuildCommand, commandId);

        verify(eventStoreVerification).verifyEventStore(commandId, verifyRebuildCommand);
    }

    @Test
    public void shouldRunVerificationForCatchup() throws Exception {

        final UUID commandId = randomUUID();
        final VerifyCatchupCommand verifyCatchupCommand = new VerifyCatchupCommand();

        eventStoreVerificationCommandHandler.verifyCatchup(verifyCatchupCommand, commandId);

        verify(eventStoreVerification).verifyEventStore(commandId, verifyCatchupCommand);
    }
}
