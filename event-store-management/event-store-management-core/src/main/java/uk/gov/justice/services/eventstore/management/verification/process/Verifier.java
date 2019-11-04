package uk.gov.justice.services.eventstore.management.verification.process;

import java.util.List;

public interface Verifier {

    List<VerificationResult> verify();
}
