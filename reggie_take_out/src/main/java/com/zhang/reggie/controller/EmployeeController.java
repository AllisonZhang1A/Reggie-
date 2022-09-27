package com.zhang.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhang.reggie.common.R;
import com.zhang.reggie.entity.Employee;
import com.zhang.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    /**
     * 员工登录
     */
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request,@RequestBody Employee employee) {//返回结果是一个R类型
        //1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查看数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);//数据库对username字段做了唯一约束
        //3.如果没有查询到就返回登录失败结果
        if (emp == null) {
            return R.error("登录失败");
        }
        //4、密码对比，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误");
        }
        //5、查看员工状态，如果已为禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {//0-禁用，1-非禁用
            return R.error("账号已禁用");
        }
        //6、登录成功，将员工id存入session并返回登录成功结果
        request.getSession().setAttribute("employee", employee.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中保存的当前登录员工Id
        request.getSession().removeAttribute("employee");
        return  R.success("退出成功");
    }
}
