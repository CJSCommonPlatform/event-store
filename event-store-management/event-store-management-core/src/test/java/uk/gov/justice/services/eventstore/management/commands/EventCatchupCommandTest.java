package uk.gov.justice.services.eventstore.management.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupCommandTest {

    @InjectMocks
    private EventCatchupCommand eventCatchupCommand;

    @Test
    public void shouldBeEventCatchupCommand() throws Exception {

        assertThat(eventCatchupCommand.isEventCatchup(), is(true));
    }
}
