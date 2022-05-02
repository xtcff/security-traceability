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
        log.info("BlockChainController BlockChain scanBlock end, resp = [{}]", resp);
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
    public String createFirstBlock() {
        log.info("BlockChainController BlockChain createFirstBlock start");
        String genesisBlock = JSON.toJSONString(blockChainService.createGenesisBlock());
        log.info("BlockChainController BlockChain createFirstBlock end, genesisBlock = [{}]", genesisBlock);
        return scanBlock();
    }


    /**
     * 工作量证明PoW
     * 挖矿生成新的区块
     */
    @GetMapping("/mine")
    public String createNewBlock(@RequestBody CreateNewBlockReq createNewBlockReq) {
        log.info("BlockChainController BlockChain createNewBlock start, createNewBlock:[{}]", createNewBlockReq);
        blockChainService.createBlock(createNewBlockReq);
        log.info("BlockChainController BlockChain createFirstBlock end");
        return scanBlock();
    }

    /**
     * 查看数据库
     * @return String
     */
    @GetMapping("/database/{database}")
    public String scanDatabase(@PathVariable("database") String s) throws Exception {
        log.info("BlockChainController BlockChain scanDatabase start");
        String resp = JSON.toJSONString(rocksDBUtil.getBlock(s));
        log.info("BlockChainController BlockChain scanDatabase end, resp = [{}]", resp);
        return resp;
    }
}
