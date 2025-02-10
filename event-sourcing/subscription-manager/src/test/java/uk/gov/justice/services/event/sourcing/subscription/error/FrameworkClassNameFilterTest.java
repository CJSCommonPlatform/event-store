package uk.gov.justice.services.event.sourcing.subscription.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FrameworkClassNameFilterTest {

    @InjectMocks
    private FrameworkClassNameFilter frameworkClassNameFilter;

    @Test
    public void shouldReturnTrueIfClassNameIsFrameworkClass() throws Exception {

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn("uk.gov.justice.services.event.sourcing.subscription.SomeClass");

        assertThat(frameworkClassNameFilter.isFrameworkOrContextClass(stackTraceElement), is(true));
    }

    @Test
    public void shouldReturnTrueIfClassNameIsContextClass() throws Exception {

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn("uk.gov.moj.cpp.people.event.listener.SomeEventListener");

        assertThat(frameworkClassNameFilter.isFrameworkOrContextClass(stackTraceElement), is(true));
    }

    @Test
    public void shouldReturnFalseIfClassNameIsFrameworkClassButClassIsDeltaspikeProxyClass() throws Exception {

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn("uk.gov.moj.services.cakeshop.persistence.CakeShopRepository$$DSPartialBeanProxy");

        assertThat(frameworkClassNameFilter.isFrameworkOrContextClass(stackTraceElement), is(false));
    }

    @Test
    public void shouldReturnFalseIfClassNameIsContextClassButClassIsDeltaspikeProxyClass() throws Exception {

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn("uk.gov.moj.cpp.people.persistence.PeopleRepository$$DSPartialBeanProxy");

        assertThat(frameworkClassNameFilter.isFrameworkOrContextClass(stackTraceElement), is(false));
    }

    @Test
    public void shouldReturnFalseIfClassNameIsNeitherFrameworkNorContextClass() throws Exception {

        final StackTraceElement stackTraceElement = mock(StackTraceElement.class);

        when(stackTraceElement.getClassName()).thenReturn("org.jboss.invocation.InterceptorContext");

        assertThat(frameworkClassNameFilter.isFrameworkOrContextClass(stackTraceElement), is(false));
    }
}