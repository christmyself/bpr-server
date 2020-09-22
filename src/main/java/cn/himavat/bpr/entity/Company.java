package cn.himavat.bpr.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
public class Company {
    @Id
    private int id;
    private String name;
    private String description;
    private Date joinedAt;
}
