package com.lzj.soufang.service.search;

import com.google.gson.Gson;
import com.lzj.soufang.entity.House;
import com.lzj.soufang.entity.HouseDetail;
import com.lzj.soufang.entity.HouseTag;
import com.lzj.soufang.repository.HouseDetailRepository;
import com.lzj.soufang.repository.HouseRepository;
import com.lzj.soufang.repository.HouseTagRepository;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements ISearchService {
    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);
    private static final String INDEX_NAME = "xunwun";
    private static final String INDEX_TYPE = "house";

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient esClient;

    @Autowired
    private Gson gson;

    @Override
    public boolean index(Integer houseId) {
        //查询house完整信息并封装在houseIndexTemplate中
        House house = houseRepository.findById(houseId).get();
        if(house == null) {
            logger.error("Index house {} dose not exist!", houseId);
            return false;
        }

        HouseIndexTemplate houseIndexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, houseIndexTemplate);

        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
        if(houseDetail == null) {
            //TODO 异常情况
        }
        modelMapper.map(houseDetail, houseIndexTemplate);

        List<HouseTag> houseTagList = houseTagRepository.findByHouseId(houseId);
        if(houseTagList != null && !houseTagList.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            houseTagList.forEach(houseTag -> {
                tagStrings.add(houseTag.getName());
            });
            houseIndexTemplate.setTags(tagStrings);
        }

        //在es查询索引，根据查到的结果执行不同的操作
        SearchRequestBuilder builder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId()));

        logger.debug(builder.toString());

        SearchResponse response = builder.get();
        long totalHits = response.getHits().getTotalHits();

        boolean success;

        if(totalHits == 0) {
            success = create(houseIndexTemplate);
        } else if(totalHits == 1) {
            String esId = response.getHits().getAt(0).getId();
            success = update(esId, houseIndexTemplate);
        } else {
            success = deleteAndCreate(totalHits, houseIndexTemplate);
        }

        if(success) {
            logger.debug("Index success with house!");
        }

        return success;
    }

    private boolean create(HouseIndexTemplate indexTemplate) {
        IndexResponse indexResponse = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
                .setSource(gson.toJson(indexTemplate)).get();

        logger.debug("Create index with house: ", indexTemplate.getHouseId());

        if(indexResponse.status() == RestStatus.CREATED) {
            return true;
        }

        return false;
    }

    private boolean update(String esId, HouseIndexTemplate indexTemplate) {
        UpdateResponse updateResponse = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                .setDoc(gson.toJson(indexTemplate)).get();

        logger.debug("Update index with house: ", indexTemplate.getHouseId());

        if(updateResponse.status() == RestStatus.OK) {
            return true;
        }

        return false;
    }

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(this.esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()))
                .source(INDEX_NAME);

        logger.debug("Delete by query house index: " + builder);

        BulkByScrollResponse bulkByScrollResponse = builder.get();
        long deleted = bulkByScrollResponse.getDeleted();
        if(totalHit != deleted) {
            logger.warn("Need deleted {}, but {} was deleted", totalHit, deleted);
            return false;
        } else {
            return create(indexTemplate);
        }
    }

    @Override
    public void remove(Integer houseId) {

    }
}