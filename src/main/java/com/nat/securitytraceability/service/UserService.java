package com.nat.securitytraceability.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nat.securitytraceability.data.User;
import com.nat.securitytraceability.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * 登录服务
 * @author hhf
 */
@Slf4j
@Service
public class UserService {

    @Resource
    UserMapper userMapper;

    /**
     * 查询用户
     * @return User
     */
    public User adminLogin(String username) {
        return userMapper.selectOne(Wrappers.lambdaQuery(User.class).
                eq(StringUtils.isNotBlank(username), User::getUsername, username).
                eq(User::getDeleteFlag, "false"));
    }
}