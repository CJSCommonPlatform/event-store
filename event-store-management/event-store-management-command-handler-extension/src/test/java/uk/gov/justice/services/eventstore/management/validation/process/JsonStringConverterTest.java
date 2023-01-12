package uk.gov.justice.services.eventstore.management.validation.process;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class JsonStringConverterTest {

    private final String json = "" +
            "{ \n" +
            "   \"name_1\":\"value_1\",\n" +
            "   \"name_2\":\"value_2\"\n" +
            "}";

    @InjectMocks
    private JsonStringConverter jsonStringConverter;

    @Test
    public void shouldConvertValidJsonToJsonObject() throws Exception {

        final JSONObject jsonObject = jsonStringConverter.asJsonObject(json);

        assertThat(jsonObject.getString("name_1"), is("value_1"));
        assertThat(jsonObject.getString("name_2"), is("value_2"));

    }
}
