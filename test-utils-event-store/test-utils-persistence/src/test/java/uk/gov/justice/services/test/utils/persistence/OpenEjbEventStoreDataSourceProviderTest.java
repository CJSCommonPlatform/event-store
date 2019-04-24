package uk.gov.justice.services.test.utils.persistence;

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
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;

import javax.sql.DataSource;


@RunWith(MockitoJUnitRunner.class)
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
