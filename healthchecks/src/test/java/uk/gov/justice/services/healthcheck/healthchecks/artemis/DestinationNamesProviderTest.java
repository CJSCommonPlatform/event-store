package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DestinationNamesProviderTest {

    @Mock
    private JndiContextNameProvider jndiContextNameProvider;

    @InjectMocks
    private DestinationNamesProvider destinationNamesProvider;

    @Test
    public void shouldReturnContextSpecificCQRSDestinationNames() {
        given(jndiContextNameProvider.getContextName()).willReturn("people");

        assertThat(destinationNamesProvider.getDestinationNames(), hasItems("people.controller.command",
                "people.handler.command",
                "people.event"));

    }
}