package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import org.slf4j.Logger;
import uk.gov.justice.services.healthcheck.api.Healthcheck;
import uk.gov.justice.services.healthcheck.api.HealthcheckResult;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class ArtemisHealthcheck implements Healthcheck {

    @Inject
    private Logger logger;

    @Inject
    private ConnectionFactory connectionFactory;

    @Inject
    private JmsDestinationsVerifier jmsDestinationsVerifier;

    @Override
    public String getHealthcheckName() {
        return "artemis-healthcheck";
    }

    @Override
    public String healthcheckDescription() {
        return "Checks connectivity to the artemis broker and that all required destinations are available";
    }

    @Override
    public HealthcheckResult runHealthcheck() {
            try(final var connection = connectionFactory.createConnection()) {
                connection.start();
                try(final var session = connection.createSession()) {
                    jmsDestinationsVerifier.verify(session);
                    return HealthcheckResult.success();
                }
            }
        catch (DestinationNotFoundException e) {
            this.logger.error("Healthcheck for artemis failed.", e);
            return HealthcheckResult.failure(String.format("Exception thrown while checking existence of destination: %s", e.getMessage()));
        } catch (JMSException e) {
            this.logger.error("Healthcheck for artemis failed.", e);
            return HealthcheckResult.failure(String.format("Exception thrown while accessing artemis broker. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }
}
