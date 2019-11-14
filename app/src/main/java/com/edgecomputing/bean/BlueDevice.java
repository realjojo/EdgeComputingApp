package com.edgecomputing.bean;

/**
 * @Author: jojo
 * @Date: Created on 2019/11/12 15:31
 */
public class BlueDevice {
    private String name;
    private String address;
    private int state;

    public BlueDevice() {
        this.name = "";
        this.address = "";
        this.state = 0;
    }

    public BlueDevice(String name, String address, int state) {
        this.name = name;
        this.address = address;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
