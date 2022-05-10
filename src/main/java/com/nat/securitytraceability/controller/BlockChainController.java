package com.nat.securitytraceability.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.req.CreateNewBlockReq;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.service.BlockChainService;
import com.nat.securitytraceability.util.RocksDBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 区块链controller
 * @author hhf
 */
@Slf4j
@RestController
@RequestMapping("/blockchain")
public class BlockChainController {

    @Resource
    BlockChainService blockChainService;

    @Resource
    BlockChain blockChain;

    @Resource
    RocksDBUtil rocksDBUtil;

    /**
     * 查看当前节点区块链数据
     * @return String
     */
    @GetMapping("/scan")
    public String scanBlock() {
        log.info("BlockChainController BlockChain scanBlock start");
        String resp = JSON.toJSONString(blockChain.getBlockChain());
        log.info("BlockChainController BlockChain scanBlock end, resp = [{}], publicKey = [{}]", resp, blockChain.getPublicKeys());
        return resp;
    }

    /**
     * 查看当前节点区块交易数据
     * @return String
     */
    @GetMapping("/data")
    public String scanData() {
        log.info("BlockChainController BlockChain scanData start");
        String resp = JSON.toJSONString(blockChain.getPackedNATInfos());
        log.info("BlockChainController BlockChain scanData end, resp = [{}]", resp);
        return resp;
    }

    /**
     * 创建创世区块
     * @return String
     */
    @GetMapping("/create")
    public String createFirstBlock() throws Exception {
        log.info("BlockChainController BlockChain createFirstBlock start");
        String genesisBlock = JSON.toJSONString(blockChainService.createGenesisBlock());
        log.info("BlockChainController BlockChain createFirstBlock end, genesisBlock = [{}]", genesisBlock);
        return scanBlock();
    }


    /**
     * 生成新区块
     */
    @GetMapping("/mine")
    public String createNewBlock(@RequestBody CreateNewBlockReq createNewBlockReq) throws Exception {
        log.info("BlockChainController BlockChain createNewBlock start, createNewBlock:[{}]", createNewBlockReq);
        blockChainService.createBlock(createNewBlockReq);
        log.info("BlockChainController BlockChain createFirstBlock end");
        return scanBlock();
    }

    /**
     * 查看数据库区块
     * @return String
     */
    @GetMapping("/database/block/{block}")
    public String scanDatabaseBlock(@PathVariable("block") String s) throws Exception {
        log.info("BlockChainController BlockChain scanDatabase start");
        String resp = JSON.toJSONString(rocksDBUtil.getBlock(s));
        log.info("BlockChainController BlockChain scanDatabase end, resp = [{}]", resp);
        return resp;
    }

    /**
     * 查看数据库最新核酸
     * @return String
     */
    @GetMapping("/database/natInfos")
    public String scanDatabaseNATInfos() throws Exception {
        log.info("BlockChainController BlockChain scanDatabase start");
        String resp = JSON.toJSONString(rocksDBUtil.getNatInfos());
        log.info("BlockChainController BlockChain scanDatabase end, resp = [{}]", resp);
        return resp;
    }
}
