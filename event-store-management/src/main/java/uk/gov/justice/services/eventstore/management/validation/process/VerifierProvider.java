package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.Arrays.asList;

import java.util.List;

import javax.inject.Inject;

public class VerifierProvider {

    @Inject
    private AllEventsInStreamsVerifier allEventsInStreamsVerifier;

    @Inject
    private ProcessedEventCountVerifier processedEventCountVerifier;

    @Inject
    private ProcessedEventLinkVerifier processedEventLinkVerifier;

    @Inject
    private PublishedEventCountVerifier publishedEventCountVerifier;

    @Inject
    private PublishedEventLinkVerifier publishedEventLinkVerifier;

    @Inject
    private StreamBufferEmptyVerifier streamBufferEmptyVerifier;

    public List<Verifier> getVerifiers() {
        return asList(
                streamBufferEmptyVerifier,
                publishedEventCountVerifier,
                processedEventCountVerifier,
                publishedEventLinkVerifier,
                processedEventLinkVerifier,
                allEventsInStreamsVerifier
        );
    }
}
