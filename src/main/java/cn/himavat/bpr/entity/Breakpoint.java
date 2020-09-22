package cn.himavat.bpr.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class Breakpoint {
    @Id
    @GeneratedValue
    private int id;
    private int deviceId;
    private Date generatedAt;
    /**
     * 1.新增一条断线信息
     * -1.恢复一条断线信息
     */
    private int bpType;
    private int current;
}