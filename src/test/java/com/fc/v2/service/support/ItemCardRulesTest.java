package com.fc.v2.service.support;

import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.custom.item.CardSubmitCmd;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 实施清单配置卡纯规则测试
 *
 * @author fuce
 * @date 2026-10-01
 */
public class ItemCardRulesTest {

    private static final BigDecimal TH1 = new BigDecimal("300");
    private static final BigDecimal TH2 = new BigDecimal("1500");
    private static final BigDecimal TH3 = new BigDecimal("6000");

    // ---------- 服务监督层级折算 ----------

    @Test
    public void grade_belowLowBound_stopsAtFirstLayer() {
        // 头一个没够着的界就是它该停的那一层
        assertEquals(Integer.valueOf(1), ItemCardRules.computeGradeLevel(new BigDecimal("299"), TH1, TH2, TH3));
        assertEquals(Integer.valueOf(1), ItemCardRules.computeGradeLevel(new BigDecimal("0.01"), TH1, TH2, TH3));
    }

    @Test
    public void grade_reachesLowBound_goesUpOneLayer() {
        assertEquals(Integer.valueOf(2), ItemCardRules.computeGradeLevel(new BigDecimal("300"), TH1, TH2, TH3));
        assertEquals(Integer.valueOf(2), ItemCardRules.computeGradeLevel(new BigDecimal("1499"), TH1, TH2, TH3));
    }

    @Test
    public void grade_exactlyMidBound_takesOneLayerUp() {
        // 恰好同中界持平的那一张往上取一层
        assertEquals(Integer.valueOf(3), ItemCardRules.computeGradeLevel(new BigDecimal("1500"), TH1, TH2, TH3));
        assertEquals(Integer.valueOf(3), ItemCardRules.computeGradeLevel(new BigDecimal("1501"), TH1, TH2, TH3));
        assertEquals(Integer.valueOf(3), ItemCardRules.computeGradeLevel(new BigDecimal("5999"), TH1, TH2, TH3));
    }

    @Test
    public void grade_reachesHighBound_landsOnTopLayer() {
        // 越过中界又够到高界的，落到最高那一层
        assertEquals(Integer.valueOf(4), ItemCardRules.computeGradeLevel(new BigDecimal("6000"), TH1, TH2, TH3));
        assertEquals(Integer.valueOf(4), ItemCardRules.computeGradeLevel(new BigDecimal("6001"), TH1, TH2, TH3));
        assertEquals(Integer.valueOf(4), ItemCardRules.computeGradeLevel(new BigDecimal("99999"), TH1, TH2, TH3));
    }

    @Test
    public void grade_boundsUpsideDown_sortedBeforeUse() {
        // 三条折线自低向高排，册上压歪了也先摆正再靠
        assertEquals(Integer.valueOf(3), ItemCardRules.computeGradeLevel(new BigDecimal("1500"), TH3, TH1, TH2));
    }

    @Test
    public void grade_nullQty_returnsNullNoThrow() {
        // 旧卡办件量空着：折不出，不抛错
        assertNull(ItemCardRules.computeGradeLevel(null, TH1, TH2, TH3));
        assertNull(ItemCardRules.computeGradeLevel(new BigDecimal("100"), null, null, null));
    }

    // ---------- 立卡校验指名空栏 ----------

    private CardSubmitCmd validProvinceCmd() {
        CardSubmitCmd cmd = new CardSubmitCmd();
        cmd.setSiteNo("ZW00");
        cmd.setItemCode("XK-001");
        cmd.setUndertakeLevel(ItemCardRules.LEVEL_PROVINCE_STD);
        cmd.setUpperItemCode("UP-001");
        cmd.setBasisClause("第3条");
        cmd.setQty(new BigDecimal("100"));
        cmd.setFineAmt(new BigDecimal("7"));
        return cmd;
    }

    @Test
    public void missing_levelBlank_named() {
        CardSubmitCmd cmd = validProvinceCmd();
        cmd.setUndertakeLevel(null);
        List<String> missing = ItemCardRules.missingFields(cmd);
        assertTrue(missing.contains(ItemCardRules.F_UNDERTAKE_LEVEL), "层级空着没选要指名");
    }

    @Test
    public void missing_provinceStdWithoutUpperCode_named() {
        // 选了省定却空着上位代号，卡不进库
        CardSubmitCmd cmd = validProvinceCmd();
        cmd.setUpperItemCode(null);
        List<String> missing = ItemCardRules.missingFields(cmd);
        assertTrue(missing.contains(ItemCardRules.F_UPPER_ITEM_CODE));
        assertFalse(missing.contains(ItemCardRules.F_BASIS_CLAUSE));
    }

    @Test
    public void missing_cityImplNeedsItemsAndTwoDates() {
        CardSubmitCmd cmd = validProvinceCmd();
        cmd.setUndertakeLevel(ItemCardRules.LEVEL_CITY_IMPL);
        List<String> missing = ItemCardRules.missingFields(cmd);
        assertTrue(missing.contains(ItemCardRules.F_CITY_ITEMS));
        assertTrue(missing.contains(ItemCardRules.F_START_DATE));
        assertTrue(missing.contains(ItemCardRules.F_END_DATE));
    }

    @Test
    public void missing_countyExtNeedsWindowsAndPoints() {
        CardSubmitCmd cmd = validProvinceCmd();
        cmd.setUndertakeLevel(ItemCardRules.LEVEL_COUNTY_EXT);
        List<String> missing = ItemCardRules.missingFields(cmd);
        assertTrue(missing.contains(ItemCardRules.F_WINDOWS));
        assertTrue(missing.contains(ItemCardRules.F_RECV_POINTS));
    }

    @Test
    public void missing_zeroOrNegativeQty_named() {
        // 年办件量填零、填负数，卡不进库
        CardSubmitCmd zero = validProvinceCmd();
        zero.setQty(BigDecimal.ZERO);
        assertTrue(ItemCardRules.missingFields(zero).contains(ItemCardRules.F_QTY));
        CardSubmitCmd negative = validProvinceCmd();
        negative.setQty(new BigDecimal("-5"));
        assertTrue(ItemCardRules.missingFields(negative).contains(ItemCardRules.F_QTY));
    }

    @Test
    public void missing_negativeFine_named() {
        // 承诺时限填成负数，同样不进
        CardSubmitCmd cmd = validProvinceCmd();
        cmd.setFineAmt(new BigDecimal("-1"));
        assertTrue(ItemCardRules.missingFields(cmd).contains(ItemCardRules.F_FINE_AMT));
    }

    @Test
    public void missing_validCmd_passes() {
        assertTrue(ItemCardRules.missingFields(validProvinceCmd()).isEmpty());
    }

    @Test
    public void missing_nullCmd_namedLevel() {
        assertFalse(ItemCardRules.missingFields(null).isEmpty());
    }

    @Test
    public void requiredFields_pointedByLevel() {
        assertTrue(ItemCardRules.requiredFields(ItemCardRules.LEVEL_PROVINCE_STD).contains(ItemCardRules.F_UPPER_ITEM_CODE));
        assertTrue(ItemCardRules.requiredFields(ItemCardRules.LEVEL_CITY_IMPL).contains(ItemCardRules.F_START_DATE));
        assertTrue(ItemCardRules.requiredFields(ItemCardRules.LEVEL_COUNTY_EXT).contains(ItemCardRules.F_RECV_POINTS));
        assertFalse(ItemCardRules.requiredFields(null).contains(ItemCardRules.F_UPPER_ITEM_CODE));
    }

    @Test
    public void futureStart_detected() {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, 3);
        assertTrue(ItemCardRules.isFutureStart(cal.getTime(), today));
        cal.add(Calendar.DAY_OF_MONTH, -10);
        assertFalse(ItemCardRules.isFutureStart(cal.getTime(), today));
        assertFalse(ItemCardRules.isFutureStart(null, today));
    }

    // ---------- 状态机 ----------

    private TItemImplementCard cardAt(Integer stepCount, Integer status) {
        TItemImplementCard card = new TItemImplementCard();
        card.setStepCount(stepCount);
        card.setStatus(status);
        card.setDelFlag(0);
        return card;
    }

    @Test
    public void deriveStep_prefersStepCount() {
        assertEquals(1, ItemCardRules.deriveStep(cardAt(1, 0)));
        assertEquals(2, ItemCardRules.deriveStep(cardAt(2, 0)));
    }

    @Test
    public void deriveStep_legacyCardFallsBackToStatus() {
        // 旧卡步数空着，拿留下的档次接着数；都空着就当刚立
        assertEquals(1, ItemCardRules.deriveStep(cardAt(null, 1)));
        assertEquals(0, ItemCardRules.deriveStep(cardAt(null, null)));
        assertEquals(0, ItemCardRules.deriveStep(null));
    }

    @Test
    public void stateMachine_advanceAndRollback() {
        TItemImplementCard pending = cardAt(0, 0);
        assertTrue(ItemCardRules.canAdvance(pending));
        assertFalse(ItemCardRules.canRollback(pending), "待核对没处退");

        TItemImplementCard checked = cardAt(1, 1);
        assertTrue(ItemCardRules.canAdvance(checked));
        assertTrue(ItemCardRules.canRollback(checked), "未冻住前一步的核对能退一档");

        TItemImplementCard frozen = cardAt(2, 2);
        assertFalse(ItemCardRules.canAdvance(frozen), "到冻住这张便到了头");
        assertFalse(ItemCardRules.canRollback(frozen), "冻住不许退");
        assertTrue(ItemCardRules.isFrozen(frozen));
    }

    @Test
    public void inFlight_legacyCardNotTreatedAsFinished() {
        // 仍在走的旧卡不能当成已经结束的
        TItemImplementCard legacy = cardAt(null, null);
        assertTrue(ItemCardRules.isInFlight(legacy));
        TItemImplementCard halted = cardAt(0, 0);
        halted.setDelFlag(1);
        assertFalse(ItemCardRules.isInFlight(halted));
        assertFalse(ItemCardRules.isInFlight(cardAt(2, 2)));
    }

    // ---------- 校验串 ----------

    @Test
    public void checksum_deterministic() {
        String a = ItemCardRules.buildChecksum("LK1", "ZW00", "XK-001", 1,
                new BigDecimal("100"), new BigDecimal("7"), 2, 1);
        String b = ItemCardRules.buildChecksum("LK1", "ZW00", "XK-001", 1,
                new BigDecimal("100"), new BigDecimal("7"), 2, 1);
        assertEquals(a, b);
    }

    @Test
    public void checksum_versionChangeRecomputes() {
        // 换一次版次这串跟着新版重算，两版共用一串即算错
        String v1 = ItemCardRules.buildChecksum("LK1", "ZW00", "XK-001", 1,
                new BigDecimal("100"), new BigDecimal("7"), 2, 1);
        String v2 = ItemCardRules.buildChecksum("LK2", "ZW00", "XK-001", 2,
                new BigDecimal("100"), new BigDecimal("7"), 2, 1);
        assertNotEquals(v1, v2);
    }

    @Test
    public void checksum_runChangeRecomputes() {
        // 退档重走那一趟另钉新串
        String run1 = ItemCardRules.buildChecksum("LK1", "ZW00", "XK-001", 1,
                new BigDecimal("100"), new BigDecimal("7"), 2, 1);
        String run2 = ItemCardRules.buildChecksum("LK1", "ZW00", "XK-001", 1,
                new BigDecimal("100"), new BigDecimal("7"), 2, 2);
        assertNotEquals(run1, run2);
    }

    @Test
    public void checksum_legacyNullParts_noThrow() {
        // 旧卡版次序空着也能拼，不抛错
        assertNotNull(ItemCardRules.buildChecksum(null, "ZW00", "XK-001", null, null, null, null, null));
    }
}
