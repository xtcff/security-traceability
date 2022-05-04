package com.nat.securitytraceability.p2p;

public class BlockConstant {

    // 请求查询最新的区块
    public final static int QUERY_LATEST_BLOCK = 1;


    // 收到最新的区块
    public final static int RESPONSE_LATEST_BLOCK = 2;


    // 请求查询整个区块链
    public final static int QUERY_BLOCKCHAIN = 3;


    // 收到整个区块链
    public final static int RESPONSE_BLOCKCHAIN = 4;

    // 收到公钥
    public final static int BROADCAST_PUBLIC_KEY = 5;

    // 收到最新核酸信息
    public final static int RESPONSE_LATEST_NATINFOS = 6;

    // 请求查询最新核酸信息
    public final static int QUERY_LATEST_NATINFOS = 7;
}
