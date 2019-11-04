package uk.gov.justice.services.eventstore.management.verification.process.verifiers;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PROCESSED_EVENT;

import uk.gov.justice.services.eventstore.management.verification.process.EventLinkageChecker;
import uk.gov.justice.services.eventstore.management.verification.process.VerificationResult;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ProcessedEventLinkVerifierTest {

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Mock
    private EventLinkageChecker eventLinkageChecker;

    @Mock
    private Logger logger;

    @InjectMocks
    private ProcessedEventLinkVerifier processedEventLinkVerifier;

    @Test
    public void shouldCheckTheLinkingOfEventNumbersInProcessedEvent() throws Exception {

        final DataSource viewStoreDataSource = mock(DataSource.class);
        final List<VerificationResult> results = singletonList(mock(VerificationResult.class));

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        when(eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(PROCESSED_EVENT, viewStoreDataSource)).thenReturn(results);

        assertThat(processedEventLinkVerifier.verify(), is(results));
    }
}
