package com.nat.securitytraceability.service;

import com.alibaba.fastjson.JSON;
import com.nat.securitytraceability.req.CreateNewBlockReq;
import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.data.BlockChain;
import com.nat.securitytraceability.data.NATInfo;
import com.nat.securitytraceability.data.Person;
import com.nat.securitytraceability.util.CryptoUtil;
import com.nat.securitytraceability.util.RSAUtil;
import com.nat.securitytraceability.util.RocksDBUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 区块链服务
 * @author hhf
 */
@Slf4j
@Service
public class BlockChainService {

    @Resource
    BlockChain blockChain;

    @Resource
    RocksDBUtil rocksDBUtil;

    @Resource
    P2PService p2PService;
    /**
     * 创建创世区块
     * @return String
     */
    public Block createGenesisBlock() throws Exception {
        Block genesisBlock = new Block();
        //设置创世区块高度为1
        genesisBlock.setIndex(1);
        genesisBlock.setTimestamp(System.currentTimeMillis());
        genesisBlock.setPublicKey(blockChain.getPublicKey());
        //封装业务数据
        List<NATInfo> natInfoList = new ArrayList<>();
        NATInfo natInfoForPersonOne = new NATInfo();
        Person person0 = new Person();
        person0.setIdNumber("232546199001024468");
        person0.setName("dlrb");
        Person person3 = new Person();
        person3.setIdNumber("445212199102055463");
        person3.setName("glnz");
        Person person1 = new Person();
        person1.setIdNumber("411202200002144516");
        person1.setName("hhf");
        Person person2 = new Person();
        person2.setIdNumber("212452199803284565");
        person2.setName("qja");
        natInfoForPersonOne.setReportId(UUID.randomUUID().toString().replaceAll("-", ""));
        natInfoForPersonOne.setIsSamplingPerson(person1);
        natInfoForPersonOne.setSamplingPerson(person0);
        natInfoForPersonOne.setSamplingTime(20220414);
        natInfoForPersonOne.setSamplingPlace("河南省郑州市金水区郑州航空工业管理学院东校区中心广场");
        NATInfo natInfoForPersonOneResult = new NATInfo();
        BeanUtils.copyProperties(natInfoForPersonOne, natInfoForPersonOneResult);
        natInfoForPersonOneResult.setSamplingPerson(person0);
        natInfoForPersonOneResult.setIsSamplingPerson(person1);
        natInfoForPersonOneResult.setDetectHospital("郑州大学第一附属医院");
        natInfoForPersonOneResult.setDetectTime(20220415);
        natInfoForPersonOneResult.setDetectPerson(person0);
        natInfoForPersonOneResult.setDetectResult("阴性");
        natInfoList.add(natInfoForPersonOne);
        natInfoList.add(natInfoForPersonOneResult);
        NATInfo natInfoForPersonTwo = new NATInfo();
        natInfoForPersonTwo.setReportId(UUID.randomUUID().toString().replaceAll("-", ""));
        natInfoForPersonTwo.setIsSamplingPerson(person2);
        natInfoForPersonTwo.setSamplingPerson(person3);
        natInfoForPersonTwo.setSamplingTime(20220320);
        natInfoForPersonTwo.setSamplingPlace("上海市浦东新区曹路镇康平佳苑门卫处");
        natInfoList.add(natInfoForPersonTwo);
        String natInfosString = RSAUtil.encryptByPrivateKey(blockChain.getPrivateKey(),
                JSON.toJSONString(natInfoList));
        genesisBlock.setNatInfosString(natInfosString);
        genesisBlock.setPublicKey(blockChain.getPublicKey());
        //设置创世区块的hash值
        genesisBlock.setHash(calculateHash("",natInfosString, genesisBlock.getPublicKey()));
        //添加创世区块到区块链和数据库中
        try {
            rocksDBUtil.putBlock(genesisBlock);
        } catch (Exception e) {
            log.error("创世区块入库错误, msg: [{}]", e.getMessage());
        }
        blockChain.getBlockChain().add(genesisBlock);
        return genesisBlock;
    }

    /**
     * 生成新区块
     */
    public void createBlock(CreateNewBlockReq createNewBlockReq) throws Exception {

        // 定义每次哈希函数的结果
        String newBlockHash;
        //使用私钥加密核酸信息
        String natInfosString = RSAUtil.encryptByPrivateKey(blockChain.getPrivateKey(),
                JSON.toJSONString(createNewBlockReq.getNatInfos()));
        //使用上一个区块哈希、加密后的核酸信息、当前节点的公钥生成新区块的哈希
        newBlockHash = calculateHash(blockChain.getLatestBlock().getHash(), natInfosString, blockChain.getPublicKey());

        // 创建新的区块
        Block block = new Block();
        block.setIndex(blockChain.getBlockChain().size() + 1);
        //时间戳
        block.setTimestamp(System.currentTimeMillis());

        block.setNatInfosString(natInfosString);
        block.setPublicKey(blockChain.getPublicKey());
        block.setPreviousHash(blockChain.getLatestBlock().getHash());
        block.setHash(newBlockHash);
        if (!addBlock(block)) {
            throw new Exception("添加新区块错误！");
        }

    }

    /**
     * 添加新区块到当前节点的区块链中并存库
     *
     * @param newBlock newBlock
     */
    public boolean addBlock(Block newBlock) {
        //先对新区块的合法性进行校验
        if (isValidNewBlock(newBlock, blockChain.getLatestBlock())) {
            try {
                rocksDBUtil.putBlock(newBlock);
                log.info("数据库添加区块成功！");
            } catch (Exception e) {
                log.error("新生成区块入库错误, msg: [{}]", e.getMessage());
                return false;
            }
            blockChain.getBlockChain().add(newBlock);
            return true;
        }
        return false;
    }

    /**
     * 验证整个区块链是否有效
     * @param chain chain
     * @return boolean
     */
    public boolean isValidChain(List<Block> chain) {
        Block block;
        Block lastBlock = chain.get(0);
        int currentIndex = 1;
        while (currentIndex < chain.size()) {
            block = chain.get(currentIndex);

            if (!isValidNewBlock(block, lastBlock)) {
                return false;
            }

            lastBlock = block;
            currentIndex++;
        }
        return true;
    }

    /**
     * 验证新区块是否有效
     *
     * @param newBlock newBlock
     * @param previousBlock previousBlock
     * @return boolean
     */
    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
            log.info("新区块的前一个区块哈希验证不通过");
            return false;
        } else {
            // 验证新区块hash值的正确性
            String hash = calculateHash(newBlock.getPreviousHash(), newBlock.getNatInfosString(), newBlock.getPublicKey());
            if (!hash.equals(newBlock.getHash())) {
                log.info("新区块的哈希无效: [{}], [{}]", hash, newBlock.getHash());
                return false;
            }
            return true;
        }
    }

    /**
     * 计算区块的hash
     *
     * @param previousHash previousHash
     * @param natInfosString natInfosString
     * @param publicKey publicKey
     * @return String
     */
    public String calculateHash(String previousHash, String natInfosString, String publicKey) {
        return CryptoUtil.SHA256(previousHash + natInfosString + publicKey);
    }

    /**
     * 替换本地区块链
     *
     * @param newBlocks newBlocks
     */
    public void replaceChain(List<Block> newBlocks) {
        if (isValidChain(newBlocks) && newBlocks.size() > blockChain.getBlockChain().size()) {
            try{
                rocksDBUtil.updateBlockChain(newBlocks);
                blockChain.setBlockChain(newBlocks);
                //替换已打包保存的核酸信息
                p2PService.broadcast(p2PService.queryLatestNATInfos());
                log.info("替换后的本节点区块链: [{}]", JSON.toJSONString(blockChain.getBlockChain()));
            } catch (Exception e) {
                log.error("替换本节点数据库区块链失败: [{}]", e.getMessage());
            }
        } else {
            log.info("接收的区块链无效");
        }
    }
}
