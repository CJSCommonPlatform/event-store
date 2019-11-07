package uk.gov.justice.services.test.utils.jmx;

import static uk.gov.justice.services.jmx.system.command.client.connection.JmxParametersBuilder.jmxParameters;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.eventstore.management.commands.AddTriggerCommand;
import uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.RebuildCommand;
import uk.gov.justice.services.eventstore.management.commands.RemoveTriggerCommand;
import uk.gov.justice.services.eventstore.management.commands.ValidatePublishedEventsCommand;
import uk.gov.justice.services.eventstore.management.commands.VerifyCatchupCommand;
import uk.gov.justice.services.jmx.api.command.SystemCommand;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClient;
import uk.gov.justice.services.jmx.system.command.client.TestSystemCommanderClientFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import com.google.common.annotations.VisibleForTesting;

public class EventStoreSystemCommandCaller {

    private static final String HOST = getHost();
    private static final int JMX_PORT = 9990;
    private static final String USERNAME = "admin";

    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD = "admin";


    private final TestSystemCommanderClientFactory testSystemCommanderClientFactory;
    private final JmxParameters jmxParameters;

    public EventStoreSystemCommandCaller(final String contextName) {
        this(jmxParameters()
                .withContextName(contextName)
                .withHost(HOST)
                .withPort(JMX_PORT)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .build());
    }

    public EventStoreSystemCommandCaller(final JmxParameters jmxParameters) {
        this(jmxParameters, new TestSystemCommanderClientFactory());
    }

    @VisibleForTesting
    EventStoreSystemCommandCaller(final JmxParameters jmxParameters, final TestSystemCommanderClientFactory testSystemCommanderClientFactory) {
        this.jmxParameters = jmxParameters;
        this.testSystemCommanderClientFactory = testSystemCommanderClientFactory;
    }

    public void callRebuild() {
        callSystemCommand(new RebuildCommand());
    }

    public void callCatchup() {
        callSystemCommand(new EventCatchupCommand());
    }

    public void callIndexerCatchup() {
        callSystemCommand(new IndexerCatchupCommand());
    }

    public void callAddTrigger() {
        callSystemCommand(new AddTriggerCommand());
    }

    public void callRemoveTrigger() {
        callSystemCommand(new RemoveTriggerCommand());
    }

    public void callValidateCatchup() {
        callSystemCommand(new VerifyCatchupCommand());
    }

    public void callValidatePublishedEvents() {
        callSystemCommand(new ValidatePublishedEventsCommand());
    }

    private void callSystemCommand(final SystemCommand systemCommand) {
        try (final SystemCommanderClient systemCommanderClient = testSystemCommanderClientFactory.create(jmxParameters)) {
            systemCommanderClient.getRemote(jmxParameters.getContextName()).call(systemCommand.getName());
        }
    }
}
