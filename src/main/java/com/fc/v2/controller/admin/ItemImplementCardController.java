package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.service.ITItemImplementCardService;
import com.fc.v2.service.itemcard.CardRejectException;
import com.fc.v2.service.itemcard.CensusView;
import com.fc.v2.service.itemcard.ViewerScope;
import com.fc.v2.shiro.util.ShiroUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 实施清单配置卡 Controller
 *
 * <p>控制器只做转发：能不能立卡、当前算哪一步、还能不能走/退，说了算的只有
 * 服务层那几条方法。屏上摆的层级、校验串、名录数都以服务层给回为准。
 * 旧的 add/edit/remove 保存端点原样留着，新规矩的端点与它们并排另立。</p>
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

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("item:itemImplementCard:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    /* ================= 旧端点：原样留着 ================= */

    @Log(title = "实施清单配置卡集合查询", action = "list")
    @ApiOperation(value = "分页查询(旧)", notes = "分页查询(旧)")
    @GetMapping("/list")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public ResultTable list(TItemImplementCard record) {
        QueryWrapper<TItemImplementCard> queryWrapper = new QueryWrapper<TItemImplementCard>();
        startPage();
        com.github.pagehelper.PageInfo<TItemImplementCard> page =
                new com.github.pagehelper.PageInfo<TItemImplementCard>(itemImplementCardService.selectTItemImplementCardList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "实施清单配置卡新增", action = "add")
    @ApiOperation(value = "新增(旧保存方法,原样留着)", notes = "新增(旧保存方法,原样留着)")
    @PostMapping("/add")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult add(TItemImplementCard record) {
        return toAjax(itemImplementCardService.insertTItemImplementCard(record));
    }

    @Log(title = "实施清单配置卡修改", action = "edit")
    @ApiOperation(value = "修改保存(旧保存方法,原样留着)", notes = "修改保存(旧保存方法,原样留着)")
    @PostMapping("/edit")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult editSave(TItemImplementCard record) {
        return toAjax(itemImplementCardService.updateTItemImplementCard(record));
    }

    @Log(title = "实施清单配置卡删除", action = "remove")
    @ApiOperation(value = "删除(旧,已冻住撤不掉)", notes = "删除(旧,已冻住撤不掉)")
    @DeleteMapping("/remove")
    @RequiresPermissions("item:itemImplementCard:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(itemImplementCardService.deleteTItemImplementCardByIds(ids));
    }

    /* ================= 并排另立：新规矩的入口 ================= */

    @Log(title = "实施清单配置卡划范围查询", action = "visibleList")
    @ApiOperation(value = "按账号范围查现行卡", notes = "县看本县/市看所辖县/省看全辖/受托只看受托一家")
    @GetMapping("/visibleList")
    @RequiresPermissions("item:itemImplementCard:list")
    @ResponseBody
    public AjaxResult visibleList(@RequestParam(defaultValue = "1") Integer scopeGrade,
                                  String regionPath, String entrustSiteNo, String yearNo) {
        ViewerScope scope = buildScope(scopeGrade, regionPath, entrustSiteNo);
        return AjaxResult.successData(200, itemImplementCardService.visibleCards(scope));
    }

    @ApiOperation(value = "按承办层级点必填栏", notes = "哪几栏非写不可由服务层当场点出")
    @GetMapping("/requiredFields")
    @RequiresPermissions("item:itemImplementCard:view")
    @ResponseBody
    public AjaxResult requiredFields(Integer handleLevel) {
        return AjaxResult.successData(200, itemImplementCardService.requiredFields(handleLevel));
    }

    @Log(title = "实施清单配置卡立卡", action = "open")
    @ApiOperation(value = "立卡", notes = "系统发号/验栏/折层/占名额；起头日在日后需confirm=1")
    @PostMapping("/open")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult open(TItemImplementCard record,
                           @RequestParam(defaultValue = "0") Integer confirm) {
        try {
            TItemImplementCard card = itemImplementCardService.openCard(record, currentOperator(), confirm != null && confirm == 1);
            return AjaxResult.success("卡立起来了").put("data", card);
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "实施清单配置卡往前再走一步", action = "advance")
    @ApiOperation(value = "往前再走一步", notes = "待核对→已核对(钉串)→已冻住")
    @PostMapping("/advance")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult advance(Long id) {
        try {
            return AjaxResult.success("走了一步").put("data", itemImplementCardService.advanceCard(id, currentOperator()));
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "实施清单配置卡退回一步", action = "back")
    @ApiOperation(value = "退回一步", notes = "未冻住可退,旧趟留痕另存")
    @PostMapping("/back")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult back(Long id) {
        try {
            return AjaxResult.success("退回一档").put("data", itemImplementCardService.backCard(id, currentOperator()));
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "实施清单配置卡就地叫停", action = "stop")
    @ApiOperation(value = "就地叫停", notes = "叫停即腾出同事项名额")
    @PostMapping("/stop")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult stop(Long id) {
        try {
            return AjaxResult.success("已叫停，名额空出").put("data", itemImplementCardService.stopCard(id, currentOperator()));
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "实施清单配置卡年办件量增减", action = "changeQty")
    @ApiOperation(value = "年办件量增减", notes = "每回重新折层写回")
    @PostMapping("/changeQty")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult changeQty(Long id, BigDecimal qty) {
        try {
            return AjaxResult.success("办件量已改，层级重折").put("data",
                    itemImplementCardService.changeQty(id, qty, currentOperator()));
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "实施清单配置卡改挂靠", action = "rehang")
    @ApiOperation(value = "改挂靠底册", notes = "只认代号不认名号,按新册三线重折")
    @PostMapping("/rehang")
    @RequiresPermissions("item:itemImplementCard:edit")
    @ResponseBody
    public AjaxResult rehang(Long id, String newSiteNo) {
        try {
            return AjaxResult.success("已改挂新册").put("data",
                    itemImplementCardService.rehangCard(id, newSiteNo, currentOperator()));
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "实施清单配置卡换版", action = "revise")
    @ApiOperation(value = "换版", notes = "旧版退现行归历史保旧串,新版新立新串")
    @PostMapping("/revise")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult revise(Long id, TItemImplementCard changes,
                             @RequestParam(defaultValue = "0") Integer confirm) {
        try {
            TItemImplementCard neo = itemImplementCardService.reviseCard(id, changes, currentOperator(),
                    confirm != null && confirm == 1);
            return AjaxResult.success("新版已立，旧版归历史").put("data", neo);
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @Log(title = "纸质簿旧卡迁入", action = "admitLegacy")
    @ApiOperation(value = "旧纸卡迁入", notes = "缺步数/版次/串也接得住,不抛错")
    @PostMapping("/admitLegacy")
    @RequiresPermissions("item:itemImplementCard:add")
    @ResponseBody
    public AjaxResult admitLegacy(TItemImplementCard legacy) {
        try {
            return AjaxResult.success("旧卡已接入").put("data", itemImplementCardService.admitLegacy(legacy));
        } catch (CardRejectException e) {
            return reject(e);
        }
    }

    @ApiOperation(value = "历史版次翻检", notes = "同卡号各版按版次序排开")
    @GetMapping("/history")
    @RequiresPermissions("item:itemImplementCard:view")
    @ResponseBody
    public AjaxResult history(String billNo) {
        return AjaxResult.successData(200, itemImplementCardService.historyOf(billNo));
    }

    @ApiOperation(value = "年底本级一屏", notes = "卡上数与名录数同一回算出,空级不摆空行")
    @GetMapping("/census")
    @RequiresPermissions("item:itemImplementCard:view")
    @ResponseBody
    public AjaxResult census(@RequestParam(defaultValue = "1") Integer scopeGrade,
                             String regionPath, String entrustSiteNo, String yearNo) {
        ViewerScope scope = buildScope(scopeGrade, regionPath, entrustSiteNo);
        CensusView view = itemImplementCardService.census(scope, yearNo);
        return AjaxResult.successData(200, view)
                .put("year", yearNo == null ? String.format("%tY", new Date()) : yearNo);
    }

    /**
     * 账号划范围：摆出来的范围由账号权限定，自己那一级以外不许同屏并列。
     * scopeGrade 1省办 2市局 3县局 4跨市协办受托人。
     */
    private ViewerScope buildScope(Integer scopeGrade, String regionPath, String entrustSiteNo) {
        int grade = scopeGrade == null ? ViewerScope.GRADE_PROVINCE : scopeGrade;
        switch (grade) {
            case ViewerScope.GRADE_CITY:
                return ViewerScope.city(regionPath);
            case ViewerScope.GRADE_COUNTY:
                return ViewerScope.county(regionPath);
            case ViewerScope.GRADE_AGENT:
                return ViewerScope.agent(entrustSiteNo);
            case ViewerScope.GRADE_PROVINCE:
            default:
                return ViewerScope.province(regionPath);
        }
    }

    /** 拒收回执：必须指名空在哪一栏，needConfirm 让屏上先问一句。 */
    private AjaxResult reject(CardRejectException e) {
        AjaxResult r = AjaxResult.error(400, e.getMessage());
        r.put("fields", e.getEmptyFields());
        r.put("needConfirm", e.isNeedConfirm());
        return r;
    }

    private String currentOperator() {
        try {
            return ShiroUtils.getLoginName();
        } catch (RuntimeException e) {
            return "Task";
        }
    }
}
