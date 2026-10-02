package com.fc.v2.model.custom.item;

import com.fc.v2.model.auto.TItemImplementCard;

import java.util.List;
import java.util.Map;

/**
 * 同屏两数（本卡在册上占的数与本级名录上的数，同一回算出来）
 *
 * @author fuce
 * @date 2026-10-01
 */
public class SamePassCount {

    /** 本卡在册上占的数(现行版卡数) */
    private long cardCount;

    /** 本级名录上的数(同一回算出的去重事项数，跨年不双计) */
    private long catalogCount;

    /** 分承办层级计数(某一级底下空着就不摆空行) */
    private Map<Integer, Long> levelCounts;

    /** 这一回算到的那一份快照(两处数都从它出) */
    private List<TItemImplementCard> snapshot;

    public long getCardCount() {
        return cardCount;
    }

    public void setCardCount(long cardCount) {
        this.cardCount = cardCount;
    }

    public long getCatalogCount() {
        return catalogCount;
    }

    public void setCatalogCount(long catalogCount) {
        this.catalogCount = catalogCount;
    }

    public Map<Integer, Long> getLevelCounts() {
        return levelCounts;
    }

    public void setLevelCounts(Map<Integer, Long> levelCounts) {
        this.levelCounts = levelCounts;
    }

    public List<TItemImplementCard> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(List<TItemImplementCard> snapshot) {
        this.snapshot = snapshot;
    }
}
