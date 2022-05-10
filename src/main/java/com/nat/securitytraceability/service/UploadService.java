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
import java.time.LocalDateTime;
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

    @Resource
    QueryService queryService;

    @Value("${block.size}")
    private int blockSize;

    public String uploadNATInfo(UploadNATInfoReq uploadNATInfoReq) throws Exception {
        for (NATInfo natInfo : uploadNATInfoReq.getNatInfos()) {
            //根据是否入参有reportId判断是采样信息还是检测结果
            if (natInfo.getReportId() == null) {
                //没有reportId使用UUID生成唯一编码
                natInfo.setReportId(UUID.randomUUID().toString().replaceAll("-",""));
                //设置采样时间
                natInfo.setSamplingTime(LocalDateTime.now());
            } else {
                //有reportId根据其查询采样信息后完善检测结果信息
                NATInfo natInfoTemp = getNATInfoByReportId(natInfo.getReportId());
                natInfo.setIsSamplingPerson(natInfoTemp.getIsSamplingPerson());
                natInfo.setSamplingPerson(natInfoTemp.getSamplingPerson());
                natInfo.setSamplingTime(natInfoTemp.getSamplingTime());
                natInfo.setSamplingPlace(natInfoTemp.getSamplingPlace());
                natInfo.setDetectTime(LocalDateTime.now());
            }
        }
        //判断当前已打包的信息数量是否足够生成新区块
        if (blockChain.getPackedNATInfos().size() + uploadNATInfoReq.getNatInfos().size() <= blockSize) {
            //打包的核酸信息未满
            try{
                //直接加入信息
                blockChain.getPackedNATInfos().addAll(uploadNATInfoReq.getNatInfos());
                //存数据库
                rocksDBUtil.updateNATInfos(blockChain.getPackedNATInfos());
            } catch (Exception e) {
                log.error("信息添加失败！");
                return "新增信息存库失败";
            }
        } else {
            //打包的核酸信息已满, 需生成新区块
            int size = blockSize - blockChain.getPackedNATInfos().size();
            //需放到新区块中的核酸信息
            List<NATInfo> lastBlockNatInfos = uploadNATInfoReq.getNatInfos().subList(0,size);
            //需放到已打包的信息
            List<NATInfo> nextBlockNatInfos = uploadNATInfoReq.getNatInfos().subList(size,uploadNATInfoReq.getNatInfos().size());
            try {
                //创建新区块
                blockChain.getPackedNATInfos().addAll(lastBlockNatInfos);
                CreateNewBlockReq createNewBlockReq = new CreateNewBlockReq();
                createNewBlockReq.setNatInfos(blockChain.getPackedNATInfos());
                blockChainService.createBlock(createNewBlockReq);
                //更新已打包的信息并存库
                blockChain.setPackedNATInfos(nextBlockNatInfos);
                rocksDBUtil.updateNATInfos(blockChain.getPackedNATInfos());
            } catch (Exception e) {
                log.error("信息添加失败！");
                return "新增信息存库失败";
            }
            //生成新区块后向其他节点同步最新区块
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
        List<NATInfo> natInfosTemp = queryService.queryNATInfos();
        //根据个人信息筛选
        natInfosTemp = natInfosTemp.stream()
                .filter(natInfo -> (natInfo.getReportId().equals(reportId)))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        return natInfosTemp.get(0);
    }
}
