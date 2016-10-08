package com.klauncher.biddingos.commons.net;


import com.klauncher.biddingos.commons.Setting;
import com.klauncher.biddingos.commons.utils.AESUtils;

/**
 * HTTP响应相关信息
 */
public class HttpResponse {


    private HttpResponseStatus.Code respCode;
    private String respBody;

    public HttpResponse(HttpResponseStatus.Code respCode, String respBody) {
        this.respCode = respCode;
        this.respBody = respBody;
    }

    public HttpResponseStatus.Code getRespCode() {
        return respCode;
    }

    public String getRespBody(boolean bNeedDecrypt) {
        String responseStr;
        if(bNeedDecrypt) {
            responseStr = AESUtils.decode(Setting.SECRET, respBody);
        }else {
            responseStr = respBody;
        }
        return responseStr;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "respCode=" + respCode +
                ", respBody='" + respBody + '\'' +
                '}';
    }
}
