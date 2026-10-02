package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.service.itemcard.CensusView;
import com.fc.v2.service.itemcard.ViewerScope;

import java.math.BigDecimal;
import java.util.List;

/**
 * 实施清单配置卡 Service接口
 *
 * <p>旧的一套增删改查方法原样留着；新规矩（立卡、走步、折层、换版、划范围）
 * 以同名/新名方法并排再立，一律由领域服务那几条方法说了算，寻不到第二条绕过它的通道。</p>
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITItemImplementCardService {

    /** 按主键查询 */
    TItemImplementCard selectTItemImplementCardById(Long id);

    /** 按条件查询列表（分页由调用方统一处理；列表只读数，不现造卡、不覆写旧版次） */
    List<TItemImplementCard> selectTItemImplementCardList(Wrapper<TItemImplementCard> queryWrapper);

    /** 旧保存方法：原样留着（裸入库，不走新规矩），新卡请走 {@link #openCard} */
    int insertTItemImplementCard(TItemImplementCard record);

    /** 旧修改方法：原样留着（裸覆写），改在办卡请走走步/增减/改挂/换版那几个方法 */
    int updateTItemImplementCard(TItemImplementCard record);

    /** 批量删除：已冻住的卡整张撤不掉，跳过不计 */
    int deleteTItemImplementCardByIds(String ids);

    /** 按主键删除：已冻住的卡撤不掉 */
    int deleteTItemImplementCardById(Long id);

    // ========== 并排另立的新方法 ==========

    /** 立卡：系统发号、按层级验栏、折头回层、占名额；起头日在日后需 confirmFutureStart=true */
    TItemImplementCard openCard(TItemImplementCard input, String operator, boolean confirmFutureStart);

    /** 往前再走一步（待核对→已核对钉串→已冻住） */
    TItemImplementCard advanceCard(Long id, String operator);

    /** 未冻住可退回一档重来，旧趟留痕另存 */
    TItemImplementCard backCard(Long id, String operator);

    /** 就地叫停，腾出同事项名额 */
    TItemImplementCard stopCard(Long id, String operator);

    /** 年办件量增减，每回重折一层写回 */
    TItemImplementCard changeQty(Long id, BigDecimal qty, String operator);

    /** 改挂靠底册（只认代号），按新册三线重折 */
    TItemImplementCard rehangCard(Long id, String newSiteNo, String operator);

    /** 换版：旧版退出现行归历史、旧串不动，新版新立、走到已核对钉新串 */
    TItemImplementCard reviseCard(Long id, TItemImplementCard changes, String operator, boolean confirmFutureStart);

    /** 纸质簿旧卡迁入：缺步数/版次/串也接得住 */
    TItemImplementCard admitLegacy(TItemImplementCard legacy);

    /** 账号划范围后的现行卡列表 */
    List<TItemImplementCard> visibleCards(ViewerScope scope);

    /** 同卡号各版次翻检 */
    List<TItemImplementCard> historyOf(String billNo);

    /** 年底本级一屏：卡上数与名录数同一回算出 */
    CensusView census(ViewerScope scope, String yearNo);

    /** 按承办层级当场点出非写不可的栏 */
    List<String> requiredFields(Integer handleLevel);

    /** 三条折线折服务监督层级（权威口径） */
    Integer foldGrade(TItemDeptArch arch, BigDecimal qty);

    /** 校验串拼成何样（权威口径） */
    String buildChecksum(TItemImplementCard card);

    /** 当前走到哪一步，由已走完步数自数（权威口径） */
    int currentStep(TItemImplementCard card);
}
