package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TallyCounterTest {

    @InjectMocks
    private TallyCounter tallyCounter;

    @Test
    public void shouldStartWithTallyOfZeroAndIncrementByOne() throws Exception {

        assertThat(tallyCounter.getTallyCount(), is(0));

        tallyCounter.incrementByOne();;
        assertThat(tallyCounter.getTallyCount(), is(1));
        tallyCounter.incrementByOne();;
        assertThat(tallyCounter.getTallyCount(), is(2));
        tallyCounter.incrementByOne();;
        assertThat(tallyCounter.getTallyCount(), is(3));
        tallyCounter.incrementByOne();;
        assertThat(tallyCounter.getTallyCount(), is(4));
        tallyCounter.incrementByOne();;
        assertThat(tallyCounter.getTallyCount(), is(5));
    }
}