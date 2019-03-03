package com.lzj.soufang.web.dto;

/**
 * 七牛服务上传文件后，返回的信息封装类
 */
public final class QiNiuPutRet {
    /*照片路径key*/
    public String key;
    public String hash;
    public String bucket;
    public int width;
    public int height;

    @Override
    public String toString() {
        return "QiNiuPutRet{" +
                "key='" + key + '\'' +
                ", hash='" + hash + '\'' +
                ", bucket='" + bucket + '\'' +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
