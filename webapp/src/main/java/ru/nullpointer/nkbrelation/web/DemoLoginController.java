package ru.nullpointer.nkbrelation.web;

import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.nullpointer.nkbrelation.service.security.SecurityService;
import ru.nullpointer.nkbrelation.service.security.TicketPrincipal;

/**
 *
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
@Controller
public class DemoLoginController {

    private final Logger logger = LoggerFactory.getLogger(DemoLoginController.class);
    //
    @Resource
    private SecurityService securityService;
    //
    private static final String[] TICKET_COOKIE_NAMES = new String[]{
        "creditnet_ticket", //"creditnet_backend_ticket",
    };

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("login", "password");
        binder.setRequiredFields("login", "password");
    }

    @RequestMapping(value = "/demo/login", method = RequestMethod.GET)
    public String handleGet(HttpServletRequest request, ModelMap model) {
        Cookie[] cc = request.getCookies();

        if (cc != null) {
            cookie_found:
            for (Cookie c : cc) {
                for (String name : TICKET_COOKIE_NAMES) {
                    if (name.equals(c.getName())) {
                        model.addAttribute("ticket", c.getValue());
                        break cookie_found;
                    }
                }
            }
        }

        return "views/demo/login";
    }

    @RequestMapping(value = "/demo/login", method = RequestMethod.POST)
    public String handlePost(@ModelAttribute("model") @Valid Login login, BindingResult result,
            HttpServletRequest request, HttpServletResponse response) {
        logger.debug("handle post: {}", login);
        if (result.hasErrors()) {
            return "views/demo/login";
        }

        TicketPrincipal principal;
        try {
            principal = securityService.authenticate(login.getLogin(), login.getPassword(), request.getRemoteAddr());
        } catch (Exception ex) { // NOPMD
            logger.debug("Authenticate Exception Message: {}", ex.getMessage());

            result.reject("auth.error", ex.getMessage());
            return "views/demo/login";
        }

        String ticket = principal.getTicketId();
        logger.debug("token: {}", ticket);

        for (String name : TICKET_COOKIE_NAMES) {
            Cookie c = new Cookie(name, ticket);
            c.setPath("/");
            response.addCookie(c);
        }
        return "redirect:/report";
    }

    @RequestMapping(value = "/demo/logout", method = RequestMethod.GET)
    public String handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        for (String name : TICKET_COOKIE_NAMES) {
            Cookie c = new Cookie(name, "");
            c.setMaxAge(0);
            c.setPath("/");
            response.addCookie(c);
        }
        return "redirect:/demo/login";
    }

    public static class Login {

        private String login;
        private String password;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
