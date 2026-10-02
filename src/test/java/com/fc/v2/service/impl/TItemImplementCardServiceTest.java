package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.mapper.auto.TItemCardFlowMapper;
import com.fc.v2.mapper.auto.TItemDeptArchMapper;
import com.fc.v2.mapper.auto.TItemImplementCardMapper;
import com.fc.v2.model.auto.TItemCardFlow;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.custom.item.CardReceipt;
import com.fc.v2.model.custom.item.CardSubmitCmd;
import com.fc.v2.model.custom.item.ItemScope;
import com.fc.v2.model.custom.item.SamePassCount;
import com.fc.v2.service.support.ItemCardRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * 实施清单配置卡服务层测试（mapper 全 mock，只验服务层说了算的那几条）
 *
 * @author fuce
 * @date 2026-10-01
 */
public class TItemImplementCardServiceTest {

    private TItemImplementCardServiceImpl service;
    private TItemImplementCardMapper cardMapper;
    private TItemDeptArchMapper archMapper;
    private TItemCardFlowMapper flowMapper;

    @BeforeEach
    public void setUp() {
        service = new TItemImplementCardServiceImpl();
        cardMapper = mock(TItemImplementCardMapper.class);
        archMapper = mock(TItemDeptArchMapper.class);
        flowMapper = mock(TItemCardFlowMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", cardMapper);
        ReflectionTestUtils.setField(service, "itemDeptArchMapper", archMapper);
        ReflectionTestUtils.setField(service, "itemCardFlowMapper", flowMapper);
    }

    private TItemDeptArch openArch() {
        TItemDeptArch arch = new TItemDeptArch();
        arch.setId(0L);
        arch.setSiteNo("ZW00");
        arch.setSiteName("青屏市承办部门底册");
        arch.setTh1Max(new BigDecimal("300"));
        arch.setTh2Max(new BigDecimal("1500"));
        arch.setTh3Max(new BigDecimal("6000"));
        arch.setStatus(0);
        arch.setDelFlag(0);
        return arch;
    }

    private CardSubmitCmd validCmd() {
        CardSubmitCmd cmd = new CardSubmitCmd();
        cmd.setSiteNo("ZW00");
        cmd.setItemCode("XK-001");
        cmd.setUndertakeLevel(ItemCardRules.LEVEL_PROVINCE_STD);
        cmd.setUpperItemCode("UP-001");
        cmd.setBasisClause("第3条");
        cmd.setQty(new BigDecimal("100"));
        cmd.setFineAmt(new BigDecimal("7"));
        cmd.setOperator("county_clerk");
        return cmd;
    }

    private TItemImplementCard dbCard(long id, Integer step, Integer status) {
        TItemImplementCard card = new TItemImplementCard();
        card.setId(id);
        card.setBillNo("LK" + id);
        card.setSiteNo("ZW00");
        card.setItemCode("XK-001");
        card.setNodeNo(1);
        card.setQty(new BigDecimal("100"));
        card.setFineAmt(new BigDecimal("7"));
        card.setGradeLevel(1);
        card.setStepCount(step);
        card.setStatus(status);
        card.setRunNo(1);
        card.setVerFlag(1);
        card.setDelFlag(0);
        return card;
    }

    // ---------- 立卡 ----------

    @Test
    public void submit_success_systemIssuesCodesAndGrade() {
        when(archMapper.selectOne(any())).thenReturn(openArch());
        when(cardMapper.selectList(any())).thenReturn(new ArrayList<TItemImplementCard>());
        when(cardMapper.insert(any())).thenReturn(1);

        CardReceipt receipt = service.insertTItemImplementCard(validCmd());

        assertTrue(receipt.isSuccess(), "能立的卡要立得起来");
        ArgumentCaptor<TItemImplementCard> captor = ArgumentCaptor.forClass(TItemImplementCard.class);
        verify(cardMapper).insert(captor.capture());
        TItemImplementCard saved = captor.getValue();
        assertTrue(saved.getBillNo().startsWith("LK"), "清单卡号系统发");
        assertTrue(saved.getImplCode().startsWith("SS"), "实施编码系统发");
        assertEquals("ZW00", saved.getSiteNo());
        assertEquals(Integer.valueOf(1), saved.getNodeNo());
        assertEquals(Integer.valueOf(1), saved.getGradeLevel(), "100件够不着低界300，停在第一层");
        assertEquals(Integer.valueOf(0), saved.getStatus());
        assertEquals(Integer.valueOf(1), saved.getVerFlag());
        assertEquals("county_clerk", saved.getCreateBy(), "开卡人一钉终身");
    }

    @Test
    public void submit_missingField_namedInReceipt() {
        CardSubmitCmd cmd = validCmd();
        cmd.setQty(BigDecimal.ZERO);
        CardReceipt receipt = service.insertTItemImplementCard(cmd);
        assertFalse(receipt.isSuccess());
        assertTrue(receipt.getMissingFields().contains(ItemCardRules.F_QTY), "回执要指名空在哪一栏");
        verify(cardMapper, never()).insert(any());
    }

    @Test
    public void submit_futureStart_needsConfirmFirst() {
        CardSubmitCmd cmd = validCmd();
        cmd.setUndertakeLevel(ItemCardRules.LEVEL_CITY_IMPL);
        cmd.setCityItems("细化第1、2条");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 5);
        cmd.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 30);
        cmd.setEndDate(cal.getTime());

        when(archMapper.selectOne(any())).thenReturn(openArch());
        when(cardMapper.selectList(any())).thenReturn(new ArrayList<TItemImplementCard>());
        when(cardMapper.insert(any())).thenReturn(1);

        // 起头那一日写成往后的日子，得先问一句才收
        CardReceipt first = service.insertTItemImplementCard(cmd);
        assertFalse(first.isSuccess());
        assertTrue(first.isNeedConfirm(), "先要问一句");
        verify(cardMapper, never()).insert(any());

        // 问过确认后再递就收
        cmd.setConfirmFutureStart(true);
        CardReceipt second = service.insertTItemImplementCard(cmd);
        assertTrue(second.isSuccess());
    }

    @Test
    public void submit_archNotMatchedByCode_rejected() {
        // 事项归哪一册只凭代号去对；拿名号相近认门，不算对上
        when(archMapper.selectOne(any())).thenReturn(null);
        CardReceipt receipt = service.insertTItemImplementCard(validCmd());
        assertFalse(receipt.isSuccess());
        assertTrue(receipt.getMissingFields().contains(ItemCardRules.F_SITE_NO));
        verify(cardMapper, never()).insert(any());
    }

    @Test
    public void submit_closedArch_rejected() {
        // 年内撤并掉的那一册当场关口，往后不收新卡
        TItemDeptArch closed = openArch();
        closed.setStatus(1);
        when(archMapper.selectOne(any())).thenReturn(closed);
        CardReceipt receipt = service.insertTItemImplementCard(validCmd());
        assertFalse(receipt.isSuccess());
        verify(cardMapper, never()).insert(any());
    }

    @Test
    public void submit_inFlightSibling_blocksSecondCard() {
        // 头一张尚未冻住、也没就地叫停之前，第二张开不出来
        when(archMapper.selectOne(any())).thenReturn(openArch());
        TItemImplementCard inFlight = dbCard(9L, 0, 0);
        when(cardMapper.selectList(any())).thenReturn(Collections.singletonList(inFlight));

        CardReceipt receipt = service.insertTItemImplementCard(validCmd());
        assertFalse(receipt.isSuccess());
        verify(cardMapper, never()).insert(any());
    }

    @Test
    public void submit_frozenSibling_newVersionAndOldGoesHistory() {
        // 走到冻住名额才空出来：新版一立，旧版退出现行归历史
        when(archMapper.selectOne(any())).thenReturn(openArch());
        TItemImplementCard frozen = dbCard(9L, 2, 2);
        when(cardMapper.selectList(any())).thenReturn(Collections.singletonList(frozen));
        when(cardMapper.updateById(any())).thenReturn(1);
        when(cardMapper.insert(any())).thenReturn(1);

        CardReceipt receipt = service.insertTItemImplementCard(validCmd());
        assertTrue(receipt.isSuccess());

        ArgumentCaptor<TItemImplementCard> histCaptor = ArgumentCaptor.forClass(TItemImplementCard.class);
        verify(cardMapper).updateById(histCaptor.capture());
        assertEquals(Integer.valueOf(0), histCaptor.getValue().getVerFlag(), "旧版退出现行归历史");
        assertEquals(Long.valueOf(9L), histCaptor.getValue().getId());

        ArgumentCaptor<TItemImplementCard> insertCaptor = ArgumentCaptor.forClass(TItemImplementCard.class);
        verify(cardMapper).insert(insertCaptor.capture());
        assertEquals(Integer.valueOf(2), insertCaptor.getValue().getNodeNo(), "新版版次序往后排");
    }

    // ---------- 前进一步 / 退一档 / 叫停 ----------

    @Test
    public void advance_pendingToChecked_sealsChecksum() {
        TItemImplementCard card = dbCard(1L, 0, 0);
        when(cardMapper.selectOne(any())).thenReturn(card);
        when(cardMapper.selectCount(any())).thenReturn(0);
        when(cardMapper.update(isNull(), any())).thenReturn(1);
        when(flowMapper.insert(any())).thenReturn(1);

        CardReceipt receipt = service.advance(1L, "checker");
        assertTrue(receipt.isSuccess());
        assertEquals(Integer.valueOf(1), receipt.getStatus());

        ArgumentCaptor<Wrapper> uwCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(cardMapper).update(isNull(), uwCaptor.capture());
        String sqlSet = uwCaptor.getValue().getSqlSet();
        assertTrue(sqlSet.contains("step_count"), "步数写回");
        assertTrue(sqlSet.contains("content"), "走到已核对校验串钉死");

        ArgumentCaptor<TItemCardFlow> flowCaptor = ArgumentCaptor.forClass(TItemCardFlow.class);
        verify(flowMapper).insert(flowCaptor.capture());
        assertEquals(TItemCardFlow.ACTION_ADVANCE, flowCaptor.getValue().getAction());
        assertNotNull(flowCaptor.getValue().getChecksumSnap(), "这一趟落下的串留着");
    }

    @Test
    public void advance_checkedToFrozen_marksFreeze() {
        TItemImplementCard card = dbCard(1L, 1, 1);
        card.setContent("sealed-string");
        when(cardMapper.selectOne(any())).thenReturn(card);
        when(cardMapper.update(isNull(), any())).thenReturn(1);
        when(flowMapper.insert(any())).thenReturn(1);

        CardReceipt receipt = service.advance(1L, "checker");
        assertTrue(receipt.isSuccess());
        assertEquals(Integer.valueOf(2), receipt.getStatus());

        ArgumentCaptor<TItemCardFlow> flowCaptor = ArgumentCaptor.forClass(TItemCardFlow.class);
        verify(flowMapper).insert(flowCaptor.capture());
        assertEquals(TItemCardFlow.ACTION_FREEZE, flowCaptor.getValue().getAction());
    }

    @Test
    public void advance_frozenCard_rejected() {
        // 推进到冻住这张便到了头
        TItemImplementCard card = dbCard(1L, 2, 2);
        when(cardMapper.selectOne(any())).thenReturn(card);
        CardReceipt receipt = service.advance(1L, "checker");
        assertFalse(receipt.isSuccess());
        verify(cardMapper, never()).update(isNull(), any());
    }

    @Test
    public void advance_checksumSharedByTwoVersions_isError() {
        // 两版共用一串即算错
        TItemImplementCard card = dbCard(1L, 0, 0);
        when(cardMapper.selectOne(any())).thenReturn(card);
        when(cardMapper.selectCount(any())).thenReturn(1);
        CardReceipt receipt = service.advance(1L, "checker");
        assertFalse(receipt.isSuccess());
        verify(cardMapper, never()).update(isNull(), any());
    }

    @Test
    public void rollback_fromChecked_keepsOldRecordApart() {
        TItemImplementCard card = dbCard(1L, 1, 1);
        card.setContent("sealed-string");
        when(cardMapper.selectOne(any())).thenReturn(card);
        when(cardMapper.update(isNull(), any())).thenReturn(1);
        when(flowMapper.insert(any())).thenReturn(1);

        CardReceipt receipt = service.rollback(1L, "checker");
        assertTrue(receipt.isSuccess());

        ArgumentCaptor<TItemCardFlow> flowCaptor = ArgumentCaptor.forClass(TItemCardFlow.class);
        verify(flowMapper).insert(flowCaptor.capture());
        TItemCardFlow flow = flowCaptor.getValue();
        assertEquals(TItemCardFlow.ACTION_ROLLBACK, flow.getAction());
        assertEquals("sealed-string", flow.getChecksumSnap(), "上一趟落下的记录照样留着");

        ArgumentCaptor<Wrapper> uwCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(cardMapper).update(isNull(), uwCaptor.capture());
        String sqlSet = uwCaptor.getValue().getSqlSet();
        assertTrue(sqlSet.contains("run_no"), "退档重走算新一趟，不跟后补的排作一处");
    }

    @Test
    public void rollback_pendingOrFrozen_rejected() {
        when(cardMapper.selectOne(any())).thenReturn(dbCard(1L, 0, 0));
        assertFalse(service.rollback(1L, "checker").isSuccess(), "待核对没处退");
        when(cardMapper.selectOne(any())).thenReturn(dbCard(1L, 2, 2));
        assertFalse(service.rollback(1L, "checker").isSuccess(), "冻住不许退");
    }

    @Test
    public void halt_pendingCard_freesSlot_frozenCardRefused() {
        TItemImplementCard pending = dbCard(1L, 0, 0);
        when(cardMapper.selectOne(any())).thenReturn(pending);
        when(cardMapper.update(isNull(), any())).thenReturn(1);
        when(flowMapper.insert(any())).thenReturn(1);
        assertTrue(service.halt(1L, "checker").isSuccess(), "未冻住的能就地叫停");

        TItemImplementCard frozen = dbCard(2L, 2, 2);
        when(cardMapper.selectOne(any())).thenReturn(frozen);
        assertFalse(service.halt(2L, "checker").isSuccess(), "已冻住整张撤不掉");
    }

    // ---------- 修改护栏 ----------

    @Test
    public void update_checkedOrFrozenCard_rejected() {
        when(cardMapper.selectOne(any())).thenReturn(dbCard(1L, 1, 1));
        TItemImplementCard record = new TItemImplementCard();
        record.setId(1L);
        record.setQty(new BigDecimal("200"));
        assertEquals(0, service.updateTItemImplementCard(record), "已核想要改先退一档");

        when(cardMapper.selectOne(any())).thenReturn(dbCard(1L, 2, 2));
        assertEquals(0, service.updateTItemImplementCard(record), "冻住字也动不得");
    }

    @Test
    public void update_historyVersion_rejected() {
        // 不许覆写已经摆着的旧版次
        TItemImplementCard history = dbCard(1L, 0, 0);
        history.setVerFlag(0);
        when(cardMapper.selectOne(any())).thenReturn(history);
        TItemImplementCard record = new TItemImplementCard();
        record.setId(1L);
        record.setQty(new BigDecimal("200"));
        assertEquals(0, service.updateTItemImplementCard(record));
    }

    @Test
    public void update_qtyChange_recomputesGradeAndStripsSystemFields() {
        TItemImplementCard db = dbCard(1L, 0, 0);
        when(cardMapper.selectOne(any())).thenReturn(db);
        when(archMapper.selectOne(any())).thenReturn(openArch());
        when(cardMapper.update(any(), any())).thenReturn(1);

        TItemImplementCard record = new TItemImplementCard();
        record.setId(1L);
        record.setQty(new BigDecimal("2000"));
        record.setGradeLevel(4);          // 手填的层级不算数
        record.setContent("hand-made");   // 手填的串不算数
        record.setStatus(2);              // 屏上填的档次不算数
        record.setCreateBy("someone-else");

        assertEquals(1, service.updateTItemImplementCard(record));
        ArgumentCaptor<TItemImplementCard> captor = ArgumentCaptor.forClass(TItemImplementCard.class);
        verify(cardMapper).update(captor.capture(), any());
        TItemImplementCard written = captor.getValue();
        assertEquals(Integer.valueOf(3), written.getGradeLevel(),
                "办件量增减每一次折一回：2000越过中界够不着高界，落第三层");
        assertNull(written.getContent(), "校验串没有手填的把手");
        assertNull(written.getStatus(), "档次由系统按步数数出来");
        assertNull(written.getCreateBy(), "开卡人一钉终身，重录轮不到换人");
    }

    @Test
    public void update_zeroQtyOrNegativeFine_rejected() {
        when(cardMapper.selectOne(any())).thenReturn(dbCard(1L, 0, 0));
        TItemImplementCard zeroQty = new TItemImplementCard();
        zeroQty.setId(1L);
        zeroQty.setQty(BigDecimal.ZERO);
        assertEquals(0, service.updateTItemImplementCard(zeroQty));

        TItemImplementCard negativeFine = new TItemImplementCard();
        negativeFine.setId(1L);
        negativeFine.setFineAmt(new BigDecimal("-1"));
        assertEquals(0, service.updateTItemImplementCard(negativeFine));
    }

    // ---------- 删除（叫停语义） ----------

    @Test
    public void delete_frozenSkipped_pendingHalted() {
        when(cardMapper.selectOne(any()))
                .thenReturn(dbCard(1L, 2, 2))
                .thenReturn(dbCard(2L, 0, 0));
        when(cardMapper.update(isNull(), any())).thenReturn(1);
        when(flowMapper.insert(any())).thenReturn(1);
        int rows = service.deleteTItemImplementCardByIds("1,2");
        assertEquals(1, rows, "冻住的撤不掉，未冻住的叫停");
    }

    // ---------- 同屏两数 ----------

    @Test
    public void countSamePass_twoNumbersFromOneSnapshot() {
        TItemImplementCard a = dbCard(1L, 0, 0);
        a.setUndertakeLevel(1);
        TItemImplementCard b = dbCard(2L, 0, 0);
        b.setUndertakeLevel(2);
        TItemImplementCard c = dbCard(3L, 0, 0);
        c.setUndertakeLevel(2);
        c.setItemCode("XK-002");
        // a 与 b 同一事项码：跨年那一屏不许把同一事项计两遍
        when(cardMapper.selectList(any())).thenReturn(Arrays.asList(a, b, c));

        SamePassCount count = service.countSamePass(ItemScope.province(), null);
        assertEquals(3, count.getCardCount(), "册上占的数按卡数");
        assertEquals(2, count.getCatalogCount(), "名录数按事项码去重，同一回算出来");
        assertEquals(2, count.getLevelCounts().size(), "某一级底下空着就不摆空行");
        assertEquals(Long.valueOf(2L), count.getLevelCounts().get(2));
    }

    @Test
    public void currentList_entrustedScopeWithNoArch_showsNothing() {
        // 跨市协办：受托那家不在圈里，一张也不摆
        when(archMapper.selectList(any())).thenReturn(new ArrayList<TItemDeptArch>());
        List<TItemImplementCard> list = service.selectCurrentCardList(ItemScope.entrusted("ZW99"), null);
        assertTrue(list.isEmpty());
        verify(cardMapper, never()).selectList(any());
    }

    @Test
    public void currentList_provinceScope_queriesCurrentVersionsOnly() {
        when(cardMapper.selectList(any())).thenReturn(new ArrayList<TItemImplementCard>());
        service.selectCurrentCardList(ItemScope.province(), null);
        ArgumentCaptor<Wrapper> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(cardMapper).selectList(captor.capture());
        String where = captor.getValue().getSqlSegment();
        assertTrue(where.contains("ver_flag"), "日常摆出来的只许当下最新那一版");
        assertTrue(where.contains("del_flag"), "叫停的不摆");
    }
}
