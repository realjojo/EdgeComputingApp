package com.edgecomputing.bean;

/**
 * @Author: jojo
 * @Date: Created on 2020/1/7 20:03
 */
public enum ServerAddress {

    SERVER_URL1("http://10.109.246.55:8089"),
    SERVER_URL2("http://192.168.1.35:8089");

    private String server_url;

    ServerAddress(String s) {
        this.server_url = s;
    }

    public String getServer_url() {
        return server_url;
    }
}
