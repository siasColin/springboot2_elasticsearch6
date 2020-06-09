package com.colin.es.entities.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * @Package: cn.thislx.springbootes.bean
 * @Author: sxf
 * @Date: 2020-6-7
 * @Description: 评论实体类
 */
@Data
public class Comment implements Serializable {
    private static final long serialVersionUID = 906486972823572963L;

    @Field(type = FieldType.Text,analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String name;
    @Field(type = FieldType.Text,analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String comment;
    @Field(type = FieldType.Integer)
    private Integer age;
    @Field(type = FieldType.Integer)
    private Integer stars;

    @Field(type = FieldType.Date,format = DateFormat.custom, pattern ="yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date date;
}
