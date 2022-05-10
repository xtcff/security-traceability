package com.nat.securitytraceability.req;

import com.nat.securitytraceability.data.Person;
import lombok.Data;
import java.io.Serializable;

/**
 * 查询核酸检测信息返回值
 * @author hhf
 */
@Data
public class QueryNATInfosVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报告ID
     */
    private String reportId;

    /**
     * 被采样人
     */
    private Person isSamplingPerson;

    /**
     * 采样人
     */
    private Person samplingPerson;

    /**
     * 采样时间
     */
    private String samplingTime;

    /**
     * 采样地点
     */
    private String samplingPlace;

    /**
     * 检测机构
     */
    private String detectHospital;

    /**
     * 检测时间
     */
    private String detectTime;

    /**
     * 检测人
     */
    private Person detectPerson;

    /**
     * 检测结果
     */
    private String detectResult;
}
