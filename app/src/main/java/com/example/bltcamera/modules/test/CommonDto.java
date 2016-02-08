package com.example.bltcamera.modules.test;

/**
 * Created by hmspl on 7/2/16.
 */
public class CommonDto {

    private String type;

    private String data;

    public CommonDto(String type, String data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
