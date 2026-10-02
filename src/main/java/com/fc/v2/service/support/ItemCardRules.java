package com.fc.v2.service.support;

import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.custom.item.CardSubmitCmd;
import com.fc.v2.util.MD5Util;
import com.fc.v2.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 实施清单配置卡纯规则（不碰库，屏上摆的字一律以这里给回的为准）。
 * <p>
 * 三句铁律：一项事项一张卡，一张卡只挂一本册，一册只归一家部门。
 * 卡起得起不来，看挂得对不对；层级算得准不准，看折的是哪一套。
 *
 * @author fuce
 * @date 2026-10-01
 */
public class ItemCardRules {

    private ItemCardRules() {
    }

    /** 承办层级：省定标准 */
    public static final int LEVEL_PROVINCE_STD = 1;
    /** 承办层级：市实施 */
    public static final int LEVEL_CITY_IMPL = 2;
    /** 承办层级：县延伸 */
    public static final int LEVEL_COUNTY_EXT = 3;

    /** 步数/档次：待核对 */
    public static final int STEP_PENDING = 0;
    /** 步数/档次：已核对 */
    public static final int STEP_CHECKED = 1;
    /** 步数/档次：已冻住 */
    public static final int STEP_FROZEN = 2;

    /** 版次落点：现行 */
    public static final int VER_CURRENT = 1;
    /** 版次落点：历史 */
    public static final int VER_HISTORY = 0;

    /** 栏位中文名（回执指名用） */
    public static final String F_SITE_NO = "site_no(底册代号)";
    public static final String F_ITEM_CODE = "item_code(事项码)";
    public static final String F_UNDERTAKE_LEVEL = "undertake_level(承办层级)";
    public static final String F_UPPER_ITEM_CODE = "upper_item_code(上位名录条目代号)";
    public static final String F_BASIS_CLAUSE = "basis_clause(设定依据条款号)";
    public static final String F_CITY_ITEMS = "city_items(本市细化条目)";
    public static final String F_START_DATE = "start_date(起头之日)";
    public static final String F_END_DATE = "end_date(结尾之日)";
    public static final String F_WINDOWS = "windows(延伸窗口)";
    public static final String F_RECV_POINTS = "recv_points(收件点数目)";
    public static final String F_QTY = "qty(年办件量)";
    public static final String F_FINE_AMT = "fine_amt(承诺时限)";

    /**
     * 哪几栏非写不可，按承办层级当场点出来。
     *
     * @param undertakeLevel 承办层级
     * @return 非写不可的栏位名（指名到栏）
     */
    public static List<String> requiredFields(Integer undertakeLevel) {
        List<String> fields = new ArrayList<String>();
        fields.add(F_SITE_NO);
        fields.add(F_ITEM_CODE);
        fields.add(F_QTY);
        fields.add(F_FINE_AMT);
        if (undertakeLevel != null) {
            if (undertakeLevel == LEVEL_PROVINCE_STD) {
                // 走省定标准的：上位名录那一条的代号与设定依据的条款号
                fields.add(F_UPPER_ITEM_CODE);
                fields.add(F_BASIS_CLAUSE);
            } else if (undertakeLevel == LEVEL_CITY_IMPL) {
                // 走市实施的：本市细化那几条与起头到结尾那两日
                fields.add(F_CITY_ITEMS);
                fields.add(F_START_DATE);
                fields.add(F_END_DATE);
            } else if (undertakeLevel == LEVEL_COUNTY_EXT) {
                // 走县延伸的：延伸到哪些窗口与收件点的数目
                fields.add(F_WINDOWS);
                fields.add(F_RECV_POINTS);
            }
        }
        return fields;
    }

    /**
     * 立卡校验：空着没选、选了省定却空着上位代号、办件量零或负、承诺时限为负，
     * 一律指名到栏收进回执；回执不空，卡不进库。
     *
     * @param cmd 立卡递法
     * @return 指名空栏的清单（空清单=这关过了）
     */
    public static List<String> missingFields(CardSubmitCmd cmd) {
        List<String> missing = new ArrayList<String>();
        if (cmd == null) {
            missing.add(F_UNDERTAKE_LEVEL);
            return missing;
        }
        if (StringUtils.isEmpty(cmd.getSiteNo())) {
            missing.add(F_SITE_NO);
        }
        if (StringUtils.isEmpty(cmd.getItemCode())) {
            missing.add(F_ITEM_CODE);
        }
        Integer level = cmd.getUndertakeLevel();
        if (level == null) {
            // 层级空着没选，卡不进库
            missing.add(F_UNDERTAKE_LEVEL);
        } else if (level == LEVEL_PROVINCE_STD) {
            if (StringUtils.isEmpty(cmd.getUpperItemCode())) {
                missing.add(F_UPPER_ITEM_CODE);
            }
            if (StringUtils.isEmpty(cmd.getBasisClause())) {
                missing.add(F_BASIS_CLAUSE);
            }
        } else if (level == LEVEL_CITY_IMPL) {
            if (StringUtils.isEmpty(cmd.getCityItems())) {
                missing.add(F_CITY_ITEMS);
            }
            if (cmd.getStartDate() == null) {
                missing.add(F_START_DATE);
            }
            if (cmd.getEndDate() == null) {
                missing.add(F_END_DATE);
            }
        } else if (level == LEVEL_COUNTY_EXT) {
            if (StringUtils.isEmpty(cmd.getWindows())) {
                missing.add(F_WINDOWS);
            }
            if (cmd.getRecvPoints() == null) {
                missing.add(F_RECV_POINTS);
            }
        } else {
            // 三取一以外的层级不认
            missing.add(F_UNDERTAKE_LEVEL);
        }
        // 年办件量填零、填负数，卡不进库
        if (cmd.getQty() == null || cmd.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            missing.add(F_QTY);
        }
        // 承诺时限填成负数，同样不进
        if (cmd.getFineAmt() == null || cmd.getFineAmt().compareTo(BigDecimal.ZERO) < 0) {
            missing.add(F_FINE_AMT);
        }
        return missing;
    }

    /**
     * 起头那一日是不是写成了往后的日子（是的话得先问一句才收）。
     */
    public static boolean isFutureStart(Date startDate, Date today) {
        if (startDate == null || today == null) {
            return false;
        }
        return dayStart(startDate).after(dayStart(today));
    }

    /**
     * 服务监督层级：三条折线自低向高排，拿年办件量往上靠，
     * 头一个没够着的界就是它该停的那一层；恰好持平也算够着，再往上靠一界；
     * 三线全够着，落到最高那一层。不给人报，也不给人改。
     *
     * @param qty 年办件量（旧卡空着就折不出，返回 null，不抛错）
     * @param th1 低界
     * @param th2 中界
     * @param th3 高界
     * @return 折到的层级 1..(界数+1)
     */
    public static Integer computeGradeLevel(BigDecimal qty, BigDecimal th1, BigDecimal th2, BigDecimal th3) {
        if (qty == null) {
            return null;
        }
        List<BigDecimal> bounds = new ArrayList<BigDecimal>();
        if (th1 != null) {
            bounds.add(th1);
        }
        if (th2 != null) {
            bounds.add(th2);
        }
        if (th3 != null) {
            bounds.add(th3);
        }
        if (bounds.isEmpty()) {
            return null;
        }
        // 三条折线自低向高排（册上压歪了也先摆正再靠）
        Collections.sort(bounds);
        int level = 1;
        for (BigDecimal bound : bounds) {
            if (qty.compareTo(bound) >= 0) {
                // 够着这一界（持平中界的那一张也由此往上取一层）
                level++;
            } else {
                // 头一个没够着的界，就停在这一层
                break;
            }
        }
        return level;
    }

    /**
     * 这张眼下处在哪一档：由系统按已经走完的步数自己数出来，
     * 屏上填的档次、经办人记的档次都不作数。
     * 早年纸质簿搬进来的旧卡步数空着，就拿留下的档次接着数；
     * 都空着就当刚立，绝不把仍在走的旧卡当成已经结束的。
     */
    public static int deriveStep(TItemImplementCard card) {
        if (card == null) {
            return STEP_PENDING;
        }
        if (card.getStepCount() != null) {
            return card.getStepCount();
        }
        if (card.getStatus() != null) {
            return card.getStatus();
        }
        return STEP_PENDING;
    }

    /**
     * 步数数出来的档次（0待核对 1已核对 2已冻住）。
     */
    public static int statusOfStep(int step) {
        if (step <= STEP_PENDING) {
            return STEP_PENDING;
        }
        if (step >= STEP_FROZEN) {
            return STEP_FROZEN;
        }
        return STEP_CHECKED;
    }

    /**
     * 能不能往前再走一步：一步没走完后头不给走，到冻住这张便到了头。
     */
    public static boolean canAdvance(TItemImplementCard card) {
        return deriveStep(card) < STEP_FROZEN;
    }

    /**
     * 能不能退一档重来：卡未到冻住，前一步的核对能退一档。
     */
    public static boolean canRollback(TItemImplementCard card) {
        return deriveStep(card) == STEP_CHECKED;
    }

    /**
     * 是不是已冻住（到了头：添不进去、字也动不得、整张撤不掉）。
     */
    public static boolean isFrozen(TItemImplementCard card) {
        return deriveStep(card) >= STEP_FROZEN;
    }

    /**
     * 是不是还在办（没冻住、也没就地叫停）。同一部门名下同一事项，
     * 同一时刻只容一张在办的；旧卡步数空着也算在走，不算已经结束。
     */
    public static boolean isInFlight(TItemImplementCard card) {
        if (card == null) {
            return false;
        }
        if (card.getDelFlag() != null && card.getDelFlag() == 1) {
            return false;
        }
        return !isFrozen(card);
    }

    /**
     * 拼校验串：卡号、底册代号、事项码、版次序、办件量、承诺时限、
     * 折到的层级、第几趟，一串钉一串。换一次版次跟着新版重算，
     * 新版那行用新串、旧版那行仍是旧串；退档重走那一趟也另算，
     * 两版共用一串即算错。
     */
    public static String buildChecksum(String billNo, String siteNo, String itemCode, Integer nodeNo,
                                       BigDecimal qty, BigDecimal fineAmt, Integer gradeLevel, Integer runNo) {
        String raw = nvl(billNo) + '|' + nvl(siteNo) + '|' + nvl(itemCode) + '|' + nvl(nodeNo)
                + '|' + nvl(qty) + '|' + nvl(fineAmt) + '|' + nvl(gradeLevel) + '|' + nvl(runNo);
        return MD5Util.encode(raw);
    }

    private static String nvl(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    private static Date dayStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
