package ru.nullpointer.nkbrelation.service.security;

import java.util.Set;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public interface SecurityService {

    TicketPrincipal authenticate(String login, String password, String ip);

    TicketPrincipal authenticateWithTicket(String ticket);

    String getAuthenticatedUserId();

    Set<String> getAuthenticatedPermissions();

    Authentication getAuthentication();

    void ensureHasPermission(String permission);

    void accessDenied(String message);
}
