package cn.himavat.bpr.onenet;

import cn.himavat.bpr.entity.Breakpoint;
import cn.himavat.bpr.entity.BreakpointRepository;
import cn.himavat.bpr.entity.BreakpointType;
import cn.himavat.bpr.onenet.model.OnenetMsg;
import cn.himavat.bpr.wework.WeworkMsgSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Component
public class OnenetMsgHandler {
    @Autowired
    WeworkMsgSender weworkMsgSender = null;
    @Autowired
    private BreakpointRepository breakpointRepository = null;

    public void handle(OnenetMsg onenetMsg) {
        var msg = (Map) onenetMsg.getMsg();
        log.info("The Signature is {} And the nonce is {}", onenetMsg.getMsg_signature(), onenetMsg.getNonce());
        log.info("Received:{}", msg.toString());
        var at = (Long) msg.get("at");
        var devId = (Integer) msg.get("dev_id");
        /**
         * 标识消息类型
         * 1：设备上传数据点消息
         * 2：设备上下线消息
         * 7：缓存命令下发后结果上报（仅支持NB设备）
         */
        var type = (Integer) msg.get("type");
        var imei = msg.get("imei");

        //var toUser = "ShanShengJun|WuHanAKang|A001";
        var toUser = "A001";
        var msgToSend = "";

        var dateFormat = "yyyy-MM-dd HH:mm:ss";
        var sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getDefault());
        var atStr = sdf.format(at);
        switch (type) {
            case 1:
                var value = Integer.parseInt((String) msg.get("value"));
                var dsId = msg.get("ds_id");
                if(value == 0){
                    msgToSend = atStr + ":断线检测器"+ imei +"状态正常。";
                }else if(value == -1){
                    msgToSend = atStr + ":断线检测器"+ imei +"状态异常。";
                }else{
                    var breakpoint = new Breakpoint();
                    breakpoint.setDeviceId(devId);
                    breakpoint.setGeneratedAt(new Date(at));
                    if (value > 10000 && value < 20000) {
                        msgToSend = "断线器设备：" + imei + "，于" + atStr + "线头断裂被修复";
                        breakpoint.setBpType(BreakpointType.RESTORE.getValue());
                    } else if (value > 30000 && value < 40000) {
                        msgToSend = "断线器设备：" + imei + "，于" + atStr + "出现线头断裂";
                        breakpoint.setBpType(BreakpointType.BROKEN.getValue());
                    }
                    breakpointRepository.save(breakpoint);
                }
                log.info("即将发送的信息为：{}", msgToSend);
                try {
                    weworkMsgSender.sendTextMsg(toUser, msgToSend);
                } catch (IOException | URISyntaxException exception) {
                    exception.printStackTrace();
                }
                break;
            case 2:
                var loginType = (Integer) msg.get("login_type");
                var status = (Integer) msg.get("status");
                log.info("Login msg Received, the Login type is: {}, status is {}", loginType, status);
                break;
            case 7:
                log.info("Batch msg {} has not bean handled。", msg.toString());
                break;
            default:
                log.info("Unknown msg type: {}", msg.toString());
                break;
        }
        for (Breakpoint breakpoint : breakpointRepository.findAll()) {
            log.info("ID:{} Device ID:{} Bp Type:{} Generated At:{}", breakpoint.getId(), breakpoint.getDeviceId(), breakpoint.getBpType(), breakpoint.getGeneratedAt());
        }
    }
}
