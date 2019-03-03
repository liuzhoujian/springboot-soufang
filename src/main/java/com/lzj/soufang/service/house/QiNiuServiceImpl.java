package com.lzj.soufang.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

@Service
public class QiNiuServiceImpl implements IQiNiuService, InitializingBean {
    /*以下注入的属性在webFileUploadConfig中配置*/

    //文件上传工具
    @Autowired
    private UploadManager uploadManager;

    //空间管理器
    @Autowired
    private BucketManager bucketManager;

    //认证实例
    @Autowired
    private Auth auth;

    //bucket
    @Value("${qiniu.Bucket}")
    private String bucket;

    //上传文件后返回格式的定制
    private StringMap putPolicy;

    @Override
    public void afterPropertiesSet() throws Exception {
        putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");
    }

    /**
     * 获取上传凭证
     * @return
     */
    private String getUploadToken() {
        return this.auth.uploadToken(bucket, null, 3600, putPolicy);
    }

    /**
     * 上传文件
     * @param file
     * @return
     * @throws QiniuException
     */
    @Override
    public Response uploadFile(File file) throws QiniuException {
        Response response = this.uploadManager.put(file, null, getUploadToken());
        //要是上传不成功,重试3次
        int retry = 0;
        if(!response.isOK() && retry < 3) {
            response = this.uploadManager.put(file, null, getUploadToken());
            retry++;
        }
        return response;
    }

    /**
     * 上传文件
     * @param inputStream
     * @return
     * @throws QiniuException
     */
    @Override
    public Response uploadFile(InputStream inputStream) throws QiniuException {
        Response response = this.uploadManager.put(inputStream, null, getUploadToken(), null, null);
        //要是上传不成功，重试3次
        int retry = 0;
        if(!response.isOK() && retry < 3) {
            response = this.uploadManager.put(inputStream, null, getUploadToken(), null, null);
            retry++;
        }
        return response;
    }

    /**
     * 删除文件
     * @param key
     * @return
     * @throws QiniuException
     */
    @Override
    public Response delete(String key) throws QiniuException {
        Response response = this.bucketManager.delete(bucket, key);
        int retry = 0;
        if(!response.isOK() && retry < 3) {
            response =  this.bucketManager.delete(bucket, key);
            retry++;
        }
        return response;
    }
}
