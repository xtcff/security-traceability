package com.nat.securitytraceability.req;

import com.nat.securitytraceability.data.NATInfo;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 创建新区块请求体
 * @author hhf
 */
@Data
public class CreateNewBlockReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 核酸信息
     */
    private List<NATInfo> natInfos;

}
