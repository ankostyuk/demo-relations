package ru.nullpointer.nkbrelation.service.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public class SimpleSecurityService extends AbstractSecurityService implements InitializingBean {

    private static final String TICKET_PREFIX = "ticket_";
    //
    private UserDetailsService userDetailsService;

    @Override
    public TicketPrincipal authenticate(String login, String password, String ip) {
        logger.debug("authenticate ip: {}", ip);
        UserDetails details = userDetailsService.loadUserByUsername(login);
        if (!details.getPassword().equals(password)) {
            throw new BadCredentialsException("Authentication failed for " + login);
        }

        String userId = details.getUsername();
        String ticket = TICKET_PREFIX + details.getUsername();

        return new TicketPrincipal(userId, ticket, ticket, getPermissions(details.getAuthorities()));
    }

    @Override
    public TicketPrincipal authenticateWithTicket(String ticket) {
        if (ticket == null
                || !ticket.startsWith(TICKET_PREFIX)
                || ticket.length() == TICKET_PREFIX.length()) {
            throw new BadCredentialsException("Invalid ticket");
        }
        String userId = ticket.substring(TICKET_PREFIX.length());
        UserDetails details = userDetailsService.loadUserByUsername(userId);

        return new TicketPrincipal(userId, ticket, ticket, getPermissions(details.getAuthorities()));
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(userDetailsService, "'userService' must be set");
    }
}
