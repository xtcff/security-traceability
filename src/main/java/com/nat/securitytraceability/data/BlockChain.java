package com.nat.securitytraceability.data;

import lombok.Data;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
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
     * 挖矿的难度系数
     */
    @Value("${block.difficulty}")
    private int difficulty;

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

    /**
     * 替换最新的区块
     */
    public void setLatestBlock(Block block) {
        packedNATInfos.removeAll(getLatestBlock().getNatInfos());
        blockChain.remove(blockChain.size() - 1);
        blockChain.add(block);
        packedNATInfos.addAll(block.getNatInfos());
    }
}
