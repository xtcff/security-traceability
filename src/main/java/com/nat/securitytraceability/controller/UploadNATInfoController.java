package com.nat.securitytraceability.controller;

import com.nat.securitytraceability.req.UploadNATInfoReq;
import com.nat.securitytraceability.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 工作人员上传核酸信息controller
 * @author hhf
 */
@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadNATInfoController {

    @Resource
    UploadService uploadService;

    /**
     * 上传采样信息
     * @return String
     */
    @PostMapping("/uploadSamplingNATInfo")
    public String uploadSamplingNATInfo(@RequestBody UploadNATInfoReq uploadNATInfoReq) {
        log.info("UploadNATInfoController uploadSamplingNATInfo start, [{}]", uploadNATInfoReq);
        String resp = uploadService.uploadNATInfo(uploadNATInfoReq);
        log.info("UploadNATInfoController uploadSamplingNATInfo end, resp = [{}]", resp);
        return resp;
    }

    /**
     * 上传检测信息
     * @return String
     */
    @PostMapping("/uploadDetectNATInfo")
    public String uploadDetectNATInfo(@RequestBody UploadNATInfoReq uploadNATInfoReq) {
        log.info("UploadNATInfoController uploadDetectNATInfo start, [{}]", uploadNATInfoReq);
        String resp = uploadService.uploadNATInfo(uploadNATInfoReq);
        log.info("UploadNATInfoController uploadDetectNATInfo end, resp = [{}]", resp);
        return resp;
    }
}
