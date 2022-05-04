package com.nat.securitytraceability.controller;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.req.QueryNATInfosReq;
import com.nat.securitytraceability.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

/**
 * 用户查询核酸信息controller
 * @author hhf
 */
@Slf4j
@RestController
@RequestMapping("/query")
public class QueryController {

    @Resource
    QueryService queryService;

    /**
     * 根据个人信息查看核酸报告
     * @return String
     */
    @PostMapping("/queryNATInfosByPerson")
    public String queryNATInfosByPerson(@RequestBody QueryNATInfosReq queryNATInfosReq) throws Exception {
        log.info("QueryController queryNATInfos start, [{}]", queryNATInfosReq);
        String resp = JSON.toJSONString(queryService.queryNATInfosByPerson(queryNATInfosReq));
        log.info("QueryController queryNATInfos end, resp = [{}]", resp);
        return resp;
    }

    /**
     * 查询所有核酸报告
     * @return String
     */
    @PostMapping("/queryNATInfos")
    public String queryNATInfos() throws Exception {
        log.info("QueryController queryNATInfos start");
        String resp = JSON.toJSONString(queryService.queryNATInfos());
        log.info("QueryController queryNATInfos end, resp = [{}]", resp);
        return resp;
    }
}
