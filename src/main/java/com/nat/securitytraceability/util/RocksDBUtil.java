package com.nat.securitytraceability.util;

import com.nat.securitytraceability.data.Block;
import com.nat.securitytraceability.data.BlockChain;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.springframework.stereotype.Component;

/**
 * RocksDB 工具
 *
 * @author hhf
 *
 */
@Slf4j
@Component
public class RocksDBUtil {


    /**
     * 区块链数据文件
     */
    private static final String DB_FILE = "blockchain.db";
    /**
     * 区块桶前缀
     */
    private static final String BLOCKS_BUCKET_PREFIX = "blocks_";

    private volatile static RocksDBUtil instance;

    public static RocksDBUtil getInstance() {
        if (instance == null) {
            synchronized (RocksDBUtil.class) {
                if (instance == null) {
                    instance = new RocksDBUtil();
                }
            }
        }
        return instance;
    }

    @Getter
    private RocksDB rocksDB;

    private RocksDBUtil() {
        initRocksDB();
    }

    /**
     * 初始化RocksDB
     */
    private void initRocksDB() {
        try {
            rocksDB = RocksDB.open(new Options().setCreateIfMissing(true), DB_FILE);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询最新区块
     *
     * @return Block Block
     */
    public Block getLatestBlock() throws Exception {
        byte[] latestBlockBytes = rocksDB.get(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + "l"));
        if (latestBlockBytes != null) {
            return (Block) SerializeUtil.deserialize(latestBlockBytes);
        }
        return new Block();
    }

    /**
     * 更新最新区块
     *
     * @param block block
     */
    public void updateLatestBlock(Block block) throws Exception {
        rocksDB.delete(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + "l"));
        rocksDB.put(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + "l"),
                SerializeUtil.serialize(block));
    }

    /**
     * 保存区块
     *
     * @param block block
     */
    public void putBlock(Block block) throws Exception {
        if (block.getIndex() != 1) {
            Block latestBlock = getLatestBlock();
            if (latestBlock.getHash().equals(block.getPreviousHash())) {
                try{
                    rocksDB.delete(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + "l"));
                    rocksDB.put(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + latestBlock.getHash()),
                            SerializeUtil.serialize(latestBlock));
                } catch (Exception e) {
                    log.error("替换数据库最新区块错误: [{}]", e.getMessage());
                }
            } else {
                log.error("最新区块才能被添加到数据库！");
                return;
            }
        }
        rocksDB.put(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + "l"),
                SerializeUtil.serialize(block));
    }

    /**
     * 查询区块
     *
     * @param blockHash blockHash
     * @return Block
     */
    public Block getBlock(String blockHash) throws Exception {
        Block latestBlock = getLatestBlock();
        byte[] key = SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + blockHash);
        return (Block) SerializeUtil.deserialize(rocksDB.get(key));
    }

    /**
     * 查询区块链
     *
     * @return Block
     */
    public BlockChain getBlockChain() throws Exception {
        BlockChain blockChain = new BlockChain();
        byte[] latestBlockBytes = rocksDB.get(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + "l"));
        if (latestBlockBytes != null) {
            Block latestBlock =  (Block) SerializeUtil.deserialize(latestBlockBytes);
            while (latestBlock.getIndex() >= 1){
                blockChain.getBlockChain().add(0, latestBlock);
                blockChain.getPackedNATInfos().addAll(latestBlock.getNatInfos());
                if (latestBlock.getIndex() != 1){
                    latestBlock = (Block) SerializeUtil.deserialize(rocksDB
                            .get(SerializeUtil.serialize(BLOCKS_BUCKET_PREFIX + latestBlock.getPreviousHash())));
                } else {
                    break;
                }
            }
        }
        return blockChain;
    }
}
