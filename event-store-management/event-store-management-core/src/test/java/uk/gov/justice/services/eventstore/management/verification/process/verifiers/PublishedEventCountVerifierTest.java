package uk.gov.justice.services.eventstore.management.verification.process.verifiers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.SUCCESS;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventstore.management.verification.process.EventLogActiveEventRowCounter;
import uk.gov.justice.services.eventstore.management.verification.process.TableRowCounter;
import uk.gov.justice.services.eventstore.management.verification.process.VerificationResult;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventCountVerifierTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private EventLogActiveEventRowCounter eventLogActiveEventRowCounter;

    @Mock
    private TableRowCounter tableRowCounter;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishedEventCountVerifier publishedEventCountVerifier;

    @Test
    public void shouldReturnSuccessIfTheNumberOfActiveEventsInEventLogMatchesTheNumberOfEventsInPublishedEvent() throws Exception {

        final int eventLogCount = 23;
        final int publishedEventCount = 23;

        final DataSource eventStoreDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        when(eventLogActiveEventRowCounter.getActiveEventCountFromEventLog(eventStoreDataSource)).thenReturn(eventLogCount);
        when(tableRowCounter.countRowsIn("published_event", eventStoreDataSource)).thenReturn(publishedEventCount);

        final List<VerificationResult> verificationResult = publishedEventCountVerifier.verify();

        assertThat(verificationResult.size(), is(1));
        assertThat(verificationResult.get(0).getVerificationResultType(), is(SUCCESS));
        assertThat(verificationResult.get(0).getMessage(), is("The tables event_log and published_event both contain 23 active events"));
    }

    @Test
    public void shouldReturnSuccessIfTheNumberOfActiveEventsInEventLogDoesNotMatchTheNumberOfEventsInPublishedEvent() throws Exception {

        final int eventLogCount = 234;
        final int publishedEventCount = 23;

        final DataSource eventStoreDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        when(eventLogActiveEventRowCounter.getActiveEventCountFromEventLog(eventStoreDataSource)).thenReturn(eventLogCount);
        when(tableRowCounter.countRowsIn("published_event", eventStoreDataSource)).thenReturn(publishedEventCount);

        final List<VerificationResult> verificationResult = publishedEventCountVerifier.verify();

        assertThat(verificationResult.size(), is(1));
        assertThat(verificationResult.get(0).getVerificationResultType(), is(ERROR));
        assertThat(verificationResult.get(0).getMessage(), is("The number of active events in event_log does not match the number of events in published event. event_log: 234, published: 23"));
    }

    @Test
    public void shouldReturnSuccessIfNeitherEventLogNorPublishedEventContainAnyActiveEvents() throws Exception {

        final int eventLogCount = 0;
        final int publishedEventCount = 0;

        final DataSource eventStoreDataSource = mock(DataSource.class);

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        when(eventLogActiveEventRowCounter.getActiveEventCountFromEventLog(eventStoreDataSource)).thenReturn(eventLogCount);
        when(tableRowCounter.countRowsIn("published_event", eventStoreDataSource)).thenReturn(publishedEventCount);

        final List<VerificationResult> verificationResult = publishedEventCountVerifier.verify();

        assertThat(verificationResult.size(), is(1));
        assertThat(verificationResult.get(0).getVerificationResultType(), is(ERROR));
        assertThat(verificationResult.get(0).getMessage(), is("The tables event_log and published both contain zero active events"));
    }
}
