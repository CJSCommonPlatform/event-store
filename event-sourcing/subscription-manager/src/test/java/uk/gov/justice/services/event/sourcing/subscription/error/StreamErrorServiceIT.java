package uk.gov.justice.services.event.sourcing.subscription.error;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamError;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetails;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorDetailsPersistence;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorHash;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorHashPersistence;
import uk.gov.justice.services.event.buffer.core.repository.streamerror.StreamErrorRepository;
import uk.gov.justice.services.event.buffer.core.repository.subscription.StreamStatusJdbcRepository;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.DatabaseCleaner;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamErrorServiceIT {

    private final StreamErrorHashPersistence streamErrorHashPersistence = new StreamErrorHashPersistence();
    private final StreamErrorDetailsPersistence streamErrorDetailsPersistence = new StreamErrorDetailsPersistence();
    private final DataSource viewStoreDataSource = new TestJdbcDataSourceProvider().getViewStoreDataSource("framework");
    private final DatabaseCleaner databaseCleaner = new DatabaseCleaner();
    private final PreparedStatementWrapperFactory preparedStatementWrapperFactory = new PreparedStatementWrapperFactory();

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreDataSourceProvider;

    @Spy
    private StreamErrorRepository streamErrorRepository = new StreamErrorRepository();

    @Spy
    private StreamStatusJdbcRepository streamStatusJdbcRepository = new StreamStatusJdbcRepository();

    @InjectMocks
    private StreamErrorService streamErrorService;

    @BeforeEach
    public void cleanTablesAndSetUpClasses() {

        databaseCleaner.cleanViewStoreTables("framework", "stream_error_hash", "stream_error");
        databaseCleaner.cleanStreamStatusTable("framework");
        databaseCleaner.cleanStreamBufferTable("framework");

        setField(streamErrorRepository, "viewStoreDataSourceProvider", viewStoreDataSourceProvider);
        setField(streamErrorRepository, "streamErrorHashPersistence", streamErrorHashPersistence);
        setField(streamErrorRepository, "streamErrorDetailsPersistence", streamErrorDetailsPersistence);

        setField(streamStatusJdbcRepository, "preparedStatementWrapperFactory", preparedStatementWrapperFactory);
        setField(streamStatusJdbcRepository, "viewStoreJdbcDataSourceProvider", viewStoreDataSourceProvider);
        setField(streamStatusJdbcRepository, "clock", new UtcClock());
    }

    @Test
    public void shouldMarkAsErrored() throws Exception {

        final StreamError streamError = aStreamError();
        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        streamErrorService.markStreamAsErrored(streamError);

        final Optional<StreamError> streamErrorOptional = streamErrorRepository.findBy(streamError.streamErrorDetails().id());

        assertThat(streamErrorOptional, is(of(streamError)));
    }
    @Test
    public void shouldMarkAsErroredThenFix() throws Exception {

        final StreamError streamError = aStreamError();
        when(viewStoreDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        streamErrorService.markStreamAsErrored(streamError);

        final StreamErrorDetails streamErrorDetails = streamError.streamErrorDetails();
        final UUID streamErrorId = streamErrorDetails.id();
        final String source = streamErrorDetails.source();
        final String componentName = streamErrorDetails.componentName();

        assertThat(streamErrorRepository.findBy(streamErrorId).isPresent(), is(true));

        streamErrorService.markStreamAsFixed(streamErrorId, source, componentName);
//
//        assertThat(streamErrorRepository.findBy(streamErrorId).isPresent(), is(false));
//
    }

    private StreamError aStreamError() {

        final String hash = "9374874397kjshdkfjhsdf";
        final UUID streamErrorId = randomUUID();
        final UUID eventId = randomUUID();
        final UUID streamId = randomUUID();
        final Long positionInStream = 9837489L;
        final String componentName= "componennt-name";
        final String source = "some-source";


        final StreamErrorHash streamErrorHash = new StreamErrorHash(
                hash,
                "some.exception.ClassName",
                empty(),
                "some.java.ClassName",
                "someMethod",
                2334
        );

        final StreamErrorDetails streamErrorDetails = new StreamErrorDetails(
                streamErrorId,
                hash,
                "some-exception-message",
                empty(),
                "events.context.some-event-name",
                eventId,
                streamId,
                positionInStream,
                new UtcClock().now(),
                "stack-trace",
                componentName,
                source
        );

        return new StreamError(
                streamErrorDetails,
                streamErrorHash
        );
    }
}