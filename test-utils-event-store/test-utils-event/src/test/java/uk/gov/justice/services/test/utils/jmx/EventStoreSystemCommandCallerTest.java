package uk.gov.justice.services.test.utils.jmx;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.commands.AddTriggerCommand.ADD_TRIGGER;
import static uk.gov.justice.services.eventstore.management.commands.EventCatchupCommand.CATCHUP;
import static uk.gov.justice.services.eventstore.management.commands.IndexerCatchupCommand.INDEXER_CATCHUP;
import static uk.gov.justice.services.eventstore.management.commands.RebuildCommand.REBUILD;
import static uk.gov.justice.services.eventstore.management.commands.RemoveTriggerCommand.REMOVE_TRIGGER;
import static uk.gov.justice.services.eventstore.management.commands.ValidatePublishedEventsCommand.VALIDATE_EVENTS;
import static uk.gov.justice.services.eventstore.management.commands.VerifyCatchupCommand.VERIFY_CATCHUP;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jmx.api.mbean.SystemCommanderMBean;
import uk.gov.justice.services.jmx.system.command.client.SystemCommanderClient;
import uk.gov.justice.services.jmx.system.command.client.TestSystemCommanderClientFactory;
import uk.gov.justice.services.jmx.system.command.client.connection.Credentials;
import uk.gov.justice.services.jmx.system.command.client.connection.JmxParameters;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventStoreSystemCommandCallerTest {

    @Mock
    private TestSystemCommanderClientFactory testSystemCommanderClientFactory;

    @Test
    public void shouldCallRebuild() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callRebuild();

        verify(systemCommanderMBean).call(REBUILD);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallCatchup() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callCatchup();

        verify(systemCommanderMBean).call(CATCHUP);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallIndexerCatchup() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callIndexerCatchup();

        verify(systemCommanderMBean).call(INDEXER_CATCHUP);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallAddTrigger() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callAddTrigger();

        verify(systemCommanderMBean).call(ADD_TRIGGER);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallRemoveTrigger() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callRemoveTrigger();

        verify(systemCommanderMBean).call(REMOVE_TRIGGER);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallValidateCatchup() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callValidateCatchup();

        verify(systemCommanderMBean).call(VERIFY_CATCHUP);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCallValidatePublishedEvents() throws Exception {

        final String contextName = "contextName";

        final JmxParameters jmxParameters = mock(JmxParameters.class);
        final SystemCommanderClient systemCommanderClient = mock(SystemCommanderClient.class);
        final SystemCommanderMBean systemCommanderMBean = mock(SystemCommanderMBean.class);

        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(jmxParameters, testSystemCommanderClientFactory);

        when(jmxParameters.getContextName()).thenReturn(contextName);
        when(testSystemCommanderClientFactory.create(jmxParameters)).thenReturn(systemCommanderClient);
        when(systemCommanderClient.getRemote(contextName)).thenReturn(systemCommanderMBean);

        eventStoreSystemCommandCaller.callValidatePublishedEvents();

        verify(systemCommanderMBean).call(VALIDATE_EVENTS);
        verify(systemCommanderClient).close();
    }

    @Test
    public void shouldCreateWithCorrectDefaultParametersIfInstantiatingUsingTheContextName() throws Exception {

        final String contextName = "contextName";
        final EventStoreSystemCommandCaller eventStoreSystemCommandCaller = new EventStoreSystemCommandCaller(contextName);

        final JmxParameters jmxParameters = getValueOfField(eventStoreSystemCommandCaller, "jmxParameters", JmxParameters.class);

        assertThat(jmxParameters.getContextName(), is(contextName));
        assertThat(jmxParameters.getHost(), is(getHost()));
        assertThat(jmxParameters.getPort(), is(9990));

        final Optional<Credentials> credentials = jmxParameters.getCredentials();

        if (credentials.isPresent()) {
            assertThat(credentials.get().getUsername(), is("admin"));
            assertThat(credentials.get().getPassword(), is("admin"));
        } else {
            fail();
        }
    }
}
