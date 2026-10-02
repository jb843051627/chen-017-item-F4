package com.fc.v2.model.custom.item;

/**
 * 账号一进系统先划的范围（摆出来的卡不许越出这个圈）
 *
 * @author fuce
 * @date 2026-10-01
 */
public class ItemScope {

    /** 省办：看全辖 */
    public static final int LEVEL_PROVINCE = 1;
    /** 市局：看本市所辖那几个县 */
    public static final int LEVEL_CITY = 2;
    /** 县局：只看本县那一本册 */
    public static final int LEVEL_COUNTY = 3;
    /** 跨市协办：只把受托那一家摆给受托人 */
    public static final int LEVEL_ENTRUSTED = 4;

    /** 范围档次 */
    private Integer level;

    /** 本级区划名(与底册 road_name 的段对，如 青屏市 / 青屏县) */
    private String regionName;

    /** 受托那一家底册代号(仅跨市协办用) */
    private String siteNo;

    public ItemScope() {
    }

    public ItemScope(Integer level, String regionName, String siteNo) {
        this.level = level;
        this.regionName = regionName;
        this.siteNo = siteNo;
    }

    public static ItemScope province() {
        return new ItemScope(LEVEL_PROVINCE, null, null);
    }

    public static ItemScope city(String cityName) {
        return new ItemScope(LEVEL_CITY, cityName, null);
    }

    public static ItemScope county(String countyName) {
        return new ItemScope(LEVEL_COUNTY, countyName, null);
    }

    public static ItemScope entrusted(String siteNo) {
        return new ItemScope(LEVEL_ENTRUSTED, null, siteNo);
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }
}
