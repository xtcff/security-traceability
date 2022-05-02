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
     * 根据个人信息查询所有核酸信息
     *
     * @return String
     */
    public User adminLogin(String username) {
        return userMapper.selectOne(Wrappers.lambdaQuery(User.class).
                eq(StringUtils.isNotBlank(username), User::getUsername, username).
                eq(User::getDeleteFlag, "false"));
    }
}