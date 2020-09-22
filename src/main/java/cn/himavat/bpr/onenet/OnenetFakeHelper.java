package cn.himavat.bpr.onenet;

import cn.himavat.bpr.onenet.model.OnenetMsg;
import org.springframework.stereotype.Component;

@Component
public class OnenetFakeHelper {

    public boolean checkToken(String msg, String nonce, String signature, String token){
        return true;
    }

    public boolean checkSignature(OnenetMsg obj, String token){
        return true;
    }

    public String decryptMsg(OnenetMsg obj, String encodeKey){
        return "";
    }

    public OnenetMsg resolveBody(String body, boolean encrypted){
        return new OnenetMsg();
    }
}
