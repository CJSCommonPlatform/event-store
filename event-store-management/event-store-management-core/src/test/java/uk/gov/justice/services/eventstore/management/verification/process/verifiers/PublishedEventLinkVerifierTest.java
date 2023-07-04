package uk.gov.justice.services.eventstore.management.verification.process.verifiers;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.LinkedEventNumberTable.PUBLISHED_EVENT;

import uk.gov.justice.services.eventsourcing.source.core.EventStoreDataSourceProvider;
import uk.gov.justice.services.eventstore.management.verification.process.EventLinkageChecker;
import uk.gov.justice.services.eventstore.management.verification.process.VerificationResult;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;


@ExtendWith(MockitoExtension.class)
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

        when(eventLinkageChecker.verifyEventNumbersAreLinkedCorrectly(PUBLISHED_EVENT, eventStoreDataSource)).thenReturn(results);

        assertThat(publishedEventLinkVerifier.verify(), is(results));
    }
}
