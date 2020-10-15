package uk.gov.justice.services.eventstore.management.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexerCatchupCommandTest {

    @InjectMocks
    private IndexerCatchupCommand indexerCatchupCommand;

    @Test
    public void shouldBeIndexerCatchupCommand() throws Exception {

        assertThat(indexerCatchupCommand.isEventCatchup(), is(false));
    }
}
