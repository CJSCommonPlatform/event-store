package uk.gov.justice.services.eventsourcing.publishedevent.publishing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublisherTimerConfigTest {

    @InjectMocks
    private PublisherTimerConfig publisherTimerConfig;

    @Test
    public void shouldGetTheStartWaitTime() throws Exception {

        final long milliseconds = 982374L;

        publisherTimerConfig.timerStartWaitMilliseconds = "" + milliseconds;

        assertThat(publisherTimerConfig.getTimerStartWaitMilliseconds(), is(milliseconds));
    }

    @Test
    public void shouldGetTheTimerInterval() throws Exception {

        final long milliseconds = 2998734L;

        publisherTimerConfig.timerIntervalMilliseconds = "" + milliseconds;

        assertThat(publisherTimerConfig.getTimerIntervalMilliseconds(), is(milliseconds));
    }

    @Test
    public void shouldGetTheTimerMaxRuntime() throws Exception {

        final long milliseconds = 600L;

        publisherTimerConfig.timerMaxRuntimeMilliseconds = "" + milliseconds;

        assertThat(publisherTimerConfig.getTimerMaxRuntimeMilliseconds(), is(milliseconds));
    }
}
