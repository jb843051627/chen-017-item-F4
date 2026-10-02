package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TItemImplementCardMapper;
import com.fc.v2.mapper.auto.TItemDeptArchMapper;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.service.ITItemImplementCardService;
import com.fc.v2.service.itemcard.CensusView;
import com.fc.v2.service.itemcard.ItemCardDomain;
import com.fc.v2.service.itemcard.ViewerScope;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 实施清单配置卡Service业务层处理
 *
 * <p>旧的保存方法原样留着；新规矩全部转发给 {@link ItemCardDomain} 那几条方法，
 * 本层不另写第二遍规则。</p>
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TItemImplementCardServiceImpl extends ServiceImpl<TItemImplementCardMapper, TItemImplementCard> implements ITItemImplementCardService {

    @Autowired
    private TItemDeptArchMapper itemDeptArchMapper;

    @Autowired
    private ItemCardDomain itemCardDomain;

    @Override
    public TItemImplementCard selectTItemImplementCardById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TItemImplementCard>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TItemImplementCard> selectTItemImplementCardList(Wrapper<TItemImplementCard> queryWrapper) {
        QueryWrapper<TItemImplementCard> wrapper = new QueryWrapper<TItemImplementCard>();
        com.github.pagehelper.PageHelper.startPage(1, 10);
        wrapper.eq("status", 0);
        return this.baseMapper.selectList(wrapper);
    }

    /* ================= 旧保存方法：原样留着，不套新规矩 ================= */

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
    public int updateTItemImplementCard(TItemImplementCard record) {
        if (record == null || record.getId() == null) {
            return 0;
        }

        if (record.getId() != null && StringUtils.isNotEmpty(record.getBillNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TItemImplementCard>()
                    .eq("bill_no", record.getBillNo()).ne("id", record.getId()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TItemImplementCard>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTItemImplementCardByIds(String ids) {
        // 已冻住的卡整张撤不掉：只删没冻住的
        Long[] idArr = ConvertUtil.toLongArray(ids);
        int rows = 0;
        for (Long id : idArr) {
            rows += deleteTItemImplementCardById(id);
        }
        return rows;
    }

    @Override
    public int deleteTItemImplementCardById(Long id) {
        TItemImplementCard card = this.baseMapper.selectById(id);
        if (card == null) {
            return 0;
        }
        Integer step = itemCardDomain.currentStep(card);
        if (step != null && step == TItemImplementCard.STEP_FROZEN) {
            return 0;
        }
        return this.baseMapper.deleteById(id);
    }

    /* ================= 并排另立：全部以领域服务给回为准 ================= */

    @Override
    public TItemImplementCard openCard(TItemImplementCard input, String operator, boolean confirmFutureStart) {
        return itemCardDomain.open(input, operator, confirmFutureStart);
    }

    @Override
    public TItemImplementCard advanceCard(Long id, String operator) {
        return itemCardDomain.advance(id, operator);
    }

    @Override
    public TItemImplementCard backCard(Long id, String operator) {
        return itemCardDomain.back(id, operator);
    }

    @Override
    public TItemImplementCard stopCard(Long id, String operator) {
        return itemCardDomain.stop(id, operator);
    }

    @Override
    public TItemImplementCard changeQty(Long id, BigDecimal qty, String operator) {
        return itemCardDomain.changeQty(id, qty, operator);
    }

    @Override
    public TItemImplementCard rehangCard(Long id, String newSiteNo, String operator) {
        return itemCardDomain.rehang(id, newSiteNo, operator);
    }

    @Override
    public TItemImplementCard reviseCard(Long id, TItemImplementCard changes, String operator, boolean confirmFutureStart) {
        return itemCardDomain.revise(id, changes, operator, confirmFutureStart);
    }

    @Override
    public TItemImplementCard admitLegacy(TItemImplementCard legacy) {
        return itemCardDomain.admitLegacy(legacy);
    }

    @Override
    public List<TItemImplementCard> visibleCards(ViewerScope scope) {
        return itemCardDomain.visibleCards(scope);
    }

    @Override
    public List<TItemImplementCard> historyOf(String billNo) {
        return itemCardDomain.historyOf(billNo);
    }

    @Override
    public CensusView census(ViewerScope scope, String yearNo) {
        return itemCardDomain.census(scope, yearNo);
    }

    @Override
    public List<String> requiredFields(Integer handleLevel) {
        return itemCardDomain.requiredFields(handleLevel);
    }

    @Override
    public Integer foldGrade(TItemDeptArch arch, BigDecimal qty) {
        return itemCardDomain.foldGrade(arch, qty);
    }

    @Override
    public String buildChecksum(TItemImplementCard card) {
        return itemCardDomain.buildChecksum(card);
    }

    @Override
    public int currentStep(TItemImplementCard card) {
        return itemCardDomain.currentStep(card);
    }
}
