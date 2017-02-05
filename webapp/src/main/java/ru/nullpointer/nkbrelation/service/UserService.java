package ru.nullpointer.nkbrelation.service;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import ru.nullpointer.nkbrelation.domain.UserInfo;
import ru.nullpointer.nkbrelation.service.security.SecurityService;

/**
 * @author ankostyuk
 * @author Alexander Yastrebov
 */
@Service
public class UserService {

    @Resource
    private SecurityService securityService;

    public UserInfo getUserInfo() {
        String userId = securityService.getAuthenticatedUserId();

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setPermissions(securityService.getAuthenticatedPermissions());

        return userInfo;
    }
}
