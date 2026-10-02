package com.fc.v2.service.itemcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 事项卡被服务层拒收：回执必须指名空在哪一栏，
 * 光回一句"数不像话"不算交代。
 *
 * @author fuce
 * @date 2026-10-02
 */
public class CardRejectException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /** 空着或不合法的栏（按承办层级当场点出） */
    private final List<String> emptyFields;

    /** true：起头那一日写成往后的日子，得先问一句才收 */
    private final boolean needConfirm;

    public CardRejectException(String message) {
        this(message, new ArrayList<String>(), false);
    }

    public CardRejectException(String message, List<String> emptyFields) {
        this(message, emptyFields, false);
    }

    public CardRejectException(String message, List<String> emptyFields, boolean needConfirm) {
        super(message);
        this.emptyFields = emptyFields == null ? new ArrayList<String>() : emptyFields;
        this.needConfirm = needConfirm;
    }

    public List<String> getEmptyFields() {
        return Collections.unmodifiableList(emptyFields);
    }

    public boolean isNeedConfirm() {
        return needConfirm;
    }
}
