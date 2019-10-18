package uk.gov.justice.services.eventstore.management.validation.process.verifiers;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventstore.management.validation.process.EventLinkageChecker;
import uk.gov.justice.services.eventstore.management.validation.process.VerificationResult;
import uk.gov.justice.services.eventstore.management.validation.process.Verifier;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

public class PublishedEventLinkVerifier implements Verifier {

    @Inject
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;
    
    @Inject
    private EventLinkageChecker eventLinkageChecker;

    @Inject
    private Logger logger;

    @Override
    public List<VerificationResult> verify() {

        logger.info("Verifying all previous_event_numbers in processed_event point to an existing event...");

        return eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                "published_event",
                eventStoreDataSourceProvider.getDefaultDataSource());
    }
}
