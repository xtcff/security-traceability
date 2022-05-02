package com.nat.securitytraceability.p2p;

import com.nat.securitytraceability.service.P2PService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * p2p客户端
 * @author hhf
 */
@Slf4j
@Component
public class P2PClient {

    @Resource
    P2PService p2pService;


    public void connectToPeer(String[] addresses) {
        for(String address: addresses){
            try {
                final WebSocketClient socketClient = new WebSocketClient(new URI(address)) {

                    /**
                     * 连接建立后触发
                     */
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        //客户端发送请求，查询最新区块
                        log.info("P2PClient connectToPeer 连接节点 [{}] 成功",address);
                        p2pService.write(this, p2pService.queryLatestBlockMsg());
                        p2pService.getSockets().add(this);
                    }

                    /**
                     * 接收到消息时触发
                     * @param msg msg
                     */
                    @Override
                    public void onMessage(String msg) {
                        log.info("P2PClient connectToPeer 接收到节点 [{}] 消息:[{}]", address, msg);
                        p2pService.handleMessage(this, msg, p2pService.getSockets());
                    }

                    @SneakyThrows
                    @Override
                    public void onClose(int i, String msg, boolean b) {
                        p2pService.getSockets().remove(this);
                        log.info("P2PClient connectToPeer 与节点 [{}] 的连接已关闭: [{}], 正在重连...", address, msg);
                        Thread.sleep(30000);
                        String[] addes = new String[]{address};
                        connectToPeer(addes);
                    }

                    @Override
                    public void onError(Exception e) {
                        p2pService.getSockets().remove(this);
                        log.info("P2PClient connectToPeer 与节点 [{}] 的连接出现错误: [{}]", address, e.getMessage());
                    }
                };
                socketClient.connect();
            } catch (URISyntaxException e) {
                log.info("p2p connect is error:" + e.getMessage());
            }
        }
    }
}
