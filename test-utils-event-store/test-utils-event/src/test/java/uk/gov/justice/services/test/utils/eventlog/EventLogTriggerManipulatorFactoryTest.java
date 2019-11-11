package uk.gov.justice.services.test.utils.eventlog;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.eventsourcing.util.sql.triggers.EventLogTriggerManipulator;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import javax.sql.DataSource;

@RunWith(MockitoJUnitRunner.class)
public class EventLogTriggerManipulatorFactoryTest {

    @Test
    public void shouldCreateEventLogTriggerManipulatorFactory() throws Exception {

        final DataSource eventStoreDataSource = mock(DataSource.class);

        final EventLogTriggerManipulatorFactory eventLogTriggerManipulatorFactory = new EventLogTriggerManipulatorFactory();

        assertThat(eventLogTriggerManipulatorFactory.create(eventStoreDataSource), is(notNullValue()));
    }
}
