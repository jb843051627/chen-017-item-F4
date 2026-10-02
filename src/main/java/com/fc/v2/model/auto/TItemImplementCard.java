package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 实施清单配置卡对象 t_item_implement_card
 *
 * <p>一项事项一张卡，一张卡只挂一本册，一册只归一家部门。
 * 卡上哪几栏非写不可按承办层级当场点出；服务监督层级、校验串、
 * 现行档次数都只认服务层方法给回的值，屏上填的不作数。</p>
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_item_implement_card")
@ApiModel(value = "TItemImplementCard", description = "实施清单配置卡")
public class TItemImplementCard implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 进展：待核对（一步未走完） */
    public static final int STEP_PENDING = 0;
    /** 进展：已核对（走完第一步，校验串在这一档钉死） */
    public static final int STEP_CHECKED = 1;
    /** 进展：已冻住（走完两步，到了头） */
    public static final int STEP_FROZEN = 2;

    /** 承办层级：省定标准 */
    public static final int LEVEL_PROVINCE = 1;
    /** 承办层级：市实施 */
    public static final int LEVEL_CITY = 2;
    /** 承办层级：县延伸 */
    public static final int LEVEL_COUNTY = 3;

    /** 现行 */
    public static final int CURRENT_YES = 1;
    /** 历史旧版 */
    public static final int CURRENT_NO = 0;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 实施清单配置卡号（系统发，同一卡跨版次共用） */
    @TableField("bill_no")
    @ApiModelProperty(value = "实施清单配置卡号")
    private String billNo;

    /** 实施编码（系统发） */
    @TableField("impl_code")
    @ApiModelProperty(value = "实施编码")
    private String implCode;

    /** 本卡版次序（旧纸卡迁入可空，首立按1起） */
    @TableField("node_no")
    @ApiModelProperty(value = "本卡版次序")
    private Integer nodeNo;

    /** 所属承办部门底册主键 */
    @TableField("site_id")
    @ApiModelProperty(value = "所属承办部门底册主键")
    private Long siteId;

    /** 所属部门底册代号（只凭代号对册，名号相近不算对上） */
    @TableField("site_no")
    @ApiModelProperty(value = "所属部门底册代号")
    private String siteNo;

    /** 事项码（与底册代号配平认卡） */
    @TableField("item_code")
    @ApiModelProperty(value = "事项码")
    private String itemCode;

    /** 承办层级 1省定标准 2市实施 3县延伸 */
    @TableField("handle_level")
    @ApiModelProperty(value = "承办层级 1省定标准 2市实施 3县延伸")
    private Integer handleLevel;

    /** 省定：上位名录那一条的代号 */
    @TableField("upper_item_no")
    @ApiModelProperty(value = "省定:上位名录那一条的代号")
    private String upperItemNo;

    /** 省定：设定依据的条款号 */
    @TableField("upper_basis_clause")
    @ApiModelProperty(value = "省定:设定依据的条款号")
    private String upperBasisClause;

    /** 市实施：本市细化那几条 */
    @TableField("city_refine_text")
    @ApiModelProperty(value = "市实施:本市细化那几条")
    private String cityRefineText;

    /** 市实施：起头那一日 */
    @TableField("city_eff_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "市实施:起头那一日")
    private Date cityEffStart;

    /** 市实施：结尾那一日 */
    @TableField("city_eff_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "市实施:结尾那一日")
    private Date cityEffEnd;

    /** 县延伸：延伸到的窗口数 */
    @TableField("extend_window_count")
    @ApiModelProperty(value = "县延伸:延伸到的窗口数")
    private Integer extendWindowCount;

    /** 县延伸：收件点数目 */
    @TableField("extend_point_count")
    @ApiModelProperty(value = "县延伸:收件点数目")
    private Integer extendPointCount;

    /** 立卡时底册区划快照（省—市—县），区划改隶旧卡不跟着搬 */
    @TableField("region_path")
    @ApiModelProperty(value = "立卡时底册区划快照")
    private String regionPath;

    /** 跨市协办受托底册代号（只摆给受托那一家） */
    @TableField("entrust_site_no")
    @ApiModelProperty(value = "跨市协办受托底册代号")
    private String entrustSiteNo;

    /** 立卡年度（跨年同屏不重复计） */
    @TableField("year_no")
    @ApiModelProperty(value = "立卡年度")
    private String yearNo;

    /** 本卡申报年办件量(件/年) */
    @TableField("qty")
    @ApiModelProperty(value = "本卡申报年办件量(件/年)")
    private BigDecimal qty;

    /** 本卡承诺时限(工作日) */
    @TableField("fine_amt")
    @ApiModelProperty(value = "本卡承诺时限(工作日)")
    private BigDecimal fineAmt;

    /** 校验串（走到已核对才钉，只由系统写，钉过覆写不了） */
    @TableField("content")
    @ApiModelProperty(value = "校验串")
    private String content;

    /** 年办件量折到的服务监督层级（只由系统折，不留手填把手） */
    @TableField("grade_level")
    @ApiModelProperty(value = "年办件量折到的服务监督层级")
    private Integer gradeLevel;

    /** 已走完步数 0待核对 1已核对 2已冻住，当前档由系统照它自数 */
    @TableField("steps_done")
    @ApiModelProperty(value = "已走完步数")
    private Integer stepsDone;

    /** 进展镜像 0待核对 1已核对 2已冻住（旧纸卡可空） */
    @TableField("status")
    @ApiModelProperty(value = "进展 0待核对 1已核对 2已冻住")
    private Integer status;

    /** 现行标记 0历史旧版 1现行（日常只摆现行最新版） */
    @TableField("current_flag")
    @ApiModelProperty(value = "现行标记 0历史旧版 1现行")
    private Integer currentFlag;

    /** 就地叫停 0否 1是（叫停即腾出同事项名额） */
    @TableField("stop_flag")
    @ApiModelProperty(value = "就地叫停 0否 1是")
    private Integer stopFlag;

    /** 纸质簿迁入旧卡 0否 1是（缺步数/版次/串也接得住） */
    @TableField("legacy_flag")
    @ApiModelProperty(value = "纸质簿迁入旧卡 0否 1是")
    private Integer legacyFlag;

    /** 开卡人（一钉下随卡一生，重录多少遍都不换人） */
    @TableField("opener_name")
    @ApiModelProperty(value = "开卡人")
    private String openerName;

    /** 本趟经办人（办一次续一名，不覆盖开卡人） */
    @TableField("handler_name")
    @ApiModelProperty(value = "本趟经办人")
    private String handlerName;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getImplCode() {
        return implCode;
    }

    public void setImplCode(String implCode) {
        this.implCode = implCode;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public Integer getHandleLevel() {
        return handleLevel;
    }

    public void setHandleLevel(Integer handleLevel) {
        this.handleLevel = handleLevel;
    }

    public String getUpperItemNo() {
        return upperItemNo;
    }

    public void setUpperItemNo(String upperItemNo) {
        this.upperItemNo = upperItemNo;
    }

    public String getUpperBasisClause() {
        return upperBasisClause;
    }

    public void setUpperBasisClause(String upperBasisClause) {
        this.upperBasisClause = upperBasisClause;
    }

    public String getCityRefineText() {
        return cityRefineText;
    }

    public void setCityRefineText(String cityRefineText) {
        this.cityRefineText = cityRefineText;
    }

    public Date getCityEffStart() {
        return cityEffStart;
    }

    public void setCityEffStart(Date cityEffStart) {
        this.cityEffStart = cityEffStart;
    }

    public Date getCityEffEnd() {
        return cityEffEnd;
    }

    public void setCityEffEnd(Date cityEffEnd) {
        this.cityEffEnd = cityEffEnd;
    }

    public Integer getExtendWindowCount() {
        return extendWindowCount;
    }

    public void setExtendWindowCount(Integer extendWindowCount) {
        this.extendWindowCount = extendWindowCount;
    }

    public Integer getExtendPointCount() {
        return extendPointCount;
    }

    public void setExtendPointCount(Integer extendPointCount) {
        this.extendPointCount = extendPointCount;
    }

    public String getRegionPath() {
        return regionPath;
    }

    public void setRegionPath(String regionPath) {
        this.regionPath = regionPath;
    }

    public String getEntrustSiteNo() {
        return entrustSiteNo;
    }

    public void setEntrustSiteNo(String entrustSiteNo) {
        this.entrustSiteNo = entrustSiteNo;
    }

    public String getYearNo() {
        return yearNo;
    }

    public void setYearNo(String yearNo) {
        this.yearNo = yearNo;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public BigDecimal getFineAmt() {
        return fineAmt;
    }

    public void setFineAmt(BigDecimal fineAmt) {
        this.fineAmt = fineAmt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public Integer getStepsDone() {
        return stepsDone;
    }

    public void setStepsDone(Integer stepsDone) {
        this.stepsDone = stepsDone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCurrentFlag() {
        return currentFlag;
    }

    public void setCurrentFlag(Integer currentFlag) {
        this.currentFlag = currentFlag;
    }

    public Integer getStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(Integer stopFlag) {
        this.stopFlag = stopFlag;
    }

    public Integer getLegacyFlag() {
        return legacyFlag;
    }

    public void setLegacyFlag(Integer legacyFlag) {
        this.legacyFlag = legacyFlag;
    }

    public String getOpenerName() {
        return openerName;
    }

    public void setOpenerName(String openerName) {
        this.openerName = openerName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
