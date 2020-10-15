package uk.gov.justice.services.event.sourcing.subscription.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.event.sourcing.subscription.manager.cdi.EventSourceNameQualifier;
import uk.gov.justice.services.eventsourcing.source.api.service.core.PublishedEventSource;

import javax.enterprise.inject.Instance;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishedEventSourceProviderTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Instance<PublishedEventSource> publishedEventSources;

    @InjectMocks
    private PublishedEventSourceProvider publishedEventSourceProvider;

    @Test
    public void shouldGetTheCorrectEventSourceByName() throws Exception {

        final String eventSourceName = "eventSourceName";

        final PublishedEventSource publishedEventSource = mock(PublishedEventSource.class);

        when(publishedEventSources.select(new EventSourceNameQualifier(eventSourceName)).get()).thenReturn(publishedEventSource);

        assertThat(publishedEventSourceProvider.getPublishedEventSource(eventSourceName), is(publishedEventSource));
    }
}
