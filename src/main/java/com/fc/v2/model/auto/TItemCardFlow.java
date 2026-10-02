package com.fc.v2.model.auto;

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
import java.util.Date;

/**
 * 实施清单配置卡过口记录对象 t_item_card_flow
 *
 * @author fuce
 * @date 2026-10-01
 */
@TableName("t_item_card_flow")
@ApiModel(value = "TItemCardFlow", description = "实施清单配置卡过口记录")
public class TItemCardFlow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 前进 */
    public static final String ACTION_ADVANCE = "advance";
    /** 退档 */
    public static final String ACTION_ROLLBACK = "rollback";
    /** 冻住 */
    public static final String ACTION_FREEZE = "freeze";
    /** 叫停 */
    public static final String ACTION_HALT = "halt";

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 所属卡主键 */
    @TableField("card_id")
    @ApiModelProperty(value = "所属卡主键")
    private Long cardId;

    /** 卡号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "卡号")
    private String billNo;

    /** 第几趟(退档重走各趟分开排) */
    @TableField("run_no")
    @ApiModelProperty(value = "第几趟(退档重走各趟分开排)")
    private Integer runNo;

    /** 过口动作 advance前进 rollback退档 freeze冻住 halt叫停 */
    @TableField("action")
    @ApiModelProperty(value = "过口动作")
    private String action;

    /** 动作前步数 */
    @TableField("from_step")
    @ApiModelProperty(value = "动作前步数")
    private Integer fromStep;

    /** 动作后步数 */
    @TableField("to_step")
    @ApiModelProperty(value = "动作后步数")
    private Integer toStep;

    /** 这一趟落下的校验串快照 */
    @TableField("checksum_snap")
    @ApiModelProperty(value = "这一趟落下的校验串快照")
    private String checksumSnap;

    /** 本趟经办 */
    @TableField("operator")
    @ApiModelProperty(value = "本趟经办")
    private String operator;

    /** 落笔时间 */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "落笔时间")
    private Date createTime;

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

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Integer getRunNo() {
        return runNo;
    }

    public void setRunNo(Integer runNo) {
        this.runNo = runNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getFromStep() {
        return fromStep;
    }

    public void setFromStep(Integer fromStep) {
        this.fromStep = fromStep;
    }

    public Integer getToStep() {
        return toStep;
    }

    public void setToStep(Integer toStep) {
        this.toStep = toStep;
    }

    public String getChecksumSnap() {
        return checksumSnap;
    }

    public void setChecksumSnap(String checksumSnap) {
        this.checksumSnap = checksumSnap;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
