package com.wdjr.service.house;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.wdjr.ApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class QiNiuTestService extends ApplicationTests {

    @Autowired
    private  IQiNiuService qiNiuService;

    @Test
    public void testUploadFile(){

        String filename = "E:\\ElasticSearchHouse\\xunwu-project\\tmp\\14.RabbitMQDirect.png";
        File file = new File(filename);

        Assert.assertTrue(file.exists());

        try {
            Response response = qiNiuService.uploadFile(file);
            Assert.assertTrue(response.isOK());
            System.out.println(response);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteFile(){
        String key = "FgnPYoxZBdgiDu3ULFQdZRtTit45";
        try {
            qiNiuService.delete(key);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }

}
