package com.mashang.scoreservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.common.constants.CacheConstants;
import com.mashang.scoreservice.common.KeyCommon;
import com.mashang.scoreservice.domain.entity.PersonalResult;
import com.mashang.scoreservice.domain.query.create.PersonalResultQuery;
import com.mashang.scoreservice.domain.vo.PersonalResultVo;
import com.mashang.scoreservice.domain.vo.RankingVo;
import com.mashang.scoreservice.mapper.PersonalResultMapper;
import com.mashang.scoreservice.mapping.ScoreMapping;
import com.mashang.scoreservice.service.IPersonalResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PersonalResultServiceImpl extends ServiceImpl<PersonalResultMapper, PersonalResult> implements IPersonalResultService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PersonalResultMapper personalResultMapper;

    @Override
    public int entry(PersonalResultQuery query) {

        // 检查是否已有成绩记录（同一用户同一项目）
        LambdaQueryWrapper<PersonalResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalResult::getUserId, query.getUserId())
                .eq(PersonalResult::getItemId, query.getItemId())
                .eq(PersonalResult::getMeetingId, query.getMeetingId());

        PersonalResult existing = personalResultMapper.selectOne(wrapper);

        int result;
        if (existing != null) {
            // 已存在，更新成绩
            LambdaUpdateWrapper<PersonalResult> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PersonalResult::getResultId, existing.getResultId())
                    .set(PersonalResult::getScore, query.getScore());
            result = personalResultMapper.update(null, updateWrapper);
        } else {
            // 不存在，新增
            PersonalResult entity = ScoreMapping.INSTANCE.toEntity(query);
            entity.setStatus(1L);
            result = personalResultMapper.insert(entity);
        }

        // 自动重新计算该项目排名
        recalculateRanking(query.getItemId());

        // 清除排行榜缓存
        redisTemplate.delete(KeyCommon.buildRankingKey(query.getMeetingId()));

        return result;
    }

    @Override
    public int batchImport(List<PersonalResultQuery> queryList) {
        int total = 0;
        for (PersonalResultQuery query : queryList) {
            total += entry(query);
        }
        return total;
    }

    @Override
    public List<PersonalResultVo> listByItemId(Long itemId) {
        return personalResultMapper.selectByItemId(itemId);
    }

    @Override
    public List<PersonalResultVo> listByUserId(Long userId) {
        return personalResultMapper.selectByUserId(userId);
    }

    @Override
    public List<RankingVo> getRanking(Long meetingId) {

        String cacheKey = KeyCommon.buildRankingKey(meetingId);

        // 1. 先查缓存
        List<RankingVo> rankingVoList = (List<RankingVo>) redisTemplate.opsForValue().get(cacheKey);
        if (rankingVoList != null) {
            return rankingVoList;
        }

        // 2. 缓存未命中，查数据库
        rankingVoList = personalResultMapper.selectRanking(meetingId, null);

        // 3. 防止缓存穿透
        if (rankingVoList == null || rankingVoList.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, "NULL", 2, TimeUnit.MINUTES);
            return null;
        }

        // 4. 写缓存
        redisTemplate.opsForValue().set(cacheKey, rankingVoList, CacheConstants.SCORE_TTL, TimeUnit.MINUTES);

        return rankingVoList;
    }

    /**
     * 重新计算项目排名（按成绩降序）
     */
    private void recalculateRanking(Long itemId) {
        List<PersonalResultVo> resultList = personalResultMapper.selectByItemId(itemId);
        if (resultList != null && !resultList.isEmpty()) {
            for (int i = 0; i < resultList.size(); i++) {
                PersonalResultVo vo = resultList.get(i);
                LambdaUpdateWrapper<PersonalResult> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(PersonalResult::getResultId, vo.getResultId())
                        .set(PersonalResult::getRank, (long) (i + 1));
                personalResultMapper.update(null, updateWrapper);
            }
        }
    }

}
