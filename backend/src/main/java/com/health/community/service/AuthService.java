package com.health.community.service;
import com.health.community.common.enumeration.Role;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.JwtProperties;
import com.health.community.common.util.JwtUtils;
import com.health.community.dto.AdminCreateDTO;
import com.health.community.dto.LoginDTO;

import com.health.community.entity.User;
import com.health.community.repository.UserRepository;
import com.health.community.vo.AdminInitVO;
import com.health.community.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static com.health.community.common.constant.MessageConstant.ACCOUNT_OR_PASSWORD_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor // 自动生成构造器
public class AuthService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
//TODO redis 登录失败次数 退出登录token立即失效




    /**
     * 用户登录认证
     * @param loginDTO 登录凭证（用户名、密码、rememberMe）
     * @return 登录成功后的令牌和用户基本信息
     * @throws BusinessException 账号或密码错误
     */
    public LoginVO login(LoginDTO loginDTO, Role role){

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        //1.查询用户是否存在
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ACCOUNT_OR_PASSWORD_ERROR));
        //检查用户权限是否正确
        if (role != user.getRole()) {
            throw new BusinessException("账号角色不匹配");
        }

     //2.比较账号密码对不对

       //2.1错误，抛异常 账号或密码有误
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ACCOUNT_OR_PASSWORD_ERROR);
        }

       //2.2正确，生成token，查询相关信息，返回loginVO

        String token = jwtUtils.generateToken(user.getUserId(), user.getRole().name(), loginDTO.isRememberMe());
        log.info("用户登录成功: {}, role: {}, rememberMe: {}",
                user.getUsername(), user.getRole(), loginDTO.isRememberMe());
        return LoginVO.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickName())
                .role(user.getRole().name())
                .avatar(user.getAvatarUrl())
                .expiresIn(loginDTO.isRememberMe()? jwtProperties.getLongTtl() /1000: jwtProperties.getShortTtl()/1000)
                .build();

    }
    /**
     * 管理员统一登录入口（支持 ADMIN / SUPER_ADMIN）
     */
    public LoginVO adminLogin(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 1. 查询用户是否存在
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ACCOUNT_OR_PASSWORD_ERROR));

        // 2. 检查是否为管理员角色（只允许 ADMIN 或 SUPER_ADMIN 登录管理后台）
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException("该账号无管理权限");
        }

        // 3. 校验密码
        log.info("输入的密码: {}, 数据库密文: {}", password, user.getPassword());
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        log.info("密码匹配结果: {}", matches);

        log.info("PasswordEncoder 类型: {}", passwordEncoder.getClass().getName());



        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 4. 生成 Token 并返回
        String token = jwtUtils.generateToken(
                user.getUserId(),
                user.getRole().name(),
                loginDTO.isRememberMe()
        );

        log.info("管理员登录成功: {}, role: {}", user.getUsername(), user.getRole());

        return LoginVO.builder()
                .token(token)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickName())
                .role(user.getRole().name())
                .avatar(user.getAvatarUrl())
                .expiresIn(loginDTO.isRememberMe() ?
                        jwtProperties.getLongTtl() / 1000 :
                        jwtProperties.getShortTtl() / 1000)
                .build();
    }

    private String generateNickName() {
        return "管理员" + RandomStringUtils.randomNumeric(6);
    }
    public AdminInitVO createAdmin(AdminCreateDTO adminCreateDTO) {
        // 1. 用户名唯一性校验
        if (userRepository.existsByUsername(adminCreateDTO.getUsername())) {
            throw new BusinessException("账号已被使用！");
        }

        // 2. 确定密码：如果前端没传，就自动生成
        String rawPassword;
        if (adminCreateDTO.getPassword() != null && !adminCreateDTO.getPassword().isBlank()) {
            rawPassword = adminCreateDTO.getPassword();
        } else {
            rawPassword = generateRandomPassword(); // 推荐方式
        }

        // 3. 构建用户
        User user = new User();
        user.setUsername(adminCreateDTO.getUsername());
        user.setPassword(passwordEncoder.encode(rawPassword)); // 加密存储
        user.setNickName(generateNickName());

        user.setRole(Role.ADMIN);

        // 4. 保存
        User saved = userRepository.save(user);

        // 5. 返回结果（包含明文密码！）
        return AdminInitVO.builder()
                .userId(saved.getUserId())
                .username(saved.getUsername())
                .nickname(saved.getNickName())
                .rawPassword(rawPassword)
                .build();
    }
    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*";
        String all = upper + lower + digits + symbols;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);

        // 确保至少包含每类字符中的一个
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(symbols.charAt(random.nextInt(symbols.length())));

        // 填充剩余长度
        for (int i = 4; i < 12; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }

        // 打乱顺序
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars); // e.g., "k7#Lm9$vQ2!p"
    }
    public String resetPasswordToRandom(Integer userId) {
        String rawPassword = generateRandomPassword();
        User user = userRepository.findById(userId) .orElseThrow(() -> new BusinessException("没有该用户"));
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        return rawPassword; // 返回明文
    }
}
