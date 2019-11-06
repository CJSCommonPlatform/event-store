package uk.gov.justice.services.eventstore.management.commands;

import uk.gov.justice.services.jmx.api.command.BaseSystemCommand;

public class DisablePublishingCommand extends BaseSystemCommand implements PublishingCommand {

    public static final String DISABLE_PUBLISHING = "DISABLE_PUBLISHING";
    public static final String DESCRIPTION = "Disables the publishing of any newly received events";

    public DisablePublishingCommand() {
        super(DISABLE_PUBLISHING, DESCRIPTION);
    }
}
