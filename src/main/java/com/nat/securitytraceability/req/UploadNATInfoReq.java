package com.nat.securitytraceability.req;

import com.nat.securitytraceability.data.NATInfo;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 上传核酸信息请求体
 * @author hhf
 */
@Data
public class UploadNATInfoReq implements Serializable {

    private static final long serialVersionUID = 1L;

    List<NATInfo> natInfos;
}
