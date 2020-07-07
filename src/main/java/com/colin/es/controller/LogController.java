package com.colin.es.controller;

import com.colin.es.entities.log.Log;
import com.colin.es.utils.JsonUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Package: com.colin.es.controller
 * @Author: sxf
 * @Date: 2020-7-7
 * @Description:
 */
@RestController
public class LogController {
    private final static Logger logger = LoggerFactory.getLogger(TestController.class);
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @RequestMapping(value = "/initLog")
    public String initLogData() throws InterruptedException {
        for (int i = 0;i<10;i++) {
            Log infoLog = new Log();
            infoLog.setModule("系统管理==="+i);
            infoLog.setMsg("info级别日志==="+i);
            infoLog.setHost("192.168.0.135");
            infoLog.setCreatetime(new Date());
            logger.info(JsonUtils.toString(infoLog));
            Thread.sleep(1000);
            Log warnLog = new Log();
            warnLog.setModule("系统管理==="+i);
            warnLog.setMsg("warn级别日志==="+i);
            warnLog.setHost("192.168.0.135");
            warnLog.setCreatetime(new Date());
            logger.warn(JsonUtils.toString(warnLog));
            Thread.sleep(1000);
            Log errorLog = new Log();
            errorLog.setModule("系统管理==="+i);
            errorLog.setMsg("error级别日志==="+i);
            errorLog.setHost("192.168.0.135");
            errorLog.setCreatetime(new Date());
            logger.error(JsonUtils.toString(errorLog));
        }
        return "success";
    }
    @RequestMapping(value = "/highlightList")
    public Object highlightList(String keyword){
        // 初始化分页参数
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.matchQuery("msg",keyword));
        /**
         * msg和module可以是不同索引中的属性
         */
        boolQueryBuilder.should(QueryBuilders.matchQuery("module",keyword));

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withSort(SortBuilders.fieldSort("createtime").order(SortOrder.DESC))
                .withIndices("logstash") //可以直接使用别名，一个别名可以对应多个索引，可以实现从多个索引中检索数据
                .withQuery(boolQueryBuilder)
                //设置高亮字段
                .withHighlightFields(new HighlightBuilder.Field("msg"),new HighlightBuilder.Field("module"))
                .withHighlightBuilder(new HighlightBuilder().preTags("<span style='color:red'>").postTags("</span>"))
                .build();
        List<Map> listMap =this.elasticsearchTemplate.query(searchQuery, response -> {
            SearchHits hits = response.getHits();
            List<Map> list=new ArrayList<>();
            Arrays.stream(hits.getHits()).forEach(h -> {
                Map<String, Object> source = h.getSourceAsMap();
                //处理高亮字段
                Map<String, HighlightField> fieldMap =h.getHighlightFields();
                if(fieldMap != null && fieldMap.size() > 0){
                    for (Map.Entry<String,HighlightField> entry : fieldMap.entrySet()) {
                        source.put(entry.getKey(),entry.getValue().fragments()[0].toString());
                    }
                }
                list.add(source);
            });
            return list;
        });
        return listMap;
    }
}
