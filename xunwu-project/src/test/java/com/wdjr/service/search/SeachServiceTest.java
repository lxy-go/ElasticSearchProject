package com.wdjr.service.search;

import com.wdjr.ApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SeachServiceTest extends ApplicationTests {
    @Autowired
    ISearchService searchService;

    @Test
    public void testIndex(){

        Long targetHouseId = 15L;

        boolean success = searchService.index(targetHouseId);

    }

    @Test
    public void testRemove(){
        searchService.remove(15L);
    }
}
