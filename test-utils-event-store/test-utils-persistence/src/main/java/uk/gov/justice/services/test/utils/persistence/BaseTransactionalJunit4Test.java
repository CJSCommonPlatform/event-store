package uk.gov.justice.services.test.utils.persistence;

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Before;

/**
 * This class should be extended by any test which require managed persistence/transactions provided
 * by deltaspike via JPA and currently deltaspike is not supporting junit5 and hence all deltaspike tests
 * can't be migrated to junit5, but they will be run using jupiter vintage engine
 */
@Deprecated
public abstract class BaseTransactionalJunit4Test {

    @Inject
    UserTransaction userTransaction;

    @Before
    public final void setup() throws Exception {
        userTransaction.begin();
        setUpBefore();
    }

    @After
    public final void tearDown() throws Exception {
        tearDownAfter();
        userTransaction.rollback();
    }

    /**
     * Implement this method if you require to do something before the test
     */
    protected void setUpBefore() {

    }

    /**
     * Implement this method if you require to do something after the test
     */
    protected void tearDownAfter() {

    }
}
