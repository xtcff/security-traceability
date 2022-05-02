package com.nat.securitytraceability.service;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.data.NATInfo;
import com.nat.securitytraceability.p2p.BlockConstant;
import com.nat.securitytraceability.p2p.Message;
import com.nat.securitytraceability.req.CreateNewBlockReq;
import com.nat.securitytraceability.req.UploadNATInfoReq;
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

    public String uploadNATInfo(UploadNATInfoReq uploadNATInfoReq) {
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
        Block latestBlock = blockChain.getLatestBlock();
        if ((latestBlock.getNatInfos().size() + uploadNATInfoReq.getNatInfos().size()) <= blockSize) {
            //区块未满或刚满, 直接加入信息
            for (NATInfo natInfo : uploadNATInfoReq.getNatInfos()) {
                latestBlock.getNatInfos().add(natInfo);
            }
            //添加到内存中已打包核酸信息
            blockChain.getPackedNATInfos().addAll(uploadNATInfoReq.getNatInfos());
            //存库
            try {
                rocksDBUtil.updateLatestBlock(latestBlock);
            } catch (Exception e) {
                log.error("信息添加失败！");
                return "新增信息存库失败";
            }
        } else {
            //区块要满, 需生成新区块
            int size = blockSize - latestBlock.getNatInfos().size();
            List<NATInfo> lastBlockNatInfos = uploadNATInfoReq.getNatInfos().subList(0,size);
            List<NATInfo> nextBlockNatInfos = uploadNATInfoReq.getNatInfos().subList(size,uploadNATInfoReq.getNatInfos().size());
            if (lastBlockNatInfos.size() != 0) {
                try {
                    latestBlock.getNatInfos().addAll(lastBlockNatInfos);
                    latestBlock.setHash(blockChainService.calculateHash(latestBlock.getPreviousHash(), latestBlock.getNatInfos(), latestBlock.getNonce()));
                    rocksDBUtil.updateLatestBlock(latestBlock);
                } catch (Exception e) {
                    log.error("信息添加失败！");
                    return "新增信息存库失败";
                }
            }
            log.info("nextBlockNatInfos:[{}]", nextBlockNatInfos);
            CreateNewBlockReq createNewBlockReq = new CreateNewBlockReq();
            createNewBlockReq.setNatInfos(new ArrayList<>(nextBlockNatInfos));
            blockChainService.createBlock(createNewBlockReq);
        }
        //向其他节点同步信息
        Message message = new Message();
        message.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
        message.setData(JSON.toJSONString(latestBlock));
        p2PService.broadcast(JSON.toJSONString(latestBlock));
        return "上传成功！";
    }

    public NATInfo getNATInfoByReportId(String reportId) {
        List<NATInfo> natInfosTemp = blockChain.getPackedNATInfos();
        //根据个人信息筛选
        natInfosTemp = natInfosTemp.stream()
                .filter(natInfo -> (natInfo.getReportId().equals(reportId)))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        return natInfosTemp.get(0);
    }
}
