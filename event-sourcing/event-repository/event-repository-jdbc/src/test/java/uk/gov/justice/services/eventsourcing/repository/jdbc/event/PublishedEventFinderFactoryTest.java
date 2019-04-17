package uk.gov.justice.services.eventsourcing.repository.jdbc.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
public class PublishedEventFinderFactoryTest {

    @Mock
    private JdbcResultSetStreamer jdbcResultSetStreamer;

    @Mock
    private PreparedStatementWrapperFactory preparedStatementWrapperFactory;

    @InjectMocks
    private PublishedEventFinderFactory publishedEventFinderFactory;

    @Test
    public void shouldCreateAPublishedEventFinder() throws Exception {

        final DataSource dataSource = mock(DataSource.class);

        final PublishedEventFinder publishedEventFinder = publishedEventFinderFactory.create(dataSource);

        assertThat(getValueOfField(publishedEventFinder, "jdbcResultSetStreamer", JdbcResultSetStreamer.class), is(jdbcResultSetStreamer));
        assertThat(getValueOfField(publishedEventFinder, "preparedStatementWrapperFactory", PreparedStatementWrapperFactory.class), is(preparedStatementWrapperFactory));
        assertThat(getValueOfField(publishedEventFinder, "dataSource", DataSource.class), is(dataSource));
    }
}
