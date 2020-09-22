package cn.himavat.bpr.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Device {
    @Id
    private int id;
    private String imsi;
    private String description;
    /**
     * 当status=0时，认为该设备不存在断线，大于零时，说明该设备断线的数量。
     */
    private int status;
}