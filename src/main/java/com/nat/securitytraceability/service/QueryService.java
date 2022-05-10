package com.nat.securitytraceability.service;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.data.NATInfo;
import com.nat.securitytraceability.req.QueryNATInfosReq;
import com.nat.securitytraceability.req.QueryNATInfosVo;
import com.nat.securitytraceability.req.QueryPageNATInfosReq;
import com.nat.securitytraceability.util.PageVo;
import com.nat.securitytraceability.util.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
    public List<QueryNATInfosVo> queryNATInfosByPerson(QueryNATInfosReq queryNATInfosReq) throws Exception {
        //所有采样和检测信息
        List<NATInfo> natInfosTemp = queryNATInfos();
        //根据个人信息筛选
        natInfosTemp = natInfosTemp.stream()
                .filter(natInfo -> (natInfo.getIsSamplingPerson().getIdNumber()
                        .equals(queryNATInfosReq.getPerson().getIdNumber()) && natInfo.getIsSamplingPerson().getName()
                        .equals(queryNATInfosReq.getPerson().getName())))
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        //转换时间格式返回
        return transTimeToDate(natInfosTemp);
    }

    /**
     * 分页条件查询所有核酸信息
     * @return String
     */
    public PageVo<QueryNATInfosVo> queryPageNATInfos(QueryPageNATInfosReq req) throws Exception {
        //所有采样和检测信息
        List<NATInfo> natInfosTemp = queryNATInfos();
        //根据请求中的条件筛选
        natInfosTemp = natInfosTemp.stream()
                .filter(natInfo -> {
                    if(StringUtils.isEmpty(req.getReportId())) {
                        return true;
                    }
                    return natInfo.getReportId().startsWith(req.getReportId());
                })
                .filter(natInfo -> {
                    if(StringUtils.isEmpty(req.getIdNumber())) {
                        return true;
                    }
                    return natInfo.getIsSamplingPerson().getIdNumber().startsWith(req.getIdNumber());
                })
                .filter(natInfo -> {
                    if(StringUtils.isEmpty(req.getDetectResult())) {
                        return true;
                    }
                    return req.getDetectResult().equals(natInfo.getDetectResult());
                })
                .filter(natInfo -> {
                    if(req.getSamplingTimeBegin() == null) {
                        return true;
                    }
                    return natInfo.getSamplingTime().isAfter(
                            req.getSamplingTimeBegin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                })
                .filter(natInfo -> {
                    if(req.getSamplingTimeEnd() == null) {
                        return true;
                    }
                    return natInfo.getSamplingTime().isBefore(
                            req.getSamplingTimeEnd().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                })
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        //转化日期格式后返回分页结果
        List<QueryNATInfosVo> queryNATInfosVos = transTimeToDate(natInfosTemp);
        int fromIndex = (req.getPageNum() - 1) * req.getPageSize();
        int endIndex = Math.min(queryNATInfosVos.size(), req.getPageNum() * req.getPageSize());
        return new PageVo<>(queryNATInfosVos.subList(fromIndex, endIndex), (long) queryNATInfosVos.size(), req);
    }

    /**
     * 查询所有核酸信息
     * @return List
     */
    public List<NATInfo> queryNATInfos() throws Exception {
        //所有采样和检测信息
        List<NATInfo> natInfosTemp = new ArrayList<>(blockChain.getPackedNATInfos());
        String natInfosStr;
        for (Block block : blockChain.getBlockChain()) {
            natInfosStr = RSAUtil.decryptByPublicKey(block.getPublicKey(), block.getNatInfosString());
            natInfosTemp.addAll(JSON.parseArray(natInfosStr, NATInfo.class));
        }
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
        //定义去重后信息
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
        //按采样时间倒序后返回
        resp = resp.stream().sorted(Comparator.comparing(NATInfo::getSamplingTime).reversed())
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        return resp;
    }

    /**
     * 转换时间戳为字符串日期格式
     * @return List
     */
    public List<QueryNATInfosVo> transTimeToDate(List<NATInfo> natInfos) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
        return natInfos.stream().map(natInfo -> {
            QueryNATInfosVo queryNATInfosVo = new QueryNATInfosVo();
            BeanUtils.copyProperties(natInfo, queryNATInfosVo);
            queryNATInfosVo.setSamplingTime(sdf.format(
                    Date.from(natInfo.getSamplingTime().atZone(ZoneId.systemDefault()).toInstant())));
            if (natInfo.getDetectTime() != null) {
                queryNATInfosVo.setDetectTime(sdf.format(
                        Date.from(natInfo.getDetectTime().atZone(ZoneId.systemDefault()).toInstant())));
            }
            return queryNATInfosVo;
        }).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }
}
