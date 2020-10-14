package com.lazyman.timetennis.privilege;

import com.lazyman.timetennis.user.User;
import com.wisesupport.commons.exceptions.BusinessException;


public class PrivilegeTool {

    public static void assertHasArenaManagerRole(User user) {
        if (!user.isArenaAdmin()) {
            throw new BusinessException("您没有权限");
        }
    }

}
