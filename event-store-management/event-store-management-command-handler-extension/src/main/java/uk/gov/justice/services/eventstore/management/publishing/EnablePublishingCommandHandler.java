package uk.gov.justice.services.eventstore.management.publishing;

import static uk.gov.justice.services.eventstore.management.commands.DisablePublishingCommand.DISABLE_PUBLISHING;
import static uk.gov.justice.services.eventstore.management.commands.EnablePublishingCommand.ENABLE_PUBLISHING;

import uk.gov.justice.services.eventstore.management.commands.DisablePublishingCommand;
import uk.gov.justice.services.eventstore.management.commands.EnablePublishingCommand;
import uk.gov.justice.services.jmx.api.parameters.JmxCommandRuntimeParameters;
import uk.gov.justice.services.jmx.command.HandlesSystemCommand;
import uk.gov.justice.services.jmx.logging.MdcLoggerInterceptor;

import java.util.UUID;

import javax.inject.Inject;
import javax.interceptor.Interceptors;

public class EnablePublishingCommandHandler {

    @Inject
    private EnablePublishingProcessor enablePublishingProcessor;

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(ENABLE_PUBLISHING)
    public void enablePublishing(
            final EnablePublishingCommand enablePublishingCommand,
            final UUID commandId,
            @SuppressWarnings("unused")
            final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) {
        enablePublishingProcessor.enableDisable(enablePublishingCommand, commandId);
    }

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(DISABLE_PUBLISHING)
    public void disablePublishing(
            final DisablePublishingCommand disablePublishingCommand,
            final UUID commandId,
            @SuppressWarnings("unused")
            final JmxCommandRuntimeParameters jmxCommandRuntimeParameters) {
        enablePublishingProcessor.enableDisable(disablePublishingCommand, commandId);
    }
}
