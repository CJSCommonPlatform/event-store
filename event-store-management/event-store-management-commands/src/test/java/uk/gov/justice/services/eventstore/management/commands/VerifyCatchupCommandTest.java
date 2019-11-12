package uk.gov.justice.services.eventstore.management.commands;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class VerifyCatchupCommandTest {

    @Test
    public void shouldBeCatchupVerification() throws Exception {
        assertThat(new VerifyCatchupCommand().isCatchupVerification(), is(true));
    }
}
