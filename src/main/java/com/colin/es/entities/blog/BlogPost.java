package com.colin.es.entities.blog;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.List;

/**
 * @Package: cn.thislx.springbootes.bean
 * @Author: sxf
 * @Date: 2020-6-7
 * @Description: 博文实体类
 *      indexName：索引，相当于数据库
 *      type：类型，相当于表
 *      shards: 每个索引的主分片数
 *      replicas: 每个主分片的副本数
 */
@Data
@Document(indexName = "#{esConfig.getBlogIndexName()}", type = "blogpost",shards = 5,replicas = 1)
public class BlogPost implements Serializable {
    private static final long serialVersionUID = -8162151579771217651L;
    @Id
    private Long id;
    /**
     * analyzer：
     *      默认standard，
     *      内置的分析器有whitespace 、 simple和english
     *      第三方分词器：ik分词器 包括ik_max_word和ik_smart，ik_max_word：会将文本做最细粒度的拆分；尽可能多的拆分出词语 ，ik_smart：会做最粗粒度的拆分；已被分出的词语将不会再次被其它词语占有
     * searchAnalyzer:
     *      指定查询的分词器,默认和analyzer保持一致，一般分词器和查询分词器要保持一致
     */
    @Field(type = FieldType.Text,analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text,analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String body;
    @Field(type = FieldType.Keyword)
    private String[] tags;
    @Field(type = FieldType.Nested)
    private List<Comment> comments;
}
