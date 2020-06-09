package com.colin.es.dao;

import com.colin.es.entities.blog.BlogPost;
import com.colin.es.entities.geo.GeoPointLocation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Package: com.colin.es.dao
 * @Author: sxf
 * @Date: 2020-6-9
 * @Description:
 */
public interface LocationRepository extends ElasticsearchRepository<GeoPointLocation,Long> {
}
