package com.example.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie_take_out.common.R;
import com.example.reggie_take_out.entity.User;
import com.example.reggie_take_out.service.UserService;
import com.example.reggie_take_out.utils.SMSUtils;
import com.example.reggie_take_out.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 向指定手机发送验证码短信
     * @param user (user.getPhone())
     * @param session
     * @return
     */
    // http://localhost:8080/user/sendMsg
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone=user.getPhone();
        if(!StringUtils.isEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);
            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("手机短信发送失败");
    }

    /**
     * 移动端用户登录(验证验证码是否一致)
     * @param map
     * @param session
     * @return
     */
    // http://localhost:8080/user/login
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        log.info("map:{}", map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        //进行验证码比对（页面提交的验证码和Session中保存的验证码比对）
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果能够比对成功，说明登录成功
            //判断当前手机号是否为新用户，如果是新用户则自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            //新用户，注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登陆失败");
    }
}
