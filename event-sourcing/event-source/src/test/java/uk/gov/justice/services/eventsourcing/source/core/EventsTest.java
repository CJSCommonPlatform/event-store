package uk.gov.justice.services.eventsourcing.source.core;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;

public class EventsTest {

    private static final Object OBJECT_1 = new Object();
    private static final Object OBJECT_2 = new Object();

    @Test
    public void shouldReturnStreamOfObjects() {
        List<Object> objects = Events.streamOf(OBJECT_1, OBJECT_2).collect(Collectors.toList());
        assertThat(objects, IsIterableContainingInOrder.contains(OBJECT_1, OBJECT_2));
    }

}