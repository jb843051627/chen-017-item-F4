package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TItemCardFlow;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.custom.item.CardReceipt;
import com.fc.v2.model.custom.item.CardSubmitCmd;
import com.fc.v2.model.custom.item.ItemScope;
import com.fc.v2.model.custom.item.SamePassCount;

import java.util.List;

/**
 * 实施清单配置卡 Service接口。
 * 能不能立卡、这张当前算哪一步、还能不能往前走一步或退回一步，
 * 说了算的只有这里这几条方法，旁处寻不到第二条能绕过它的通道。
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITItemImplementCardService {

    /** 按主键查询 */
    TItemImplementCard selectTItemImplementCardById(Long id);

    /** 按条件查询列表（分页由调用方统一处理；只查不造，不覆写旧版次） */
    List<TItemImplementCard> selectTItemImplementCardList(Wrapper<TItemImplementCard> queryWrapper);

    /** 新增（旧保存方法，原样留着） */
    int insertTItemImplementCard(TItemImplementCard record);

    /** 修改（过服务层护栏：冻住不动、旧版次不覆写、系统栏无手填把手） */
    int updateTItemImplementCard(TItemImplementCard record);

    /** 批量删除（未冻住的就地叫停；已冻住的整张撤不掉） */
    int deleteTItemImplementCardByIds(String ids);

    /** 按主键删除（同上，叫停语义） */
    int deleteTItemImplementCardById(Long id);

    /**
     * 新递法立卡（与旧保存方法并排的同名重载）：系统发号、只凭代号对册、
     * 同一底册代号＋事项码只容一张在办、立卡当场折层级。
     *
     * @param cmd 立卡递法
     * @return 回执（缺哪一栏指名到哪一栏）
     */
    CardReceipt insertTItemImplementCard(CardSubmitCmd cmd);

    /**
     * 立卡预检：能不能给一个事项立起这样一张卡，先问不落库。
     */
    CardReceipt precheckSubmit(CardSubmitCmd cmd);

    /**
     * 哪几栏非写不可，按承办层级当场点出来。
     */
    List<String> requiredFields(Integer undertakeLevel);

    /**
     * 这张当前算哪一步（系统按已经走完的步数自己数出来）。
     */
    int currentStage(Long id);

    /**
     * 还能不能往前再走一步。
     */
    boolean canAdvance(Long id);

    /**
     * 还能不能退回一步。
     */
    boolean canRollback(Long id);

    /**
     * 往前走一步：待核对→已核对（钉校验串）→已冻住（到了头）。
     */
    CardReceipt advance(Long id, String operator);

    /**
     * 退一档重来：已核对→待核对；上一趟落下的记录照样留着，不跟后补的排作一处。
     */
    CardReceipt rollback(Long id, String operator);

    /**
     * 就地叫停：未冻住的卡叫停后名额空出来；已冻住的整张撤不掉。
     */
    CardReceipt halt(Long id, String operator);

    /**
     * 日常摆出来的列表：只许当下最新那一版，范围由账号权限划定的圈子定，
     * 自己那一级以外别处的卡不同屏并列。
     */
    List<TItemImplementCard> selectCurrentCardList(ItemScope scope, TItemImplementCard query);

    /**
     * 旧版翻检：同一底册代号＋事项码归到历史一层的各版次。
     */
    List<TItemImplementCard> selectHistoryVersions(String siteNo, String itemCode);

    /**
     * 同屏两数：本卡在册上占的数与本级名录上的数，同一回算出来。
     */
    SamePassCount countSamePass(ItemScope scope, String siteNo);

    /**
     * 这张卡的过口记录（各趟分开排）。
     */
    List<TItemCardFlow> selectFlowList(Long cardId);
}
