package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.repository.jdbc.EventRepository;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PublishingEventAppenderFactoryTest {

    @InjectMocks
    private PublishingEventAppenderFactory publishingEventAppenderFactory;

    @Test
    public void shouldProducePublishingEventAppender() throws Exception {
        final EventRepository eventRepository = mock(EventRepository.class);

        final PublishingEventAppender publishingEventAppender = publishingEventAppenderFactory.publishingEventAppender(eventRepository);

        assertThat(publishingEventAppender, is(notNullValue()));

        final EventRepository eventRepositoryField = getValueOfField(publishingEventAppender, "eventRepository", EventRepository.class);
        assertThat(eventRepositoryField, is(eventRepository));
    }
}
