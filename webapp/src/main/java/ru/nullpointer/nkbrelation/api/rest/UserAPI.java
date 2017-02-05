package ru.nullpointer.nkbrelation.api.rest;

import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import ru.nullpointer.nkbrelation.domain.UserInfo;
import ru.nullpointer.nkbrelation.service.UserService;

/**
 *
 * @author ankostyuk
 */
@Controller
public class UserAPI extends AbstractAPI {

    @Resource
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "/api/users/me/info", method = RequestMethod.GET)
    public UserInfo getInfo() {
        return userService.getUserInfo();
    }
}
