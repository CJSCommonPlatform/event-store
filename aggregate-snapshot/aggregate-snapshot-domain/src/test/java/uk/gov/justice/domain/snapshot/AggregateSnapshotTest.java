package uk.gov.justice.domain.snapshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.fail;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.core.aggregate.exception.AggregateChangeDetectedException;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;


public class AggregateSnapshotTest {

    private final static long VERSION_ID = 1L;

    private final DefaultObjectInputStreamStrategy streamStrategy = new DefaultObjectInputStreamStrategy();
    private final UUID STREAM_ID = UUID.randomUUID();
    private final String TYPE = "uk.gov.justice.domain.snapshot.AggregateSnapshotTest$TestAggregate";

    @Test
    public void shouldCreateAnAggregateSnapshot() throws Exception {
        final TestAggregate aggregate = new TestAggregate("STATE1");

        final AggregateSnapshot<TestAggregate> snapshot = new AggregateSnapshot<>(STREAM_ID, VERSION_ID, aggregate);

        assertThat(snapshot.getStreamId(), is(STREAM_ID));
        assertThat(snapshot.getPositionInStream(), is(VERSION_ID));
        assertThat(snapshot.getType(), is(TYPE));
        assertThat(snapshot.getAggregateByteRepresentation(), is(SerializationUtils.serialize(aggregate)));
    }

    @Test
    public void shouldGetAnAggregateSnapshot() throws Exception {
        final TestAggregate aggregate = new TestAggregate("STATE1");

        final AggregateSnapshot<TestAggregate> snapshot = new AggregateSnapshot<>(STREAM_ID, VERSION_ID, aggregate);

        assertThat(snapshot.getStreamId(), is(STREAM_ID));
        assertThat(snapshot.getPositionInStream(), is(VERSION_ID));
        assertThat(snapshot.getType(), is(TYPE));
        assertThat(snapshot.getAggregate(streamStrategy), is(aggregate));
    }

    @Test
    public void shouldThrowAAggregateChangeDetectedExceptionIfTheAggregateCannotBeDeserialised() throws Exception {

        final byte[] aggregate = "Not a serialised Aggregate".getBytes();

        final AggregateSnapshot<TestAggregate> aggregateSnapshot = new AggregateSnapshot<>(
                STREAM_ID,
                VERSION_ID,
                TestAggregate.class,
                aggregate);

        try {
            aggregateSnapshot.getAggregate(streamStrategy);
            fail();
        } catch (final AggregateChangeDetectedException e) {
            assertThat(e.getLocalizedMessage(), is("Failed to deserialise Aggregate into uk.gov.justice.domain.snapshot.AggregateSnapshotTest$TestAggregate. Cause: invalid stream header: 4E6F7420"));
        }
    }

    public static class TestAggregate implements Aggregate, Serializable {
        private static final long serialVersionUID = 42L;

        private final String name;

        public TestAggregate(String name) {
            this.name = name;
        }

        @Override
        public Object apply(Object event) {
            return event;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestAggregate that = (TestAggregate) o;

            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
