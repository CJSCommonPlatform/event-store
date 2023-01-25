package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDestinationNamesProviderTest {

    @Mock
    private JndiContextNameProvider jndiContextNameProvider;

    @InjectMocks
    private DefaultDestinationNamesProvider destinationNamesProvider;

    @Test
    public void shouldReturnContextSpecificCQRSDestinationNames() {
        given(jndiContextNameProvider.getContextName()).willReturn("people");

        assertThat(destinationNamesProvider.getDestinationNames(), hasItems("people.controller.command",
                "people.handler.command",
                "people.event"));
    }

    @Test
    public void shouldReturnContextNae() {
        given(jndiContextNameProvider.getContextName()).willReturn("people");

        assertThat(destinationNamesProvider.getContextName(), is("people"));

    }
}