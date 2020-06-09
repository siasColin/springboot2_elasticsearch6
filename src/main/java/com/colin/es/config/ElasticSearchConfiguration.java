package com.colin.es.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Package: com.colin.es.config
 * @Author: sxf
 * @Date: 2020-6-9
 * @Description:
 */
@Component(value = "esConfig")
public class ElasticSearchConfiguration {

    @Value("${spring.data.elasticsearch.index.blog}")
    private String blogIndexNamePrefix;

    /**
     * 生成动态索引名称
     *      @Document(indexName = "#{esConfig.getBlogIndexName()}", type = "blogpost",shards = 5,replicas = 1)
     * @return
     */
    public String getBlogIndexName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM");
        return blogIndexNamePrefix +"-"+ LocalDateTime.now().format(formatter);
    }
}
