package uk.gov.justice.services.eventstore.management.verification.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import uk.gov.justice.services.eventstore.management.commands.VerifyCatchupCommand;
import uk.gov.justice.services.eventstore.management.commands.VerifyRebuildCommand;
import uk.gov.justice.services.eventstore.management.verification.process.verifiers.ProcessedEventCountVerifier;
import uk.gov.justice.services.eventstore.management.verification.process.verifiers.ProcessedEventLinkVerifier;
import uk.gov.justice.services.eventstore.management.verification.process.verifiers.PublishedEventCountVerifier;
import uk.gov.justice.services.eventstore.management.verification.process.verifiers.PublishedEventLinkVerifier;
import uk.gov.justice.services.eventstore.management.verification.process.verifiers.StreamBufferEmptyVerifier;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VerifierProviderTest {

    @Mock
    private AllEventsInStreamsVerifier allEventsInStreamsVerifier;

    @Mock
    private ProcessedEventCountVerifier processedEventCountVerifier;

    @Mock
    private ProcessedEventLinkVerifier processedEventLinkVerifier;

    @Mock
    private PublishedEventCountVerifier publishedEventCountVerifier;

    @Mock
    private PublishedEventLinkVerifier publishedEventLinkVerifier;

    @Mock
    private StreamBufferEmptyVerifier streamBufferEmptyVerifier;

    @InjectMocks
    private VerifierProvider verifierProvider;

    @Test
    public void shouldGetTheListOfAllCatchupVerifiersInTheCorrectOrder() throws Exception {

        final List<Verifier> verifiers = verifierProvider.getVerifiers(new VerifyCatchupCommand());

        assertThat(verifiers.size(), is(6));

        assertThat(verifiers.get(0), is(streamBufferEmptyVerifier));
        assertThat(verifiers.get(1), is(publishedEventCountVerifier));
        assertThat(verifiers.get(2), is(processedEventCountVerifier));
        assertThat(verifiers.get(3), is(publishedEventLinkVerifier));
        assertThat(verifiers.get(4), is(processedEventLinkVerifier));
        assertThat(verifiers.get(5), is(allEventsInStreamsVerifier));
    }

    @Test
    public void shouldGetTheListOfAllRebuildVerifiersInTheCorrectOrder() throws Exception {

        final List<Verifier> verifiers = verifierProvider.getVerifiers(new VerifyRebuildCommand());

        assertThat(verifiers.size(), is(3));

        assertThat(verifiers.get(0), is(publishedEventCountVerifier));
        assertThat(verifiers.get(1), is(publishedEventLinkVerifier));
        assertThat(verifiers.get(2), is(allEventsInStreamsVerifier));
    }
}
