package com.colin.es.entities.geo;

import lombok.Data;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

/**
 * @Package: com.colin.es.entities.geo
 * @Author: sxf
 * @Date: 2020-6-9
 * @Description:
 */
@Data
@Document(indexName = "attractions", type = "restaurant",shards = 5,replicas = 1)
public class GeoPointLocation {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;

    @GeoPointField
    private GeoPoint location;//位置坐标 lat纬度 lon经度
    //位置坐标 格式： 纬度,经度
//    private String location;

    private String distance;

}
