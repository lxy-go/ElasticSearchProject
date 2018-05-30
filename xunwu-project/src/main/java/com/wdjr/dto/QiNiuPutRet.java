package com.wdjr.dto;

public final class QiNiuPutRet {
    public String key;
    public String hash;
    public String bucket;
    public String width;
    public String height;

    @Override
    public String toString() {
        return "QiNiuPutRet{" +
                "key='" + key + '\'' +
                ", hash='" + hash + '\'' +
                ", bucket='" + bucket + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                '}';
    }
}
