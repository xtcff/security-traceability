package com.nat.securitytraceability.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
@ApiModel(description = "分页请求")
public class PageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    //当前页
    @ApiModelProperty(value = "当前页")
    private int pageNum = 1;

    //每页的数量
    @ApiModelProperty(value = "每页的数量")
    private int pageSize = 10;

    @ApiModelProperty(value = "是否正序", required = false)
    private boolean asc = false;
}
