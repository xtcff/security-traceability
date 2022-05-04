package com.nat.securitytraceability.controller;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.data.User;
import com.nat.securitytraceability.req.LoginReq;
import com.nat.securitytraceability.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 管理员登录
 * @author hhf
 */
@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    UserService userService;

    /**
     * 管理员登录
     * @return String
     */
    @PostMapping("/admin")
    public String adminLogin(@RequestBody LoginReq loginReq) throws Exception {
        log.info("LoginController adminLogin start, [{}]", loginReq);
        User user = userService.adminLogin(loginReq.getUsername());
        log.info("LoginController adminLogin end, resp = [{}]", user);
        if (user.getPassword().equals(loginReq.getPassword())) {
            return JSON.toJSONString(user);
        } else {
            throw new Exception("用户名或密码错误");
        }
    }
}
