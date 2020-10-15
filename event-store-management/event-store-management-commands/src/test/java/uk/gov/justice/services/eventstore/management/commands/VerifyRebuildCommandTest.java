package uk.gov.justice.services.eventstore.management.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class VerifyRebuildCommandTest {

    @Test
    public void shouldNotBeCatchupVerification() throws Exception {
        assertThat(new VerifyRebuildCommand().isCatchupVerification(), is(false));
    }
}
