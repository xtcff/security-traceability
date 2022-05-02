package com.nat.securitytraceability.req;

import com.nat.securitytraceability.data.Person;
import lombok.Data;
import java.io.Serializable;

/**
 * 用户查询核酸信息请求体
 * @author hhf
 */
@Data
public class QueryNATInfosReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 个人信息
     */
    private Person person;
}
