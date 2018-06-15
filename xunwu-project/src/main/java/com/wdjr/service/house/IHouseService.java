package com.wdjr.service.house;

import com.wdjr.dto.HouseDTO;
import com.wdjr.form.DataTableSearch;
import com.wdjr.form.HouseForm;
import com.wdjr.form.RentSearch;
import com.wdjr.service.ServiceMultiResult;
import com.wdjr.service.ServiceResult;

/**
 * 房屋管理接口
 */
public interface IHouseService {

    //增
    ServiceResult<HouseDTO> save(HouseForm houseForm);
    //改
    ServiceResult update(HouseForm houseForm);
    //查
    ServiceMultiResult<HouseDTO> adminQuery(DataTableSearch searchBody);
    //根据id数据回显
    ServiceResult<HouseDTO> findCompleteOne(Long id);


    ServiceResult removePhoto(Long id);

    ServiceResult updateCover(Long coverId, Long targetId);

    ServiceResult addTag(Long houseId, String tag);

    ServiceResult removeTag(Long houseId, String tag);


    ServiceResult updateStatus(Long id, int status);

    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);
}
