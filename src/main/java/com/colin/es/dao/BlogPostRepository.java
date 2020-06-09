package com.colin.es.dao;

import com.colin.es.entities.blog.BlogPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @Package: com.colin.es.dao
 * @Author: sxf
 * @Date: 2020-6-7
 * @Description: 继承Repository提供的一些子接口，就能具备各种基本的CRUD功能，这里继承ElasticsearchCrudRepository/ElasticsearchRepository
 */
public interface BlogPostRepository extends ElasticsearchRepository<BlogPost,Long> {

}
