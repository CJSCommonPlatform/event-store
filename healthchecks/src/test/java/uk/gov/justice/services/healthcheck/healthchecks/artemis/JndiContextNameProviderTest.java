package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;


public class JndiContextNameProviderTest {



    @Test
    public void shouldReturnContextNameWhenAppNameContainsVersionInformation() throws Exception {
        var jndiContextNameProvider = new JndiContextNameProvider();
        setAppName(jndiContextNameProvider, "stagingdarts-11.0.8-FRAMEWORK-SNAPSHOT");

        assertThat(jndiContextNameProvider.getContextName(), is("stagingdarts"));
    }

    @Test
    public void shouldReturnContextNameWhenAppNameDoesNotContainVersionInformation() throws Exception {
        var jndiContextNameProvider = new JndiContextNameProvider();
        setAppName(jndiContextNameProvider, "people");

        assertThat(jndiContextNameProvider.getContextName(), is("people"));
    }

    private void setAppName(JndiContextNameProvider jndiContextNameProvider, String appName) throws Exception {
        var appNameField = JndiContextNameProvider.class.getDeclaredField("appName");
        appNameField.setAccessible(true);
        appNameField.set(jndiContextNameProvider, appName);
    }
}