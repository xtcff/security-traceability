package com.nat.securitytraceability.data;

import com.nat.securitytraceability.util.RSAUtil;
import lombok.Data;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 区块链
 * @author hhf
 */
@Data
@Component
public class BlockChain {

    /**
     * 当前节点的区块链结构
     */
    private List<Block> blockChain = new CopyOnWriteArrayList<>();

    /**
     * 要放到新区块的核酸信息集合
     */
    private List<NATInfo> packedNATInfos = new CopyOnWriteArrayList<>();

    /**
     * 当前节点的socket对象
     */
    private List<WebSocket> socketsList = new CopyOnWriteArrayList<>();

    /**
     * 节点公钥
     */
    @Value("${rsaKeyPair.publicKey}")
    private String publicKey;

    /**
     * 节点私钥
     */
    @Value("${rsaKeyPair.privateKey}")
    private String privateKey;

    /**
     * 所有节点的公钥
     */
    private Set<String> publicKeys = new HashSet<>();

    /**
     * 当前节点p2pserver端口号
     */
    @Value("${block.p2pport}")
    private int p2pport;

    /**
     * 要连接的节点地址
     */
    @Value("#{'${block.address}'.split(',')}")
    private String[] addresses;

    /**
     * 获取最新的区块，即当前链上最后一个区块
     *
     * @return Block
     */
    public Block getLatestBlock() {
        return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
    }
}
