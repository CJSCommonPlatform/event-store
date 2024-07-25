package uk.gov.justice.services.eventstore.management.catchup.process;

import static java.util.Optional.empty;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.jdbc.persistence.InitialContextFactory;

import java.util.List;
import java.util.Optional;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class CatchupWhitelistedEventSourcesProviderTest {

    @Mock
    private InitialContextFactory initialContextFactory;

    @Mock
    private Logger logger;

    @InjectMocks
    private CatchupWhitelistedEventSourcesProvider catchupWhitelistedEventSourcesProvider;

    @Test
    public void shouldGetCommaSeparatedListOfEventSourcesFromJndi() throws Exception {

        final String catchupEventSourceWhitelist = "event-source_1,event-source_2,event-source_3";

        final InitialContext initialContext = mock(InitialContext.class);

        when(initialContextFactory.create()).thenReturn(initialContext);
        when(initialContext.lookup("java:global/catchup.event.source.whitelist")).thenReturn(catchupEventSourceWhitelist);

        final Optional<List<String>> whiteListedEventSources = catchupWhitelistedEventSourcesProvider.getWhiteListedEventSources();

        if(whiteListedEventSources.isPresent()) {
            assertThat(whiteListedEventSources.get().size(), is(3));
            assertThat(whiteListedEventSources.get(), hasItem("event-source_1"));
            assertThat(whiteListedEventSources.get(), hasItem("event-source_2"));
            assertThat(whiteListedEventSources.get(), hasItem("event-source_3"));
        } else {
            fail();
        }

        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldLogWarningAndReturnEmptyListIfLookingUpJndiNameFails() throws Exception {

        final NamingException namingException = new NamingException("Name not found");
        final InitialContext initialContext = mock(InitialContext.class);

        when(initialContextFactory.create()).thenReturn(initialContext);
        when(initialContext.lookup("java:global/catchup.event.source.whitelist")).thenThrow(namingException);

        assertThat(catchupWhitelistedEventSourcesProvider.getWhiteListedEventSources(), is(empty()));

        verify(logger).warn("No whitelisted catchup event-sources found: allowing catchup for all event-sources. Lookup made using JNDI name 'java:global/catchup.event.source.whitelist'.  Lookup error message: 'Name not found'");
    }

    @Test
    public void shouldReturnEmptyListIfWhitelistedNamesIsSetToAllowAll() throws Exception {

        final String catchupEventSourceWhitelist = "ALLOW_ALL";

        final InitialContext initialContext = mock(InitialContext.class);

        when(initialContextFactory.create()).thenReturn(initialContext);
        when(initialContext.lookup("java:global/catchup.event.source.whitelist")).thenReturn(catchupEventSourceWhitelist);

        assertThat(catchupWhitelistedEventSourcesProvider.getWhiteListedEventSources(), is(empty()));

        verifyZeroInteractions(logger);
    }

    @Test
    public void shouldHandleSpacesInWhitelistString() throws Exception {

        final String catchupEventSourceWhitelist = "event-source_1, event-source_2, event-source_3";

        final InitialContext initialContext = mock(InitialContext.class);

        when(initialContextFactory.create()).thenReturn(initialContext);
        when(initialContext.lookup("java:global/catchup.event.source.whitelist")).thenReturn(catchupEventSourceWhitelist);

        final Optional<List<String>> whiteListedEventSources = catchupWhitelistedEventSourcesProvider.getWhiteListedEventSources();

        if(whiteListedEventSources.isPresent()) {
            assertThat(whiteListedEventSources.get().size(), is(3));
            assertThat(whiteListedEventSources.get(), hasItem("event-source_1"));
            assertThat(whiteListedEventSources.get(), hasItem("event-source_2"));
            assertThat(whiteListedEventSources.get(), hasItem("event-source_3"));
        } else {
            fail();
        }

        verifyZeroInteractions(logger);
    }
}