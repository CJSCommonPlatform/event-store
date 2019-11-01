package uk.gov.justice.services.eventstore.management.validation.process;

import java.io.StringReader;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonStringConverter {

    public JSONObject asJsonObject(final String json) {
        return new JSONObject(new JSONTokener(new StringReader(json)));
    }
}
