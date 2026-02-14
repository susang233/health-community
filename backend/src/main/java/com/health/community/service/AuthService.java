package com.health.community.service;
import com.health.community.common.exception.BusinessException;
import com.health.community.common.properties.JwtProperties;
import com.health.community.common.util.JwtUtils;
import com.health.community.dto.LoginDTO;

import com.health.community.entity.User;
import com.health.community.repository.UserRepository;
import com.health.community.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public LoginVO login(LoginDTO loginDTO){

        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        //1.查询用户是否存在
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ACCOUNT_OR_PASSWORD_ERROR));

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
                .username(user.getUsername())
                .nickname(user.getNickName())
                .role(user.getRole().name())
                .expiresIn(loginDTO.isRememberMe()? jwtProperties.getLongTtl() /1000: jwtProperties.getShortTtl()/1000)
                .build();

    }


}
