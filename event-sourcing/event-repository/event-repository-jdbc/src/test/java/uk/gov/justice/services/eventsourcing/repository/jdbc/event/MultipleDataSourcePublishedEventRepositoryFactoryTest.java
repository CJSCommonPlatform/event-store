package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.getValueOfField;

import uk.gov.justice.services.jdbc.persistence.JdbcResultSetStreamer;
import uk.gov.justice.services.jdbc.persistence.PreparedStatementWrapperFactory;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MultipleDataSourcePublishedEventRepositoryFactoryTest {

    @Mock
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @InjectMocks
    private MultipleDataSourcePublishedEventRepositoryFactory multipleDataSourcePublishedEventRepositoryFactory;

    @Test
    public void shouldCreateAPublishedEventFinder() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        final MultipleDataSourcePublishedEventRepository multipleDataSourcePublishedEventRepository = multipleDataSourcePublishedEventRepositoryFactory.create(dataSource);

        assertThat(getValueOfField(multipleDataSourcePublishedEventRepository, "jdbcResultSetStreamer", JdbcResultSetStreamer.class), is(jdbcResultSetStreamer));
        assertThat(getValueOfField(multipleDataSourcePublishedEventRepository, "preparedStatementWrapperFactory", PreparedStatementWrapperFactory.class), is(preparedStatementWrapperFactory));
        assertThat(getValueOfField(multipleDataSourcePublishedEventRepository, "dataSource", DataSource.class), is(dataSource));
    }
}
