package com.lzj.soufang;

import com.lzj.soufang.service.search.ISearchService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SearchServiceTests extends SpringbootSoufangApplicationTests {

    @Autowired
    private ISearchService searchService;

    @Test
    public void searchServiceTest() {
        boolean success = searchService.index(15);
        System.out.println(success);
    }
}
