package uk.gov.justice.services.eventstore.management.validation.process.verifiers;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventstore.management.validation.process.EventLinkageChecker;
import uk.gov.justice.services.eventstore.management.validation.process.VerificationResult;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;


@RunWith(MockitoJUnitRunner.class)
public class PublishedEventLinkVerifierTest {

    @Mock
    private EventStoreDataSourceProvider eventStoreDataSourceProvider;

    @Mock
    private EventLinkageChecker eventLinkageChecker;

    @Mock
    private Logger logger;

    @InjectMocks
    private PublishedEventLinkVerifier publishedEventLinkVerifier;

    @Test
    public void shouldCheckTheLinkingOfEventNumbersInPublishedEvent() throws Exception {

        final DataSource eventStoreDataSource = mock(DataSource.class);
        final List<VerificationResult> results = singletonList(mock(VerificationResult.class));

        when(eventStoreDataSourceProvider.getDefaultDataSource()).thenReturn(eventStoreDataSource);

        when(eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly("published_event", eventStoreDataSource)).thenReturn(results);

        assertThat(publishedEventLinkVerifier.verify(), is(results));
    }
}