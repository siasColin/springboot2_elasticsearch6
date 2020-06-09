package com.colin.es.controller;

import com.colin.es.dao.BlogPostRepository;
import com.colin.es.dao.LocationRepository;
import com.colin.es.entities.geo.GeoPointLocation;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @Package: com.colin.es.controller
 * @Author: sxf
 * @Date: 2020-6-9
 * @Description:
 */
@RestController
@RequestMapping("/location")
public class LocationController {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private LocationRepository locationRepository;

    @RequestMapping(value = "/add")
    public Object add() {
        return this.locationRepository.saveAll(initData());
    }

    /**
     * 搜索附近
     * @param lon 当前位置 经度
     * @param lat 当前位置 纬度
     * @param distance 搜索多少范围
     * @param pageable 分页参数
     * @return
     *      http://localhost:8080/location/searchNear?lon=-73.989&lat=40.722&distance=10
     */
    @GetMapping("/searchNear")
    public List<GeoPointLocation> searchNear(double lon, double lat, String distance, @PageableDefault Pageable pageable){
//        lat = 40.722;
//        lon = -73.989;
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        //搜索字段为 location
        GeoDistanceQueryBuilder geoBuilder = new GeoDistanceQueryBuilder("location");
        geoBuilder.point(lat, lon);//指定从哪个位置搜索
        geoBuilder.distance(distance, DistanceUnit.KILOMETERS);//指定搜索多少km
        qb.filter(geoBuilder);

        //可添加其他查询条件
        //qb.must(QueryBuilders.matchQuery("name", name));
        Page<GeoPointLocation> page = locationRepository.search(qb, pageable);
        List<GeoPointLocation> list = page.getContent();
        list.forEach(l -> {
            double calculate = GeoDistance.ARC.calculate(l.getLocation().getLat(), l.getLocation().getLon(), lat, lon, DistanceUnit.METERS);
            l.setDistance("距离" + (int)calculate + "m");
        });
        return list;
    }

    /**
     * 搜索附近
     * @param pageable 分页参数
     * @return
     *      http://localhost:8080/location/searchBoundingBox?name=薯条
     */
    @GetMapping("/searchBoundingBox")
    public List<GeoPointLocation> searchBoundingBox(String name,@PageableDefault Pageable pageable){
        double top_left_lat = 40.8;
        double top_left_lon = -74.0;
        double bottom_right_lat = 40.7;
        double bottom_right_lon = -73.0;

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        //搜索字段为 location
        GeoBoundingBoxQueryBuilder geoBoundingBuilder = new GeoBoundingBoxQueryBuilder("location");
        geoBoundingBuilder.setCorners(new GeoPoint(top_left_lat,-top_left_lon),new GeoPoint(bottom_right_lat,-bottom_right_lon));
        qb.filter(geoBoundingBuilder);

        //可添加其他查询条件
        if(name != null && !name.trim().equals("")){
            qb.must(QueryBuilders.matchQuery("name", name));
        }
        Page<GeoPointLocation> page = locationRepository.search(qb, pageable);
        /*SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("attractions","attractions2")
                .withQuery(qb)
                .withPageable(pageable)
                .build();
        Page<GeoPointLocation> page = locationRepository.search(searchQuery);*/
        List<GeoPointLocation> list = page.getContent();
        return list;
    }

    /**
     *
     * @param lon
     * @param lat
     * @param distance
     * @param pageable
     * @return
     *      http://localhost:8080/location/searchNearWithOrder?lon=-73.989&lat=40.722&distance=10
     */
    @GetMapping("/searchNearWithOrder")
    public List<GeoPointLocation> searchNearWithOrder(double lon, double lat, String distance, @PageableDefault Pageable pageable){
//        lat = 40.722;
//        lon = -73.989;
        //搜索字段为 location
        GeoDistanceQueryBuilder geoBuilder = new GeoDistanceQueryBuilder("location");
        geoBuilder.point(lat, lon);//指定从哪个位置搜索
        geoBuilder.distance(distance, DistanceUnit.KILOMETERS);//指定搜索多少km

        //距离排序
        GeoDistanceSortBuilder sortBuilder = new GeoDistanceSortBuilder("location", lat, lon);
        sortBuilder.order(SortOrder.ASC);//升序
        sortBuilder.unit(DistanceUnit.METERS);

        //构造查询器
        NativeSearchQueryBuilder qb = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withFilter(geoBuilder)
                .withSort(sortBuilder);

        //可添加其他查询条件
        //qb.must(QueryBuilders.matchQuery("name", name));
        Page<GeoPointLocation> page = locationRepository.search(qb.build());
        List<GeoPointLocation> list = page.getContent();
        list.forEach(l -> {
            double calculate = GeoDistance.PLANE.calculate(l.getLocation().getLat(), l.getLocation().getLon(), lat, lon, DistanceUnit.METERS);
            l.setDistance("距离" + (int)calculate + "m");
        });
        return list;
    }

    private List<GeoPointLocation> initData(){
        List<GeoPointLocation> resultList = new ArrayList<GeoPointLocation>();
        //第一条数据
        GeoPointLocation firstLocation = new GeoPointLocation();
        firstLocation.setId(1l);
        firstLocation.setName("墨西哥烤薯条");
        firstLocation.setLocation(new GeoPoint("40.715, -74.011"));
        resultList.add(firstLocation);
        //第二条数据
        GeoPointLocation secondLocation = new GeoPointLocation();
        secondLocation.setId(2l);
        secondLocation.setName("帕拉披萨");
        secondLocation.setLocation(new GeoPoint(40.722,-73.989));
        resultList.add(secondLocation);
        //第三条数据
        GeoPointLocation thirdLocation = new GeoPointLocation();
        thirdLocation.setId(3l);
        thirdLocation.setName("小点心披萨");
        thirdLocation.setLocation(new GeoPoint(40.719,-73.983));
        resultList.add(thirdLocation);
        return resultList;
    }
}
