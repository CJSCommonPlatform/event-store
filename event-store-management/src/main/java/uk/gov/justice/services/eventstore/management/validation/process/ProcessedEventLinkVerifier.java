package uk.gov.justice.services.eventstore.management.validation.process;

import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

public class ProcessedEventLinkVerifier implements Verifier{

    @Inject
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;
    
    @Inject
    private EventLinkageChecker eventLinkageChecker;

    @Inject
    private Logger logger;

    @Override
    public List<VerificationResult> verify() {

        logger.info("Verifying all previous_event_numbers in processed_event point to an existing event...");

        return eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(
                "processed_event",
                viewStoreJdbcDataSourceProvider.getDataSource());
    }
}
