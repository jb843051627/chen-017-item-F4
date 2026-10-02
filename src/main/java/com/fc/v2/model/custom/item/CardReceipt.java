package com.fc.v2.model.custom.item;

import java.util.ArrayList;
import java.util.List;

/**
 * 立卡回执（缺哪一栏指名到哪一栏，光回一句"数不像话"不算交代）
 *
 * @author fuce
 * @date 2026-10-01
 */
public class CardReceipt {

    /** 立没立起来 */
    private boolean success;

    /** 空着/不像话的那些栏(指名) */
    private List<String> missingFields = new ArrayList<String>();

    /** 起头之日写成往后的日子，得先问一句 */
    private boolean needConfirm;

    /** 回话 */
    private String message;

    /** 立起来的卡主键 */
    private Long cardId;

    /** 系统发的清单卡号 */
    private String billNo;

    /** 系统发的实施编码 */
    private String implCode;

    /** 折到的服务监督层级(以服务层给回的为准) */
    private Integer gradeLevel;

    /** 当前走到哪一步(系统按步数数出来的) */
    private Integer status;

    /** 本卡版次序 */
    private Integer nodeNo;

    public static CardReceipt reject(List<String> missingFields, String message) {
        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(false);
        receipt.setMissingFields(missingFields);
        receipt.setMessage(message);
        return receipt;
    }

    public static CardReceipt askConfirm(String message) {
        CardReceipt receipt = new CardReceipt();
        receipt.setSuccess(false);
        receipt.setNeedConfirm(true);
        receipt.setMessage(message);
        return receipt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getMissingFields() {
        return missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

    public boolean isNeedConfirm() {
        return needConfirm;
    }

    public void setNeedConfirm(boolean needConfirm) {
        this.needConfirm = needConfirm;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getImplCode() {
        return implCode;
    }

    public void setImplCode(String implCode) {
        this.implCode = implCode;
    }

    public Integer getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Integer gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }
}
