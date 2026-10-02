package com.fc.v2.service.itemcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TItemCardTrailMapper;
import com.fc.v2.mapper.auto.TItemDeptArchMapper;
import com.fc.v2.mapper.auto.TItemImplementCardMapper;
import com.fc.v2.model.auto.TItemCardTrail;
import com.fc.v2.model.auto.TItemDeptArch;
import com.fc.v2.model.auto.TItemImplementCard;
import com.fc.v2.util.SnowflakeIdWorker;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 事项卡领域网关的 MyBatis 落库一头。
 *
 * @author fuce
 * @date 2026-10-02
 */
@Component
public class ItemCardGatewayMyBatis implements ItemCardGateway {

    @Resource
    private TItemImplementCardMapper cardMapper;

    @Resource
    private TItemCardTrailMapper trailMapper;

    @Resource
    private TItemDeptArchMapper archMapper;

    @Override
    public TItemImplementCard findById(Long id) {
        return cardMapper.selectById(id);
    }

    @Override
    public List<TItemImplementCard> findCurrent() {
        return cardMapper.selectList(new QueryWrapper<TItemImplementCard>()
                .eq("del_flag", 0)
                .eq("current_flag", TItemImplementCard.CURRENT_YES));
    }

    @Override
    public List<TItemImplementCard> findAll() {
        return cardMapper.selectList(new QueryWrapper<TItemImplementCard>()
                .eq("del_flag", 0));
    }

    @Override
    public void insert(TItemImplementCard card) {
        cardMapper.insert(card);
    }

    @Override
    public void update(TItemImplementCard card) {
        cardMapper.updateById(card);
    }

    @Override
    public void appendTrail(TItemCardTrail trail) {
        trailMapper.insert(trail);
    }

    @Override
    public List<TItemCardTrail> findTrails(Long cardId) {
        return trailMapper.selectList(new QueryWrapper<TItemCardTrail>()
                .eq("card_id", cardId)
                .eq("del_flag", 0)
                .orderByAsc("trail_time", "id"));
    }

    @Override
    public TItemDeptArch findArchBySiteNo(String siteNo) {
        if (siteNo == null || siteNo.trim().isEmpty()) {
            return null;
        }
        return archMapper.selectOne(new QueryWrapper<TItemDeptArch>()
                .eq("site_no", siteNo)
                .eq("del_flag", 0));
    }

    @Override
    public String nextBillNo() {
        // 清单卡号由系统发，谁往里塞自家拟的号库里都不留
        return "CARD" + SnowflakeIdWorker.getUUID();
    }

    @Override
    public String nextImplCode() {
        return "IMP" + SnowflakeIdWorker.getUUID();
    }

    @Override
    public Date now() {
        return new Date();
    }
}
