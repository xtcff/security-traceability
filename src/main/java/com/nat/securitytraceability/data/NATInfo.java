package com.nat.securitytraceability.data;

import lombok.Data;
import java.io.Serializable;

/**
 * 核酸检测信息
 *
 * @author hhf
 *
 */
@Data
public class NATInfo implements Serializable {

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
    private long samplingTime;

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
    private long detectTime;

    /**
     * 检测人
     */
    private Person detectPerson;

    /**
     * 检测结果
     */
    private String detectResult;
}
