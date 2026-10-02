package com.fc.v2.service.itemcard;

import com.fc.v2.model.auto.TItemCardTrail;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;

import java.util.Date;
import java.util.List;

/**
 * 事项卡持久化网关：领域规则只认这一道口，
 * MyBatis 那一头与内存那一头各自实现，规则不另写第二遍。
 *
 * @author fuce
 * @date 2026-10-02
 */
public interface ItemCardGateway {

    /** 按主键取卡（含历史旧版、旧纸卡），没有返回 null */
    TItemImplementCard findById(Long id);

    /** 现行卡（current_flag=1，未删除），含已冻住与已叫停 */
    List<TItemImplementCard> findCurrent();

    /** 全部卡（含历史版次、旧纸卡） */
    List<TItemImplementCard> findAll();

    /** 落一张新卡 */
    void insert(TItemImplementCard card);

    /** 覆写一张已存在的卡 */
    void update(TItemImplementCard card);

    /** 追加一行过步留痕 */
    void appendTrail(TItemCardTrail trail);

    /** 这张卡到目前为止的留痕，按落记先后 */
    List<TItemCardTrail> findTrails(Long cardId);

    /** 凭底册代号对册（名号相近不算对上），对不上返回 null */
    TItemDeptArch findArchBySiteNo(String siteNo);

    /** 系统发：清单卡号 */
    String nextBillNo();

    /** 系统发：实施编码 */
    String nextImplCode();

    /** 此刻（折年、问往后日子用） */
    Date now();
}
