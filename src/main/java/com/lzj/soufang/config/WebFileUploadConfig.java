package com.lzj.soufang.config;

import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//七牛云图片上传的配置
@Configuration
public class WebFileUploadConfig {

    //构建配置类:华东机房
    @Bean
    public com.qiniu.storage.Configuration qiniuConfig() {
        return new com.qiniu.storage.Configuration(Zone.zone0());
    }

    //构建七牛文件上传工具类
    @Bean
    public UploadManager uploadManager() {
        return new UploadManager(qiniuConfig());
    }

    //获取配置文件中的AccessKey SecrectKey
    @Value("${qiniu.AccessKey}")
    private String accessKey;

    @Value("${qiniu.SecretKey}")
    private String secretKey;

    //认证信息实例
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }

    //构建七牛空间管理实例
    @Bean
    public BucketManager bucketManager() {
        return new BucketManager(auth(), qiniuConfig());
    }

    //gson
    @Bean
    public Gson gson() {
        return new Gson();
    }
}
