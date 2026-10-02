package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.custom.item.CardReceipt;
import com.fc.v2.service.ITItemDeptArchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 承办部门底册 Controller
 *
 * @author fuce
 * @date 2026-10-01
 */
@Api(value = "承办部门底册")
@Controller
@RequestMapping("/ItemDeptArchController")
public class ItemDeptArchController extends BaseController {

    private final String prefix = "admin/itemDeptArch";

    @Autowired
    private ITItemDeptArchService itemDeptArchService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("item:itemDeptArch:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "承办部门底册集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("item:itemDeptArch:list")
    @ResponseBody
    public ResultTable list(TItemDeptArch record) {
        QueryWrapper<TItemDeptArch> queryWrapper = new QueryWrapper<TItemDeptArch>(record);
        queryWrapper.eq("del_flag", 0);
        startPage();
        List<TItemDeptArch> list = itemDeptArchService.selectTItemDeptArchList(queryWrapper);
        com.github.pagehelper.PageInfo<TItemDeptArch> page =
                new com.github.pagehelper.PageInfo<TItemDeptArch>(list);
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "承办部门底册立册", action = "add")
    @ApiOperation(value = "立册", notes = "代号系统发，塞进来的自拟号不存")
    @PostMapping("/add")
    @RequiresPermissions("item:itemDeptArch:add")
    @ResponseBody
    public AjaxResult add(TItemDeptArch record) {
        CardReceipt receipt = itemDeptArchService.insertTItemDeptArch(record);
        return receipt.isSuccess() ? retobject(0, receipt) : AjaxResult.error(receipt.getMessage()).put("data", receipt);
    }

    @Log(title = "承办部门底册撤并", action = "close")
    @ApiOperation(value = "撤并关口", notes = "当场关口，往后不收新卡，先前挂上去的照旧查得着")
    @PostMapping("/close")
    @RequiresPermissions("item:itemDeptArch:edit")
    @ResponseBody
    public AjaxResult close(Long id) {
        return toAjax(itemDeptArchService.closeTItemDeptArch(id));
    }
}
