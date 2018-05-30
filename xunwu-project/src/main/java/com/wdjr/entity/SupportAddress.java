package com.wdjr.entity;


import javax.persistence.*;

@Entity
@Table(name = "support_address")
public class SupportAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "belong_to")
    private String belongTo;
    @Column(name = "en_name")
    private String enName;
    @Column(name = "cn_name")
    private String cnName;

    private String level;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * 行政级别定义
     */
    public enum Level {
        CITY("city"),
        REGION("region");

        private String value;

        Level(String value) {
            this.value = value;
        }

        public String getValue(){
            return value;
        }
        private static Level of(String value){
            for (Level level : Level.values()) {
                if(level.value.equals(value)){
                    return level;
                }

            }
            throw new IllegalArgumentException();
        }
    }

}
