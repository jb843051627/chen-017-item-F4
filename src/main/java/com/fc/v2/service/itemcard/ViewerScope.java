package com.fc.v2.service.itemcard;

/**
 * 账号能翻见的范围：账号一进系统先划范围，摆出来的卡由它限定。
 * <ul>
 *   <li>县局账号只看本县那一本册（regionPath 落到县，精确相等）；</li>
 *   <li>市局账号看本市所辖那几个县（regionPath 以"省—市"起头）；</li>
 *   <li>省办账号看全辖；</li>
 *   <li>跨市协办的，只把受托那一家摆给受托人（entrustSiteNo 精确相等）。</li>
 * </ul>
 * 自己那一级以外，别处的卡不许同屏并列任人挑。
 *
 * @author fuce
 * @date 2026-10-02
 */
public class ViewerScope {

    /** 省办 */
    public static final int GRADE_PROVINCE = 1;
    /** 市局 */
    public static final int GRADE_CITY = 2;
    /** 县局 */
    public static final int GRADE_COUNTY = 3;
    /** 跨市协办受托人 */
    public static final int GRADE_AGENT = 4;

    private final int grade;
    /** 本级区划串：省，或"省—市"，或"省—市—县" */
    private final String regionPath;
    /** 协办时受托那一家底册代号，其余级别为 null */
    private final String entrustSiteNo;

    private ViewerScope(int grade, String regionPath, String entrustSiteNo) {
        this.grade = grade;
        this.regionPath = regionPath;
        this.entrustSiteNo = entrustSiteNo;
    }

    public static ViewerScope province(String provincePath) {
        return new ViewerScope(GRADE_PROVINCE, provincePath, null);
    }

    public static ViewerScope city(String cityPath) {
        return new ViewerScope(GRADE_CITY, cityPath, null);
    }

    public static ViewerScope county(String countyPath) {
        return new ViewerScope(GRADE_COUNTY, countyPath, null);
    }

    public static ViewerScope agent(String entrustSiteNo) {
        return new ViewerScope(GRADE_AGENT, null, entrustSiteNo);
    }

    public int getGrade() {
        return grade;
    }

    public String getRegionPath() {
        return regionPath;
    }

    public String getEntrustSiteNo() {
        return entrustSiteNo;
    }
}
