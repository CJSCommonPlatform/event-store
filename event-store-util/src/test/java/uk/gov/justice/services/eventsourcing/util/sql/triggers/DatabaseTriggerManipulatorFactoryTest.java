package uk.gov.justice.services.eventsourcing.util.sql.triggers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTriggerManipulatorFactoryTest {

    @InjectMocks
    private DatabaseTriggerManipulatorFactory databaseTriggerManipulatorFactory;

    @Test
    public void shouldCreateDatabaseTriggerManipulator() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        final DatabaseTriggerManipulator databaseTriggerManipulator = databaseTriggerManipulatorFactory.databaseTriggerManipulator(dataSource);

        assertThat(ReflectionUtil.getValueOfField(databaseTriggerManipulator, "dataSource", DataSource.class), is(dataSource));

    }
}
