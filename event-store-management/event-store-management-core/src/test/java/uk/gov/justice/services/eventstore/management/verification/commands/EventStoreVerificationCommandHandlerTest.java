package uk.gov.justice.services.eventstore.management.verification.commands;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters.withNoCommandParameters;

import uk.gov.justice.services.eventstore.management.commands.VerifyCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.VerifyRebuildCommand;
import uk.gov.justice.services.eventstore.management.verification.process.EventStoreVerification;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EventStoreVerificationCommandHandlerTest {

    @Mock
    private EventStoreVerification eventStoreVerification;

    @InjectMocks
    private EventStoreVerificationCommandHandler eventStoreVerificationCommandHandler;

    @Test
    public void shouldRunVerificationForRebuild() throws Exception {

        final UUID commandId = randomUUID();
        final VerifyRebuildCommand verifyRebuildCommand = new VerifyRebuildCommand();

        eventStoreVerificationCommandHandler.verifyRebuild(verifyRebuildCommand, commandId, withNoCommandParameters());

        verify(eventStoreVerification).verifyEventStore(commandId, verifyRebuildCommand);
    }

    @Test
    public void shouldRunVerificationForCatchup() throws Exception {

        final UUID commandId = randomUUID();
        final VerifyCatchupCommand verifyCatchupCommand = new VerifyCatchupCommand();

        eventStoreVerificationCommandHandler.verifyCatchup(verifyCatchupCommand, commandId, withNoCommandParameters());

        verify(eventStoreVerification).verifyEventStore(commandId, verifyCatchupCommand);
    }
}
