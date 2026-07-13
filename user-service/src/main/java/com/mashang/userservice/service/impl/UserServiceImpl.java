package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.SysUser;
import com.mashang.userservice.domain.query.LoginUserQuery;
import com.mashang.userservice.domain.query.create.RegisterUserQuery;
import com.mashang.userservice.domain.vo.RefereeVo;
import com.mashang.userservice.mapper.UserMapper;
import com.mashang.userservice.service.IUserService;
import com.mashang.userservice.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现 —— 认证、注册、密码管理及裁判查询等核心功能
 *
 * <h3>安全机制</h3>
 * <ul>
 *   <li><b>密码存储：</b>使用 BCryptPasswordEncoder 单向哈希加密，数据库中永不存储明文密码</li>
 *   <li><b>认证流程：</b>通过 Spring Security 的 AuthenticationManager.authenticate() 统一认证，
 *       内部由 DaoAuthenticationProvider 自动使用 BCrypt 算法比对密码</li>
 *   <li><b>JWT 令牌：</b>认证通过后由 JWTUtil.createToken() 签发令牌，令牌中包含用户ID和用户名</li>
 *   <li><b>会话管理：</b>登录后将用户信息存入 Redis（Key: user:{userId}），
 *       供 Gateway 全局过滤器校验会话有效性和实现登出控制</li>
 * </ul>
 *
 * <h3>设计模式</h3>
 * <ul>
 *   <li>继承 MyBatis-Plus 的 ServiceImpl，获得 CRUD 基础能力</li>
 *   <li>裁判查询采用 <b>Cache Aside 模式</b>：读缓存 → 查DB → 写缓存</li>
 * </ul>
 *
 * @see com.mashang.userservice.utils.JWTUtil
 * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements IUserService {

    // ======================== 依赖注入 ========================

    /** Spring Security 认证管理器，负责协调认证提供者完成用户身份验证 */
    @Autowired
    private AuthenticationManager authenticationManager;

    /** Redis 工具类，封装了常用的缓存读写操作 */
    @Autowired
    private RedisUtil redisUtil;

    /** 用户数据访问层 */
    @Autowired
    private UserMapper userMapper;

    /**
     * BCrypt 密码编码器
     * - encode(rawPassword): 将明文密码加密为 BCrypt 密文（每次生成不同的盐值）
     * - matches(rawPassword, encodedPassword): 比对明文与密文是否匹配
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Redis 模板，用于直接操作 Redis 缓存（如裁判查询缓存） */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ======================== 认证与注册 ========================

    /**
     * 用户登录 —— Spring Security 认证 + JWT 令牌签发
     *
     * <h3>完整认证流程：</h3>
     * <ol>
     *   <li><b>构建认证凭据：</b>将用户名和密码封装为 UsernamePasswordAuthenticationToken（未认证状态）</li>
     *   <li><b>调用认证管理器：</b>authenticationManager.authenticate(token) 触发以下链路：
     *     <ul>
     *       <li>DaoAuthenticationProvider 调用 UserDetailsServiceImpl.loadUserByUsername() 从数据库加载用户</li>
     *       <li>使用 BCryptPasswordEncoder.matches() 比对用户输入的明文密码与数据库中存储的 BCrypt 密文</li>
     *       <li>比对成功 → 返回已认证的 Authentication 对象（包含 LoginUser 主体）</li>
     *       <li>比对失败 → 抛出 BadCredentialsException</li>
     *     </ul>
     *   </li>
     *   <li><b>签发 JWT：</b>从认证结果中提取 LoginUser，调用 JWTUtil.createToken() 生成令牌</li>
     *   <li><b>写入 Redis 会话：</b>将 LoginUser 对象存入 Redis（Key: user:{userId}），
     *       供 Gateway 校验登录状态和踢人下线功能使用</li>
     * </ol>
     *
     * @param user 登录请求对象，包含 username 和 password
     * @return ApiResponse，data 字段为 JWT 令牌字符串
     * @throws RuntimeException 当认证失败时抛出（authenticate 返回 null）
     */
    @Override
    public ApiResponse login(LoginUserQuery user) {
        // 第1步：构建未认证的 Authentication 令牌
        // UsernamePasswordAuthenticationToken 的两个参数分别为用户名和密码
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

        // 第2步：委托 AuthenticationManager 执行认证
        // authenticate() 内部调用链：
        //   AuthenticationManager → DaoAuthenticationProvider →
        //     1) UserDetailsServiceImpl.loadUserByUsername() 查用户
        //     2) BCryptPasswordEncoder.matches() 比对密码
        //     3) 认证通过 → 返回已认证的 Authentication
        Authentication authenticate = authenticationManager.authenticate(token);

        // 第2.5步：认证结果校验（理论上 authenticate 失败会直接抛异常，此处为防御性编程）
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("认证失败");
        }

        // 第3步：从认证主体中获取 LoginUser（包含完整的用户信息和权限列表）
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();

        // 第4步：签发 JWT 令牌
        // JWT 中包含 userId、username 等字段，由 Gateway 解析并校验
        String jwtToken = JWTUtil.createToken(loginUser.getUser());

        // 第5步：将用户会话信息写入 Redis
        // Key 格式: user:{userId}，Value: LoginUser 的 JSON 序列化内容
        // 用途：
        //   - Gateway 全局过滤器校验每个请求的登录状态
        //   - 支持"踢人下线"功能（管理员删除 Redis 中特定用户的 Key）
        redisUtil.setCacheObject("user:" + loginUser.getUser().getUserId(), loginUser);

        return ApiResponse.ok(jwtToken);
    }

    /**
     * 用户注册 —— 密码 BCrypt 加密存储
     *
     * <h3>注册流程：</h3>
     * <ol>
     *   <li><b>唯一性校验：</b>查询数据库确认用户名未被占用，若已存在则抛出异常</li>
     *   <li><b>密码加密：</b>使用 BCryptPasswordEncoder.encode() 将明文密码转为不可逆的 BCrypt 哈希值
     *       （每次加密生成随机盐，即使相同密码加密结果也不同）</li>
     *   <li><b>设置默认值：</b>roleId 默认为 1L（管理员角色）；status 默认为 "0"（启用状态）</li>
     *   <li><b>入库：</b>调用 userMapper.insert() 写入数据库</li>
     * </ol>
     *
     * @param query 注册请求对象，包含 username、password、realName、phone、roleId 等字段
     * @return 影响行数（>0 表示注册成功）
     * @throws RuntimeException 当用户名已存在时抛出
     */
    @Override
    public int register(RegisterUserQuery query) {
        // 用户名唯一性校验：查询同名的未逻辑删除记录
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, query.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(query.getUsername());
        // 密码 BCrypt 加密后再存储 —— 数据库中永不保存明文密码
        // BCrypt 特点：每次加密生成随机盐值，相同密码多次加密结果不同
        user.setPassword(passwordEncoder.encode(query.getPassword()));
        user.setRealName(query.getRealName());
        user.setPhone(query.getPhone());
        // 默认角色为管理员（roleId=1），如果请求中指定了 roleId 则使用指定值
        user.setRoleId(query.getRoleId() != null ? query.getRoleId() : 1L);
        // status: "0"=启用, "1"=停用
        user.setStatus("0");
        return userMapper.insert(user);
    }

    /**
     * 修改密码 —— BCrypt 加密后更新
     *
     * <h3>修改流程：</h3>
     * <ol>
     *   <li>从 JWT 中解析当前登录用户的 userId（通过 JWTUtil.getUserId()）</li>
     *   <li>使用 BCryptPasswordEncoder.encode() 加密新密码</li>
     *   <li>通过 LambdaUpdateWrapper 构造 WHERE userId = ? 的更新条件，更新密码字段</li>
     * </ol>
     *
     * <h3>安全说明：</h3>
     * 不需要验证旧密码，因为在 Gateway 层已经校验了 JWT 的有效性，能到达此方法的请求
     * 必然是已认证用户的请求。如需验证旧密码，可在调用此方法前单独校验。
     *
     * @param password 新密码（明文，由本方法负责加密）
     * @return 影响行数（>0 表示修改成功）
     */
    @Override
    public int update(String password) {
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                // WHERE 条件：限定仅更新当前登录用户
                .eq(SysUser::getUserId, JWTUtil.getUserId())
                // SET 子句：新密码 BCrypt 加密后写入数据库
                .set(SysUser::getPassword, passwordEncoder.encode(password));

        return userMapper.update(null, updateWrapper);
    }

    // ======================== 裁判查询 ========================

    /** 全部裁判列表的 Redis 缓存 Key */
    private static final String REFEREE_ALL_KEY = "referee:all";

    /**
     * 查询所有裁判 —— Cache Aside 缓存模式
     *
     * <h3>缓存策略（Cache Aside 读模式）：</h3>
     * <ol>
     *   <li><b>读缓存：</b>尝试从 Redis 读取 Key 为 referee:all 的数据</li>
     *   <li><b>缓存命中：</b>直接返回缓存中的裁判列表</li>
     *   <li><b>缓存未命中：</b>调用 userMapper.allReferee() 查询数据库，
     *       将结果写入 Redis（TTL 60 分钟），然后返回结果</li>
     * </ol>
     *
     * <h3>缓存一致性：</h3>
     * 裁判数据变更频率较低，采用 TTL 过期策略（60 分钟），
     * 如需立即生效，需手动清除 Redis 中的 referee:all 键。
     *
     * @return 裁判视图对象列表（RefereeVo）
     */
    @Override
    public List<RefereeVo> allReferee() {
        // Step 1: 先从 Redis 缓存中读取
        Object cached = redisTemplate.opsForValue().get(REFEREE_ALL_KEY);
        if (cached != null) {
            // 缓存命中：直接返回，无需查询数据库
            return (List<RefereeVo>) cached;
        }

        // Step 2: 缓存未命中 → 查询数据库
        List<RefereeVo> list = userMapper.allReferee();

        // Step 3: 将查询结果回写到 Redis 缓存，TTL 60 分钟
        if (list != null && !list.isEmpty()) {
            redisTemplate.opsForValue().set(REFEREE_ALL_KEY, list, 60, TimeUnit.MINUTES);
        }
        return list;
    }
}
