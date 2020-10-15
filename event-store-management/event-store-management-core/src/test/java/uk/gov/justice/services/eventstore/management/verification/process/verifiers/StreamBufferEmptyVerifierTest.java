package uk.gov.justice.services.eventstore.management.verification.process.verifiers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.ERROR;
import static uk.gov.justice.services.eventstore.management.verification.process.VerificationResult.VerificationResultType.SUCCESS;

import uk.gov.justice.services.eventstore.management.verification.process.TableRowCounter;
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
public class StreamBufferEmptyVerifierTest {

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @Mock
    private TableRowCounter tableRowCounter;

    @Mock
    private Logger logger;

    @InjectMocks
    private StreamBufferEmptyVerifier streamBufferEmptyVerifier;

    @Test
    public void shouldReturnSuccessIfTheStreamBufferTableIsEmpty() throws Exception {

        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        when(tableRowCounter.countRowsIn("stream_buffer", viewStoreDataSource)).thenReturn(0);

        final List<VerificationResult> verificationResult = streamBufferEmptyVerifier.verify();

        assertThat(verificationResult.size(), is(1));
        assertThat(verificationResult.get(0).getVerificationResultType(), is(SUCCESS));
        assertThat(verificationResult.get(0).getMessage(), is("stream_buffer table is empty"));
    }

    @Test
    public void shouldReturnErrorIfTheStreamBufferTableIsNotEmpty() throws Exception {

        final DataSource viewStoreDataSource = mock(DataSource.class);

        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);
        when(tableRowCounter.countRowsIn("stream_buffer", viewStoreDataSource)).thenReturn(23);

        final List<VerificationResult> verificationResult = streamBufferEmptyVerifier.verify();

        assertThat(verificationResult.size(), is(1));
        assertThat(verificationResult.get(0).getVerificationResultType(), is(ERROR));
        assertThat(verificationResult.get(0).getMessage(), is("23 events found in the stream_buffer table"));
    }
}
