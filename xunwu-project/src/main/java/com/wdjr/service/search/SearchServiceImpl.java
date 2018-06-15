package com.wdjr.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wdjr.entity.House;
import com.wdjr.entity.HouseDetail;
import com.wdjr.entity.HouseTag;
import com.wdjr.repository.HouseDetailRepository;
import com.wdjr.repository.HouseRepository;
import com.wdjr.repository.HouseTagRepository;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements ISearchService {

    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    @Autowired
    private HouseRepository houseRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TransportClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HouseDetailRepository houseDetailRepository;

    @Autowired
    private HouseTagRepository houseTagRepository;

    @Override
    public boolean index(Long houseId) {
        House house = houseRepository.findOne(houseId);
        if(house == null) {
            logger.error("Index house {} dose not exist!", houseId);
            return false;
        }

        HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
        modelMapper.map(house, indexTemplate);

        HouseDetail detail = houseDetailRepository.findByHouseId(houseId);
        if (detail == null){
            //异常处理
        }
        modelMapper.map(detail, indexTemplate);

        List<HouseTag> tags = houseTagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()){
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(houseTag -> tagStrings.add(houseTag.getName()));
        }

        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));

        logger.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();

        boolean success ;
        long totalHits = searchResponse.getHits().getTotalHits();
        if (totalHits == 0){
            success = create(indexTemplate);
        }else if(totalHits == 1){
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(indexTemplate,esId);
        }else{
            success = deleteAndCreate(indexTemplate, totalHits);
        }
        if (success){
            logger.debug("Index success with house"+houseId);
        }
        return success;
    }

    private boolean create(HouseIndexTemplate indexTemplate){
        try {
            //设置json格式进行索引
            IndexResponse response = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();

            logger.debug("Create idnex with house: "+indexTemplate.getHouseId());

            if(response.status() == RestStatus.CREATED){
                return true;
            }else{
                return false;
            }

        } catch (JsonProcessingException e) {
            logger.error("Error to index house"+ indexTemplate.getHouseId(),e);
            return false;
        }
    }
    private boolean update(HouseIndexTemplate indexTemplate,String esId){
        try {
            //设置json格式进行索引
            UpdateResponse response = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                    .setDoc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();

            logger.debug("update with house: "+indexTemplate.getHouseId());

            if(response.status() == RestStatus.CREATED){
                return true;
            }else{
                return false;
            }

        } catch (JsonProcessingException e) {
            logger.error("Error to update house"+ indexTemplate.getHouseId(),e);
            return false;
        }
    }
    private boolean deleteAndCreate(HouseIndexTemplate indexTemplate, long totalHit){
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()))
                .source(INDEX_NAME);
        logger.debug("Delete by query for house: "+ builder);
        //获取结果
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        if(deleted != totalHit){
           logger.warn("Need delete {}, but {} was deleted!",totalHit,deleted);
           return false;
        }else{
            return create(indexTemplate);
        }
    }

    @Override
    public void remove(Long houseId) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);
        logger.debug("Delete by query for house: "+ builder);
        //获取结果
        BulkByScrollResponse response = builder.get();
        long deleted = response.getDeleted();
        logger.debug("Delete total"+deleted);
    }
}
