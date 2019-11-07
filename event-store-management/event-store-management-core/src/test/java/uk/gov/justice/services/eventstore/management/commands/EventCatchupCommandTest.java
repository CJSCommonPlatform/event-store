package uk.gov.justice.services.eventstore.management.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventCatchupCommandTest {

    @InjectMocks
    private EventCatchupCommand eventCatchupCommand;

    @Test
    public void shouldBeEventCatchupCommand() throws Exception {

        assertThat(eventCatchupCommand.isEventCatchup(), is(true));
    }
}
