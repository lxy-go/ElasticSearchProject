package com.wdjr.controller.admin;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.wdjr.base.ApiResponse;
import com.wdjr.dto.QiNiuPutRet;
import com.wdjr.service.house.IQiNiuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class AdminController {

    @Autowired
    Gson gson;

    @Autowired
    IQiNiuService qiNiuService;

    @GetMapping("/admin/center")
    public String adminCenterPage(){
        return "admin/center";
    }
    @GetMapping("/admin/welcome")
    public String adminWelcomePage(){
        return "/admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(){
        return "admin/login";
    }
    @GetMapping("admin/add/house")
    public String addHouse(){
        return "admin/house-add";
    }
    @PostMapping(value = "admin/upload/photo",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ApiResponse uploadPhtot(@RequestParam("file")MultipartFile file){
        if(file.isEmpty()){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        String filename = file.getOriginalFilename();

        try {
            InputStream inputStream = file.getInputStream();
            Response response = qiNiuService.uploadFile(inputStream);
            if(response.isOK()){
                QiNiuPutRet ret = gson.fromJson(response.bodyString(), QiNiuPutRet.class);
                return ApiResponse.ofSuccess(ret);
            }else{
                return ApiResponse.ofMessage(response.statusCode, response.getInfo());
            }


        }
        catch (QiniuException e){
            Response response = e.response;
            try {
                return ApiResponse.ofMessage(response.statusCode, response.bodyString());
            } catch (QiniuException e1) {
                return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
            }

        }

        catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
        }

    }

}
