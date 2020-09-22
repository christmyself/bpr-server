package cn.himavat.bpr.wework.model;

import lombok.Data;

@Data
public class GetTokenRet {
    private int errcode;
    private String errmsg;
    private String access_token;
    private int expires_in;
}
