package uk.gov.justice.services.healthcheck.healthchecks.artemis;

import javax.annotation.Resource;

public class JndiContextNameProvider {

    @Resource(
            lookup = "java:app/AppName"
    )
    private String appName;

    public JndiContextNameProvider() {
    }

    public String getContextName() {
        String warFileName = this.appName;
        return warFileName.contains("-") ? warFileName.substring(0, warFileName.indexOf(45)) : warFileName;
    }
}
