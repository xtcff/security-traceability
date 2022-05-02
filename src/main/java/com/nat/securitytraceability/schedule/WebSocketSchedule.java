package com.nat.securitytraceability.schedule;

import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.p2p.P2PClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Slf4j
@Component
@Async
public class WebSocketSchedule {

    @Resource
    private P2PClient p2pClient;

    @Resource
    private BlockChain blockChain;

    //    0/5 * * * * ?             0 */1 * * * ?
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void scheduled(){
//        log.info("我是任务！！！");
////        p2pClient.connectToPeer(blockChain.getAddresses());
//    }
}
