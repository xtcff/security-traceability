package com.nat.securitytraceability.util;

import com.nat.securitytraceability.req.PageDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 对Page<E>结果进行包装
 * <p/>
 *
 */
@Data
@ApiModel(description = "分页结果封装")
@NoArgsConstructor
public class PageVo<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;
    //当前页
    @ApiModelProperty(value = "当前页", required = true)
    private int pageNum;
    //每页的数量
    @ApiModelProperty(value = "每页的数量", required = true)
    private int pageSize;
    //当前页的数量
    @ApiModelProperty(value = "当前页的数量", required = true)
    private int size;
    //总记录数
    @ApiModelProperty(value = "总记录数", required = true)
    private long total;
    //总页数
    @ApiModelProperty(value = "总页数", required = true)
    private int pages;
    //结果集
    @ApiModelProperty(value = "结果集")
    private List<T> list;

    public PageVo(List<T> list, Long total, int pageNum, int pageSize) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        this.size = list.size();
        this.pages = (int)(total/pageSize);
        if (total % pageSize != 0){
            this.pages ++;
        }
    }

    public PageVo(List<T> list, Long total, PageDto pageDto) {
        this(list, total, pageDto.getPageNum(), pageDto.getPageSize());
    }
}