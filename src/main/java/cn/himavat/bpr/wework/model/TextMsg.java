package cn.himavat.bpr.wework.model;

import lombok.Data;

import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

@Data
public class TextMsg {
    public TextMsg(int agentid,String touser,String msgContent){
        this.agentid = agentid;
        this.touser = touser;
        this.text = new Hashtable();
        text.put("content",msgContent);
        this.climsgid = "climsgid_" + new Random().ints();
        this.msgtype = "text";
        this.safe = 0;
    }
    private String touser;
    private int agentid;
    private String msgtype;
    private String climsgid;
    private int safe = 0;
    private Map text = null;
}
