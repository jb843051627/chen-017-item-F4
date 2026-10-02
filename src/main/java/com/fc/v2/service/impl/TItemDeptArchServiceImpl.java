package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.mapper.auto.TItemDeptArchMapper;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.custom.item.CardReceipt;
import com.fc.v2.service.ITItemDeptArchService;
import com.fc.v2.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 承办部门底册Service业务层处理
 *
 * @author fuce
 * @date 2026-10-01
 */
@Service
public class TItemDeptArchServiceImpl extends ServiceImpl<TItemDeptArchMapper, TItemDeptArch> implements ITItemDeptArchService {

    /** 机构类别各有各的排法，只认这三样 */
    private static final List<String> SITE_TYPES =
            Arrays.asList("行政许可", "行政确认", "行政给付");

    @Override
    public TItemDeptArch selectBySiteNo(String siteNo) {
        if (StringUtils.isEmpty(siteNo)) {
            return null;
        }
        return this.baseMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                .eq("site_no", siteNo)
                .eq("del_flag", 0));
    }

    @Override
    public TItemDeptArch selectTItemDeptArchById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TItemDeptArch> selectTItemDeptArchList(Wrapper<TItemDeptArch> queryWrapper) {
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public CardReceipt insertTItemDeptArch(TItemDeptArch record) {
        List<String> missing = new ArrayList<String>();
        if (record == null) {
            return CardReceipt.reject(Collections.singletonList("site_name(部门全称)"), "空册立不起来");
        }
        if (StringUtils.isEmpty(record.getSiteName())) {
            missing.add("site_name(部门全称)");
        }
        // 机构类别：行政许可、行政确认、行政给付各有各的排法，别的不认
        if (StringUtils.isEmpty(record.getSiteType()) || !SITE_TYPES.contains(record.getSiteType())) {
            missing.add("site_type(机构类别)");
        }
        // 落在哪一级政务区划：省—市—县一路写下来，三段都得在
        if (StringUtils.isEmpty(record.getRoadName())
                || record.getRoadName().split("—", -1).length != 3) {
            missing.add("road_name(政务区划省—市—县)");
        }
        // 册里压的三条折线：低界、中界、高界，自低向高排齐
        if (record.getTh1Max() == null) {
            missing.add("th1_max(低界)");
        }
        if (record.getTh2Max() == null) {
            missing.add("th2_max(中界)");
        }
        if (record.getTh3Max() == null) {
            missing.add("th3_max(高界)");
        }
        if (missing.isEmpty()
                && (record.getTh1Max().compareTo(record.getTh2Max()) > 0
                || record.getTh2Max().compareTo(record.getTh3Max()) > 0)) {
            missing.add("th1_max/th2_max/th3_max(三条折线须自低向高)");
        }
        if (!missing.isEmpty()) {
            return CardReceipt.reject(missing, "册上这几栏不齐，册立不起来");
        }
        // 底册代号由系统发，谁往里塞自家拟的号，库里不存
        record.setSiteNo(nextSiteNo());
        record.setStatus(0);
        record.setDelFlag(0);
        this.baseMapper.insert(record);

        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(true);
        receipt.setMessage("册已立起");
        receipt.setCardId(record.getId());
        receipt.setBillNo(record.getSiteNo());
        return receipt;
    }

    @Override
    public int closeTItemDeptArch(Long id) {
        TItemDeptArch arch = selectTItemDeptArchById(id);
        if (arch == null) {
            return 0;
        }
        // 年内撤并掉的那一册当场关口，往后不收新卡；先前挂上去的照旧查得着
        TItemDeptArch upd = new TItemDeptArch();
        upd.setId(arch.getId());
        upd.setStatus(1);
        return this.baseMapper.updateById(upd);
    }

    /**
     * 发底册代号：ZW 起头两位流水，顺着库里最大的往后排。
     */
    private String nextSiteNo() {
        List<TItemDeptArch> all = this.baseMapper.selectList(new QueryWrapper<TItemDeptArch>()
                .select("site_no")
                .likeRight("site_no", "ZW"));
        int max = -1;
        for (TItemDeptArch arch : all) {
            String no = arch.getSiteNo();
            if (no != null && no.length() > 2) {
                try {
                    max = Math.max(max, Integer.parseInt(no.substring(2)));
                } catch (NumberFormatException ignore) {
                    // 不是 ZW+数字 的旧号，跳过
                }
            }
        }
        return String.format("ZW%02d", max + 1);
    }
}
