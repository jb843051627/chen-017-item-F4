package com.fc.v2.service.itemcard;

import com.fc.v2.model.auto.TItemCardTrail;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事项卡规矩逐条验：折层边界、缺栏指名、负数拒收、名额占用、
 * 步数自数、回退另趟留痕、校验串钉死与换版重算、旧卡接纳、跨年不重复计。
 *
 * @author fuce
 * @date 2026-10-02
 */
class ItemCardDomainTest {

    private MemGateway gw;
    private ItemCardDomain domain;

    private static final String ZW00 = "ZW00";
    private static final String ZW01 = "ZW01";
    private static final String ITEM = "LABOR-DISPATCH-LICENSE";

    @BeforeEach
    void setUp() {
        gw = new MemGateway();
        // 在编册：三线 300/1500/6000，区划 青屏省—青屏市—青屏县
        gw.putArch(MemGateway.arch(0L, ZW00, "青屏省—青屏市—青屏县", 0, "300", "1500", "6000"));
        // 已撤并册
        gw.putArch(MemGateway.arch(1L, ZW01, "青屏省—青屏市—青屏县", 1, "300", "1500", "6000"));
        domain = new ItemCardDomain(gw);
    }

    private TItemImplementCard fillLevel1(String qty, String days) {
        TItemImplementCard c = MemGateway.draft(ZW00, ITEM, TItemImplementCard.LEVEL_PROVINCE, qty, days);
        c.setUpperItemNo("SW2026-007");
        c.setUpperBasisClause("第12条第3项");
        return c;
    }

    // ============ 三条折线 ============

    @Test
    @DisplayName("折层：自低向高，头一个没够着的界就是停的那层")
    void foldBands() {
        TItemDeptArch a = gw.findArchBySiteNo(ZW00);
        assertEquals(1, domain.foldGrade(a, new BigDecimal("10")));
        assertEquals(1, domain.foldGrade(a, new BigDecimal("299.99")));
        assertEquals(2, domain.foldGrade(a, new BigDecimal("300")));
        assertEquals(2, domain.foldGrade(a, new BigDecimal("1499")));
        // 恰好同中界持平，往上取一层到第3层
        assertEquals(3, domain.foldGrade(a, new BigDecimal("1500")));
        assertEquals(3, domain.foldGrade(a, new BigDecimal("5999")));
        // 越过中界又够到高界，落到最高那一层
        assertEquals(4, domain.foldGrade(a, new BigDecimal("6000")));
        assertEquals(4, domain.foldGrade(a, new BigDecimal("9000")));
        // 界没齐（旧纸卡）不硬折，也不抛错
        TItemDeptArch broken = MemGateway.arch(9L, "X", "省—市—县", 0, null, "1500", "6000");
        assertNull(domain.foldGrade(broken, new BigDecimal("500")));
    }

    // ============ 立卡验栏 ============

    @Test
    @DisplayName("必填栏按承办层级当场点出")
    void requiredFieldsByLevel() {
        List<String> base = domain.requiredFields(null);
        assertTrue(base.contains(ItemCardDomain.F_HANDLE_LEVEL));
        assertTrue(domain.requiredFields(TItemImplementCard.LEVEL_PROVINCE).contains(ItemCardDomain.F_UPPER_ITEM_NO));
        assertTrue(domain.requiredFields(TItemImplementCard.LEVEL_CITY).contains(ItemCardDomain.F_CITY_START));
        assertTrue(domain.requiredFields(TItemImplementCard.LEVEL_COUNTY).contains(ItemCardDomain.F_WINDOW_COUNT));
    }

    @Test
    @DisplayName("层级空着或省定缺上位代号，卡不进库，回执指名空在哪栏")
    void rejectNamesEmptyFields() {
        TItemImplementCard noLevel = MemGateway.draft(ZW00, ITEM, null, "500", "7");
        CardRejectException ex = assertThrows(CardRejectException.class,
                () -> domain.open(noLevel, "张三", false));
        assertTrue(ex.getEmptyFields().contains(ItemCardDomain.F_HANDLE_LEVEL));
        assertFalse(ex.getMessage().equals("数不像话"));

        TItemImplementCard provinceMissingCode =
                MemGateway.draft(ZW00, ITEM, TItemImplementCard.LEVEL_PROVINCE, "500", "7");
        CardRejectException ex2 = assertThrows(CardRejectException.class,
                () -> domain.open(provinceMissingCode, "张三", false));
        assertTrue(ex2.getEmptyFields().contains(ItemCardDomain.F_UPPER_ITEM_NO));
        assertTrue(ex2.getEmptyFields().contains(ItemCardDomain.F_UPPER_CLAUSE));
        assertEquals(0, gw.findAll().size(), "拒收不得落库");
    }

    @Test
    @DisplayName("年办件量为零或负数、承诺时限负数，卡不进库")
    void rejectZeroNegative() {
        CardRejectException z = assertThrows(CardRejectException.class,
                () -> domain.open(fillLevel1("0", "7"), "张三", false));
        assertTrue(z.getEmptyFields().contains(ItemCardDomain.F_QTY));
        assertTrue(assertThrows(CardRejectException.class,
                () -> domain.open(fillLevel1("-3", "7"), "张三", false))
                .getEmptyFields().contains(ItemCardDomain.F_QTY));
        assertTrue(assertThrows(CardRejectException.class,
                () -> domain.open(fillLevel1("500", "-1"), "张三", false))
                .getEmptyFields().contains(ItemCardDomain.F_FINE_AMT));
    }

    @Test
    @DisplayName("起头那一日写成往后的日子，先问一句才收")
    void futureStartNeedsConfirm() {
        TItemImplementCard c = MemGateway.draft(ZW00, ITEM, TItemImplementCard.LEVEL_CITY, "500", "12");
        c.setCityRefineText("细化三条");
        c.setCityEffStart(new Date(gw.now().getTime() + 86400000L));
        c.setCityEffEnd(new Date(gw.now().getTime() + 9L * 86400000L));
        CardRejectException ex = assertThrows(CardRejectException.class,
                () -> domain.open(c, "张三", false));
        assertTrue(ex.isNeedConfirm());
        assertEquals(0, gw.findAll().size(), "没问过不收");
        // 问过答是，收下
        TItemImplementCard got = domain.open(c, "张三", true);
        assertNotNull(got.getId());
    }

    @Test
    @DisplayName("撤并册当场关口，新卡不收；只认代号不认名号")
    void mergedArchRejectsAndCodeOnly() {
        TItemImplementCard c = fillLevel1("500", "7");
        c.setSiteNo(ZW01);
        CardRejectException ex = assertThrows(CardRejectException.class,
                () -> domain.open(c, "张三", false));
        assertTrue(ex.getEmptyFields().contains(ItemCardDomain.F_SITE_NO));

        TItemImplementCard ghost = fillLevel1("500", "7");
        ghost.setSiteNo("名号很像的部门");
        assertThrows(CardRejectException.class, () -> domain.open(ghost, "张三", false));
    }

    @Test
    @DisplayName("系统发号：自家塞的号被盖过")
    void systemIssuedNumbers() {
        TItemImplementCard c = fillLevel1("500", "7");
        c.setBillNo("MY-OWN-NO");
        TItemImplementCard got = domain.open(c, "张三", false);
        assertNotEquals("MY-OWN-NO", got.getBillNo());
        assertTrue(got.getImplCode().startsWith("IMP"));
        assertEquals(1, got.getNodeNo());
    }

    // ============ 名额：同部门同事项同时只一张在办 ============

    @Test
    @DisplayName("头一张未冻住也没叫停，第二张开不出来；冻住或叫停后名额空出")
    void slotSingleInFlight() {
        TItemImplementCard first = domain.open(fillLevel1("500", "7"), "窗口甲", false);
        // 两家窗口同一天各报一遍
        assertThrows(CardRejectException.class, () -> domain.open(fillLevel1("600", "7"), "窗口乙", false));

        // 叫停 -> 第二张可立
        domain.stop(first.getId(), "科长");
        TItemImplementCard second = domain.open(fillLevel1("600", "7"), "窗口乙", false);
        assertNotNull(second);
        assertNotEquals(first.getId(), second.getId());

        // 再立第三张又被占，第二张走到冻住后才空出
        assertThrows(CardRejectException.class, () -> domain.open(fillLevel1("601", "7"), "窗口丙", false));
        domain.advance(second.getId(), "经办人");
        domain.advance(second.getId(), "经办人");
        TItemImplementCard third = domain.open(fillLevel1("602", "7"), "窗口丙", false);
        assertNotNull(third, "走到冻住，占过的名额空出来");
    }

    // ============ 步数自数、走/退、校验串 ============

    @Test
    @DisplayName("当前档由步数自数，屏上填的status不作数")
    void statusCountedFromSteps() {
        TItemImplementCard c = fillLevel1("500", "7");
        c.setStatus(2); // 屏上硬塞"已冻住"
        TItemImplementCard got = domain.open(c, "张三", false); // 立卡时系统盖回待核对
        assertEquals(TItemImplementCard.STEP_PENDING, domain.currentStep(got));
    }

    @Test
    @DisplayName("走到已核对钉校验串；冻住到头不能再走；未冻住可退，旧趟留痕另存")
    void advanceFreezeBackAndChecksum() {
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "张三", false);
        assertNull(c.getContent(), "待核对校验串还没钉");

        domain.advance(c.getId(), "李四");
        TItemImplementCard checked = gw.findById(c.getId());
        assertEquals(TItemImplementCard.STEP_CHECKED, domain.currentStep(checked));
        String seal = checked.getContent();
        assertNotNull(seal);
        assertEquals(seal, domain.buildChecksum(checked), "串的拼法以领域给回为准");

        // 钉过覆写不了：改承诺时限后重算的串不同，但卡上那串不动
        checked.setFineAmt(new BigDecimal("9"));
        assertNotEquals(seal, domain.buildChecksum(checked));
        checked.setFineAmt(new BigDecimal("7"));

        // 退回一档重来，串不抹，留痕另起一趟
        domain.back(c.getId(), "李四");
        TItemImplementCard back = gw.findById(c.getId());
        assertEquals(TItemImplementCard.STEP_PENDING, domain.currentStep(back));
        assertEquals(seal, back.getContent(), "钉过覆写不了");
        List<TItemCardTrail> trails = gw.trailsOf(c.getId());
        int rounds = 0;
        for (TItemCardTrail t : trails) {
            rounds = Math.max(rounds, t.getRoundNo());
        }
        assertEquals(2, rounds, "回退后后补记录进第2趟，不与第1趟并作一处");

        // 重走到已核对沿用原串
        domain.advance(c.getId(), "王五");
        assertEquals(seal, gw.findById(c.getId()).getContent());

        // 再走到冻住，到头
        domain.advance(c.getId(), "王五");
        TItemImplementCard frozen = gw.findById(c.getId());
        assertEquals(TItemImplementCard.STEP_FROZEN, domain.currentStep(frozen));
        assertThrows(CardRejectException.class, () -> domain.advance(c.getId(), "王五"));
        assertThrows(CardRejectException.class, () -> domain.back(c.getId(), "王五"));
        assertThrows(CardRejectException.class, () -> domain.changeQty(c.getId(), new BigDecimal("700"), "王五"));
    }

    @Test
    @DisplayName("开卡人一生不换，经办人办一次续一名")
    void openerImmutableHandlerRenews() {
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "开卡赵六", false);
        domain.advance(c.getId(), "经办钱七");
        TItemImplementCard card = gw.findById(c.getId());
        assertEquals("开卡赵六", card.getOpenerName());
        assertEquals("经办钱七", card.getHandlerName());
        domain.back(c.getId(), "经办孙八");
        card = gw.findById(c.getId());
        assertEquals("开卡赵六", card.getOpenerName(), "重录多少遍都轮不到开卡人换人");
        assertEquals("经办孙八", card.getHandlerName());
    }

    // ============ 办件量增减、改挂靠重折 ============

    @Test
    @DisplayName("办件量每增减一回就重新折层，层级格没有手填把手")
    void qtyChangeRefolds() {
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "张三", false);
        assertEquals(2, c.getGradeLevel());
        domain.changeQty(c.getId(), new BigDecimal("2000"), "张三");
        assertEquals(3, gw.findById(c.getId()).getGradeLevel());
        assertThrows(CardRejectException.class,
                () -> domain.changeQty(c.getId(), new BigDecimal("0"), "张三"));
    }

    @Test
    @DisplayName("改挂靠只认代号，换册按新三线重折、区划快照跟新册")
    void rehangByCodeOnly() {
        gw.putArch(MemGateway.arch(2L, "ZW02", "青屏省—碧川市—碧川县", 0, "100", "800", "3000"));
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "张三", false);
        // 名号相近不认
        assertThrows(CardRejectException.class, () -> domain.rehang(c.getId(), "不存在的册", "张三"));
        domain.rehang(c.getId(), "ZW02", "张三");
        TItemImplementCard moved = gw.findById(c.getId());
        assertEquals("ZW02", moved.getSiteNo());
        assertEquals("青屏省—碧川市—碧川县", moved.getRegionPath());
        // 500 件在新册三线（100/800/3000）下折到第2层
        assertEquals(2, moved.getGradeLevel());
    }

    // ============ 换版 ============

    @Test
    @DisplayName("换版：旧版退现行保旧串，新版新立新串，两版共用一串即算错")
    void reviseSealsSeparately() {
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "张三", false);
        domain.advance(c.getId(), "张三");
        String oldSeal = gw.findById(c.getId()).getContent();
        // 改承诺时限触发新版内容不同
        TItemImplementCard changes = new TItemImplementCard();
        changes.setFineAmt(new BigDecimal("12"));
        TItemImplementCard neo = domain.revise(c.getId(), changes, "张三", false);
        TItemImplementCard oldRow = gw.findById(c.getId());
        assertEquals(TItemImplementCard.CURRENT_NO, oldRow.getCurrentFlag(), "旧版退出当前清单");
        assertEquals(oldSeal, oldRow.getContent(), "旧版那行仍是旧串");
        assertEquals(c.getBillNo(), neo.getBillNo(), "同一张卡跨版共用卡号");
        assertEquals(2, neo.getNodeNo().intValue());
        assertNull(neo.getContent(), "新版待核对，串尚未钉");
        assertEquals(c.getOpenerName(), neo.getOpenerName(), "换版不换开卡人");
        domain.advance(neo.getId(), "张三");
        String newSeal = gw.findById(neo.getId()).getContent();
        assertNotEquals(oldSeal, newSeal, "新版那行用新串；两版共用一串即算错");
        // 日常摆出来的只许最新版
        List<TItemImplementCard> visible = domain.visibleCards(null);
        assertEquals(1, visible.size());
        assertEquals(neo.getId(), visible.get(0).getId());
        // 历史两版都翻得着
        assertEquals(2, domain.historyOf(c.getBillNo()).size());
    }

    // ============ 旧纸卡 ============

    @Test
    @DisplayName("旧纸卡缺步数/版次/校验串也接得住，按在走对待，不抛错")
    void legacyAcceptedAsInFlight() {
        TItemImplementCard legacy = new TItemImplementCard();
        legacy.setSiteNo(ZW00);
        legacy.setItemCode("LEGACY-1");
        // 步数、版次序、校验串、层级、承办层级全空
        TItemImplementCard got = domain.admitLegacy(legacy);
        assertEquals(TItemImplementCard.STEP_PENDING, domain.currentStep(got), "不当成已结束");
        assertEquals(1, got.getLegacyFlag().intValue());
        assertNotNull(got.getBillNo(), "缺号补系统号");
        // 仍是在办，同事项新卡开不出来（占名额）
        TItemImplementCard dup = MemGateway.draft(ZW00, "LEGACY-1",
                TItemImplementCard.LEVEL_PROVINCE, "500", "7");
        dup.setUpperItemNo("X");
        dup.setUpperBasisClause("Y");
        assertThrows(CardRejectException.class, () -> domain.open(dup, "张三", false));
        // 能往前走到已核对、冻住
        domain.advance(got.getId(), "张三");
        assertNotNull(gw.findById(got.getId()).getContent());
    }

    // ============ 账号划范围 ============

    @Test
    @DisplayName("县只看本县、市看所辖县、省看全辖、协办只见受托一家")
    void viewerScopes() {
        gw.putArch(MemGateway.arch(3L, "C01", "青屏省—碧川市—碧川县", 0, "300", "1500", "6000"));
        TItemImplementCard a = domain.open(fillLevel1("500", "7"), "张三", false);
        TItemImplementCard other = MemGateway.draft("C01", "OTHER",
                TItemImplementCard.LEVEL_PROVINCE, "500", "7");
        other.setUpperItemNo("S");
        other.setUpperBasisClause("C");
        other.setEntrustSiteNo("AGENT-ONLY");
        domain.open(other, "李四", false);

        // 县账号只看本县那一本册
        assertEquals(1, domain.visibleCards(ViewerScope.county("青屏省—青屏市—青屏县")).size());
        // 市账号看本市所辖各县
        assertEquals(1, domain.visibleCards(ViewerScope.city("青屏省—青屏市")).size());
        assertEquals(2, domain.visibleCards(ViewerScope.city("青屏省")).size());
        // 省办全辖
        assertEquals(2, domain.visibleCards(ViewerScope.province("青屏省")).size());
        // 跨市协办只把受托那一家摆出来
        List<TItemImplementCard> agentView = domain.visibleCards(ViewerScope.agent("AGENT-ONLY"));
        assertEquals(1, agentView.size());
        assertEquals("OTHER", agentView.get(0).getItemCode());
    }

    @Test
    @DisplayName("区划改隶前立的旧卡留在原地，不跟着搬")
    void regionReassignmentKeepsOldCards() {
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "张三", false);
        gw.archChangeRoad(ZW00, "青屏省—新海市—新海县");
        assertEquals("青屏省—青屏市—青屏县", gw.findById(c.getId()).getRegionPath());
        // 旧市账号仍翻得见，新市账号看不见这张旧卡
        assertEquals(1, domain.visibleCards(ViewerScope.city("青屏省—青屏市")).size());
        assertEquals(0, domain.visibleCards(ViewerScope.city("青屏省—新海市")).size());
    }

    // ============ 年底本级一屏 ============

    @Test
    @DisplayName("卡上数与名录数同一回算；跨年同事项不重复计；空级不摆空行")
    void censusConsistentAndNoDoubleCount() {
        TItemImplementCard c = domain.open(fillLevel1("500", "7"), "张三", false); // 第2层
        // 同事项换版：旧版退出，名录数不得因此重复
        domain.advance(c.getId(), "张三");
        domain.revise(c.getId(), new TItemImplementCard(), "张三", false);
        // 另一事项落在第3层
        TItemImplementCard second = MemGateway.draft(ZW00, "SECOND-ITEM",
                TItemImplementCard.LEVEL_PROVINCE, "2000", "7");
        second.setUpperItemNo("S");
        second.setUpperBasisClause("C");
        domain.open(second, "张三", false);

        String cardYear = c.getYearNo();
        CensusView view = domain.census(ViewerScope.county("青屏省—青屏市—青屏县"), cardYear);
        assertEquals(2, view.getCardCount(), "现行两张，旧版不计第二遍");
        assertEquals(2, view.getCatalogCount(), "两对底册代号＋事项码");
        assertFalse(view.getCountByGrade().containsKey(1), "第1层空着，不摆空行");
        assertTrue(view.getCountByGrade().containsKey(2));
        assertTrue(view.getCountByGrade().containsKey(3));
    }
}
