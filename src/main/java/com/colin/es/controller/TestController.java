package com.colin.es.controller;

import com.colin.es.dao.BlogPostRepository;
import com.colin.es.entities.blog.BlogPost;
import com.colin.es.entities.blog.Comment;
import com.colin.es.service.IBlogPostService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @Package: com.colin.es.controller
 * @Author: sxf
 * @Date: 2020-6-7
 * @Description:
 */
@RestController
public class TestController {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private IBlogPostService blogPostService;

    @RequestMapping(value = "/createIndex")
    public Object addIndexTest() {
        this.elasticsearchTemplate.createIndex(BlogPost.class);
        return this.elasticsearchTemplate.putMapping(BlogPost.class);
    }

    @RequestMapping(value = "/deleteIndex")
    public Object deleteIndex() {
        return this.elasticsearchTemplate.deleteIndex("blog_index");
    }

    @RequestMapping(value = "/add")
    public Object add() {
        return this.blogPostRepository.saveAll(initBlogPost());
    }

    @RequestMapping(value = "/findAll")
    public Object findAll(){
        // 通过查询构建器构建查询条件
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "手机");
        //执行查询
        Iterable<BlogPost> blogPosts = this.blogPostRepository.findAll();
        //Pageable pageable = PageRequest.of(param.getPageNum(), param.getPageSize(), Sort.Direction.ASC, "orderNo");
        return blogPosts;
    }

    @RequestMapping(value = "/searchByTitle")
    public Object search(String title){
        // 词条查询
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", title);
        // 执行查询
        Iterable<BlogPost> blogPosts = this.blogPostRepository.search(queryBuilder);
        return blogPosts;
    }

    @RequestMapping(value = "/nativeQueryByTitle")
    public Object nativeQuery(String title){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", title));
        //字段过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"title"},null));
        // 执行搜索，获取结果
        Page<BlogPost> blogPosts = this.blogPostRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println(blogPosts.getTotalElements());
        // 打印总页数
        System.out.println(blogPosts.getTotalPages());
        return blogPosts;
    }

    @RequestMapping(value = "/nativeQueryWithPage")
    public Object nativeQueryWithPage(String title){
        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", title));
        // 排序
        queryBuilder.withSort(SortBuilders.fieldSort("id").order(SortOrder.DESC));

        // 初始化分页参数
        int page = 0;
        int size = 1;
        // 设置分页参数
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 执行搜索，获取结果
        Page<BlogPost> blogPosts = this.blogPostRepository.search(queryBuilder.build());
        // 打印总条数
        System.out.println("总条数:"+blogPosts.getTotalElements());
        // 打印总页数
        System.out.println("总页数:"+blogPosts.getTotalPages());
        // 每页大小
        System.out.println("每页大小:"+blogPosts.getSize());
        // 当前页
        System.out.println("当前页:"+blogPosts.getNumber());
        return blogPosts;
    }

    /**
     * 嵌套查询
     * @param title
     * @param name
     * @return
     */
    @RequestMapping(value = "/searchWithNested")
    public Object searchWithNested(String title,String name){
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //不进行分词搜索
//        boolQuery.must(QueryBuilders.matchPhraseQuery("title", title));
        boolQuery.must(QueryBuilders.matchQuery("title", title));

        BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
        nestedBoolQuery.must(QueryBuilders.matchQuery("comments.name", name));
        boolQuery.must(QueryBuilders.nestedQuery("comments",nestedBoolQuery, ScoreMode.Max));
        // 执行查询
        Iterable<BlogPost> blogPosts = this.blogPostRepository.search(boolQuery);
        return blogPosts;
    }

    /**
     * 高亮检索
     * @return
     */
    @RequestMapping(value = "/highlight")
    public Object highlight(String keyword){
        String [] keys = {"title","body"};
        Integer pageNum = 0;
        Integer pageSize = 10;
        List<BlogPost> blogPostList = this.blogPostService.highlightSearch(keys,keyword,pageNum,pageSize);
        return  blogPostList;
    }

    /**
     * 在多个索引中检索数据
     *      可以使用给索引起别名的方式
     * @return
     */
    @RequestMapping(value = "/manyIndexSearch")
    public Object manyIndexSearch(String keyword){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.should(QueryBuilders.matchQuery("title",keyword));
        /**
         * content和body可以是不同索引中的属性
         */
        boolQueryBuilder.should(QueryBuilders.matchQuery("content",keyword));
        boolQueryBuilder.should(QueryBuilders.matchQuery("body",keyword));

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("blog_index") //可以直接使用别名，一个别名可以对应多个索引，可以实现从多个索引中检索数据
                .withQuery(boolQueryBuilder)
                //设置高亮字段
                .withHighlightFields(new HighlightBuilder.Field("title"),new HighlightBuilder.Field("content"),new HighlightBuilder.Field("body"))
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

    @RequestMapping(value = "/geopoint")
    public Object geopoint(){
        GeoDistanceQueryBuilder builder =
                QueryBuilders.geoDistanceQuery("location")//查询字段
                        .point(40.722,-73.989)//设置经纬度
                        .distance(100000, DistanceUnit.METERS)//设置距离和单位（米）
                        .geoDistance(GeoDistance.ARC);
        GeoDistanceSortBuilder sortBuilder =
                SortBuilders.geoDistanceSort("location",40.722,-73.989)
                        .unit(DistanceUnit.METERS)
                        .order(SortOrder.ASC);//排序方式
        //构造查询条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder =
                new NativeSearchQueryBuilder()
                        .withFilter(builder)
                        .withSort(sortBuilder);
        List<Map> listMap =this.elasticsearchTemplate.query(nativeSearchQueryBuilder.build(), response -> {
            SearchHits hits = response.getHits();
            List<Map> list=new ArrayList<>();
            Arrays.stream(hits.getHits()).forEach(h -> {
                Map<String, Object> source = h.getSourceAsMap();
                /*double targetlat = Double.parseDouble(source.get("location").toString().split(",")[0]);
                double targetlon = Double.parseDouble(source.get("location").toString().split(",")[1]);*/
                double calculate = GeoDistance.ARC.calculate(40.719, -73.983, 40.722,-73.989, DistanceUnit.METERS);
                source.put("distance",calculate);
                list.add(source);
            });
            return list;
        });
        return listMap;
    }



    /**
     * 初始化文档数据
     * @return
     */
    public List<BlogPost> initBlogPost(){
        List<BlogPost> blogPostList = new ArrayList<>();
        //第一个文档
        BlogPost firstBlog = new BlogPost();
        firstBlog.setId(1l);
        firstBlog.setTitle("Elasticsearch嵌套对象查询");
        firstBlog.setBody("由于嵌套对象 被索引在独立隐藏的文档中，我们无法直接查询它们。 相应地，我们必须使用 nested 查询 去获取它们");
        String[] firstBlogTags = {"Elasticsearch","嵌套对象","查询"};
        firstBlog.setTags(firstBlogTags);
        List<Comment> firstComments = new ArrayList<Comment>();
        for(int i = 0;i<3;i++){
            Comment comment = new Comment();
            comment.setName("colin_first_"+i);
            comment.setAge(i);
            comment.setStars(i*100);
            comment.setComment("评论_first_"+i);
            comment.setDate(new Date());
            firstComments.add(comment);
        }
        firstBlog.setComments(firstComments);
        blogPostList.add(firstBlog);

        //第二个文档
        BlogPost secondBlog = new BlogPost();
        secondBlog.setId(2l);
        secondBlog.setTitle("Elasticsearch索引文档");
        secondBlog.setBody("通过使用 index API ，文档可以被 索引 —— 存储和使文档可被搜索。 但是首先，我们要确定文档的位置。正如我们刚刚讨论的，一个文档的 _index 、 _type 和 _id 唯一标识一个文档。 我们可以提供自定义的 _id 值，或者让 index API 自动生成。");
        String[] secondBlogTags = {"Elasticsearch","索引","文档"};
        secondBlog.setTags(secondBlogTags);
        List<Comment> secondComments = new ArrayList<Comment>();
        for(int i = 0;i<3;i++){
            Comment comment = new Comment();
            comment.setName("colin_second_"+i);
            comment.setAge(i);
            comment.setStars(i*100);
            comment.setComment("评论_second_"+i);
            comment.setDate(new Date());
            secondComments.add(comment);
        }
        secondBlog.setComments(secondComments);
        blogPostList.add(secondBlog);
        return blogPostList;
    }

    public static void main(String[] args) {
        double calculate = GeoDistance.ARC.calculate(40.719, -73.983, 40.722,-73.989, DistanceUnit.METERS);
    }

}
