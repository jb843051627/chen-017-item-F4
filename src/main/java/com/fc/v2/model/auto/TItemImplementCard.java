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
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_item_implement_card")
@ApiModel(value = "TItemImplementCard", description = "实施清单配置卡")
public class TItemImplementCard implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 实施清单配置卡号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "实施清单配置卡号")
    private String billNo;

    /** 本卡版次序 */
    @TableField("node_no")
    @ApiModelProperty(value = "本卡版次序")
    private Integer nodeNo;

    /** 所属承办部门底册 */
    @TableField("site_id")
    @ApiModelProperty(value = "所属承办部门底册")
    private Integer siteId;

    /** 所属部门底册代号 */
    @TableField("site_no")
    @ApiModelProperty(value = "所属部门底册代号")
    private String siteNo;

    /** 事项码(与底册代号配平认卡) */
    @TableField("item_code")
    @ApiModelProperty(value = "事项码(与底册代号配平认卡)")
    private String itemCode;

    /** 实施编码(系统发) */
    @TableField("impl_code")
    @ApiModelProperty(value = "实施编码(系统发)")
    private String implCode;

    /** 承办层级 1省定标准 2市实施 3县延伸 */
    @TableField("undertake_level")
    @ApiModelProperty(value = "承办层级 1省定标准 2市实施 3县延伸")
    private Integer undertakeLevel;

    /** 上位名录条目代号(省定标准凭据) */
    @TableField("upper_item_code")
    @ApiModelProperty(value = "上位名录条目代号(省定标准凭据)")
    private String upperItemCode;

    /** 设定依据条款号(省定标准凭据) */
    @TableField("basis_clause")
    @ApiModelProperty(value = "设定依据条款号(省定标准凭据)")
    private String basisClause;

    /** 本市细化条目(市实施凭据) */
    @TableField("city_items")
    @ApiModelProperty(value = "本市细化条目(市实施凭据)")
    private String cityItems;

    /** 起头之日(市实施凭据) */
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "起头之日(市实施凭据)")
    private Date startDate;

    /** 结尾之日(市实施凭据) */
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(value = "结尾之日(市实施凭据)")
    private Date endDate;

    /** 延伸到的窗口(县延伸凭据) */
    @TableField("windows")
    @ApiModelProperty(value = "延伸到的窗口(县延伸凭据)")
    private String windows;

    /** 收件点数目(县延伸凭据) */
    @TableField("recv_points")
    @ApiModelProperty(value = "收件点数目(县延伸凭据)")
    private Integer recvPoints;

    /** 本卡申报年办件量(件/年) */
    @TableField("qty")
    @ApiModelProperty(value = "本卡申报年办件量(件/年)")
    private BigDecimal qty;

    /** 本卡承诺时限(工作日) */
    @TableField("fine_amt")
    @ApiModelProperty(value = "本卡承诺时限(工作日)")
    private BigDecimal fineAmt;

    /** 校验串（随版次序走） */
    @TableField("content")
    @ApiModelProperty(value = "校验串（随版次序走）")
    private String content;

    /** 年办件量折到的服务监督层级 */
    @TableField("grade_level")
    @ApiModelProperty(value = "年办件量折到的服务监督层级")
    private Integer gradeLevel;

    /** 已走完的步数(档次由它数出来) */
    @TableField("step_count")
    @ApiModelProperty(value = "已走完的步数(档次由它数出来)")
    private Integer stepCount;

    /** 第几趟(退档重走一趟加一) */
    @TableField("run_no")
    @ApiModelProperty(value = "第几趟(退档重走一趟加一)")
    private Integer runNo;

    /** 版次落点 1现行 0历史 */
    @TableField("ver_flag")
    @ApiModelProperty(value = "版次落点 1现行 0历史")
    private Integer verFlag;

    /** 进展 0待核对 1已核对 2已冻住 */
    @TableField("status")
    @ApiModelProperty(value = "进展 0待核对 1已核对 2已冻住")
    private Integer status;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 开卡人(一钉终身) */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "开卡人(一钉终身)")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 本趟经办(办一次续一名) */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "本趟经办(办一次续一名)")
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

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
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

    public String getImplCode() {
        return implCode;
    }

    public void setImplCode(String implCode) {
        this.implCode = implCode;
    }

    public Integer getUndertakeLevel() {
        return undertakeLevel;
    }

    public void setUndertakeLevel(Integer undertakeLevel) {
        this.undertakeLevel = undertakeLevel;
    }

    public String getUpperItemCode() {
        return upperItemCode;
    }

    public void setUpperItemCode(String upperItemCode) {
        this.upperItemCode = upperItemCode;
    }

    public String getBasisClause() {
        return basisClause;
    }

    public void setBasisClause(String basisClause) {
        this.basisClause = basisClause;
    }

    public String getCityItems() {
        return cityItems;
    }

    public void setCityItems(String cityItems) {
        this.cityItems = cityItems;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getWindows() {
        return windows;
    }

    public void setWindows(String windows) {
        this.windows = windows;
    }

    public Integer getRecvPoints() {
        return recvPoints;
    }

    public void setRecvPoints(Integer recvPoints) {
        this.recvPoints = recvPoints;
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

    public Integer getStepCount() {
        return stepCount;
    }

    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public Integer getRunNo() {
        return runNo;
    }

    public void setRunNo(Integer runNo) {
        this.runNo = runNo;
    }

    public Integer getVerFlag() {
        return verFlag;
    }

    public void setVerFlag(Integer verFlag) {
        this.verFlag = verFlag;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
