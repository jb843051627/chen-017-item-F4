package com.fc.v2.service.itemcard;

import java.util.Map;

/**
 * 年底本级那一屏：本卡在册上占的数与本级名录上的数同屏亮出，
 * 两处由服务层同一回算出来，不许各算各的对不到一处。
 *
 * @author fuce
 * @date 2026-10-02
 */
public class CensusView {

    private final String regionPath;
    private final String yearNo;
    /** 卡上占的数：现行在办卡行数 */
    private final long cardCount;
    /** 名录上的数：去重后的「底册代号＋事项码」对数（一项事项一张卡） */
    private final long catalogCount;
    /** 各服务监督层级下的卡数；某一级底下空着就不摆空行（键缺席即空） */
    private final Map<Integer, Long> countByGrade;

    public CensusView(String regionPath, String yearNo, long cardCount, long catalogCount,
                      Map<Integer, Long> countByGrade) {
        this.regionPath = regionPath;
        this.yearNo = yearNo;
        this.cardCount = cardCount;
        this.catalogCount = catalogCount;
        this.countByGrade = countByGrade;
    }

    public String getRegionPath() {
        return regionPath;
    }

    public String getYearNo() {
        return yearNo;
    }

    public long getCardCount() {
        return cardCount;
    }

    public long getCatalogCount() {
        return catalogCount;
    }

    public Map<Integer, Long> getCountByGrade() {
        return countByGrade;
    }
}
