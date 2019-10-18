package uk.gov.justice.services.eventstore.management.validation.process.verifiers;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventstore.management.validation.process.EventLinkageChecker;
import uk.gov.justice.services.eventstore.management.validation.process.VerificationResult;
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

        when(eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly("processed_event", viewStoreDataSource)).thenReturn(results);

        assertThat(processedEventLinkVerifier.verify(), is(results));
    }
}
