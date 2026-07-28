package com.mashang.scoreservice.controller;

import com.mashang.common.common.R;
import com.mashang.scoreservice.domain.query.create.PersonalResultQuery;
import com.mashang.scoreservice.domain.query.create.TeamResultQuery;
import com.mashang.scoreservice.domain.vo.PersonalResultVo;
import com.mashang.scoreservice.domain.vo.RankingVo;
import com.mashang.scoreservice.domain.vo.TeamResultVo;
import com.mashang.scoreservice.service.IPersonalResultService;
import com.mashang.scoreservice.service.ITeamResultService;
import com.mashang.scoreservice.mq.ScoreProducer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩服务控制器
 * <p>
 * 核心职责：提供成绩录入、查询和排行相关的 RESTful API 接口。
 * 设计思路：
 * <ul>
 *   <li>个人成绩与团体成绩分离，各自独立管理</li>
 *   <li>成绩录入后通过消息队列（MQ）异步发送通知，实现录入与通知的解耦</li>
 *   <li>排行榜数据使用 Redis 缓存提升查询性能</li>
 *   <li>支持单条录入和批量导入两种方式，满足不同规模赛事的数据录入需求</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Api(tags = "成绩服务")
@RestController
@RequestMapping("/score")
public class ScoreController {

    /** 个人成绩服务接口，负责个人成绩的增删改查及排名计算 */
    @Autowired
    private IPersonalResultService personalResultService;

    /** 团体成绩服务接口，负责团体成绩的增删改查及排名计算 */
    @Autowired
    private ITeamResultService teamResultService;

    /**
     * 成绩消息生产者
     * 用于在成绩录入成功后向消息队列发送通知，触发后续的推送/通知流程
     */
    @Autowired
    private ScoreProducer scoreProducer;

    /**
     * 个人成绩录入
     * <p>
     * 接收个人成绩数据，完成录入后通过 MQ 异步发送成绩发布通知。
     * 使用场景：裁判或管理员为某位参赛者在某个项目中录入最终成绩。
     *
     * @param query 个人成绩录入请求体，包含 userId（用户ID）、itemId（项目ID）、score（成绩值）
     * @return 统一响应对象 R，result>0 表示录入成功
     */
    @ApiOperation("个人成绩录入")
    @PostMapping("/personal")
    public R<Void> personalEntry(@RequestBody @Validated PersonalResultQuery query) {

        // 执行个人成绩录入
        int result = personalResultService.entry(query);
        // 录入成功后，通过 MQ 发送成绩发布通知，供通知服务消费处理
        if (result > 0) {
            scoreProducer.sendScorePublishNotification(query.getUserId(),
                    "项目" + query.getItemId(), String.valueOf(query.getScore()));
        }
        return R.toResult(result);
    }

    /**
     * 团体成绩录入
     * <p>
     * 接收团体（队伍）成绩数据并完成录入。
     * 使用场景：裁判或管理员为某支队伍在某个团队项目中录入成绩。
     *
     * @param query 团体成绩录入请求体，包含 teamEntryId（队伍报名ID）、itemId（项目ID）、score（成绩值）
     * @return 统一响应对象 R，result>0 表示录入成功
     */
    @ApiOperation("团体成绩录入")
    @PostMapping("/team")
    public R<Void> teamEntry(@RequestBody @Validated TeamResultQuery query) {
        return R.toResult(teamResultService.entry(query));
    }

    /**
     * 批量成绩导入
     * <p>
     * 支持一次性导入多条个人成绩记录，适用于从外部系统或 Excel 表格批量迁移成绩数据的场景。
     * 底层使用 MyBatis-Plus 的批量插入能力，配合事务保证数据一致性。
     *
     * @param queryList 个人成绩录入请求体列表，每条记录包含 userId/itemId/score
     * @return 统一响应对象 R，result>0 表示批量导入成功
     */
    @ApiOperation("批量成绩导入")
    @PostMapping("/batch")
    public R<Void> batchImport(@RequestBody @Validated List<PersonalResultQuery> queryList) {
        return R.toResult(personalResultService.batchImport(queryList));
    }

    /**
     * 查询项目个人成绩列表
     * <p>
     * 按项目 ID 查询该项目下所有参赛者的个人成绩，按成绩降序排列。
     * 使用场景：查看某个比赛项目（如100米跑）的所有选手成绩。
     *
     * @param itemId 项目ID，唯一标识一个比赛项目
     * @return 该项目下所有个人成绩的 VO 列表
     */
    @ApiOperation("查询项目个人成绩列表")
    @GetMapping("/personal/item/{itemId}")
    @ApiImplicitParam(name = "itemId", value = "项目ID", required = true)
    public R<List<PersonalResultVo>> personalList(@PathVariable Long itemId) {
        return R.ok(personalResultService.listByItemId(itemId));
    }

    /**
     * 查询项目团体成绩列表
     * <p>
     * 按项目 ID 查询该项目下所有参赛队伍的成绩，按成绩降序排列并附带排名。
     * 使用场景：查看团体项目（如4x100米接力）的各队伍成绩和排名。
     *
     * @param itemId 项目ID，唯一标识一个比赛项目
     * @return 该项目下所有团体成绩的 VO 列表
     */
    @ApiOperation("查询项目团体成绩列表")
    @GetMapping("/team/item/{itemId}")
    @ApiImplicitParam(name = "itemId", value = "项目ID", required = true)
    public R<List<TeamResultVo>> teamList(@PathVariable Long itemId) {
        return R.ok(teamResultService.listByItemId(itemId));
    }

    /**
     * 查询运动会排行榜（使用 Redis 缓存加速）
     * <p>
     * 按运动会 ID 查询各参赛单位/队伍的综合排名，数据来源于 Redis ZSet 有序集合，
     * 按总分从高到低排序。排行榜数据由 Service 层在成绩变更时自动更新。
     * 使用场景：大屏幕展示运动会实时排名、移动端查看积分榜。
     *
     * @param meetingId 运动会ID，唯一标识一届运动会
     * @return 排行榜 VO 列表，按排名升序排列
     */
    @ApiOperation("查询排行榜(Redis缓存)")
    @GetMapping("/ranking/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<RankingVo>> ranking(@PathVariable Long meetingId) {
        return R.ok(personalResultService.getRanking(meetingId));
    }

    /**
     * 查询用户个人成绩
     * <p>
     * 按用户 ID 查询该用户在所有项目中取得的成绩。
     * 使用场景：用户在个人中心查看自己的参赛成绩列表。
     *
     * @return 该用户所有个人成绩的 VO 列表
     */
    @ApiOperation("查询用户个人成绩")
    @GetMapping("/personal/user")

    public R<List<PersonalResultVo>> userScores() {
        return R.ok(personalResultService.listByUserId());
    }
}
