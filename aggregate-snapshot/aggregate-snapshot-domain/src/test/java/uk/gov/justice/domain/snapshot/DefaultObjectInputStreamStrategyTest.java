package uk.gov.justice.domain.snapshot;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class DefaultObjectInputStreamStrategyTest {


    @InjectMocks
    private DefaultObjectInputStreamStrategy defaultObjectInputStreamStrategy;

    @Test
    public void shouldCreateAnObjectInputStream() throws Exception {

        final byte[] stringAsObjectByteArray = asObjectByteArray("This is a String");
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringAsObjectByteArray);

        final ObjectInputStream objectInputStream = defaultObjectInputStreamStrategy.objectInputStreamOf(byteArrayInputStream);

        assertThat(objectInputStream, is(notNullValue()));
        assertThat(objectInputStream.readObject(), is("This is a String"));
    }

    @SuppressWarnings("SameParameterValue")
    private byte[] asObjectByteArray(final String aString) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream);
        out.writeObject(aString);
        out.flush();
        return byteArrayOutputStream.toByteArray();
    }
}
