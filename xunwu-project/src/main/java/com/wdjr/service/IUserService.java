package com.wdjr.service;

import com.wdjr.entity.User;

/**
 * 用户服务
 */

public interface IUserService {
    User findUserName(String userName);
}
