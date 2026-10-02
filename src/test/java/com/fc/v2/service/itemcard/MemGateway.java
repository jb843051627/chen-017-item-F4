package com.fc.v2.service.itemcard;

import com.fc.v2.model.auto.TItemCardTrail;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存网关：规则只写一遍，落库一头与内存一头各实现这道口。
 * 时钟可拨，专测"起头日写成往后的日子"。
 */
public class MemGateway implements ItemCardGateway {

    private final Map<Long, TItemImplementCard> cards = new HashMap<Long, TItemImplementCard>();
    private final Map<String, TItemDeptArch> archs = new HashMap<String, TItemDeptArch>();
    private final List<TItemCardTrail> trails = new ArrayList<TItemCardTrail>();
    private final AtomicLong seq = new AtomicLong(1000);
    private long billSeq = 0;
    private Date clock = new Date(1770000000000L); // 2026 年某日

    public void putArch(TItemDeptArch arch) {
        archs.put(arch.getSiteNo(), arch);
    }

    /** 底册区划改隶：册上改路名，旧卡快照不跟着动 */
    public void archChangeRoad(String siteNo, String newRoad) {
        archs.get(siteNo).setRoadName(newRoad);
    }

    public void tickTo(Date d) {
        this.clock = d;
    }

    public List<TItemCardTrail> trailsOf(Long cardId) {
        List<TItemCardTrail> out = new ArrayList<TItemCardTrail>();
        for (TItemCardTrail t : trails) {
            if (cardId.equals(t.getCardId())) {
                out.add(t);
            }
        }
        return out;
    }

    @Override
    public TItemImplementCard findById(Long id) {
        return cards.get(id);
    }

    @Override
    public List<TItemImplementCard> findCurrent() {
        List<TItemImplementCard> out = new ArrayList<TItemImplementCard>();
        for (TItemImplementCard c : cards.values()) {
            if ((c.getDelFlag() == null || c.getDelFlag() == 0)
                    && c.getCurrentFlag() != null && c.getCurrentFlag() == 1) {
                out.add(c);
            }
        }
        return out;
    }

    @Override
    public List<TItemImplementCard> findAll() {
        return new ArrayList<TItemImplementCard>(cards.values());
    }

    @Override
    public void insert(TItemImplementCard card) {
        if (card.getId() == null) {
            card.setId(seq.incrementAndGet());
        }
        cards.put(card.getId(), card);
    }

    @Override
    public void update(TItemImplementCard card) {
        cards.put(card.getId(), card);
    }

    @Override
    public void appendTrail(TItemCardTrail trail) {
        if (trail.getId() == null) {
            trail.setId(seq.incrementAndGet());
        }
        trails.add(trail);
    }

    @Override
    public List<TItemCardTrail> findTrails(Long cardId) {
        return trailsOf(cardId);
    }

    @Override
    public TItemDeptArch findArchBySiteNo(String siteNo) {
        return archs.get(siteNo);
    }

    @Override
    public String nextBillNo() {
        return "CARD" + (++billSeq);
    }

    @Override
    public String nextImplCode() {
        return "IMP" + (++billSeq);
    }

    @Override
    public Date now() {
        return new Date(clock.getTime());
    }

    // —— 造数帮手 ——

    public static TItemDeptArch arch(long id, String no, String road, int status,
                                     String th1, String th2, String th3) {
        TItemDeptArch a = new TItemDeptArch();
        a.setId(id);
        a.setSiteNo(no);
        a.setSiteName(no + "部门");
        a.setSiteType("行政许可");
        a.setRoadName(road);
        a.setTh1Max(th1 == null ? null : new BigDecimal(th1));
        a.setTh2Max(th2 == null ? null : new BigDecimal(th2));
        a.setTh3Max(th3 == null ? null : new BigDecimal(th3));
        a.setStatus(status);
        a.setDelFlag(0);
        return a;
    }

    public static TItemImplementCard draft(String siteNo, String itemCode, Integer level, String qty, String days) {
        TItemImplementCard c = new TItemImplementCard();
        c.setSiteNo(siteNo);
        c.setItemCode(itemCode);
        c.setHandleLevel(level);
        c.setQty(new BigDecimal(qty));
        c.setFineAmt(new BigDecimal(days));
        return c;
    }
}
