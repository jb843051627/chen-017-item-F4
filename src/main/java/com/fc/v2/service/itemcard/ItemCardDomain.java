package com.fc.v2.service.itemcard;

import com.fc.v2.model.auto.TItemCardTrail;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.util.MD5Util;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 事项卡领域规则：能不能立卡、当前算哪一步、还能不能往前再走一步或退回一步，
 * 说了算的只有这里的方法。哪几栏非写不可按承办层级当场点出；折到第几层级、
 * 校验串拼成何样、本级名录进几个数，也一律以这里的给回为准。
 *
 * <p>不留第二条绕过它的通道：控制器只做转发，落库走 {@link ItemCardGateway}。</p>
 *
 * @author fuce
 * @date 2026-10-02
 */
@Component
public class ItemCardDomain {

    // —— 回执里指名道姓的栏名 ——
    public static final String F_SITE_NO = "底册代号";
    public static final String F_ITEM_CODE = "事项码";
    public static final String F_HANDLE_LEVEL = "承办层级";
    public static final String F_QTY = "年办件量";
    public static final String F_FINE_AMT = "承诺时限";
    public static final String F_UPPER_ITEM_NO = "上位名录代号";
    public static final String F_UPPER_CLAUSE = "设定依据条款号";
    public static final String F_CITY_REFINE = "本市细化条目";
    public static final String F_CITY_START = "市实施起头日";
    public static final String F_CITY_END = "市实施结尾日";
    public static final String F_WINDOW_COUNT = "延伸窗口数";
    public static final String F_POINT_COUNT = "延伸收件点数";

    private final ItemCardGateway gateway;

    public ItemCardDomain(ItemCardGateway gateway) {
        this.gateway = gateway;
    }

    // ===================================================================
    // 权威口径一：哪几栏非写不可，按承办层级当场点
    // ===================================================================

    /**
     * 按承办层级点出非写不可的栏。层级空着没选，只点得公共三栏加层级本身。
     */
    public List<String> requiredFields(Integer handleLevel) {
        List<String> fields = new ArrayList<String>();
        fields.add(F_SITE_NO);
        fields.add(F_ITEM_CODE);
        fields.add(F_HANDLE_LEVEL);
        fields.add(F_QTY);
        fields.add(F_FINE_AMT);
        if (handleLevel == null) {
            return fields;
        }
        switch (handleLevel) {
            case TItemImplementCard.LEVEL_PROVINCE:
                fields.add(F_UPPER_ITEM_NO);
                fields.add(F_UPPER_CLAUSE);
                break;
            case TItemImplementCard.LEVEL_CITY:
                fields.add(F_CITY_REFINE);
                fields.add(F_CITY_START);
                fields.add(F_CITY_END);
                break;
            case TItemImplementCard.LEVEL_COUNTY:
                fields.add(F_WINDOW_COUNT);
                fields.add(F_POINT_COUNT);
                break;
            default:
                break;
        }
        return fields;
    }

    // ===================================================================
    // 权威口径二：三条折线折服务监督层级
    // ===================================================================

    /**
     * 三条折线自低向高排，拿年办件量往上靠，头一个没够着的界就是该停的那一层。
     * 恰好同中界持平的往上取一层；越过中界又够到高界的落到最高那一层。
     * 界或量没齐（旧纸卡常见）返回 null，不硬折。
     */
    public Integer foldGrade(TItemDeptArch arch, BigDecimal qty) {
        if (arch == null || qty == null
                || arch.getTh1Max() == null || arch.getTh2Max() == null || arch.getTh3Max() == null) {
            return null;
        }
        if (qty.compareTo(arch.getTh1Max()) < 0) {
            return 1;
        }
        if (qty.compareTo(arch.getTh2Max()) < 0) {
            return 2;
        }
        if (qty.compareTo(arch.getTh3Max()) < 0) {
            // 恰好同中界持平的落在这一档：已够着中界、没够着高界，往上取一层到第3层
            return 3;
        }
        // 越过中界又够到高界（含越过高界），落到最高那一层
        return 4;
    }

    // ===================================================================
    // 权威口径三：校验串
    // ===================================================================

    /**
     * 校验串拼成何样以此为准：卡号＋版次序＋配平对＋承诺时限＋承办层级＋折层
     * 一并压印，换一次版次（nodeNo 变）必然重算出另一条串。
     */
    public String buildChecksum(TItemImplementCard card) {
        String raw = join(card.getBillNo(), card.getNodeNo(), card.getSiteNo(), card.getItemCode(),
                card.getFineAmt(), card.getHandleLevel(), card.getGradeLevel(),
                card.getRegionPath(), card.getYearNo());
        return MD5Util.encode(raw);
    }

    private String join(Object... parts) {
        StringBuilder sb = new StringBuilder("CARD-V1");
        for (Object p : parts) {
            sbap(sb, p);
        }
        return sb.toString();
    }

    private void sbap(StringBuilder sb, Object p) {
        sb.append('|').append(p == null ? "" : p.toString());
    }

    // ===================================================================
    // 权威口径四：当前走到哪一步，由已走完的步数自数
    // ===================================================================

    /**
     * 当前档由系统按已走完的步数自己数出来；屏上填的档次、经办人记的档次不作数。
     * 旧纸卡步数空着也不抛错：按仍在走的待核对接住，不当成已结束。
     */
    public int currentStep(TItemImplementCard card) {
        if (card.getStepsDone() != null) {
            return Math.max(TItemImplementCard.STEP_PENDING,
                    Math.min(TItemImplementCard.STEP_FROZEN, card.getStepsDone()));
        }
        if (card.getStatus() != null) {
            return Math.max(TItemImplementCard.STEP_PENDING,
                    Math.min(TItemImplementCard.STEP_FROZEN, card.getStatus()));
        }
        return TItemImplementCard.STEP_PENDING;
    }

    // ===================================================================
    // 立卡
    // ===================================================================

    /**
     * 立一张卡。系统发卡号、实施编码，折头一回层级；同部门同事项已有在办卡的，
     * 第二张开不出来。缺栏/数不像话用 {@link CardRejectException} 指名回执；
     * 起头日写成往后的日子先抛 needConfirm 问一句。
     *
     * @param confirmFutureStart 屏上已问过一句"起头日在日后，仍要收？"且答了是
     */
    public TItemImplementCard open(TItemImplementCard input, String operator, boolean confirmFutureStart) {
        Date now = gateway.now();
        TItemDeptArch arch = resolveArch(input);
        List<String> missing = validate(input, arch, now, confirmFutureStart);
        boolean futureStart = isFutureCityStart(input, now);
        if (!missing.isEmpty()) {
            throw new CardRejectException("以下栏空着或不像话，卡不进库", missing, futureStart);
        }
        if (futureStart && !confirmFutureStart) {
            throw new CardRejectException("市实施起头那一日写成往后的日子，得先问一句才收",
                    new ArrayList<String>(), true);
        }
        if (slotOccupied(arch.getSiteNo(), input.getItemCode(), null)) {
            throw new CardRejectException("同一部门名下同一事项已有一张在办，未冻住也未叫停前，第二张开不出来");
        }

        String who = operator(operator);
        // 系统发号：塞进来的自家号一律不认，当场盖过
        input.setBillNo(gateway.nextBillNo());
        input.setImplCode(gateway.nextImplCode());
        input.setNodeNo(1);
        input.setSiteId(arch.getId() == null ? null : arch.getId());
        input.setSiteNo(arch.getSiteNo());
        input.setRegionPath(arch.getRoadName());
        input.setYearNo(yearOf(now));
        input.setGradeLevel(foldGrade(arch, input.getQty()));
        input.setStepsDone(TItemImplementCard.STEP_PENDING);
        input.setStatus(TItemImplementCard.STEP_PENDING);
        input.setCurrentFlag(TItemImplementCard.CURRENT_YES);
        input.setStopFlag(0);
        input.setLegacyFlag(0);
        input.setDelFlag(0);
        input.setContent(null);
        input.setOpenerName(who);
        input.setHandlerName(who);

        gateway.insert(input);
        recordTrail(input, "open", null, TItemImplementCard.STEP_PENDING, null, input.getGradeLevel(),
                who, "立卡");
        return input;
    }

    /**
     * 校验：层级空、省定缺上位代号、量为零/负、时限为负等，逐栏点出。
     */
    public List<String> validate(TItemImplementCard c, TItemDeptArch arch, Date now, boolean confirm) {
        List<String> missing = new ArrayList<String>();
        if (StringUtils.isEmpty(c.getSiteNo())) {
            missing.add(F_SITE_NO);
        } else if (arch == null) {
            missing.add(F_SITE_NO);
        } else if (arch.getStatus() != null && arch.getStatus() == 1) {
            missing.add(F_SITE_NO);
        }
        if (StringUtils.isEmpty(c.getItemCode())) {
            missing.add(F_ITEM_CODE);
        }
        if (c.getHandleLevel() == null
                || (c.getHandleLevel() != TItemImplementCard.LEVEL_PROVINCE
                && c.getHandleLevel() != TItemImplementCard.LEVEL_CITY
                && c.getHandleLevel() != TItemImplementCard.LEVEL_COUNTY)) {
            missing.add(F_HANDLE_LEVEL);
        }
        if (c.getQty() == null || c.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            missing.add(F_QTY);
        }
        if (c.getFineAmt() == null || c.getFineAmt().compareTo(BigDecimal.ZERO) < 0) {
            missing.add(F_FINE_AMT);
        }
        if (c.getHandleLevel() != null) {
            switch (c.getHandleLevel()) {
                case TItemImplementCard.LEVEL_PROVINCE:
                    if (StringUtils.isEmpty(c.getUpperItemNo())) {
                        missing.add(F_UPPER_ITEM_NO);
                    }
                    if (StringUtils.isEmpty(c.getUpperBasisClause())) {
                        missing.add(F_UPPER_CLAUSE);
                    }
                    break;
                case TItemImplementCard.LEVEL_CITY:
                    if (StringUtils.isEmpty(c.getCityRefineText())) {
                        missing.add(F_CITY_REFINE);
                    }
                    if (c.getCityEffStart() == null) {
                        missing.add(F_CITY_START);
                    }
                    if (c.getCityEffEnd() == null) {
                        missing.add(F_CITY_END);
                    } else if (c.getCityEffStart() != null
                            && c.getCityEffEnd().before(c.getCityEffStart())) {
                        missing.add(F_CITY_END);
                    }
                    break;
                case TItemImplementCard.LEVEL_COUNTY:
                    if (c.getExtendWindowCount() == null || c.getExtendWindowCount() < 0) {
                        missing.add(F_WINDOW_COUNT);
                    }
                    if (c.getExtendPointCount() == null || c.getExtendPointCount() < 0) {
                        missing.add(F_POINT_COUNT);
                    }
                    break;
                default:
                    break;
            }
        }
        return missing;
    }

    private boolean isFutureCityStart(TItemImplementCard c, Date now) {
        return c.getHandleLevel() != null
                && c.getHandleLevel() == TItemImplementCard.LEVEL_CITY
                && c.getCityEffStart() != null
                && c.getCityEffStart().after(now);
    }

    private TItemDeptArch resolveArch(TItemImplementCard c) {
        if (StringUtils.isEmpty(c.getSiteNo())) {
            return null;
        }
        return gateway.findArchBySiteNo(c.getSiteNo());
    }

    /**
     * 名额占着没有：同「底册代号＋事项码」一对，现行、没叫停、步数没到冻住的，
     * 就算一张在办。旧纸卡步数空着按在办算，不当作已结束。
     */
    public boolean slotOccupied(String siteNo, String itemCode, Long excludeCardId) {
        for (TItemImplementCard c : gateway.findCurrent()) {
            if (excludeCardId != null && excludeCardId.equals(c.getId())) {
                continue;
            }
            if (siteNo != null && siteNo.equals(c.getSiteNo())
                    && itemCode != null && itemCode.equals(c.getItemCode())
                    && (c.getStopFlag() == null || c.getStopFlag() == 0)
                    && currentStep(c) < TItemImplementCard.STEP_FROZEN) {
                return true;
            }
        }
        return false;
    }

    // ===================================================================
    // 往前走 / 退回一步
    // ===================================================================

    /** 往前再走一步：待核对→已核对（钉串）→已冻住（到头）。 */
    public TItemImplementCard advance(Long id, String operator) {
        TItemImplementCard card = mustGet(id);
        if (card.getCurrentFlag() != null && card.getCurrentFlag() == TItemImplementCard.CURRENT_NO) {
            throw new CardRejectException("旧版次已退出当前清单，只能翻检不能再走步");
        }
        if (card.getStopFlag() != null && card.getStopFlag() == 1) {
            throw new CardRejectException("这张已就地叫停，不能再往前走");
        }
        int step = currentStep(card);
        if (step >= TItemImplementCard.STEP_FROZEN) {
            throw new CardRejectException("已冻住：往后添不进去、字也动不得、整张撤不掉，要改只能另起新版");
        }
        Integer fromGrade = card.getGradeLevel();
        int to = step + 1;
        card.setStepsDone(to);
        card.setStatus(to);
        if (to == TItemImplementCard.STEP_CHECKED) {
            // 走到已核对才算钉死；旧纸卡若已带串则沿用其旧串，不覆写
            if (StringUtils.isEmpty(card.getContent())) {
                card.setContent(buildChecksum(card));
            }
        }
        card.setHandlerName(operator(operator));
        gateway.update(card);
        recordTrail(card, "advance", step, to, fromGrade, card.getGradeLevel(),
                operator(operator), to == TItemImplementCard.STEP_CHECKED ? "核对通过，钉校验串" : "冻住");
        return card;
    }

    /** 退回一步：只有未冻住能退，已核对退回待核对重走；旧趟留痕照旧留着。 */
    public TItemImplementCard back(Long id, String operator) {
        TItemImplementCard card = mustGet(id);
        int step = currentStep(card);
        if (step >= TItemImplementCard.STEP_FROZEN) {
            throw new CardRejectException("已冻住，退不回来，要改只能另起新版");
        }
        if (step <= TItemImplementCard.STEP_PENDING) {
            throw new CardRejectException("还在待核对，没有上一步可退");
        }
        if (card.getStopFlag() != null && card.getStopFlag() == 1) {
            throw new CardRejectException("这张已就地叫停，不能回退");
        }
        int to = step - 1;
        card.setStepsDone(to);
        card.setStatus(to);
        // 钉过的串覆写不了：退回待核对也不抹，重走到已核对时沿用原串
        card.setHandlerName(operator(operator));
        gateway.update(card);
        recordTrail(card, "back", step, to, card.getGradeLevel(), card.getGradeLevel(),
                operator(operator), "退回上一档重来，旧趟记录另存");
        return card;
    }

    /** 就地叫停：名额当场空出，同事项再立新卡接得上；卡本身照旧查得着。 */
    public TItemImplementCard stop(Long id, String operator) {
        TItemImplementCard card = mustGet(id);
        int step = currentStep(card);
        if (step >= TItemImplementCard.STEP_FROZEN) {
            throw new CardRejectException("已冻住的卡到头了，谈不上叫停");
        }
        if (card.getStopFlag() != null && card.getStopFlag() == 1) {
            throw new CardRejectException("这张早已叫停");
        }
        card.setStopFlag(1);
        card.setHandlerName(operator(operator));
        gateway.update(card);
        recordTrail(card, "stop", step, step, card.getGradeLevel(), card.getGradeLevel(),
                operator(operator), "就地叫停，腾出同事项名额");
        return card;
    }

    // ===================================================================
    // 办件量增减、改挂靠：每回都重新折一层写回卡上
    // ===================================================================

    /** 年办件量有增减的每一回：验数、重折、直接写回 grade_level，那一格没有手填把手。 */
    public TItemImplementCard changeQty(Long id, BigDecimal qty, String operator) {
        TItemImplementCard card = mustGet(id);
        assertMutable(card);
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            List<String> f = new ArrayList<String>();
            f.add(F_QTY);
            throw new CardRejectException("年办件量填零或填负数都不收", f);
        }
        Integer from = card.getGradeLevel();
        card.setQty(qty);
        card.setGradeLevel(foldGrade(gateway.findArchBySiteNo(card.getSiteNo()), qty));
        card.setHandlerName(operator(operator));
        gateway.update(card);
        recordTrail(card, "qty", currentStep(card), currentStep(card), from, card.getGradeLevel(),
                operator(operator), "年办件量增减，重新折层");
        return card;
    }

    /**
     * 改挂靠的那一册：只凭新底册代号去对，名号相近不认；撤并册不收；
     * 改挂后区划快照跟着新册走（治"换了部门还挂旧册"的缝），并按新册三线重折。
     */
    public TItemImplementCard rehang(Long id, String newSiteNo, String operator) {
        TItemImplementCard card = mustGet(id);
        assertMutable(card);
        if (StringUtils.isEmpty(newSiteNo)) {
            List<String> f = new ArrayList<String>();
            f.add(F_SITE_NO);
            throw new CardRejectException("改挂靠要给新底册代号", f);
        }
        TItemDeptArch arch = gateway.findArchBySiteNo(newSiteNo);
        if (arch == null) {
            throw new CardRejectException("底册代号对不上在册，拿名号相近认门不算对上");
        }
        if (arch.getStatus() != null && arch.getStatus() == 1) {
            throw new CardRejectException("那一册已撤并关口，不收卡");
        }
        if (slotOccupied(arch.getSiteNo(), card.getItemCode(), card.getId())) {
            throw new CardRejectException("新底册名下同一事项已有一张在办，挂不过去");
        }
        Integer from = card.getGradeLevel();
        card.setSiteId(arch.getId() == null ? null : arch.getId());
        card.setSiteNo(arch.getSiteNo());
        card.setRegionPath(arch.getRoadName());
        card.setGradeLevel(foldGrade(arch, card.getQty()));
        card.setHandlerName(operator(operator));
        gateway.update(card);
        recordTrail(card, "rehang", currentStep(card), currentStep(card), from, card.getGradeLevel(),
                operator(operator), "改挂靠底册，按新册三线重折");
        return card;
    }

    // ===================================================================
    // 换版：旧版退出当前清单归历史，新版另立、新串重算
    // ===================================================================

    /**
     * 另起新版：同卡号、版次序 +1，旧版 current_flag 置 0 另归历史、旧串不动；
     * 新版待核对重走，走到已核对时按新版内容钉新串。开卡人仍是头一位，不换人。
     */
    public TItemImplementCard revise(Long id, TItemImplementCard changes, String operator,
                                     boolean confirmFutureStart) {
        TItemImplementCard old = mustGet(id);
        if (old.getCurrentFlag() != null && old.getCurrentFlag() == TItemImplementCard.CURRENT_NO) {
            throw new CardRejectException("历史旧版不能再换版，要换得找当前最新版");
        }
        TItemImplementCard neo = new TItemImplementCard();
        neo.setSiteNo(StringUtils.isNotEmpty(changes.getSiteNo()) ? changes.getSiteNo() : old.getSiteNo());
        neo.setItemCode(StringUtils.isNotEmpty(changes.getItemCode()) ? changes.getItemCode() : old.getItemCode());
        neo.setHandleLevel(changes.getHandleLevel() != null ? changes.getHandleLevel() : old.getHandleLevel());
        neo.setQty(changes.getQty() != null ? changes.getQty() : old.getQty());
        neo.setFineAmt(changes.getFineAmt() != null ? changes.getFineAmt() : old.getFineAmt());
        neo.setUpperItemNo(changes.getUpperItemNo() != null ? changes.getUpperItemNo() : old.getUpperItemNo());
        neo.setUpperBasisClause(changes.getUpperBasisClause() != null ? changes.getUpperBasisClause() : old.getUpperBasisClause());
        neo.setCityRefineText(changes.getCityRefineText() != null ? changes.getCityRefineText() : old.getCityRefineText());
        neo.setCityEffStart(changes.getCityEffStart() != null ? changes.getCityEffStart() : old.getCityEffStart());
        neo.setCityEffEnd(changes.getCityEffEnd() != null ? changes.getCityEffEnd() : old.getCityEffEnd());
        neo.setExtendWindowCount(changes.getExtendWindowCount() != null ? changes.getExtendWindowCount() : old.getExtendWindowCount());
        neo.setExtendPointCount(changes.getExtendPointCount() != null ? changes.getExtendPointCount() : old.getExtendPointCount());
        neo.setEntrustSiteNo(changes.getEntrustSiteNo() != null ? changes.getEntrustSiteNo() : old.getEntrustSiteNo());

        Date now = gateway.now();
        TItemDeptArch arch = gateway.findArchBySiteNo(neo.getSiteNo());
        List<String> missing = validate(neo, arch, now, confirmFutureStart);
        if (!missing.isEmpty()) {
            throw new CardRejectException("新版以下栏空着或不像话，立不起来", missing,
                    isFutureCityStart(neo, now));
        }
        if (isFutureCityStart(neo, now) && !confirmFutureStart) {
            throw new CardRejectException("新版起头那一日写成往后的日子，得先问一句才收",
                    new ArrayList<String>(), true);
        }

        int nextNode = (old.getNodeNo() == null ? 0 : old.getNodeNo()) + 1;
        // 旧版退出当前清单，旧串一字不动随旧版归历史
        old.setCurrentFlag(TItemImplementCard.CURRENT_NO);
        gateway.update(old);

        neo.setBillNo(old.getBillNo());
        neo.setImplCode(gateway.nextImplCode());
        neo.setNodeNo(nextNode);
        neo.setSiteId(arch.getId() == null ? null : arch.getId());
        neo.setSiteNo(arch.getSiteNo());
        neo.setRegionPath(arch.getRoadName());
        neo.setYearNo(old.getYearNo() != null ? old.getYearNo() : yearOf(now));
        neo.setGradeLevel(foldGrade(arch, neo.getQty()));
        neo.setStepsDone(TItemImplementCard.STEP_PENDING);
        neo.setStatus(TItemImplementCard.STEP_PENDING);
        neo.setCurrentFlag(TItemImplementCard.CURRENT_YES);
        neo.setStopFlag(0);
        neo.setLegacyFlag(0);
        neo.setDelFlag(0);
        neo.setContent(null);
        neo.setOpenerName(old.getOpenerName());
        neo.setHandlerName(operator(operator));
        gateway.insert(neo);
        recordTrail(neo, "revise", null, TItemImplementCard.STEP_PENDING, null, neo.getGradeLevel(),
                operator(operator), "由第" + (old.getNodeNo() == null ? "?" : old.getNodeNo()) + "版换版新立");
        return neo;
    }

    // ===================================================================
    // 旧纸卡迁入：缺步数、版次序、校验串也接得住，不抛错、不当作已结束
    // ===================================================================

    /**
     * 纸质簿旧卡迁入。能补的（在编底册、有量）顺手折一层；补不齐的栏留空，
     * 一律按现行待核对接住，绝不一碰就抛错。
     */
    public TItemImplementCard admitLegacy(TItemImplementCard legacy) {
        if (legacy == null) {
            throw new CardRejectException("没有旧卡可迁");
        }
        if (StringUtils.isEmpty(legacy.getBillNo())) {
            legacy.setBillNo(gateway.nextBillNo());
        }
        if (StringUtils.isEmpty(legacy.getImplCode())) {
            legacy.setImplCode(gateway.nextImplCode());
        }
        // 版次序、步数、串、层级空着就空着
        legacy.setCurrentFlag(TItemImplementCard.CURRENT_YES);
        if (legacy.getStopFlag() == null) {
            legacy.setStopFlag(0);
        }
        legacy.setLegacyFlag(1);
        legacy.setDelFlag(0);
        if (legacy.getStatus() == null && legacy.getStepsDone() != null) {
            legacy.setStatus(legacy.getStepsDone());
        }
        TItemDeptArch arch = StringUtils.isEmpty(legacy.getSiteNo()) ? null
                : gateway.findArchBySiteNo(legacy.getSiteNo());
        if (arch != null) {
            if (legacy.getSiteId() == null) {
                legacy.setSiteId(arch.getId());
            }
            if (StringUtils.isEmpty(legacy.getRegionPath())) {
                legacy.setRegionPath(arch.getRoadName());
            }
            if (legacy.getGradeLevel() == null) {
                legacy.setGradeLevel(foldGrade(arch, legacy.getQty()));
            }
        }
        gateway.insert(legacy);
        return legacy;
    }

    // ===================================================================
    // 可见范围 与 年底本级一屏
    // ===================================================================

    /**
     * 账号划范围：县只看本县那一本册、市看所辖各县、省看全辖、
     * 协办只把受托那一家摆给受托人。自己那一级以外不许同屏并列。
     * 摆出来的只许现行卡。
     */
    public List<TItemImplementCard> visibleCards(ViewerScope scope) {
        List<TItemImplementCard> out = new ArrayList<TItemImplementCard>();
        for (TItemImplementCard c : gateway.findCurrent()) {
            if (inScope(c, scope)) {
                out.add(c);
            }
        }
        return out;
    }

    private boolean inScope(TItemImplementCard c, ViewerScope scope) {
        if (scope == null) {
            return true;
        }
        switch (scope.getGrade()) {
            case ViewerScope.GRADE_PROVINCE:
                return c.getRegionPath() != null
                        && (StringUtils.isEmpty(scope.getRegionPath())
                        || c.getRegionPath().startsWith(scope.getRegionPath()));
            case ViewerScope.GRADE_CITY:
                return c.getRegionPath() != null && !StringUtils.isEmpty(scope.getRegionPath())
                        && c.getRegionPath().startsWith(scope.getRegionPath());
            case ViewerScope.GRADE_COUNTY:
                return scope.getRegionPath() != null
                        && scope.getRegionPath().equals(c.getRegionPath());
            case ViewerScope.GRADE_AGENT:
                return scope.getEntrustSiteNo() != null
                        && scope.getEntrustSiteNo().equals(c.getEntrustSiteNo());
            default:
                return false;
        }
    }

    /**
     * 年底本级那一屏：卡上占的数与名录上的数同一回算出（同口径同过滤），
     * 跨年同事项不重复计（只数现行卡，旧版已退出），某一级底下空着不摆空行
     * （map 里根本不放 0 的键）。
     */
    public CensusView census(ViewerScope scope, String yearNo) {
        List<TItemImplementCard> cards = visibleCards(scope);
        Map<String, Long> pairSeen = new LinkedHashMap<String, Long>();
        Map<Integer, Long> byGrade = new LinkedHashMap<Integer, Long>();
        long cardCount = 0;
        for (TItemImplementCard c : cards) {
            if (StringUtils.isNotEmpty(yearNo) && !yearNo.equals(c.getYearNo())) {
                continue;
            }
            cardCount++;
            pairSeen.put(c.getSiteNo() + "" + c.getItemCode(), 1L);
            if (c.getGradeLevel() != null) {
                Long n = byGrade.get(c.getGradeLevel());
                byGrade.put(c.getGradeLevel(), n == null ? 1L : n + 1L);
            }
        }
        String region = scope == null ? null : scope.getRegionPath();
        return new CensusView(region, yearNo, cardCount, pairSeen.size(), byGrade);
    }

    /** 历史版次翻检：同卡号的各版，按版次序排开。 */
    public List<TItemImplementCard> historyOf(String billNo) {
        List<TItemImplementCard> out = new ArrayList<TItemImplementCard>();
        for (TItemImplementCard c : gateway.findAll()) {
            if (billNo != null && billNo.equals(c.getBillNo())) {
                out.add(c);
            }
        }
        out.sort(new Comparator<TItemImplementCard>() {
            @Override
            public int compare(TItemImplementCard a, TItemImplementCard b) {
                int an = a.getNodeNo() == null ? 0 : a.getNodeNo();
                int bn = b.getNodeNo() == null ? 0 : b.getNodeNo();
                return Integer.compare(an, bn);
            }
        });
        return out;
    }

    public TItemImplementCard mustGet(Long id) {
        if (id == null) {
            throw new CardRejectException("没指名是哪张卡");
        }
        TItemImplementCard card = gateway.findById(id);
        if (card == null) {
            throw new CardRejectException("这张卡查不着");
        }
        return card;
    }

    private void assertMutable(TItemImplementCard card) {
        if (card.getCurrentFlag() != null && card.getCurrentFlag() == TItemImplementCard.CURRENT_NO) {
            throw new CardRejectException("历史旧版只能翻检，字动不得");
        }
        int step = currentStep(card);
        if (step >= TItemImplementCard.STEP_FROZEN) {
            throw new CardRejectException("已冻住，添不进去也动不得，要改另起新版");
        }
        if (card.getStopFlag() != null && card.getStopFlag() == 1) {
            throw new CardRejectException("已就地叫停，不能再改");
        }
    }

    private void recordTrail(TItemImplementCard card, String action, Integer fromStep, Integer toStep,
                             Integer fromGrade, Integer toGrade, String who, String note) {
        TItemCardTrail t = new TItemCardTrail();
        t.setCardId(card.getId());
        t.setBillNo(card.getBillNo());
        t.setNodeNo(card.getNodeNo());
        t.setAction(action);
        t.setFromStep(fromStep);
        t.setToStep(toStep);
        t.setFromGrade(fromGrade);
        t.setToGrade(toGrade);
        t.setOperatorName(who);
        t.setTrailTime(gateway.now());
        t.setNote(note);
        t.setDelFlag(0);
        // 每回退一次，后补的记录排进下一趟，不与上一趟混作一处；
        // 退回这一笔本身就是新一趟的起头
        int round = 1;
        List<TItemCardTrail> past = gateway.findTrails(card.getId());
        if (past != null) {
            for (TItemCardTrail p : past) {
                if ("back".equals(p.getAction())) {
                    round++;
                }
            }
        }
        if ("back".equals(action)) {
            round++;
        }
        t.setRoundNo(round);
        gateway.appendTrail(t);
    }

    private String operator(String operator) {
        return StringUtils.isEmpty(operator) ? "Task" : operator;
    }

    private String yearOf(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.YEAR));
    }
}
