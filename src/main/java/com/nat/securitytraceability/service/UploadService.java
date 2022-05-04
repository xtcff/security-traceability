package com.nat.securitytraceability.service;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.data.NATInfo;
import com.nat.securitytraceability.p2p.BlockConstant;
import com.nat.securitytraceability.p2p.Message;
import com.nat.securitytraceability.req.CreateNewBlockReq;
import com.nat.securitytraceability.req.UploadNATInfoReq;
import com.nat.securitytraceability.util.RSAUtil;
import com.nat.securitytraceability.util.RocksDBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 上传服务
 * @author hhf
 */
@Slf4j
@Service
public class UploadService {

    @Resource
    BlockChain blockChain;

    @Resource
    RocksDBUtil rocksDBUtil;

    @Resource
    BlockChainService blockChainService;

    @Resource
    P2PService p2PService;

    @Value("${block.size}")
    private int blockSize;

    public String uploadNATInfo(UploadNATInfoReq uploadNATInfoReq) throws Exception {
        for (NATInfo natInfo : uploadNATInfoReq.getNatInfos()) {
            if (natInfo.getReportId() == null) {
                natInfo.setReportId(UUID.randomUUID().toString().replaceAll("-",""));
                natInfo.setSamplingTime(System.currentTimeMillis());
            } else {
                NATInfo natInfoTemp = getNATInfoByReportId(natInfo.getReportId());
                natInfo.setIsSamplingPerson(natInfoTemp.getIsSamplingPerson());
                natInfo.setSamplingPerson(natInfoTemp.getSamplingPerson());
                natInfo.setSamplingTime(natInfoTemp.getSamplingTime());
                natInfo.setSamplingPlace(natInfoTemp.getSamplingPlace());
                natInfo.setDetectTime(System.currentTimeMillis());
            }
        }
        if (blockChain.getPackedNATInfos().size() + uploadNATInfoReq.getNatInfos().size() <= blockSize) {
            //打包的核酸信息未满, 直接加入信息
            try{
                blockChain.getPackedNATInfos().addAll(uploadNATInfoReq.getNatInfos());
                rocksDBUtil.updateNATInfos(blockChain.getPackedNATInfos());
            } catch (Exception e) {
                log.error("信息添加失败！");
                return "新增信息存库失败";
            }
        } else {
            //打包的核酸信息已满, 需生成新区块
            int size = blockSize - blockChain.getPackedNATInfos().size();
            List<NATInfo> lastBlockNatInfos = uploadNATInfoReq.getNatInfos().subList(0,size);
            List<NATInfo> nextBlockNatInfos = uploadNATInfoReq.getNatInfos().subList(size,uploadNATInfoReq.getNatInfos().size());
            try {
                blockChain.getPackedNATInfos().addAll(lastBlockNatInfos);
                CreateNewBlockReq createNewBlockReq = new CreateNewBlockReq();
                createNewBlockReq.setNatInfos(blockChain.getPackedNATInfos());
                blockChainService.createBlock(createNewBlockReq);
                blockChain.setPackedNATInfos(nextBlockNatInfos);
                rocksDBUtil.updateNATInfos(blockChain.getPackedNATInfos());
            } catch (Exception e) {
                log.error("信息添加失败！");
                return "新增信息存库失败";
            }
            //向其他节点同步最新区块
            Message message = new Message();
            message.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
            message.setData(JSON.toJSONString(blockChain.getLatestBlock()));
            p2PService.broadcast(JSON.toJSONString(message));
        }
        //向其他节点同步最新核酸信息
        Message message = new Message();
        message.setType(BlockConstant.RESPONSE_LATEST_NATINFOS);
        message.setData(JSON.toJSONString(blockChain.getPackedNATInfos()));
        p2PService.broadcast(JSON.toJSONString(message));
        return "上传成功！";
    }

    public NATInfo getNATInfoByReportId(String reportId) throws Exception {
        List<NATInfo> natInfosTemp = new ArrayList<>(blockChain.getPackedNATInfos());
        String natInfosStr;
        for (Block block : blockChain.getBlockChain()) {
            natInfosStr = RSAUtil.decryptByPublicKey(block.getPublicKey(), block.getNatInfosString());
            natInfosTemp.addAll(JSON.parseArray(natInfosStr, NATInfo.class));
        }
        //根据个人信息筛选
        natInfosTemp = natInfosTemp.stream()
                .filter(natInfo -> (natInfo.getReportId().equals(reportId)))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        return natInfosTemp.get(0);
    }
}
