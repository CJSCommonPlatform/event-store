package uk.gov.justice.services.eventsourcing.repository.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventConverter;
import uk.gov.justice.services.eventsourcing.repository.jdbc.event.EventJdbcRepository;
import uk.gov.justice.services.eventsourcing.repository.jdbc.eventstream.EventStreamJdbcRepository;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class EventRepositoryFactoryTest {

    @Mock
    private EventConverter eventConverter;

    @InjectMocks
    private EventRepositoryFactory eventRepositoryFactory;

    @Test
    public void shouldProduceEventStreamManager() throws Exception {
        final EventJdbcRepository eventJdbcRepository = mock(EventJdbcRepository.class);
        final EventStreamJdbcRepository eventStreamJdbcRepository = mock(EventStreamJdbcRepository.class);

        final EventRepository eventRepository = eventRepositoryFactory.eventRepository(eventJdbcRepository, eventStreamJdbcRepository);

        assertThat(eventRepository, is(notNullValue()));

        final EventJdbcRepository eventJdbcRepositoryField = getValueOfField(eventRepository, "eventJdbcRepository", EventJdbcRepository.class);
        assertThat(eventJdbcRepositoryField, is(eventJdbcRepository));

        final EventStreamJdbcRepository eventStreamJdbcRepositoryField = getValueOfField(eventRepository, "eventStreamJdbcRepository", EventStreamJdbcRepository.class);
        assertThat(eventStreamJdbcRepositoryField, is(eventStreamJdbcRepository));

        final EventConverter eventConverterField = getValueOfField(eventRepository, "eventConverter", EventConverter.class);
        assertThat(eventConverterField, is(eventConverter));

        final Logger loggerField = getValueOfField(eventRepository, "logger", Logger.class);
        assertThat(loggerField, is(notNullValue()));
    }
}
