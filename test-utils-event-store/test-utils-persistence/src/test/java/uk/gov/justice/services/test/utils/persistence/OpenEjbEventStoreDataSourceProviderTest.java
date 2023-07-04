package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class OpenEjbEventStoreDataSourceProviderTest {

    @InjectMocks
    private OpenEjbEventStoreDataSourceProvider openEjbEventStoreDataSourceProvider;

    @Test
    public void shouldGetTheDefaultDataSourceInjectedByOpenEjbJndi() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        setField(openEjbEventStoreDataSourceProvider, "dataSource", dataSource);

        assertThat(openEjbEventStoreDataSourceProvider.getDefaultDataSource(), is(dataSource));
    }

    @Test
    public void shouldGetTheNamedDataSourceInjectedByOpenEjbJndi() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        setField(openEjbEventStoreDataSourceProvider, "dataSource", dataSource);

        assertThat(openEjbEventStoreDataSourceProvider.getDataSource("don't care"), is(dataSource));
    }
}
