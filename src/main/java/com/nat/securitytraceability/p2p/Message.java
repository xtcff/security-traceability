package com.nat.securitytraceability.p2p;

import lombok.Data;

import java.io.Serializable;

/**
 * p2p通讯消息
 * @author hhf
 */
@Data
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private int type;

    /**
     * 消息内容
     */
    private String data;

    public Message(){
    }

    public Message(int type){
        this.type = type;
    }

    public Message(int type, String data){
        this.type = type;
        this.data = data;
    }
}