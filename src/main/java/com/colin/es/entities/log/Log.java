package com.colin.es.entities.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.elasticsearch.index.mapper.IpFieldMapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;

/**
 * @Package: com.colin.es.entities.log
 * @Author: sxf
 * @Date: 2020-7-7
 * @Description:
 */
@Data
public class Log implements Serializable {
    private static final long serialVersionUID = 6478645834912448405L;
    private String msg;
    private String module;
    private String host;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createtime;
}
