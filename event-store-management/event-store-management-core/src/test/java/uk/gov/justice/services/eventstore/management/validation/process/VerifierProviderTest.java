package uk.gov.justice.services.eventstore.management.validation.process;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    public void shouldGetTheListOfAllVerifiersInTheCorrectOrder() throws Exception {

        final List<Verifier> verifiers = verifierProvider.getVerifiers();

        assertThat(verifiers.size(), is(6));

        assertThat(verifiers.get(0), is(streamBufferEmptyVerifier));
        assertThat(verifiers.get(1), is(publishedEventCountVerifier));
        assertThat(verifiers.get(2), is(processedEventCountVerifier));
        assertThat(verifiers.get(3), is(publishedEventLinkVerifier));
        assertThat(verifiers.get(4), is(processedEventLinkVerifier));
        assertThat(verifiers.get(5), is(allEventsInStreamsVerifier));
    }
}
