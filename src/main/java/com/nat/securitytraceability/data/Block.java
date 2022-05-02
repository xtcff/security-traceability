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
     * 工作量证明，计算正确hash值的次数
     */
    private int nonce;

    /**
     * 当前区块存储的核酸信息集合（例如检测时间、检测医院、检测人、被检测人等）
     */
    private List<NATInfo> natInfos;
}
