package uk.gov.justice.services.eventstore.management.validation.process;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class VerificationRunner {

    @Inject
    private VerifierProvider verifierProvider;

    public List<VerificationResult> runVerifiers() {

    return verifierProvider.getVerifiers().stream()
                .map(Verifier::verify)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
