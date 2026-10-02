package com.fc.v2.controller.admin;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.model.custom.item.CardReceipt;
import com.fc.v2.model.custom.item.CardSubmitCmd;
import com.fc.v2.model.custom.item.ItemScope;
import com.fc.v2.model.custom.item.SamePassCount;
import com.fc.v2.service.ITItemImplementCardService;
import com.fc.v2.service.support.ItemScopeResolver;
import com.fc.v2.shiro.util.ShiroUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实施清单配置卡 Controller。
 * 屏上摆的字一律以服务层给回的为准：能不能立、当前哪一步、
 * 还能不能往前走一步或退回一步，旁处寻不到第二条能绕过它的通道。
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "实施清单配置卡")
@Controller
@RequestMapping("/ItemImplementCardController")
public class ItemImplementCardController extends BaseController {

    private final String prefix = "admin/itemImplementCard";

    @Autowired
    private ITItemImplementCardService itemImplementCardService;

    @Autowired
    private ItemScopeResolver itemScopeResolver;

    /**
     * 卡上凭据日期按 yyyy-MM-dd 收
     */
    @InitBinder
    public void initItemBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("item:itemImplementCard:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "实施清单配置卡集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public ResultTable list(TItemImplementCard record) {
        // 日常摆出来的只许当下最新那一版，范围由账号权限定
        ItemScope scope = itemScopeResolver.resolve();
        List<TItemImplementCard> list = itemImplementCardService.selectCurrentCardList(scope, record);
        // 服务层一次问出范围再分页，不让分页插件截错查询
        com.fc.v2.model.custom.Tablepar tablepar = com.fc.v2.model.custom.Tablepar.buildPageRequest();
        int pageNum = tablepar.getPage();
        int pageSize = tablepar.getLimit();
        if (pageNum > 0 && pageSize > 0) {
            int from = Math.min((pageNum - 1) * pageSize, list.size());
            int to = Math.min(from + pageSize, list.size());
            return pageTable(list.subList(from, to), list.size());
        }
        return pageTable(list, list.size());
    }

    @Log(title = "实施清单配置卡新增", action = "add")
    @ApiOperation(value = "新增", notes = "新增")
    @PostMapping("/add")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult add(TItemImplementCard record) {
        // 旧保存方法的入口，原样留着
        return toAjax(itemImplementCardService.insertTItemImplementCard(record));
    }

    @Log(title = "实施清单配置卡立卡", action = "submit")
    @ApiOperation(value = "立卡(新递法)", notes = "系统发号、只凭代号对册、缺栏指名")
    @PostMapping("/submit")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult submit(CardSubmitCmd cmd) {
        cmd.setOperator(ShiroUtils.getLoginName());
        CardReceipt receipt = itemImplementCardService.insertTItemImplementCard(cmd);
        return receipt.isSuccess() ? retobject(0, receipt) : AjaxResult.error(receipt.getMessage()).put("data", receipt);
    }

    @ApiOperation(value = "立卡预检", notes = "能不能立先问一句，不落库")
    @PostMapping("/precheck")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult precheck(CardSubmitCmd cmd) {
        CardReceipt receipt = itemImplementCardService.precheckSubmit(cmd);
        return receipt.isSuccess() ? retobject(0, receipt) : AjaxResult.error(receipt.getMessage()).put("data", receipt);
    }

    @ApiOperation(value = "非写不可的栏", notes = "按承办层级当场点出来")
    @GetMapping("/requiredFields")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public AjaxResult requiredFields(Integer undertakeLevel) {
        return retobject(0, itemImplementCardService.requiredFields(undertakeLevel));
    }

    @ApiOperation(value = "当前哪一步", notes = "系统按已走完的步数自己数出来")
    @GetMapping("/stage")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public AjaxResult stage(Long id) {
        Map<String, Object> stage = new HashMap<String, Object>();
        stage.put("stage", itemImplementCardService.currentStage(id));
        stage.put("canAdvance", itemImplementCardService.canAdvance(id));
        stage.put("canRollback", itemImplementCardService.canRollback(id));
        return retobject(0, stage);
    }

    @Log(title = "实施清单配置卡前进一步", action = "advance")
    @ApiOperation(value = "往前走一步", notes = "待核对→已核对(钉校验串)→已冻住")
    @PostMapping("/advance")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult advance(Long id) {
        CardReceipt receipt = itemImplementCardService.advance(id, ShiroUtils.getLoginName());
        return receipt.isSuccess() ? retobject(0, receipt) : AjaxResult.error(receipt.getMessage()).put("data", receipt);
    }

    @Log(title = "实施清单配置卡退一档", action = "rollback")
    @ApiOperation(value = "退一档重来", notes = "已核对退回待核对，上一趟记录留存")
    @PostMapping("/rollback")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult rollback(Long id) {
        CardReceipt receipt = itemImplementCardService.rollback(id, ShiroUtils.getLoginName());
        return receipt.isSuccess() ? retobject(0, receipt) : AjaxResult.error(receipt.getMessage()).put("data", receipt);
    }

    @Log(title = "实施清单配置卡叫停", action = "halt")
    @ApiOperation(value = "就地叫停", notes = "未冻住的叫停后名额空出来")
    @PostMapping("/halt")
    @RequiresPermissions("item:itemImplementCard:remove")
    @ResponseBody
    public AjaxResult halt(Long id) {
        CardReceipt receipt = itemImplementCardService.halt(id, ShiroUtils.getLoginName());
        return receipt.isSuccess() ? retobject(0, receipt) : AjaxResult.error(receipt.getMessage()).put("data", receipt);
    }

    @ApiOperation(value = "旧版翻检", notes = "同一底册代号＋事项码归到历史一层的各版次")
    @GetMapping("/history")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public ResultTable history(String siteNo, String itemCode) {
        return dataTable(itemImplementCardService.selectHistoryVersions(siteNo, itemCode));
    }

    @ApiOperation(value = "过口记录", notes = "各趟分开排")
    @GetMapping("/flow")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public ResultTable flow(Long cardId) {
        return dataTable(itemImplementCardService.selectFlowList(cardId));
    }

    @ApiOperation(value = "同屏两数", notes = "本卡在册上占的数与本级名录上的数，同一回算出来")
    @GetMapping("/countView")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public AjaxResult countView(String siteNo) {
        ItemScope scope = itemScopeResolver.resolve();
        SamePassCount count = itemImplementCardService.countSamePass(scope, siteNo);
        return retobject(0, count);
    }

    @Log(title = "实施清单配置卡修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult editSave(TItemImplementCard record) {
        return toAjax(itemImplementCardService.updateTItemImplementCard(record));
    }

    @Log(title = "实施清单配置卡删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("item:itemImplementCard:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(itemImplementCardService.deleteTItemImplementCardByIds(ids));
    }
}
