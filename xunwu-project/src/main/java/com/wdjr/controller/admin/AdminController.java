package com.wdjr.controller.admin;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.wdjr.base.ApiDataTableResponse;
import com.wdjr.base.ApiResponse;
import com.wdjr.base.HouseOperation;
import com.wdjr.base.HouseStatus;
import com.wdjr.dto.*;
import com.wdjr.entity.SupportAddress;
import com.wdjr.form.DataTableSearch;
import com.wdjr.form.HouseForm;
import com.wdjr.service.ServiceMultiResult;
import com.wdjr.service.ServiceResult;
import com.wdjr.service.house.IAddressService;
import com.wdjr.service.house.IHouseService;
import com.wdjr.service.house.IQiNiuService;
import joptsimple.internal.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Controller
public class AdminController {

    @Autowired
    IHouseService houseService;

    @Autowired
    IAddressService addressService;

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
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(){
        return "admin/login";
    }

    /**
     * 房源浏览
     * @return
     */
    @GetMapping("/admin/house/list")
    public String houseListPage(){
        return "admin/house-list";
    }

    @PostMapping("/admin/houses")
    @ResponseBody
    public ApiDataTableResponse house(@ModelAttribute DataTableSearch searchBody){
        ServiceMultiResult<HouseDTO> result = houseService.adminQuery(searchBody);
        ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
        response.setData(result.getResult());
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        response.setDraw(searchBody.getDraw());
        return response;
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

    @PostMapping("/admin/add/house")
    @ResponseBody
    public ApiResponse addHouse(@Validated @ModelAttribute("form-house-add") HouseForm houseForm, BindingResult bindingResult){
        //首先检测绑定参数是否成功
        if (bindingResult.hasErrors()){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),bindingResult.getAllErrors().get(0).getDefaultMessage(),null);
        }
        //然后检测有图片上传
        if (houseForm.getPhotos()==null || houseForm.getCover()==null){
            return  ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }
        //是否存在区域信息
        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());

        if (addressMap.keySet().size()!=2){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        //是否成功保存
        if (result.isSuccess()){
            return ApiResponse.ofSuccess(result);
        }
        return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
    }

    @GetMapping("/admin/house/edit")
    public String houseEditPage(@RequestParam(value = "id")Long id, Model model){
        if (id == null || id<1){
            return "404";
        }

        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(id);

        if (!serviceResult.isSuccess()){
            return "404";
        }
        HouseDTO result = serviceResult.getResult();

        Map<SupportAddress.Level,SupportAddressDTO> addressMap = addressService.findCityAndRegion(result.getCityEnName(), result.getRegionEnName());
        HouseDetailDTO houseDetail = result.getHouseDetail();
        SubwayDTO subway = addressService.findSubway(houseDetail.getSubwayLineId()).getResult();
        SubwayStationDTO subwayStation = addressService.findSubwayStation(houseDetail.getSubwayStationId()).getResult();

        model.addAttribute("house",result);
        model.addAttribute("city",addressMap.get(SupportAddress.Level.CITY));
        model.addAttribute("region",addressMap.get(SupportAddress.Level.REGION));
        model.addAttribute("subway",subway);
        model.addAttribute("station",subwayStation);
        return "admin/house-edit";
    }

    /**
     * 更新房源文字信息
     * @param houseForm
     * @param bindingResult
     * @return
     */
    @PostMapping("/admin/house/edit")
    @ResponseBody
    public ApiResponse saveHouse(@Valid @ModelAttribute("form-house-list") HouseForm houseForm,BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(),bindingResult.getAllErrors().get(0).getDefaultMessage(),null);
        }
        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (addressMap.keySet().size() != 2) {
            return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
        }
        ServiceResult result = houseService.update(houseForm);
        if (result.isSuccess()){
            return ApiResponse.ofSuccess(null);
        }
        ApiResponse response = ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        response.setMessage(result.getMessage());
        return response;
    }

    /**
     * 移除图片接口
     * @param id
     * @return
     */
    @DeleteMapping("/admin/house/photo")
    @ResponseBody
    public ApiResponse removeHousePhoto(@RequestParam(value="id") Long id){
        ServiceResult result = this.houseService.removePhoto(id);
        if (result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());

    }

    /**
     *修改封面接口
     * @param coverId
     * @param targetId
     * @return
     */
    @PostMapping("/admin/house/cover")
    @ResponseBody
    public ApiResponse updateCover(@RequestParam(value = "cover_id")Long coverId,
                                   @RequestParam(value = "target_id")Long targetId){
        ServiceResult result = this.houseService.updateCover(coverId,targetId);
        if (result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    /**
     * 增加标签接口
     * @param houseId
     * @param tag
     * @return
     */
    @PostMapping("/admin/house/tag")
    @ResponseBody
    public ApiResponse addHouseTag(@RequestParam(value = "house_id") Long houseId,
                                   @RequestParam(value = "tag")String tag){
        if (houseId<1 || Strings.isNullOrEmpty(tag)){
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        ServiceResult result = this.houseService.addTag(houseId,tag);
        if (result.isSuccess()){
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
    }

    /**
     * 删除标签接口
     * @param houseId
     * @param tag
     * @return
     */
    @DeleteMapping("/admin/house/tag")
    @ResponseBody
    public ApiResponse removeHouseTag(@RequestParam(value = "house_id")Long houseId,
                                      @RequestParam(value = "tag")String tag){
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        ServiceResult result = this.houseService.removeTag(houseId, tag);
        if (result.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    @PutMapping("admin/house/operate/{id}/{operation}")
    @ResponseBody
    public ApiResponse operateHouse(@PathVariable(value = "id")Long id,
                                    @PathVariable(value = "operation") int operation){
        if (id<=0){
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }
        ServiceResult result;
        switch (operation){
            case HouseOperation.PASS:
                result = this.houseService.updateStatus(id,HouseStatus.PASSES.getValue());
                break;
            case HouseOperation.PULL_OUT:
                result = this.houseService.updateStatus(id,HouseStatus.NOT_AUDITED.getValue() );
                break;
            case HouseOperation.DELETE:
                result = this.houseService.updateStatus(id,HouseStatus.DELETED.getValue());
                break;
            case HouseOperation.RENT:
                result  = this.houseService.updateStatus(id, HouseStatus.RENTED.getValue());
                break;
            default:
                return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(null);
        }
        return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(),
                result.getMessage());
    }


}
