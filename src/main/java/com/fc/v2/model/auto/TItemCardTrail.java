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
 * 事项卡过步留痕对象 t_item_card_trail
 *
 * <p>卡每走一步、折一回层、换一回挂靠都落一行；回退重走另起一趟，
 * 上一趟落下的记录照旧留着，不跟后补的排作一处。</p>
 *
 * @author fuce
 * @date 2026-10-02
 */
@TableName("t_item_card_trail")
@ApiModel(value = "TItemCardTrail", description = "事项卡过步留痕")
public class TItemCardTrail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    @TableField("card_id")
    @ApiModelProperty(value = "卡主键")
    private Long cardId;

    @TableField("bill_no")
    @ApiModelProperty(value = "卡号")
    private String billNo;

    @TableField("node_no")
    @ApiModelProperty(value = "当时版次序")
    private Integer nodeNo;

    @TableField("round_no")
    @ApiModelProperty(value = "第几趟(每回退一次,后补记在下一趟)")
    private Integer roundNo;

    @TableField("action")
    @ApiModelProperty(value = "动作 open/advance/back/qty/rehang/revise/stop")
    private String action;

    @TableField("from_step")
    @ApiModelProperty(value = "动前步数")
    private Integer fromStep;

    @TableField("to_step")
    @ApiModelProperty(value = "动后步数")
    private Integer toStep;

    @TableField("from_grade")
    @ApiModelProperty(value = "动前层级")
    private Integer fromGrade;

    @TableField("to_grade")
    @ApiModelProperty(value = "动后层级")
    private Integer toGrade;

    @TableField("operator_name")
    @ApiModelProperty(value = "本趟经办人")
    private String operatorName;

    @TableField("trail_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "落记时刻")
    private Date trailTime;

    @TableField("note")
    @ApiModelProperty(value = "记事")
    private String note;

    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

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

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
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

    public Integer getFromGrade() {
        return fromGrade;
    }

    public void setFromGrade(Integer fromGrade) {
        this.fromGrade = fromGrade;
    }

    public Integer getToGrade() {
        return toGrade;
    }

    public void setToGrade(Integer toGrade) {
        this.toGrade = toGrade;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Date getTrailTime() {
        return trailTime;
    }

    public void setTrailTime(Date trailTime) {
        this.trailTime = trailTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }
}
