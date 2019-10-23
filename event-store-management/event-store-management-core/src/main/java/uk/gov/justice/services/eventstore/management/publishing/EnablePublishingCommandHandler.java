package uk.gov.justice.services.eventstore.management.publishing;

import static uk.gov.justice.services.jmx.api.command.DisablePublishingCommand.DISABLE_PUBLISHING;
import static uk.gov.justice.services.jmx.api.command.EnablePublishingCommand.ENABLE_PUBLISHING;

import uk.gov.justice.services.jmx.api.command.DisablePublishingCommand;
import uk.gov.justice.services.jmx.api.command.EnablePublishingCommand;
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
    public void enablePublishing(final EnablePublishingCommand enablePublishingCommand, final UUID commandId) {
        enablePublishingProcessor.enableDisable(enablePublishingCommand, commandId);
    }

    @Interceptors(MdcLoggerInterceptor.class)
    @HandlesSystemCommand(DISABLE_PUBLISHING)
    public void disablePublishing(final DisablePublishingCommand disablePublishingCommand, final UUID commandId) {
        enablePublishingProcessor.enableDisable(disablePublishingCommand, commandId);
    }
}
