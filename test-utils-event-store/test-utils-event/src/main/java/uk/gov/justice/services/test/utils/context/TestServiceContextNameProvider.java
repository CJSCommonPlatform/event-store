package uk.gov.justice.services.test.utils.context;

import uk.gov.justice.services.common.configuration.ServiceContextNameProvider;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestServiceContextNameProvider implements ServiceContextNameProvider {

    public static final String TEST_CONTEXT_NAME = "test-context";

    @Override
    public String getServiceContextName() {
        return TEST_CONTEXT_NAME;
    }
}

