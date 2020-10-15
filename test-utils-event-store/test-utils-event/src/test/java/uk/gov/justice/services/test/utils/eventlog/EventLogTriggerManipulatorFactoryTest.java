package uk.gov.justice.services.test.utils.eventlog;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventLogTriggerManipulatorFactoryTest {

    @Test
    public void shouldCreateEventLogTriggerManipulatorFactory() throws Exception {

        final DataSource eventStoreDataSource = mock(DataSource.class);

        final EventLogTriggerManipulatorFactory eventLogTriggerManipulatorFactory = new EventLogTriggerManipulatorFactory();

        assertThat(eventLogTriggerManipulatorFactory.create(eventStoreDataSource), is(notNullValue()));
    }
}
