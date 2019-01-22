package uk.gov.justice.services.event.sourcing.subscription.startup;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.InjectMocks;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class EventCatchupConfigTest {

    @InjectMocks
    private EventCatchupConfig eventCatchupConfig;

    @Test
    public void shouldReturnTrueIfEventCatchupSetToTrue() throws Exception {

        eventCatchupConfig.eventCatchupEnabled = "true";

        assertThat(eventCatchupConfig.isEventCatchupEnabled(), is(true));
    }

    @Test
    public void shouldReturnFalseIfEventCatchupSetToFalse() throws Exception {

        eventCatchupConfig.eventCatchupEnabled = "false";

        assertThat(eventCatchupConfig.isEventCatchupEnabled(), is(false));
    }

    @Test
    public void shouldReturnFalseIfEventCatchupSetToSometingRandom() throws Exception {

        eventCatchupConfig.eventCatchupEnabled = "something silly";

        assertThat(eventCatchupConfig.isEventCatchupEnabled(), is(false));
    }

    @Test
    public void shouldBeCaseInsensitive() throws Exception {

        eventCatchupConfig.eventCatchupEnabled = "TrUe";

        assertThat(eventCatchupConfig.isEventCatchupEnabled(), is(true));
    }

    @Test
    public void shouldBeFalseIfNull() throws Exception {

        eventCatchupConfig.eventCatchupEnabled = null;

        assertThat(eventCatchupConfig.isEventCatchupEnabled(), is(false));
    }
}
