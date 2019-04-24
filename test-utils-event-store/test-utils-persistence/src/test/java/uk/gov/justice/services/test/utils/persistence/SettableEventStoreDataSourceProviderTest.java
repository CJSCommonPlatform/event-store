package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SettableEventStoreDataSourceProviderTest {

    @InjectMocks
    private SettableEventStoreDataSourceProvider settableEventStoreDataSourceProvider;

    @Test
    public void shouldBeAbleToSetTheDataSource() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        settableEventStoreDataSourceProvider.setDataSource(dataSource);

        assertThat(settableEventStoreDataSourceProvider.getDefaultDataSource(), is(dataSource));
    }

    @Test
    public void shouldBeAbleToSetTheDataSourceAndGetByName() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        settableEventStoreDataSourceProvider.setDataSource(dataSource);

        assertThat(settableEventStoreDataSourceProvider.getDataSource("don't care"), is(dataSource));
    }
}
