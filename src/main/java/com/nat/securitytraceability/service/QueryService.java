package com.nat.securitytraceability.service;

import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.data.NATInfo;
import com.nat.securitytraceability.req.QueryNATInfosReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 查询服务
 * @author hhf
 */
@Slf4j
@Service
public class QueryService {

    @Resource
    BlockChain blockChain;

    /**
     * 根据个人信息查询所有核酸信息
     * @return String
     */
    public List<NATInfo> queryNATInfosByPerson(QueryNATInfosReq queryNATInfosReq) {
        //所有采样和检测信息
        List<NATInfo> natInfosTemp = blockChain.getPackedNATInfos();
        //根据个人信息筛选
        natInfosTemp = natInfosTemp.stream()
                .filter(natInfo -> (natInfo.getIsSamplingPerson().getIdNumber()
                        .equals(queryNATInfosReq.getPerson().getIdNumber()) && natInfo.getIsSamplingPerson().getName()
                        .equals(queryNATInfosReq.getPerson().getName())))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        return filterAndSort(natInfosTemp);
    }

    /**
     * 查询所有核酸信息
     * @return String
     */
    public List<NATInfo> queryNATInfos() {
        //所有采样和检测信息
        List<NATInfo> natInfosTemp = blockChain.getPackedNATInfos();
        return filterAndSort(natInfosTemp);

    }

    /**
     * 筛选排序
     * @return String
     */
    private List<NATInfo> filterAndSort(List<NATInfo> natInfosTemp) {
        //有检测结果的信息
        List<NATInfo> natInfos = new CopyOnWriteArrayList<>();
        for (NATInfo natInfo : natInfosTemp) {
            if (natInfo.getDetectResult() != null) {
                natInfos.add(natInfo);
            }
        }
        //去重后信息
        List<NATInfo> resp = new CopyOnWriteArrayList<>(natInfos);
        //去重
        if (natInfos.isEmpty()) {
            resp.addAll(natInfosTemp);
        } else{
            //有结果的报告ID列表
            List<String> list = natInfos.stream().map(NATInfo::getReportId).collect(Collectors.toList());
            for (NATInfo natInfo : natInfosTemp) {
                if (!list.contains(natInfo.getReportId())) {
                    resp.add(natInfo);
                }
            }
        }
        //排序
        resp = resp.stream().sorted(Comparator.comparing(NATInfo::getSamplingTime).reversed())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        return resp;
    }
}
