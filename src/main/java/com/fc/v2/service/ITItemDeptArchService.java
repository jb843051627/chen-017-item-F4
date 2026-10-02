package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.custom.item.CardReceipt;

import java.util.List;

/**
 * 承办部门底册 Service接口。
 * 一册只归一家部门；底册代号由系统发，谁往里塞自家拟的号库里不存；
 * 年内撤并掉的那一册当场关口，往后不收新卡，先前挂上去的照旧查得着。
 *
 * @author fuce
 * @date 2026-10-01
 */
public interface ITItemDeptArchService {

    /** 只凭代号对册（拿名号相近认门，不算对上） */
    TItemDeptArch selectBySiteNo(String siteNo);

    /** 按主键查询 */
    TItemDeptArch selectTItemDeptArchById(Long id);

    /** 按条件查询列表 */
    List<TItemDeptArch> selectTItemDeptArchList(Wrapper<TItemDeptArch> queryWrapper);

    /**
     * 立册：代号系统发（塞进来的自拟号不存），机构类别各有各的排法，
     * 政务区划省—市—县一路写下来，三条折线低中高界压齐。
     */
    CardReceipt insertTItemDeptArch(TItemDeptArch record);

    /** 撤并关口：当场关口，往后不收新卡 */
    int closeTItemDeptArch(Long id);
}
