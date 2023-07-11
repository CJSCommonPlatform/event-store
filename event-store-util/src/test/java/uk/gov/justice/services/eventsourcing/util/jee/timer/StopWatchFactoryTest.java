package uk.gov.justice.services.eventsourcing.util.jee.timer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

public class StopWatchFactoryTest {

    @Test
    public void shouldReturnNewInstanceOfStopWatch() {

        final StopWatchFactory stopWatchFactory = new StopWatchFactory();

        final StopWatch stopWatch_1 = stopWatchFactory.createStopWatch();
        final StopWatch stopWatch_2 = stopWatchFactory.createStopWatch();

        assertThat(stopWatch_1, is(notNullValue()));
        assertThat(stopWatch_2, is(notNullValue()));
        assertThat(stopWatch_1, is(not(sameInstance(stopWatch_2))));
    }

    @Test
    public void shouldCreateSartedStopWatch() throws Exception {

        final StopWatchFactory stopWatchFactory = new StopWatchFactory();

        final StopWatch startedStopWatch = stopWatchFactory.createStartedStopWatch();

        assertThat(startedStopWatch.isStarted(), is(true));
    }
}
