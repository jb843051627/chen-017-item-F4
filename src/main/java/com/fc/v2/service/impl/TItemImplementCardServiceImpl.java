package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TItemCardFlowMapper;
import com.fc.v2.mapper.auto.TItemImplementCardMapper;
import com.fc.v2.mapper.auto.TItemDeptArchMapper;
import com.fc.v2.model.auto.TItemCardFlow;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.custom.item.CardReceipt;
import com.fc.v2.model.custom.item.CardSubmitCmd;
import com.fc.v2.model.custom.item.ItemScope;
import com.fc.v2.model.custom.item.SamePassCount;
import com.fc.v2.service.ITItemImplementCardService;
import com.fc.v2.service.support.ItemCardRules;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 实施清单配置卡Service业务层处理
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TItemImplementCardServiceImpl extends ServiceImpl<TItemImplementCardMapper, TItemImplementCard> implements ITItemImplementCardService {

    @Autowired
    private TItemDeptArchMapper itemDeptArchMapper;

    @Autowired
    private TItemCardFlowMapper itemCardFlowMapper;

    @Override
    public TItemImplementCard selectTItemImplementCardById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TItemImplementCard>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TItemImplementCard> selectTItemImplementCardList(Wrapper<TItemImplementCard> queryWrapper) {
        // 列表那头只查不造卡，也不覆写已经摆着的旧版次
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public int insertTItemImplementCard(TItemImplementCard record) {
        if (record == null) {
            return 0;
        }

        record.setCreateBy(record.getBillNo());
        TItemDeptArch refArch = itemDeptArchMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                .eq("id", record.getSiteId()).eq("del_flag", 0));
        if (refArch == null) {
            return 0;
        }
        if (refArch.getStatus() != null && refArch.getStatus() == 1) {
            return 0;
        }
        record.setSiteNo(refArch.getSiteNo());
        if (StringUtils.isNotEmpty(record.getBillNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TItemImplementCard>()
                    .eq("bill_no", record.getBillNo()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }
        TItemDeptArch bandArch = itemDeptArchMapper.selectById(record.getSiteId());
        BigDecimal bandVal = record.getQty();
        int bandLevel = 0;
        if (bandVal != null && bandArch != null) {
            if (bandVal.compareTo(bandArch.getTh1Max()) <= 0) {
                bandLevel = 1;
            } else if (bandVal.compareTo(bandArch.getTh2Max()) <= 0) {
                bandLevel = 2;
            } else if (bandVal.compareTo(bandArch.getTh3Max()) <= 0) {
                bandLevel = 3;
            } else {
                bandLevel = 4;
            }
        }
        record.setGradeLevel(bandLevel);

        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CardReceipt insertTItemImplementCard(CardSubmitCmd cmd) {
        // 新递法：先问能不能立，回执指名空栏
        CardReceipt pre = precheckSubmit(cmd);
        if (!pre.isSuccess()) {
            return pre;
        }
        // 事项归哪一册只凭代号去对；拿名号相近认门，不算对上
        TItemDeptArch arch = itemDeptArchMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                .eq("site_no", cmd.getSiteNo()).eq("del_flag", 0));

        List<TItemImplementCard> siblings = selectSiblings(cmd.getSiteNo(), cmd.getItemCode());
        int nextNode = 1;
        for (TItemImplementCard sib : siblings) {
            // 旧卡版次序空着不抛错，就当还没排过序
            if (sib.getNodeNo() != null && sib.getNodeNo() >= nextNode) {
                nextNode = sib.getNodeNo() + 1;
            }
        }
        // 新版一立，旧版就从现行那份清单一并退出、另归历史一层供事后翻检（旧版次不抹）
        for (TItemImplementCard sib : siblings) {
            if (sib.getVerFlag() == null || sib.getVerFlag() == ItemCardRules.VER_CURRENT) {
                TItemImplementCard hist = new TItemImplementCard();
                hist.setId(sib.getId());
                hist.setVerFlag(ItemCardRules.VER_HISTORY);
                this.baseMapper.updateById(hist);
            }
        }

        TItemImplementCard card = new TItemImplementCard();
        // 清单卡号、实施编码由系统发，谁往里塞自家拟的号，库里不存
        card.setBillNo("LK" + IdWorker.getId());
        card.setImplCode("SS" + IdWorker.getId());
        card.setNodeNo(nextNode);
        card.setSiteId(arch.getId().intValue());
        card.setSiteNo(arch.getSiteNo());
        card.setItemCode(cmd.getItemCode());
        card.setUndertakeLevel(cmd.getUndertakeLevel());
        card.setUpperItemCode(cmd.getUpperItemCode());
        card.setBasisClause(cmd.getBasisClause());
        card.setCityItems(cmd.getCityItems());
        card.setStartDate(cmd.getStartDate());
        card.setEndDate(cmd.getEndDate());
        card.setWindows(cmd.getWindows());
        card.setRecvPoints(cmd.getRecvPoints());
        card.setQty(cmd.getQty());
        card.setFineAmt(cmd.getFineAmt());
        // 立卡那一次折一回，折完直接写回卡上那一格，那一格不留手填的把手
        card.setGradeLevel(ItemCardRules.computeGradeLevel(cmd.getQty(),
                arch.getTh1Max(), arch.getTh2Max(), arch.getTh3Max()));
        card.setStepCount(ItemCardRules.STEP_PENDING);
        card.setStatus(ItemCardRules.statusOfStep(ItemCardRules.STEP_PENDING));
        card.setRunNo(1);
        card.setVerFlag(ItemCardRules.VER_CURRENT);
        card.setDelFlag(0);
        // 开卡人一钉下便随这张卡过完一生
        card.setCreateBy(cmd.getOperator());
        card.setRemark(cmd.getRemark());
        this.baseMapper.insert(card);

        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(true);
        receipt.setMessage("卡已立起");
        receipt.setCardId(card.getId());
        receipt.setBillNo(card.getBillNo());
        receipt.setImplCode(card.getImplCode());
        receipt.setGradeLevel(card.getGradeLevel());
        receipt.setStatus(card.getStatus());
        receipt.setNodeNo(card.getNodeNo());
        return receipt;
    }

    @Override
    public CardReceipt precheckSubmit(CardSubmitCmd cmd) {
        // 哪几栏非写不可按承办层级当场点出来，缺哪栏指名哪栏
        List<String> missing = ItemCardRules.missingFields(cmd);
        if (!missing.isEmpty()) {
            return CardReceipt.reject(missing, "空着或不像话的栏指名如上，卡不进库");
        }
        // 起头那一日写成往后的日子，得先问一句才收
        if (ItemCardRules.isFutureStart(cmd.getStartDate(), new Date())
                && !Boolean.TRUE.equals(cmd.getConfirmFutureStart())) {
            return CardReceipt.askConfirm("起头之日写成往后的日子，确认无误请带确认再递一回");
        }
        TItemDeptArch arch = itemDeptArchMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                .eq("site_no", cmd.getSiteNo()).eq("del_flag", 0));
        if (arch == null) {
            return CardReceipt.reject(Collections.singletonList(ItemCardRules.F_SITE_NO),
                    "底册代号对不上，卡挂不进册");
        }
        // 年内撤并掉的那一册当场关口，往后不收新卡
        if (arch.getStatus() != null && arch.getStatus() == 1) {
            return CardReceipt.reject(Collections.singletonList(ItemCardRules.F_SITE_NO),
                    "该册年内已撤并当场关口，不收新卡");
        }
        // 同一部门名下同一事项，同一时刻只容一张在办的
        for (TItemImplementCard sib : selectSiblings(cmd.getSiteNo(), cmd.getItemCode())) {
            if (ItemCardRules.isInFlight(sib)) {
                return CardReceipt.reject(Collections.<String>emptyList(),
                        "同一部门名下同一事项已有一张在办(卡号" + sib.getBillNo() + ")，"
                                + "头一张尚未冻住、也没就地叫停，第二张开不出来");
            }
        }
        CardReceipt ok = new CardReceipt();
        ok.setSuccess(true);
        ok.setMessage("能立");
        return ok;
    }

    @Override
    public List<String> requiredFields(Integer undertakeLevel) {
        return ItemCardRules.requiredFields(undertakeLevel);
    }

    @Override
    public int currentStage(Long id) {
        return ItemCardRules.deriveStep(selectTItemImplementCardById(id));
    }

    @Override
    public boolean canAdvance(Long id) {
        TItemImplementCard card = selectTItemImplementCardById(id);
        return card != null && ItemCardRules.canAdvance(card);
    }

    @Override
    public boolean canRollback(Long id) {
        TItemImplementCard card = selectTItemImplementCardById(id);
        return card != null && ItemCardRules.canRollback(card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CardReceipt advance(Long id, String operator) {
        TItemImplementCard card = selectTItemImplementCardById(id);
        if (card == null) {
            return CardReceipt.reject(Collections.<String>emptyList(), "卡不在或已叫停");
        }
        // 一步没走完，后头不给走；到冻住这张便到了头
        if (!ItemCardRules.canAdvance(card)) {
            return CardReceipt.reject(Collections.<String>emptyList(),
                    "这张已冻住到了头，添不进去、字也动不得、整张撤不掉，要改只能另起新的一张");
        }
        int from = ItemCardRules.deriveStep(card);
        int to = from + 1;
        Integer runNo = card.getRunNo() == null ? 1 : card.getRunNo();
        String checksum = card.getContent();
        if (to == ItemCardRules.STEP_CHECKED) {
            // 走到已核对，校验串才算钉死
            checksum = ItemCardRules.buildChecksum(card.getBillNo(), card.getSiteNo(), card.getItemCode(),
                    card.getNodeNo(), card.getQty(), card.getFineAmt(), card.getGradeLevel(), runNo);
            // 两版共用一串即算错
            Integer dup = this.baseMapper.selectCount(new QueryWrapper<TItemImplementCard>()
                    .eq("site_no", card.getSiteNo())
                    .eq("item_code", card.getItemCode())
                    .eq("content", checksum)
                    .ne("id", card.getId())
                    .eq("del_flag", 0));
            if (dup != null && dup > 0) {
                return CardReceipt.reject(Collections.<String>emptyList(),
                        "两版共用一串即算错：该串已被同事项另一版次占用");
            }
        }
        UpdateWrapper<TItemImplementCard> uw = new UpdateWrapper<TItemImplementCard>();
        uw.eq("id", card.getId())
                .set("step_count", to)
                .set("status", ItemCardRules.statusOfStep(to))
                .set("update_by", operator);
        if (to == ItemCardRules.STEP_CHECKED) {
            uw.set("content", checksum);
        }
        this.baseMapper.update(null, uw);
        recordFlow(card, runNo,
                to == ItemCardRules.STEP_FROZEN ? TItemCardFlow.ACTION_FREEZE : TItemCardFlow.ACTION_ADVANCE,
                from, to, checksum, operator);

        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(true);
        receipt.setMessage(to == ItemCardRules.STEP_FROZEN ? "已冻住，这张到了头" : "已核对，校验串钉死");
        receipt.setCardId(card.getId());
        receipt.setBillNo(card.getBillNo());
        receipt.setStatus(ItemCardRules.statusOfStep(to));
        return receipt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CardReceipt rollback(Long id, String operator) {
        TItemImplementCard card = selectTItemImplementCardById(id);
        if (card == null) {
            return CardReceipt.reject(Collections.<String>emptyList(), "卡不在或已叫停");
        }
        // 卡未到冻住，前一步的核对能退一档重来；待核对没处退，冻住不许退
        if (!ItemCardRules.canRollback(card)) {
            return CardReceipt.reject(Collections.<String>emptyList(),
                    "只有已核对那一步能退一档，待核对没处退、已冻住不许退");
        }
        int from = ItemCardRules.deriveStep(card);
        Integer runNo = card.getRunNo() == null ? 1 : card.getRunNo();
        // 上一趟落下的记录照样留着，不跟后补的排作一处
        recordFlow(card, runNo, TItemCardFlow.ACTION_ROLLBACK, from,
                ItemCardRules.STEP_PENDING, card.getContent(), operator);
        UpdateWrapper<TItemImplementCard> uw = new UpdateWrapper<TItemImplementCard>();
        uw.eq("id", card.getId())
                .set("step_count", ItemCardRules.STEP_PENDING)
                .set("status", ItemCardRules.statusOfStep(ItemCardRules.STEP_PENDING))
                // 退回未钉：上一趟的串交还过口记录留存，重走那一趟另钉新串
                .set("content", null)
                .set("run_no", runNo + 1)
                .set("update_by", operator);
        this.baseMapper.update(null, uw);

        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(true);
        receipt.setMessage("已退一档回待核对，上一趟记录留存在过口里");
        receipt.setCardId(card.getId());
        receipt.setBillNo(card.getBillNo());
        receipt.setStatus(ItemCardRules.STEP_PENDING);
        return receipt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CardReceipt halt(Long id, String operator) {
        TItemImplementCard card = selectTItemImplementCardById(id);
        if (card == null) {
            return CardReceipt.reject(Collections.<String>emptyList(), "卡不在或已叫停");
        }
        // 推进到冻住这张便到了头：整张撤不掉
        if (ItemCardRules.isFrozen(card)) {
            return CardReceipt.reject(Collections.<String>emptyList(),
                    "这张已冻住，整张撤不掉，要改只能另起新的一张");
        }
        int step = ItemCardRules.deriveStep(card);
        Integer runNo = card.getRunNo() == null ? 1 : card.getRunNo();
        recordFlow(card, runNo, TItemCardFlow.ACTION_HALT, step, step, card.getContent(), operator);
        UpdateWrapper<TItemImplementCard> uw = new UpdateWrapper<TItemImplementCard>();
        uw.eq("id", card.getId()).set("del_flag", 1).set("update_by", operator);
        this.baseMapper.update(null, uw);

        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(true);
        receipt.setMessage("已就地叫停，这张占过的名额空出来了");
        receipt.setCardId(card.getId());
        receipt.setBillNo(card.getBillNo());
        return receipt;
    }

    @Override
    public List<TItemImplementCard> selectCurrentCardList(ItemScope scope, TItemImplementCard query) {
        QueryWrapper<TItemImplementCard> qw = new QueryWrapper<TItemImplementCard>();
        qw.eq("del_flag", 0);
        // 日常摆出来的只许当下最新那一版；早年旧卡版次落点空着的也算现行
        qw.and(w -> w.eq("ver_flag", ItemCardRules.VER_CURRENT).or().isNull("ver_flag"));
        if (query != null) {
            if (StringUtils.isNotEmpty(query.getSiteNo())) {
                qw.eq("site_no", query.getSiteNo());
            }
            if (StringUtils.isNotEmpty(query.getItemCode())) {
                qw.eq("item_code", query.getItemCode());
            }
            if (query.getUndertakeLevel() != null) {
                qw.eq("undertake_level", query.getUndertakeLevel());
            }
            if (query.getStatus() != null) {
                qw.eq("status", query.getStatus());
            }
        }
        // 摆出来的范围由账号权限定，自己那一级以外别处的卡不许同屏并列
        List<String> scopeSiteNos = scopeSiteNos(scope);
        if (scopeSiteNos != null) {
            if (scopeSiteNos.isEmpty()) {
                return new ArrayList<TItemImplementCard>();
            }
            qw.in("site_no", scopeSiteNos);
        }
        // 区划改隶之前立下的旧卡不跟着搬，仍按卡上底册代号原样摆
        qw.orderByDesc("id");
        return this.baseMapper.selectList(qw);
    }

    @Override
    public List<TItemImplementCard> selectHistoryVersions(String siteNo, String itemCode) {
        return this.baseMapper.selectList(new QueryWrapper<TItemImplementCard>()
                .eq("site_no", siteNo)
                .eq("item_code", itemCode)
                .eq("ver_flag", ItemCardRules.VER_HISTORY)
                .eq("del_flag", 0)
                .orderByAsc("node_no"));
    }

    @Override
    public SamePassCount countSamePass(ItemScope scope, String siteNo) {
        // 同一回算：先取同一份快照，两处的数都从这一份里出，不许各算各的
        TItemImplementCard query = new TItemImplementCard();
        query.setSiteNo(siteNo);
        List<TItemImplementCard> snapshot = selectCurrentCardList(scope, query);

        SamePassCount out = new SamePassCount();
        out.setSnapshot(snapshot);
        out.setCardCount(snapshot.size());
        Set<String> itemCodes = new HashSet<String>();
        Map<Integer, Long> levelCounts = new LinkedHashMap<Integer, Long>();
        for (TItemImplementCard card : snapshot) {
            // 跨年那一屏不许把同一事项计两遍：名录数按事项码去重
            if (StringUtils.isNotEmpty(card.getItemCode())) {
                itemCodes.add(card.getItemCode());
            }
            // 某一级底下空着，就不摆空行：只记真有卡的层级
            if (card.getUndertakeLevel() != null) {
                Long cnt = levelCounts.get(card.getUndertakeLevel());
                levelCounts.put(card.getUndertakeLevel(), cnt == null ? 1L : cnt + 1);
            }
        }
        out.setCatalogCount(itemCodes.size());
        out.setLevelCounts(levelCounts);
        return out;
    }

    @Override
    public List<TItemCardFlow> selectFlowList(Long cardId) {
        return itemCardFlowMapper.selectList(new QueryWrapper<TItemCardFlow>()
                .eq("card_id", cardId)
                .orderByAsc("id"));
    }

    @Override
    public int updateTItemImplementCard(TItemImplementCard record) {
        if (record == null || record.getId() == null) {
            return 0;
        }
        TItemImplementCard db = selectTItemImplementCardById(record.getId());
        if (db == null) {
            return 0;
        }
        // 已核想要改先退一档重来；已冻住字也动不得
        if (ItemCardRules.deriveStep(db) >= ItemCardRules.STEP_CHECKED) {
            return 0;
        }
        // 不许覆写已经摆着的旧版次
        if (db.getVerFlag() != null && db.getVerFlag() == ItemCardRules.VER_HISTORY) {
            return 0;
        }
        // 系统栏从来不留手填的把手：号、层级、校验串、档次、步数、版次落点、开卡人
        record.setBillNo(null);
        record.setImplCode(null);
        record.setSiteId(null);
        record.setGradeLevel(null);
        record.setContent(null);
        record.setStatus(null);
        record.setStepCount(null);
        record.setRunNo(null);
        record.setVerFlag(null);
        record.setCreateBy(null);
        record.setCreateTime(null);
        record.setDelFlag(null);
        // 空串不等于没填：空串不许盖掉库里的代号与事项码
        if (StringUtils.isEmpty(record.getSiteNo())) {
            record.setSiteNo(null);
        }
        if (StringUtils.isEmpty(record.getItemCode())) {
            record.setItemCode(null);
        }

        // 年办件量填零、填负数不进；承诺时限填成负数同样不进
        if (record.getQty() != null && record.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        if (record.getFineAmt() != null && record.getFineAmt().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }

        // 换承办层级：新层级那几栏凭据得补齐（以改后拼出的一张为准）
        if (record.getUndertakeLevel() != null && !record.getUndertakeLevel().equals(db.getUndertakeLevel())) {
            CardSubmitCmd merged = new CardSubmitCmd();
            merged.setSiteNo(db.getSiteNo());
            merged.setItemCode(db.getItemCode());
            merged.setUndertakeLevel(record.getUndertakeLevel());
            merged.setUpperItemCode(record.getUpperItemCode() != null ? record.getUpperItemCode() : db.getUpperItemCode());
            merged.setBasisClause(record.getBasisClause() != null ? record.getBasisClause() : db.getBasisClause());
            merged.setCityItems(record.getCityItems() != null ? record.getCityItems() : db.getCityItems());
            merged.setStartDate(record.getStartDate() != null ? record.getStartDate() : db.getStartDate());
            merged.setEndDate(record.getEndDate() != null ? record.getEndDate() : db.getEndDate());
            merged.setWindows(record.getWindows() != null ? record.getWindows() : db.getWindows());
            merged.setRecvPoints(record.getRecvPoints() != null ? record.getRecvPoints() : db.getRecvPoints());
            merged.setQty(record.getQty() != null ? record.getQty() : db.getQty());
            merged.setFineAmt(record.getFineAmt() != null ? record.getFineAmt() : db.getFineAmt());
            if (!ItemCardRules.missingFields(merged).isEmpty()) {
                return 0;
            }
        }

        // 改挂靠那一册：只凭代号去对，撤并册不收
        TItemDeptArch newArch = null;
        if (StringUtils.isNotEmpty(record.getSiteNo()) && !record.getSiteNo().equals(db.getSiteNo())) {
            newArch = itemDeptArchMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                    .eq("site_no", record.getSiteNo()).eq("del_flag", 0));
            if (newArch == null) {
                return 0;
            }
            if (newArch.getStatus() != null && newArch.getStatus() == 1) {
                return 0;
            }
            record.setSiteId(newArch.getId().intValue());
        }
        // 换了册或换了事项码，都不能撞上另一张在办的卡
        boolean pairChanged = newArch != null
                || (StringUtils.isNotEmpty(record.getItemCode()) && !record.getItemCode().equals(db.getItemCode()));
        if (pairChanged) {
            String pairSiteNo = newArch != null ? newArch.getSiteNo() : db.getSiteNo();
            String pairItemCode = StringUtils.isNotEmpty(record.getItemCode()) ? record.getItemCode() : db.getItemCode();
            List<TItemImplementCard> colliders = this.baseMapper.selectList(new QueryWrapper<TItemImplementCard>()
                    .eq("site_no", pairSiteNo)
                    .eq("item_code", pairItemCode)
                    .ne("id", db.getId())
                    .eq("del_flag", 0));
            for (TItemImplementCard collider : colliders) {
                if (ItemCardRules.isInFlight(collider)) {
                    return 0;
                }
            }
        }

        // 折线什么时候折：办件量有增减的每一次一回，改了挂靠那一册的每一次一回
        boolean qtyChanged = record.getQty() != null
                && (db.getQty() == null || record.getQty().compareTo(db.getQty()) != 0);
        if (qtyChanged || newArch != null) {
            TItemDeptArch bandArch = newArch != null ? newArch
                    : itemDeptArchMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                            .eq("site_no", db.getSiteNo()).eq("del_flag", 0));
            BigDecimal bandQty = record.getQty() != null ? record.getQty() : db.getQty();
            if (bandArch != null) {
                // 折完直接写回卡上那一格
                record.setGradeLevel(ItemCardRules.computeGradeLevel(bandQty,
                        bandArch.getTh1Max(), bandArch.getTh2Max(), bandArch.getTh3Max()));
            }
        }

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TItemImplementCard>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTItemImplementCardByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        return haltCards(Arrays.asList(idArr), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTItemImplementCardById(Long id) {
        return haltCards(Collections.singletonList(id), null);
    }

    /**
     * 就地叫停（逻辑关口）：未冻住的叫停后名额空出来，已冻住的整张撤不掉。
     */
    private int haltCards(List<Long> ids, String operator) {
        int rows = 0;
        for (Long id : ids) {
            TItemImplementCard card = selectTItemImplementCardById(id);
            if (card == null || ItemCardRules.isFrozen(card)) {
                continue;
            }
            int step = ItemCardRules.deriveStep(card);
            Integer runNo = card.getRunNo() == null ? 1 : card.getRunNo();
            recordFlow(card, runNo, TItemCardFlow.ACTION_HALT, step, step, card.getContent(), operator);
            UpdateWrapper<TItemImplementCard> uw = new UpdateWrapper<TItemImplementCard>();
            uw.eq("id", card.getId()).set("del_flag", 1);
            if (operator != null) {
                uw.set("update_by", operator);
            }
            rows += this.baseMapper.update(null, uw);
        }
        return rows;
    }

    /**
     * 同一底册代号＋事项码配平认卡，不问先来后到。
     */
    private List<TItemImplementCard> selectSiblings(String siteNo, String itemCode) {
        return this.baseMapper.selectList(new QueryWrapper<TItemImplementCard>()
                .eq("site_no", siteNo)
                .eq("item_code", itemCode)
                .eq("del_flag", 0));
    }

    /**
     * 账号权限划定的圈子：县局本县、市局本市所辖县、省办全辖、跨市协办只看受托那一家。
     * 返回 null 表示全辖不加圈；返回空清单表示圈里一本册也没有。
     */
    private List<String> scopeSiteNos(ItemScope scope) {
        if (scope == null || scope.getLevel() == null || scope.getLevel() == ItemScope.LEVEL_PROVINCE) {
            return null;
        }
        QueryWrapper<TItemDeptArch> aq = new QueryWrapper<TItemDeptArch>();
        aq.eq("del_flag", 0);
        if (scope.getLevel() == ItemScope.LEVEL_ENTRUSTED) {
            // 跨市协办：只把受托那一家摆给受托人
            if (StringUtils.isEmpty(scope.getSiteNo())) {
                return new ArrayList<String>();
            }
            aq.eq("site_no", scope.getSiteNo());
        } else if (scope.getLevel() == ItemScope.LEVEL_CITY) {
            // 市局：看本市所辖那几个县（区划省—市—县，中市段是本市的都进圈）
            if (StringUtils.isEmpty(scope.getRegionName())) {
                return new ArrayList<String>();
            }
            aq.like("road_name", "—" + scope.getRegionName() + "—");
        } else if (scope.getLevel() == ItemScope.LEVEL_COUNTY) {
            // 县局：只看本县那一本册（落在本县段收尾的）
            if (StringUtils.isEmpty(scope.getRegionName())) {
                return new ArrayList<String>();
            }
            aq.likeLeft("road_name", "—" + scope.getRegionName());
        } else {
            return new ArrayList<String>();
        }
        List<TItemDeptArch> archs = itemDeptArchMapper.selectList(aq);
        List<String> siteNos = new ArrayList<String>();
        for (TItemDeptArch arch : archs) {
            siteNos.add(arch.getSiteNo());
        }
        return siteNos;
    }

    /**
     * 落一笔过口记录：各趟分开排，上一趟落下的照样留着。
     */
    private void recordFlow(TItemImplementCard card, Integer runNo, String action, int from, int to,
                            String checksumSnap, String operator) {
        TItemCardFlow flow = new TItemCardFlow();
        flow.setCardId(card.getId());
        flow.setBillNo(card.getBillNo());
        flow.setRunNo(runNo);
        flow.setAction(action);
        flow.setFromStep(from);
        flow.setToStep(to);
        flow.setChecksumSnap(checksumSnap);
        flow.setOperator(operator);
        flow.setCreateTime(new Date());
        itemCardFlowMapper.insert(flow);
    }
}
