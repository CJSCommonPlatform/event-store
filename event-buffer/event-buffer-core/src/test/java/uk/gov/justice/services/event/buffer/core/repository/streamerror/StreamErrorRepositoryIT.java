package uk.gov.justice.services.event.buffer.core.repository.streamerror;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.jdbc.persistence.ViewStoreJdbcDataSourceProvider;
import uk.gov.justice.services.test.utils.persistence.TestJdbcDataSourceProvider;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StreamErrorRepositoryIT {

    private final TestJdbcDataSourceProvider testJdbcDataSourceProvider = new TestJdbcDataSourceProvider();

    @Mock
    private ViewStoreJdbcDataSourceProvider viewStoreJdbcDataSourceProvider;

    @InjectMocks
    private StreamErrorRepository streamErrorRepository;

    @Test
    public void shouldSaveAndFind() throws Exception {

        final DataSource viewStoreDataSource = testJdbcDataSourceProvider.getViewStoreDataSource("framework");
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        final String source = "some-source";
        final String componentName = "SOME_COMPONENT";
        final UUID id = randomUUID();
        final UUID streamId = randomUUID();
        final Optional<String> causeClassName = of("uk.gov.justice.services.SomeCauseException");
        final Optional<String> causeMessage = of("Oh no!");

        final StreamError streamError = aStreamError(id, streamId, causeClassName, causeMessage, source, componentName);
        streamErrorRepository.save(streamError);

        final Optional<StreamError> persistableEventErrorOptional = streamErrorRepository.findBy(id);

        assertThat(persistableEventErrorOptional.isPresent(), is(true));

        final StreamError foundStreamError = persistableEventErrorOptional.get();

        assertThat(foundStreamError.id(), is(streamError.id()));
        assertThat(foundStreamError.hash(), is(streamError.hash()));
        assertThat(foundStreamError.exceptionClassName(), is(streamError.exceptionClassName()));
        assertThat(foundStreamError.exceptionMessage(), is(streamError.exceptionMessage()));
        assertThat(foundStreamError.causeClassName(), is(streamError.causeClassName()));
        assertThat(foundStreamError.causeMessage(), is(streamError.causeMessage()));
        assertThat(foundStreamError.javaClassname(), is(streamError.javaClassname()));
        assertThat(foundStreamError.javaMethod(), is(streamError.javaMethod()));
        assertThat(foundStreamError.javaLineNumber(), is(streamError.javaLineNumber()));
        assertThat(foundStreamError.eventName(), is(streamError.eventName()));
        assertThat(foundStreamError.eventId(), is(streamError.eventId()));
        assertThat(foundStreamError.streamId(), is(streamError.streamId()));
        assertThat(foundStreamError.dateCreated(), is(streamError.dateCreated()));
        assertThat(foundStreamError.fullStackTrace(), is(streamError.fullStackTrace()));
        assertThat(foundStreamError.componentName(), is(streamError.componentName()));
        assertThat(foundStreamError.source(), is(streamError.source()));
    }

    @Test
    public void shouldHandleMissingCauseExceptionAndMessage() throws Exception {

        final String source = "some-source";
        final String componentName = "SOME_COMPONENT";
        final UUID id = randomUUID();
        final UUID streamId = randomUUID();

        final DataSource viewStoreDataSource = testJdbcDataSourceProvider.getViewStoreDataSource("framework");
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        final StreamError streamError = aStreamError(id, streamId, empty(), empty(), source, componentName);
        streamErrorRepository.save(streamError);

        final Optional<StreamError> streamErrorOptional = streamErrorRepository.findBy(id);

        assertThat(streamErrorOptional.isPresent(), is(true));

        final StreamError foundStreamError = streamErrorOptional.get();

        assertThat(foundStreamError.id(), is(id));
        assertThat(foundStreamError.hash(), is(streamError.hash()));
        assertThat(foundStreamError.exceptionClassName(), is(streamError.exceptionClassName()));
        assertThat(foundStreamError.exceptionMessage(), is(streamError.exceptionMessage()));
        assertThat(foundStreamError.causeClassName(), is(empty()));
        assertThat(foundStreamError.causeMessage(), is(empty()));
        assertThat(foundStreamError.javaClassname(), is(streamError.javaClassname()));
        assertThat(foundStreamError.javaMethod(), is(streamError.javaMethod()));
        assertThat(foundStreamError.javaLineNumber(), is(streamError.javaLineNumber()));
        assertThat(foundStreamError.eventName(), is(streamError.eventName()));
        assertThat(foundStreamError.eventId(), is(streamError.eventId()));
        assertThat(foundStreamError.streamId(), is(streamId));
        assertThat(foundStreamError.positionInStream(), is(streamError.positionInStream()));
        assertThat(foundStreamError.dateCreated(), is(streamError.dateCreated()));
        assertThat(foundStreamError.fullStackTrace(), is(streamError.fullStackTrace()));
        assertThat(foundStreamError.componentName(), is(streamError.componentName()));
    }

    @Test
    public void shouldDeleteAllByStreamId() throws Exception {

        final String componentName_1 = "SOME_COMPONENT_1";
        final String componentName_2 = "SOME_COMPONENT_2";
        final String source_1 = "some-source_1";
        final String source_2 = "some-source_2";
        final UUID id_1 = randomUUID();
        final UUID id_2 = randomUUID();
        final UUID id_3 = randomUUID();
        final UUID id_4 = randomUUID();

        final UUID streamId = randomUUID();
        final UUID streamToDeleteId = randomUUID();

        final StreamError streamError_1 = aStreamError(id_1, streamToDeleteId, empty(), empty(), source_1, componentName_1);
        final StreamError streamError_2 = aStreamError(id_2, streamId, empty(), empty(), source_1, componentName_2);
        final StreamError streamError_3 = aStreamError(id_3, streamToDeleteId, empty(), empty(), source_2, componentName_2);
        final StreamError streamError_4 = aStreamError(id_4, streamId, empty(), empty(), source_1, componentName_1);

        final DataSource viewStoreDataSource = testJdbcDataSourceProvider.getViewStoreDataSource("framework");
        when(viewStoreJdbcDataSourceProvider.getDataSource()).thenReturn(viewStoreDataSource);

        streamErrorRepository.save(streamError_1);
        streamErrorRepository.save(streamError_2);
        streamErrorRepository.save(streamError_3);
        streamErrorRepository.save(streamError_4);

        assertThat(streamErrorRepository.findBy(id_1).isPresent(), is(true));
        assertThat(streamErrorRepository.findBy(id_2).isPresent(), is(true));
        assertThat(streamErrorRepository.findBy(id_3).isPresent(), is(true));
        assertThat(streamErrorRepository.findBy(id_4).isPresent(), is(true));

        final int numberOfErrorsDeleted = streamErrorRepository.removeErrorForStream(streamToDeleteId,  source_2, componentName_2);

        assertThat(numberOfErrorsDeleted, is(1));

        assertThat(streamErrorRepository.findBy(id_1).isPresent(), is(true));
        assertThat(streamErrorRepository.findBy(id_2).isPresent(), is(true));
        assertThat(streamErrorRepository.findBy(id_3).isPresent(), is(false));
        assertThat(streamErrorRepository.findBy(id_4).isPresent(), is(true));
    }

    private StreamError aStreamError(
            final UUID id,
            final UUID streamId,
            final Optional<String> causeClassName,
            final Optional<String> causeMessage,
            final String source,
            final String componentName) {
        final String hash = "576b975aff05b7f2b4a1f7b26eb47aa5";
        final String exceptionClassName = "uk.gov.justice.SomeException";
        final String exceptionMessage = "We're all going to die";
        final String javaClassname = "uk.gov.justice.SomeJavaClass";
        final String javaMethod = "someMethod";
        final int javaLineNumber = 23;
        final String eventName = "some-context.events.something-happened";
        final UUID eventId = randomUUID();
        final ZonedDateTime dateCreated = new UtcClock().now();
        final String fullStackTrace = "the full stack trace";
        return new StreamError(
                id,
                hash,
                exceptionClassName,
                exceptionMessage,
                causeClassName,
                causeMessage,
                javaClassname,
                javaMethod,
                javaLineNumber,
                eventName,
                eventId,
                streamId,
                23873624L,
                dateCreated,
                fullStackTrace,
                componentName,
                source);
    }
}