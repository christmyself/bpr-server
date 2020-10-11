package cn.himavat.bpr.onenet;


import cn.himavat.bpr.onenet.OnenetHelper;
import cn.himavat.bpr.onenet.OnenetMsgHandler;
import cn.himavat.bpr.onenet.model.OnenetMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@RestController
@EnableAutoConfiguration
public class OnenetCallbackController {

    private static final String token = "zdqcmn";
    private static final String aeskey = "whBx2ZwAU5LOHVimPj1MPx56QRe3OsGGWRe4dr17crV";

    @Autowired
    private OnenetMsgHandler onenetMsgHandler = null;
    @Autowired
    private OnenetHelper onenetHelper = null;

    @RequestMapping(value = "/receive", method = RequestMethod.GET)
    public String validate(@RequestParam("msg") String msg,
                           @RequestParam("nonce") String nonce,
                           @RequestParam("signature") String signature) throws UnsupportedEncodingException {
        log.info("url&token check: msg:{} nonce{} signature:{}", msg, nonce, signature);
        return onenetHelper.checkToken(msg, nonce, signature, token) ? msg : "error";
    }

    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public String revAndHandle(@RequestBody OnenetMsg onenetMsg) {
        if (!onenetHelper.checkSignature(onenetMsg, token)) {
            return "signature is wrong";
        }
        onenetMsgHandler.handle(onenetMsg);
        return "ok";
    }

    @RequestMapping(value = "/receive2", method = RequestMethod.POST)
    public String revAndDecrypt(@RequestBody String body) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        log.info("data receive:  body String --- " + body);
        var encrypted = false;
        var obj = onenetHelper.resolveBody(body, encrypted);
        log.info("data receive:  body Object --- " + obj);
        if (obj != null) {
            boolean dataRight = onenetHelper.checkSignature(obj, token);
            if (dataRight) {
                if (encrypted) {
                    String msg = onenetHelper.decryptMsg(obj, aeskey);
                    log.info("msg receive: {}", msg);
                }
                log.info("data receive: content{}", obj.toString());
            } else {
                log.info("data receive: signature error");
            }
        } else {
            log.info("data receive: body empty error");
        }
        return "ok";
    }
}
