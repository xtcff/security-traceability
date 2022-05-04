package com.nat.securitytraceability.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 区块结构
 *
 * @author hhf
 *
 */
@Data
public class Block implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区块索引号(区块高度)
     */
    private int index;

    /**
     * 当前区块的hash值,区块唯一标识
     */
    private String hash;

    /**
     * 前一个区块的hash值
     */
    private String previousHash;

    /**
     * 生成区块的时间戳
     */
    private long timestamp;

    /**
     * 证明是哪个节点生成的该区块
     */
    private String publicKey;

    /**
     * 加密后的核酸信息集合
     */
    private String natInfosString;
}
