package com.nat.securitytraceability.service;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.data.NATInfo;
import com.nat.securitytraceability.p2p.BlockConstant;
import com.nat.securitytraceability.p2p.Message;
import com.nat.securitytraceability.p2p.P2PClient;
import com.nat.securitytraceability.p2p.P2PServer;
import com.nat.securitytraceability.util.RSAUtil;
import com.nat.securitytraceability.util.RocksDBUtil;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;

/**
 * p2p服务
 * @author hhf
 */
@Slf4j
@Service
public class P2PService implements ApplicationRunner {


    @Resource
    private BlockChainService blockChainService;

    @Resource
    private BlockChain blockChain;

    @Resource
    private P2PServer p2pServer;

    @Resource
    private P2PClient p2pClient;

    @Resource
    RocksDBUtil rocksDBUtil;

    /**
     * 全网广播消息
     * @param message message
     */
    public void broadcast(String message) {
        List<WebSocket> socketsList = this.getSockets();
        if (CollectionUtils.isEmpty(socketsList)) {
            return;
        }
        log.info("全网广播消息开始：");
        for (WebSocket socket : socketsList) {
            this.write(socket, message);
        }
        log.info("全网广播消息结束");
    }

    /**
     * 客户端和服务端共用的消息处理方法
     * @param webSocket webSocket
     * @param msg msg
     */
    public void handleMessage(WebSocket webSocket, String msg) {
        try {
            Message message = JSON.parseObject(msg, Message.class);
            switch (message.getType()) {
                //客户端请求查询最新的区块:1
                case BlockConstant.QUERY_LATEST_BLOCK:
                    write(webSocket, responseLatestBlockMsg());
                    break;
                //接收到服务端返回的最新区块:2
                case BlockConstant.RESPONSE_LATEST_BLOCK:
                    handleBlockResponse(message.getData());
                    break;
                //客户端请求查询整个区块链:3
                case BlockConstant.QUERY_BLOCKCHAIN:
                    write(webSocket, responseBlockChainMsg());
                    break;
                //接收到其他节点发送的整条区块链信息:4
                case BlockConstant.RESPONSE_BLOCKCHAIN:
                    handleBlockChainResponse(message.getData());
                    break;
                //接收到公钥:5
                case BlockConstant.BROADCAST_PUBLIC_KEY:
                    addPublicKeyToBlockChain(message.getData());
                    break;
                //接收到最新核酸信息
                case BlockConstant.RESPONSE_LATEST_NATINFOS:
                    updatePackedNATInfos(message.getData());
                    break;
                //客户端请求查询最新核酸信息
                case BlockConstant.QUERY_LATEST_NATINFOS:
                    write(webSocket, responsePackedNATInfos());
                    break;
            }
        } catch (Exception e) {
            log.info("处理IP地址为: [{}], 端口号为: [{}]的p2p消息错误: [{}]",
                    webSocket.getRemoteSocketAddress().getAddress().toString(), webSocket.getRemoteSocketAddress().getPort(), e.getMessage());
        }
    }

    /**
     * 处理其它节点发送过来的公钥
     * @param publicKeyData publicKeyData
     */
    public synchronized void addPublicKeyToBlockChain(String publicKeyData) {
        blockChain.getPublicKeys().add(blockChain.getPublicKey());
        blockChain.getPublicKeys().add(publicKeyData);
    }

    /**
     * 处理其它节点发送过来的最新核酸信息
     * @param packedNATInfosData packedNATInfosData
     */
    public synchronized void updatePackedNATInfos(String packedNATInfosData) throws Exception {
        blockChain.setPackedNATInfos(JSON.parseArray(packedNATInfosData, NATInfo.class));
        rocksDBUtil.updateNATInfos(blockChain.getPackedNATInfos());
    }

    /**
     * 处理其它节点发送过来的区块信息
     * @param blockData blockData
     */
    public synchronized void handleBlockResponse(String blockData) {
        log.info("P2PService handleBlockResponse start, [{}]", blockData);
        //反序列化得到其它节点的最新区块信息
        Block latestBlockReceived = JSON.parseObject(blockData, Block.class);

        //进行解密并验证是否是合法节点生成的区块
        if (!blockChain.getPublicKeys().contains(latestBlockReceived.getPublicKey())){
            log.error("非法节点，其公钥为: [{}], 当前节点公钥列表为: [{}]", latestBlockReceived.getPublicKey(), blockChain.getPublicKeys());
            return;
        }
        try{
            String s = RSAUtil.decryptByPublicKey(latestBlockReceived.getPublicKey(), latestBlockReceived.getNatInfosString());
            List<NATInfo> natInfos = JSON.parseArray(s, NATInfo.class);
            log.info("核酸信息解密成功: [{}]", natInfos);
        } catch (Exception e) {
            log.error("核酸信息解密失败被丢弃");
            return ;
        }

        //当前节点的最新区块
        Block latestBlock = blockChain.getLatestBlock();
        if(latestBlock != null) {
            //如果接收到的区块高度比本地区块高度大的多
            if(latestBlockReceived.getIndex() > latestBlock.getIndex() + 1) {
                broadcast(queryBlockChainMsg());
                log.info("重新查询所有节点上的整条区块链");
            }else if (latestBlockReceived.getIndex() > latestBlock.getIndex() &&
                    latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
                if (blockChainService.addBlock(latestBlockReceived)) {
                    broadcast(responseLatestBlockMsg());
                    log.info("将新接收到的区块加入到本地的区块链");
                }
            }
        }else {
            broadcast(queryBlockChainMsg());
            log.info("重新查询所有节点上的整条区块链");
        }
    }

    /**
     * 处理其它节点发送过来的区块链信息
     * @param blockData blockData
     *
     */
    public synchronized void handleBlockChainResponse(String blockData) {
        //反序列化得到其它节点的整条区块链信息
        List<Block> receiveBlockchain = JSON.parseArray(blockData, Block.class);
        if(!CollectionUtils.isEmpty(receiveBlockchain) && blockChainService.isValidChain(receiveBlockchain)) {
            //根据区块索引先对区块进行排序
            receiveBlockchain.sort(Comparator.comparingInt(Block::getIndex));

            //其它节点的最新区块
            Block latestBlockReceived = receiveBlockchain.get(receiveBlockchain.size() - 1);
            //当前节点的最新区块
            Block latestBlock = blockChain.getLatestBlock();

            if(latestBlock == null) {
                //替换本地的区块链
                blockChainService.replaceChain(receiveBlockchain);
            }else {
                //其它节点区块链如果比当前节点的长，则处理当前节点的区块链
                if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
                    if (latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
                        if (blockChainService.addBlock(latestBlockReceived)) {
                            broadcast(responseLatestBlockMsg());
                        }
                        log.info("将新接收到的区块加入到本地的区块链");
                    } else {
                        // 用长链替换本地的短链
                        blockChainService.replaceChain(receiveBlockchain);
                    }
                }
            }
        }
    }

    /**
     * 向其它节点发送消息
     * @param ws ws
     * @param message message
     */
    public void write(WebSocket ws, String message) {
        log.info("向IP地址为: [{}], 端口号为: [{}]发送p2p消息, 消息类型为: [{}]",
                ws.getRemoteSocketAddress().getAddress().toString(), ws.getRemoteSocketAddress().getPort(), message);
        ws.send(message);
    }

    /**
     * 查询整条区块链
     * @return String
     */
    public String queryBlockChainMsg() {
        return JSON.toJSONString(new Message(BlockConstant.QUERY_BLOCKCHAIN));
    }

    /**
     * 返回整条区块链数据
     * @return String
     */
    public String responseBlockChainMsg() {
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_BLOCKCHAIN);
        msg.setData(JSON.toJSONString(blockChain.getBlockChain()));
        return JSON.toJSONString(msg);
    }

    /**
     * 查询最新的区块
     * @return String
     */
    public String queryLatestBlockMsg() {
        return JSON.toJSONString(new Message(BlockConstant.QUERY_LATEST_BLOCK));
    }

    /**
     * 返回最新的区块
     * @return String
     */
    public String responseLatestBlockMsg() {
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
        Block b = blockChain.getLatestBlock();
        msg.setData(JSON.toJSONString(b));
        return JSON.toJSONString(msg);
    }

    /**
     * 查询最新的核酸信息
     * @return String
     */
    public String queryLatestNATInfos() {
        return JSON.toJSONString(new Message(BlockConstant.QUERY_LATEST_NATINFOS));
    }

    /**
     * 返回最新的核酸信息
     * @return String
     */
    public String responsePackedNATInfos() {
        Message msg = new Message();
        msg.setType(BlockConstant.RESPONSE_LATEST_NATINFOS);
        List<NATInfo> natInfos = blockChain.getPackedNATInfos();
        msg.setData(JSON.toJSONString(natInfos));
        return JSON.toJSONString(msg);
    }

    public List<WebSocket> getSockets(){
        return blockChain.getSocketsList();
    }

    @Override
    public void run(ApplicationArguments args) throws NoSuchAlgorithmException {
        try {
            blockChain.setBlockChain(rocksDBUtil.getBlockChain().getBlockChain());
            blockChain.setPackedNATInfos(rocksDBUtil.getNatInfos());
            log.info("已从本地数据库加载完成区块链, 区块链信息: [{}]", blockChain.getBlockChain());
        } catch (Exception e) {
            log.error("数据库查询区块链错误, msg: [{}]", e.getMessage());
        }
        log.info("公钥: [{}], 私钥: [{}]", blockChain.getPublicKey(), blockChain.getPrivateKey());
        p2pServer.initP2PServer(blockChain.getP2pport());
        p2pClient.connectToPeer(blockChain.getAddresses());
    }

}
