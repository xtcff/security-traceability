package com.nat.securitytraceability.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 身份证号
     */
    private String idNumber;

    /**
     * 名字
     */
    private String name;

}
