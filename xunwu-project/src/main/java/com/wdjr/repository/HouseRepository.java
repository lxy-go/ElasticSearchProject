package com.wdjr.repository;

import com.wdjr.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface HouseRepository extends PagingAndSortingRepository<House,Long>,JpaSpecificationExecutor<House> {

    @Modifying
    @Query("update House as house set house.cover = :cover where house.id = :id")
    void updateCover(@Param(value="id") Long targetId, @Param(value = "cover") String path);
    @Modifying
    @Query("update House as house set house.status = :status where house.id = :id")
    void updateStatus(@Param(value="id") Long id, @Param(value="status")int status);
}
