package uk.gov.justice.services.event.sourcing.subscription.manager;

public class TallyCounter {

    private int tallyCount = 0;

    public void incrementByOne() {
        tallyCount++;
    }

    public int getTallyCount() {
        return tallyCount;
    }
}
