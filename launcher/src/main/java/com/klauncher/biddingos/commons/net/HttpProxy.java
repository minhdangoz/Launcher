package com.klauncher.biddingos.commons.net;

/**
 * http 代理
 */
public class HttpProxy {

    public String hostAddress;
    public int port;
    public String user;
    public String password;

    /**
     * @param hostAddress 服务器域名或IP
     * @param port        端口
     * @param user        用户名，无则填null
     * @param password    用户密码，无则填null
     */
    public HttpProxy(String hostAddress, int port, String user, String password) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.user = user;
        this.password = password;
    }

}
