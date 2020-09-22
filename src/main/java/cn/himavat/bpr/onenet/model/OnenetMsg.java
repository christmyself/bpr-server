package cn.himavat.bpr.onenet.model;

import lombok.Data;

import java.util.Map;

@Data
public class OnenetMsg {
    /**
     * msg.type标识消息类型
     * 1：设备上传数据点消息，对应DsMsg
     * 2：设备上下线消息，对应LoginMsg
     * 7：缓存命令下发后结果上报（仅支持NB设备），未实现
     *
     *  msg.at
     * 平台时间戳,单位ms,"at": 1599484233778
     *
     */
    private Object msg;
    /**
     * 消息摘要
     */
    private String msg_signature;
    /**
     * 用于计算消息摘要的随机串
     */
    private String nonce;
}
