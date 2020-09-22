package cn.himavat.bpr.wework;


import cn.himavat.bpr.wework.model.GetTokenRet;
import cn.himavat.bpr.wework.model.SendMsgRet;
import cn.himavat.bpr.wework.model.TextMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;


@Slf4j
@Component
public class WeworkMsgSender {
    private static final String BASE_URL = "https://qyapi.weixin.qq.com";
    private static final String CORP_ID = "ww8ff2c92bdb9466ae";
    private static final String BP_DETECTOR_APP_SECRET = "-NXBgCjsp7sQZ9E3DhYRJPt88b8bnKtiskld7sFCgdc";
    private static final int BP_DETECTOR_APP_ID = 1000004;


    @Autowired
    RestTemplate rest = null;

    private URI tokenUri = null;
    private URI msgSendUri = null;
    private Date acExpiredAt = null;
    private String accessToken = null;

    public void sendTextMsg(String toUser, String message) throws IOException, URISyntaxException {
        getAccessToken();
        rebuildUri();
        var textMsg = new TextMsg(BP_DETECTOR_APP_ID, toUser, message);
        var ret = rest.postForObject(msgSendUri, textMsg, SendMsgRet.class);
        log.info("{}:{}:{}", ret.getErrcode(), ret.getErrmsg(), ret.getInvaliduser());
    }

    private void getAccessToken() throws IOException, URISyntaxException {
        rebuildUri();
        if (acExpiredAt == null)
            acExpiredAt = new Date();
        var now = Calendar.getInstance().getTime();
        if (accessToken == null || now.after(acExpiredAt)) {
            resetSendMsgUri();
            var ret = rest.getForObject(tokenUri, GetTokenRet.class);
            accessToken = (String) ret.getAccess_token();
            log.info("{}:{}:{}", ret.getErrcode(), ret.getErrmsg(), ret.getExpires_in());
            acExpiredAt = new Date(now.getTime() + ret.getExpires_in());
        }
        return;
    }

    private void rebuildUri() throws URISyntaxException {
        if (tokenUri == null) {
            var tokenUrl = BASE_URL + "/cgi-bin/gettoken";
            var parameters = new LinkedList<NameValuePair>();
            parameters.add(new BasicNameValuePair("corpid", CORP_ID));
            parameters.add(new BasicNameValuePair("corpsecret", BP_DETECTOR_APP_SECRET));
            tokenUri = new URIBuilder(tokenUrl).addParameters(parameters).build();
        }
        if (msgSendUri == null) {
            var msgSendUrl = BASE_URL + "/cgi-bin/message/send?access_token=" + accessToken;
            msgSendUri = new URIBuilder(msgSendUrl).build();
        }
    }
    private void resetSendMsgUri(){
        msgSendUri = null;
    }
}
