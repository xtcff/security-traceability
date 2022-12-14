package com.nat.securitytraceability.p2p;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.service.P2PService;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;

/**
 * p2p服务端
 * @author hhf
 */
@Slf4j
@Component
public class P2PServer {


    @Resource
    P2PService p2pService;

    @Resource
    BlockChain blockChain;

    public void initP2PServer(int port) {
        WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

            /**
             * 连接建立后触发
             */
            @Override
            public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
                p2pService.getSockets().add(webSocket);
                log.info("已连接节点: [{}], 端口号为: [{}]",
                        webSocket.getRemoteSocketAddress().getAddress().toString(), webSocket.getRemoteSocketAddress().getPort());
                //广播自己的公钥
                Message msg = new Message(BlockConstant.BROADCAST_PUBLIC_KEY, blockChain.getPublicKey());
                p2pService.broadcast(JSON.toJSONString(msg));
            }

            /**
             * 接收到客户端消息时触发
             */
            @Override
            public void onMessage(WebSocket webSocket, String msg) {
                //作为服务端，业务逻辑处理
                p2pService.handleMessage(webSocket, msg);
            }

            /**
             * 连接关闭后触发
             */
            @Override
            public void onClose(WebSocket webSocket, int i, String s, boolean b) {
                p2pService.getSockets().remove(webSocket);
                log.info("connection closed to address: [{}]", webSocket.getRemoteSocketAddress());
            }

            /**
             * 发生错误时触发
             */
            @Override
            public void onError(WebSocket webSocket, Exception e) {
                p2pService.getSockets().remove(webSocket);
                log.info("connection failed to address:[{}]", webSocket.getRemoteSocketAddress());
            }


            @Override
            public void onStart() {


            }


        };
        socketServer.start();
        log.info("listening websocket p2p port on: [{}]", port);
    }
}

