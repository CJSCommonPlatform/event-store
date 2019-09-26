package uk.gov.justice.services.eventstore.management.validation.process;

import java.util.List;

public interface Verifier {

    List<VerificationResult> verify();
}
