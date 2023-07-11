package uk.gov.justice.domain.snapshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import uk.gov.justice.domain.aggregate.Aggregate;

import org.junit.jupiter.api.Test;

public class VersionedAggregateTest {

    @Test
    public void shouldCreateInstanceOfVersionedAggregate() throws Exception {
        final Aggregate aggregate = mock(Aggregate.class);
        final VersionedAggregate<Aggregate> versionedAggregate = new VersionedAggregate<>(1L, aggregate);

        assertThat(versionedAggregate.getVersionId(), is(1L));
        assertThat(versionedAggregate.getAggregate(), is(aggregate));
    }
}