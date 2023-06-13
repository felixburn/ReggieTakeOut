package com.example.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie_take_out.common.R;
import com.example.reggie_take_out.entity.Employee;
import com.example.reggie_take_out.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    // http://localhost:8080/employee/login
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //1、将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名来查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        //  因为我们的username设置了unique属性，即唯一
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回失败结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4、比对密码，如果不一致则返回失败结果
        //   注意这里的equals，因为前面已判空，所以不会发生空指针异常
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误");
        }

        //5、查看员工状态，如果已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将用户id存入Session并返回成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /**
     * 退出功能
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        // 1.清理session中的用户id
        //   登录时放入的：request.getSession().setAttribute("employee", emp.getId());
        request.getSession().removeAttribute("employee");
        // 2.返回结果（前端页面会进行跳转到登录页面）
        return R.success("退出成功");
    }

    /**
     * 新增员工employee
     * @param request
     * @param employee
     * @return
     */
    // http://localhost:8080/employee/login 在类上已经写了,这里不用写
    @PostMapping()
    // @RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的);
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee) {

        log.info("新增员工，员工信息：{}", employee.toString());
        //目前employee中还没有数据的有password/create_time/update_time/create_user/update_user
        //设置默认初始password:123456,需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //获取当前日期，设置到create_time/update_time中
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //获得当前登录用户的id
        //getAttribute()统一返回的是Object类型，注意类型转换
        Long empId = (Long) request.getSession().getAttribute("employee");
        //创建人的id,就是当前用户的id（在进行添加操作的id）
        employee.setCreateUser(empId);
        //更新人的id,此时为当前用户的id
        employee.setUpdateUser(empId);
        //mybatis提供的新增方法
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    // http://localhost:8080/employee/login/page?page=1&pageSize=10
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // page=1, pageSize=10, name=null 前端设置的默认值
        log.info("page={}, pageSize={}, name={}", page, pageSize, name);
        // 1.执行分页构造器
        Page pageInfo = new Page(page, pageSize);
        // 2.构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 3.添加过滤条件
        //   public Children like(boolean condition, R column, java.lang.Object val);
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 4.添加排序条件
        //   按更新时间updateTime倒序排序desc
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 5.执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }
}
