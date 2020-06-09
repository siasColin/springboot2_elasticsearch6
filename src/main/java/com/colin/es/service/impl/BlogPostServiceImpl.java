package com.colin.es.service.impl;

import com.colin.es.entities.blog.BlogPost;
import com.colin.es.service.IBlogPostService;
import com.colin.es.utils.BeanMapUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Package: com.colin.es.service.impl
 * @Author: sxf
 * @Date: 2020-6-8
 * @Description:
 */
@Service
public class BlogPostServiceImpl implements IBlogPostService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public List<BlogPost> highlightSearch(String[] keys, String keyword, Integer pageNum, Integer pageSize) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        HighlightBuilder.Field[] highlightBuilders = new HighlightBuilder.Field[keys.length];
        if(keys != null && keys.length > 0){
            for (int i = 0;i<keys.length;i++) {
                String key = keys[i];
                //should相当于数据库的or
                boolQueryBuilder.should(QueryBuilders.matchQuery(key,keyword));
                highlightBuilders[i] = new  HighlightBuilder.Field(key);
            }
        }
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        NativeSearchQuery nativeSearchQuery=new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(boolQueryBuilder)
                .withHighlightFields(highlightBuilders)
                .withHighlightBuilder(new HighlightBuilder().preTags("<span style='color:red'>").postTags("</span>"))
                .withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC))
//                .withMinScore(1l)
                .build();

        AggregatedPage<BlogPost> page = elasticsearchTemplate.queryForPage(nativeSearchQuery, BlogPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                ArrayList<BlogPost> list = new ArrayList<BlogPost>();
                SearchHits hits = response.getHits();
                for (SearchHit searchHit : hits) {
                    if (hits.getHits().length <= 0) {
                        return null;
                    }
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    sourceAsMap.put("id",Long.parseLong(sourceAsMap.get("id").toString()));
                    List tagLsit = sourceAsMap.get("tags") == null ? null : (List)sourceAsMap.get("tags");
                    if(tagLsit != null){
                        String [] tags = new String [tagLsit.size()];
                        for (int i = 0;i<tagLsit.size();i++) {
                            tags[i] = tagLsit.get(i).toString();
                        }
                        sourceAsMap.put("tags",tags);
                    }
                    /**
                     * 替换高亮字段
                     */
                    Map<String, HighlightField> fieldMap =searchHit.getHighlightFields();
                    if(fieldMap != null && fieldMap.size() > 0){
                        for (Map.Entry<String,HighlightField> entry : fieldMap.entrySet()) {
                            sourceAsMap.put(entry.getKey(),entry.getValue().fragments()[0].toString());
                        }
                    }
                    BlogPost blogPost = new BlogPost();
                    blogPost = BeanMapUtil.mapToBean(sourceAsMap,blogPost);
                    list.add(blogPost);
                }
                if (list.size() > 0) {
                    return new AggregatedPageImpl<T>((List<T>) list,pageable,hits.totalHits,hits.getMaxScore());
                }
                return null;
            }
        });
        return page.getContent();
    }
}
