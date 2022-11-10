package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import javax.jms.*;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArtemisHealthcheckTest {

    @Mock
    private JmsDestinationsVerifier jmsDestinationsVerifier;

    @Mock
    private Logger logger;

    @Mock
    private ConnectionFactory connectionFactory;

    @InjectMocks
    private ArtemisHealthcheck artemisHealthcheck;

    @Test
    public void shouldReturnSuccessWhenDestinationsVerifiedSuccessfully() throws Exception {
        var connection = mock(Connection.class);
        var session = mock(Session.class);
        given(connectionFactory.createConnection()).willReturn(connection);
        given(connection.createSession()).willReturn(session);
        doNothing().when(jmsDestinationsVerifier).verify(session);

        var healthcheckResult = artemisHealthcheck.runHealthcheck();

        assertTrue(healthcheckResult.isPassed());
        verify(jmsDestinationsVerifier).verify(session);
        verify(connection).start();
        verify(session).close();
        verify(connection).close();
    }

    @Test
    public void shouldReturnFailedWhenErrorOccursWhileCreatingActiveMQConnection() throws Exception {
        var ex = new JMSException("Ex message");
        var connection = mock(Connection.class);
        given(connectionFactory.createConnection()).willReturn(connection);
        doThrow(ex).when(connection).start();

        var healthcheckResult = artemisHealthcheck.runHealthcheck();

        assertFalse(healthcheckResult.isPassed());
        assertThat(healthcheckResult.getErrorMessage().get(), is("Exception thrown while accessing artemis broker. javax.jms.JMSException: Ex message"));
        verify(logger).error("Healthcheck for artemis failed.", ex);
    }

    @Test
    public void shouldReturnFailedWhenErrorOccursWhileCheckingExistenceOfDestination() throws Exception {
        var connection = mock(Connection.class);
        var session = mock(Session.class);
        given(connectionFactory.createConnection()).willReturn(connection);
        given(connection.createSession()).willReturn(session);
        var ex = new DestinationNotFoundException(List.of("d-1", "d-2"), new InvalidDestinationException("Ex message"));
        doThrow(ex).when(jmsDestinationsVerifier).verify(session);

        var healthcheckResult = artemisHealthcheck.runHealthcheck();

        assertFalse(healthcheckResult.isPassed());
        assertThat(healthcheckResult.getErrorMessage().get(), is("Exception thrown while checking existence of destination(s): d-1, d-2"));
        verify(logger).error("Healthcheck for artemis failed.", ex);
    }

    @Test
    public void shouldReturnValidHealthCheckName() {
        assertThat(artemisHealthcheck.getHealthcheckName(), is("artemis-healthcheck"));
    }

    @Test
    public void shouldReturnValidHealthcheckDescription() {
        assertThat(artemisHealthcheck.healthcheckDescription(), is("Checks connectivity to the artemis broker and that all required destinations are available"));
    }
}

