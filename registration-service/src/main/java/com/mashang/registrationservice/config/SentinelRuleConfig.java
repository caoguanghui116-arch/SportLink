package com.mashang.registrationservice.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流与熔断规则初始化。
 *
 * 为什么需要 Sentinel：
 * - 报名高峰场景（赛事开放首日 / 报名截止前）瞬时并发量大
 * - 没有流量控制会导致：DB 连接池耗尽 → 服务雪崩 → 整个系统不可用
 * - Sentinel 提供精确的 QPS 限流和自动熔断，是微服务高可用的标配组件
 *
 * 核心概念：
 * - 流量控制 (FlowRule)：超过 QPS 阈值则拒绝请求，快速失败
 * - 熔断降级 (DegradeRule)：响应时间过长时自动熔断，保护下游服务
 * - 两种模式组合使用，实现"防刷、防突增、防慢调用"三层防护
 *
 * 规则持久化：
 * - 当前使用硬编码规则（应用启动时加载）
 * - 生产环境建议通过 Nacos 数据源动态管理规则，无需重启即可修改
 */
@Component
public class SentinelRuleConfig implements CommandLineRunner {

    /**
     * 应用启动后自动注册限流和熔断规则
     */
    @Override
    public void run(String... args) {
        initFlowRules();
        initDegradeRules();
    }

    // ==================== 流量控制规则 ====================

    /**
     * 限流规则 —— 针对报名高峰场景设置 QPS 上限。
     *
     * 设计依据：
     * - 个人报名 (200 QPS)：报名高峰期，假设 5000 人在线，平均 25 req/s，200 QPS 留有 8 倍余量
     * - 团队报名 (100 QPS)：低频操作，100 QPS 足够
     * - 批量导入 (50 QPS) ：涉及大量 DB 写入，需严格控制
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // ---- 个人报名接口：QPS 上限 200 ----
        FlowRule personalEntry = new FlowRule();
        personalEntry.setResource("personalEntry");                // 资源名（与 @SentinelResource value 对应）
        personalEntry.setGrade(RuleConstant.FLOW_GRADE_QPS);      // 限流模式：QPS（每秒请求数）
        personalEntry.setCount(200);                               // 阈值：200 QPS
        rules.add(personalEntry);

        // ---- 团队报名接口：QPS 上限 100 ----
        FlowRule teamEntry = new FlowRule();
        teamEntry.setResource("teamEntry");
        teamEntry.setGrade(RuleConstant.FLOW_GRADE_QPS);
        teamEntry.setCount(100);
        rules.add(teamEntry);

        // ---- 批量报名导入：QPS 上限 50 ----
        FlowRule batchImport = new FlowRule();
        batchImport.setResource("batchImport");
        batchImport.setGrade(RuleConstant.FLOW_GRADE_QPS);
        batchImport.setCount(50);
        rules.add(batchImport);

        FlowRuleManager.loadRules(rules);
    }

    // ==================== 熔断降级规则 ====================

    /**
     * 熔断降级规则 —— 慢调用比例熔断。
     *
     * 触发条件（以个人报名为例）：
     * - 1秒内 ≥ 10 个请求
     * - 其中 ≥ 50% 的请求 RT > 200ms
     * → 触发熔断，后续请求直接快速失败（不调用真实方法）
     * → 10秒后进入半开状态，允许一个请求试探
     * → 试探成功 → 关闭熔断；试探失败 → 继续熔断
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // ---- 个人报名：RT > 200ms 且比例 > 50% 时熔断，10s 后半开 ----
        DegradeRule personalDegrade = new DegradeRule();
        personalDegrade.setResource("personalEntry");
        personalDegrade.setGrade(RuleConstant.DEGRADE_GRADE_RT);  // 熔断策略：慢调用比例
        personalDegrade.setCount(200);                              // RT 阈值：200 毫秒
        personalDegrade.setTimeWindow(10);                          // 熔断恢复窗口：10 秒
        personalDegrade.setMinRequestAmount(10);                    // 最小请求数：达标 10 个才开始统计
        personalDegrade.setSlowRatioThreshold(0.5);                 // 慢调用比例阈值：50%
        rules.add(personalDegrade);

        // ---- 团队报名：RT > 500ms 且比例 > 50% 时熔断 ----
        // 团队报名涉及 batch insert 团队成员，DB 操作更重，RT 阈值放宽到 500ms
        DegradeRule teamDegrade = new DegradeRule();
        teamDegrade.setResource("teamEntry");
        teamDegrade.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        teamDegrade.setCount(500);
        teamDegrade.setTimeWindow(10);
        teamDegrade.setMinRequestAmount(5);
        teamDegrade.setSlowRatioThreshold(0.5);
        rules.add(teamDegrade);

        DegradeRuleManager.loadRules(rules);
    }
}
