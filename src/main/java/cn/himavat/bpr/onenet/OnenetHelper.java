package cn.himavat.bpr.onenet;

import cn.himavat.bpr.onenet.model.OnenetMsg;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;


@Slf4j
@Component
public class OnenetHelper {

    private MessageDigest mdInst;

    @Autowired
    private ObjectMapper om;

    {
        try {
            mdInst = MessageDigest.getInstance("MD5");
            Security.addProvider(new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    /**
     * 功能描述:在OneNet平台配置数据接收地址时，平台会发送URL&token验证请求<p>
     * 使用此功能函数验证token
     *
     * @param msg       请求参数 <msg>的值
     * @param nonce     请求参数 <nonce>的值
     * @param signature 请求参数 <signature>的值
     * @param token     OneNet平台配置页面token的值
     * @return token检验成功返回true；token校验失败返回false
     */
    public boolean checkToken(String msg, String nonce, String signature, String token) throws UnsupportedEncodingException {
        byte[] paramB = new byte[token.length() + 8 + msg.length()];
        System.arraycopy(token.getBytes(), 0, paramB, 0, token.length());
        System.arraycopy(nonce.getBytes(), 0, paramB, token.length(), 8);
        System.arraycopy(msg.getBytes(), 0, paramB, token.length() + 8, msg.length());
        String sig = String.valueOf(Base64.encodeBase64(mdInst.digest(paramB)));
        log.info("url&token validation: result {},  detail receive:{} calculate:{}", sig.equals(signature.replace(' ', '+')), signature, sig);
        return sig.equals(signature.replace(' ', '+'));
    }

    /**
     * 功能描述: 检查接收数据的信息摘要是否正确。<p>
     * 方法非线程安全。
     *
     * @param obj   消息体对象
     * @param token OneNet平台配置页面token的值
     * @return
     */
    public boolean checkSignature(OnenetMsg obj, String token) {
        //计算接受到的消息的摘要
        //token长度 + 8B随机字符串长度 + 消息长度
        byte[] signature = new byte[token.length() + 8 + obj.getMsg().toString().length()];
        System.arraycopy(token.getBytes(), 0, signature, 0, token.length());
        System.arraycopy(obj.getNonce().getBytes(), 0, signature, token.length(), 8);
        System.arraycopy(obj.getMsg().toString().getBytes(), 0, signature, token.length() + 8, obj.getMsg().toString().length());
        String calSig = Base64.encodeBase64String(mdInst.digest(signature));
        log.info("check signature: result:{}  receive sig:{},calculate sig: {}", calSig.equals(obj.getMsg_signature()), obj.getMsg_signature(), calSig);
        return calSig.equals(obj.getMsg_signature());
    }

    /**
     * 功能描述 解密消息
     *
     * @param obj       消息体对象
     * @param encodeKey OneNet平台第三方平台配置页面为用户生成的AES的BASE64编码格式秘钥
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public String decryptMsg(OnenetMsg obj, String encodeKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] encMsg = Base64.decodeBase64(obj.getMsg().toString());
        byte[] aeskey = Base64.decodeBase64(encodeKey + "=");
        SecretKey secretKey = new SecretKeySpec(aeskey, 0, 32, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(aeskey, 0, 16));
        byte[] allmsg = cipher.doFinal(encMsg);
        byte[] msgLenBytes = new byte[4];
        System.arraycopy(allmsg, 16, msgLenBytes, 0, 4);
        int msgLen = getMsgLen(msgLenBytes);
        byte[] msg = new byte[msgLen];
        System.arraycopy(allmsg, 20, msg, 0, msgLen);
        return new String(msg);
    }

    /**
     * 功能描述 解析数据推送请求，生成code>BodyObj</code>消息对象
     *
     * @param body      数据推送请求body部分
     * @param encrypted 表征是否为加密消息
     * @return 生成的<code>BodyObj</code>消息对象
     */
    public OnenetMsg resolveBody(String body, boolean encrypted) {
        om = new ObjectMapper();
        JsonNode jsonMsg = null;
        try {
            jsonMsg = om.readTree(body);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        var obj = new OnenetMsg();
        obj.setNonce(jsonMsg.get("nonce").asText());
        obj.setMsg_signature(jsonMsg.get("msg_signature").asText());
        if (encrypted) {
            if (!jsonMsg.has("enc_msg")) {
                return null;
            }
            obj.setMsg(jsonMsg.get("enc_msg").asText());
        } else {
            if (!jsonMsg.has("msg")) {
                return null;
            }
            obj.setMsg(jsonMsg.get("msg"));
        }
        return obj;
    }

    private int getMsgLen(byte[] arrays) {
        int len = 0;
        len += (arrays[0] & 0xFF) << 24;
        len += (arrays[1] & 0xFF) << 16;
        len += (arrays[2] & 0xFF) << 8;
        len += (arrays[3] & 0xFF);
        return len;
    }
}