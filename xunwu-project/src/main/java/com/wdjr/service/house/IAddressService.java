package com.wdjr.service.house;

import com.wdjr.dto.SubwayDTO;
import com.wdjr.dto.SubwayStationDTO;
import com.wdjr.dto.SupportAddressDTO;
import com.wdjr.entity.Subway;
import com.wdjr.entity.SupportAddress;
import com.wdjr.service.ServiceMultiResult;
import com.wdjr.service.ServiceResult;

import java.util.List;
import java.util.Map;

/**
 * 地址服务接口
 */

public interface IAddressService {

    /**
     * 获取所有城市的支持列表
     * @return
     */
    ServiceMultiResult<SupportAddressDTO> findAllCities();

    /**
     * 根据英文名称简写获取具体区域信息 【北京】
     * @param cityEnName
     * @param regionEnName
     * @return
     */
    Map<SupportAddress.Level,SupportAddressDTO>findCityAndRegion(String cityEnName,String regionEnName);

    /**
     * 根据城市的英文简称，获取该城市的所有支持区域信息【大兴区】
     * @param cityName
     * @return
     */
    ServiceMultiResult<SupportAddressDTO>  findAllRegionsByCityName(String cityName);

    /**
     * 根据城市英文名称获取地铁线路信息【5号线】
     * @param cityEnName
     * @return
     */
    List<SubwayDTO> findAllSubwayByCityEnName(String cityEnName);

    /**
     * 根据地铁线路获取地铁的站信息【荣昌东街】
     * @param subwayId
     * @return
     */
    List<SubwayStationDTO> findAllStationBySubway(Long subwayId);
    /**
     * 获取地铁线信息
     * @param subwayId
     * @return
     */
    ServiceResult<SubwayDTO> findSubway(Long subwayId);

    /**
     * 获取地铁站点信息
     * @param stationId
     * @return
     */
    ServiceResult<SubwayStationDTO> findSubwayStation(Long stationId);

    /**
     * 根据城市英文简写获取城市详细信息
     * @param cityEnName
     * @return
     */
    ServiceResult<SupportAddressDTO> findCity(String cityEnName);

}
