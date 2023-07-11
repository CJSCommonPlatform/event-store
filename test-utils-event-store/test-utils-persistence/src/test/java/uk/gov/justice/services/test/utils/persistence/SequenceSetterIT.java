package uk.gov.justice.services.test.utils.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SequenceSetterIT {

    private final SequenceSetter sequenceSetter = new SequenceSetter();

    private final DataSource eventStoreDataSource = new FrameworkTestDataSourceFactory().createEventStoreDataSource();
    

    @Test
    public void shouldGetCurrentValueNextValueAndResetSequence() throws Exception {

        final String sequenceName = "event_sequence_seq";

        sequenceSetter.setSequenceTo(23, sequenceName, eventStoreDataSource);

        assertThat(sequenceSetter.getCurrentSequenceValue(sequenceName, eventStoreDataSource), is(23L));

        assertThat(sequenceSetter.getNextSequenceValue(sequenceName, eventStoreDataSource), is(23L));
        assertThat(sequenceSetter.getNextSequenceValue(sequenceName, eventStoreDataSource), is(24L));
        assertThat(sequenceSetter.getNextSequenceValue(sequenceName, eventStoreDataSource), is(25L));
        assertThat(sequenceSetter.getNextSequenceValue(sequenceName, eventStoreDataSource), is(26L));
        assertThat(sequenceSetter.getNextSequenceValue(sequenceName, eventStoreDataSource), is(27L));

        assertThat(sequenceSetter.getCurrentSequenceValue(sequenceName, eventStoreDataSource), is(27L));

        sequenceSetter.setSequenceTo(1, sequenceName, eventStoreDataSource);

        assertThat(sequenceSetter.getCurrentSequenceValue(sequenceName, eventStoreDataSource), is(1L));
    }


}
