package ru.nullpointer.nkbrelation.service.security;

import creditnet.cnas.auth.SecurityServiceEndpoint;
import creditnet.cnas.auth.SsoTicket;
import creditnet.cnas.auth.exception.AuthException;
import creditnet.cnas.common.NcbUUID;
import creditnet.cnas.service.ClientRequestServiceEndpoint;
import creditnet.cnas.service.PurchaseListPossibility;
import creditnet.cnas.service.PurchasePossibility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.Assert;
import ru.nullpointer.nkbrelation.service.billing.Product;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public class CnasSecurityService extends AbstractSecurityService implements InitializingBean {

    private SecurityServiceEndpoint securityServiceEndpoint;
    private ClientRequestServiceEndpoint clientRequestServiceEndpoint;
    private Integer ticketExpiryPeriod = 3600;
    //
    private static final String[][] PERMISSION_PRODUCTS = new String[][]{
        {Permissions.SEARCH.name(), Product.RELATIONS_SEARCH.getProductCode()}, //
        {Permissions.SEARCH_RELATED.name(), Product.RELATIONS_FIND_RELATED.getProductCode()}, //
        {Permissions.SEARCH_TRACES.name(), Product.RELATIONS_FIND_RELATIONS.getProductCode()}, //
        //
        {Permissions.REQUEST_EGRUL_COMPANY.name(), Product.EGRUL_COMPANY.getProductCode()}, //
        {Permissions.REQUEST_EGRUL_INDIVIDUAL_FOUNDER.name(), Product.EGRUL_INDIVIDUAL_FOUNDER.getProductCode()}, //
        {Permissions.REQUEST_EGRUL_INDIVIDUAL_EXECUTIVE.name(), Product.EGRUL_INDIVIDUAL_EXECUTIVE.getProductCode()}, //
    };

    @Override
    public TicketPrincipal authenticate(String login, String password, String ip) {
        Assert.hasText(login);
        Assert.hasText(password);

        SsoTicket ssoTicket;
        try {
            ssoTicket = securityServiceEndpoint.authenticate(login, password, ip, ticketExpiryPeriod);
        } catch (Exception ex) { // NOPMD
            throw new BadCredentialsException("Authentication failed", ex);
        }

        String userId = ssoTicket.getUserUUID().getId();
        String ticketId = ssoTicket.getId().getId();

        return new TicketPrincipal(userId, ticketId, ssoTicket, getPermissions(ssoTicket));
    }

    @Override
    public TicketPrincipal authenticateWithTicket(String ticket) {
        Assert.notNull(ticket);
        NcbUUID uuid = parseUUID(ticket);
        if (uuid == null) {
            throw new BadCredentialsException("Invalid ticket");
        }

        SsoTicket ssoTicket;
        try {
            ssoTicket = securityServiceEndpoint.authenticateWithTicket(uuid);
        } catch (Exception ex) { // NOPMD
            throw new AuthenticationServiceException("Authentication failed", ex);
        }

        String userId = ssoTicket.getUserUUID().getId();
        String ticketId = ssoTicket.getId().getId();

        return new TicketPrincipal(userId, ticketId, ssoTicket, getPermissions(ssoTicket));
    }

    public void setSecurityServiceEndpoint(SecurityServiceEndpoint securityServiceEndpoint) {
        this.securityServiceEndpoint = securityServiceEndpoint;
    }

    public void setClientRequestServiceEndpoint(ClientRequestServiceEndpoint clientRequestServiceEndpoint) {
        this.clientRequestServiceEndpoint = clientRequestServiceEndpoint;
    }

    public void setTicketExpiryPeriod(Integer ticketExpiryPeriod) {
        this.ticketExpiryPeriod = ticketExpiryPeriod;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(securityServiceEndpoint, "'securityServiceEndpoint' must be set");

        Assert.notNull(ticketExpiryPeriod, "'ticketExpiryPeriod' must be set");
        Assert.isTrue(ticketExpiryPeriod > 0, "'ticketExpiryPeriod' must be greater than 0");
    }

    private NcbUUID parseUUID(String s) {
        try {
            return new NcbUUID(s);
        } catch (NumberFormatException ex) {
            logger.warn("Illegal UUID: {}", s);
            return null;
        }
    }

    private Set<String> getPermissions(SsoTicket ssoTicket) {
        List<String> products = new ArrayList<String>(PERMISSION_PRODUCTS.length);
        for (int i = 0; i < PERMISSION_PRODUCTS.length; i++) {
            products.add(PERMISSION_PRODUCTS[i][1]);
        }

        PurchaseListPossibility purchasePossibilities;
        try {
            purchasePossibilities = clientRequestServiceEndpoint.getPurchasePossibilities(ssoTicket, products);
        } catch (AuthException ex) {
            throw new AuthenticationServiceException("Product purchase info request failed", ex);
        }

        logger.debug("possibilities: {}", purchasePossibilities.getProductPossibilities());

        List<PurchasePossibility> possibilities = purchasePossibilities.getProductPossibilities();
        Assert.isTrue(PERMISSION_PRODUCTS.length == possibilities.size());

        Set<String> result = new HashSet<String>(Arrays.asList(Permissions.stringValues()));
        for (int i = 0; i < PERMISSION_PRODUCTS.length; i++) {
            if (!possibilities.get(i).isPossible()) {
                result.remove(PERMISSION_PRODUCTS[i][0]);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
