package com.fc.v2.service.support;

import com.fc.v2.model.auto.TSysDepartment;
import com.fc.v2.model.auto.TSysUser;
import com.fc.v2.model.custom.item.ItemScope;
import com.fc.v2.service.ITSysDepartmentService;
import com.fc.v2.shiro.util.ShiroUtils;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 账号一进系统先划范围：摆出来的卡不许越出账号权限划定的圈子。
 * 县局看本县那一本册，市局看本市所辖那几个县，省办看全辖，
 * 跨市协办只把受托那一家摆给受托人。
 * <p>
 * 范围只在这里定，是全系统唯一插口；生产接入账号区划属性后在此一处改判。
 *
 * @author fuce
 * @date 2026-10-01
 */
@Component
public class ItemScopeResolver {

    @Autowired
    private ITSysDepartmentService sysDepartmentService;

    /**
     * 按当前登录账号解出摆卡范围。
     */
    public ItemScope resolve() {
        TSysUser user = ShiroUtils.getUser();
        if (user == null) {
            // 没有登录上下文的内部调用，按省办全辖放
            return ItemScope.province();
        }
        if (StringUtils.isNotEmpty(user.getUsername()) && "admin".equals(user.getUsername())) {
            return ItemScope.province();
        }
        String deptName = currentDeptName(user);
        if (StringUtils.isEmpty(deptName)) {
            return ItemScope.province();
        }
        // 部门名里先撞见县/区就是县局，再撞见市就是市局，再撞见省就是省办
        String county = pickRegion(deptName, "县");
        if (county == null) {
            county = pickRegion(deptName, "区");
        }
        if (county != null) {
            return ItemScope.county(county);
        }
        String city = pickRegion(deptName, "市");
        if (city != null) {
            return ItemScope.city(city);
        }
        return ItemScope.province();
    }

    private String currentDeptName(TSysUser user) {
        if (user.getDepId() == null) {
            return null;
        }
        TSysDepartment dept = sysDepartmentService.selectTSysDepartmentById(user.getDepId());
        return dept == null ? null : dept.getDeptName();
    }

    /**
     * 从部门名里摘区划名：缀字往前最多取三个字，如「青屏县民政局」摘出「青屏县」。
     */
    private String pickRegion(String deptName, String suffix) {
        int idx = deptName.indexOf(suffix);
        if (idx < 0) {
            return null;
        }
        int from = Math.max(0, idx - 2);
        return deptName.substring(from, idx + 1);
    }
}
