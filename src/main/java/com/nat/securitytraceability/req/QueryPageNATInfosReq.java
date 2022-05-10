package com.nat.securitytraceability.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 分页查询核酸报告请求
 * @author hhf
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class QueryPageNATInfosReq extends PageDto {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "报告单号")
    private String reportId;

    @ApiModelProperty(value = "身份证号")
    private String idNumber;

    @ApiModelProperty(value = "检测结果 0:阴性, 1:阳性")
    private String detectResult;

    @ApiModelProperty(value = "采样时间区间起始值")
    private Date samplingTimeBegin;

    @ApiModelProperty(value = "采样时间区间结束值")
    private Date samplingTimeEnd;

}
