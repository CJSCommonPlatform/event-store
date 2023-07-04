package uk.gov.justice.services.eventstore.management.verification.process.verifiers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.SUCCESS;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventstore.management.verification.process.TableRowCounter;
import uk.gov.justice.services.eventstore.management.verification.process.VerificationResult;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class ProcessedEventCountVerifierTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Mock
    private TableRowCounter tableRowCounter;

    @Mock
    private Logger logger;

    @InjectMocks
    private ProcessedEventCountVerifier processedEventCountVerifier;

    @Test
    public void shouldReturnSuccessIfPublishedEventAndProcessedEventBothHasTheSameNumberOfEvents() throws Exception {

        final int publishedEventCount = 23;
        final int processedEventCount = 23;

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        when(tableRowCounter.countRowsIn("published_event", eventStoreDataSource)).thenReturn(publishedEventCount);
        when(tableRowCounter.countRowsIn("processed_event", viewStoreDataSource)).thenReturn(processedEventCount);

        final List<VerificationResult> verificationResult = processedEventCountVerifier.verify();

        assertThat(verificationResult.size(), is(1));

        assertThat(verificationResult.get(0).getVerificationResultType(), is(SUCCESS));
        assertThat(verificationResult.get(0).getMessage(), is("published_event and processed_event both contain 23 events"));
    }

    @Test
    public void shouldReturnErrorIfPublishedEventAndProcessedEventHaveDifferentNumbersOfEvents() throws Exception {

        final int publishedEventCount = 92384;
        final int processedEventCount = 23;

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        when(tableRowCounter.countRowsIn("published_event", eventStoreDataSource)).thenReturn(publishedEventCount);
        when(tableRowCounter.countRowsIn("processed_event", viewStoreDataSource)).thenReturn(processedEventCount);

        final List<VerificationResult> verificationResult = processedEventCountVerifier.verify();

        assertThat(verificationResult.size(), is(1));

        assertThat(verificationResult.get(0).getVerificationResultType(), is(ERROR));
        assertThat(verificationResult.get(0).getMessage(), is("The number of events in processed_event does not match the number of events in published event. published_event: 92384, processed_event: 23"));
    }

    @Test
    public void shouldReturnErrorIfPublishedEventAndProcessedEventBothHaveZeroEvents() throws Exception {

        final int publishedEventCount = 0;
        final int processedEventCount = 0;

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        when(tableRowCounter.countRowsIn("published_event", eventStoreDataSource)).thenReturn(publishedEventCount);
        when(tableRowCounter.countRowsIn("processed_event", viewStoreDataSource)).thenReturn(processedEventCount);

        final List<VerificationResult> verificationResult = processedEventCountVerifier.verify();

        assertThat(verificationResult.size(), is(1));

        assertThat(verificationResult.get(0).getVerificationResultType(), is(ERROR));
        assertThat(verificationResult.get(0).getMessage(), is("published_event and processed_event both contain zero events"));
    }
}
