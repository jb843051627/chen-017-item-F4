package com.fc.v2.model.custom.item;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 立卡递法（新递法的入参，与旧保存方法并排）
 *
 * @author fuce
 * @date 2026-10-01
 */
public class CardSubmitCmd {

    /** 所属部门底册代号(只凭代号对册) */
    private String siteNo;

    /** 事项码(与底册代号配平认卡) */
    private String itemCode;

    /** 承办层级 1省定标准 2市实施 3县延伸 */
    private Integer undertakeLevel;

    /** 上位名录条目代号(省定标准凭据) */
    private String upperItemCode;

    /** 设定依据条款号(省定标准凭据) */
    private String basisClause;

    /** 本市细化条目(市实施凭据) */
    private String cityItems;

    /** 起头之日(市实施凭据) */
    private Date startDate;

    /** 结尾之日(市实施凭据) */
    private Date endDate;

    /** 延伸到的窗口(县延伸凭据) */
    private String windows;

    /** 收件点数目(县延伸凭据) */
    private Integer recvPoints;

    /** 本卡申报年办件量(件/年) */
    private BigDecimal qty;

    /** 本卡承诺时限(工作日) */
    private BigDecimal fineAmt;

    /** 起头之日写成往后的日子时，先问一句的确认 */
    private Boolean confirmFutureStart;

    /** 开卡人(一钉终身) */
    private String operator;

    /** 备注 */
    private String remark;

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

    public Boolean getConfirmFutureStart() {
        return confirmFutureStart;
    }

    public void setConfirmFutureStart(Boolean confirmFutureStart) {
        this.confirmFutureStart = confirmFutureStart;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
