package com.wdjr.controller.search;

import com.wdjr.service.search.ISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @Autowired
    ISearchService searchService;

    @GetMapping("/search")
    public boolean searchTest(){
        boolean success = searchService.index(15L);

        return success;
    }
}
