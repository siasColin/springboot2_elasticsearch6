package com.colin.es.service;

import com.colin.es.entities.blog.BlogPost;

import java.util.List;

/**
 * @Package: com.colin.es.service
 * @Author: sxf
 * @Date: 2020-6-8
 * @Description:
 */
public interface IBlogPostService {
    List<BlogPost> highlightSearch(String[] keys, String keyword, Integer pageNum, Integer pageSize);
}
