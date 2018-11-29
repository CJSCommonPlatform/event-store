package uk.gov.justice.services.event.sourcing.subscription.startup;

import static org.mockito.Mockito.verify;

import uk.gov.justice.services.event.sourcing.subscription.startup.StartSubscriptionTask;
import uk.gov.justice.services.subscription.SubscriptionManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class StartSubscriptionTaskTest {

    @Mock
    private SubscriptionManager subscriptionManager;

    @InjectMocks
    private StartSubscriptionTask startSubscriptionTask;

    @Test
    public void shouldStartSubscription() throws Exception {

        startSubscriptionTask.run();

        verify(subscriptionManager).startSubscription();
    }
}
