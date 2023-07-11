package uk.gov.justice.services.eventsourcing.source.api.security;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;

import uk.gov.justice.services.core.accesscontrol.AccessControlViolationException;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedHashMap;

import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccessControllerTest {

    @Mock
    private SystemUserProvider systemUserProvider;

    @InjectMocks
    private AccessController accessController;

    @Test
    public void shouldPassIfSystemUserIdPassedInHeader() throws Exception {
        final UUID systemUserId = randomUUID();
        when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.of(systemUserId));

        accessController.checkAccessControl(headersWithUserId(systemUserId));
    }

    @Test
    public void shouldThrowExceptionIfUserPassedInHeaderIsNotSystemUser() throws Exception {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.of(randomUUID()));

        assertThrows(AccessControlViolationException.class, () -> accessController.checkAccessControl(headersWithUserId(randomUUID())));
    }

    @Test
    public void shouldThrowExceptionIfNoSystemUserDefined() throws Exception {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.empty());
        assertThrows(AccessControlViolationException.class, () -> accessController.checkAccessControl(headersWithUserId(randomUUID())));
    }

    @Test
    public void shouldThrowExceptionIfUserIdNotProvidedInRequest() throws Exception {
        when(systemUserProvider.getContextSystemUserId()).thenReturn(Optional.of(randomUUID()));

        assertThrows(AccessControlViolationException.class, () -> accessController.checkAccessControl(emptyHeaders()));
    }

    private ResteasyHttpHeaders emptyHeaders() {
        return new ResteasyHttpHeaders(new MultivaluedHashMap<>());
    }

    private ResteasyHttpHeaders headersWithUserId(final UUID userId) {
        final MultivaluedHashMap<String, String> headersMap = new MultivaluedHashMap<>();
        headersMap.put(USER_ID, asList(userId.toString()));
        return new ResteasyHttpHeaders(headersMap);
    }
}