package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.impl.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * 注册controller
 * @author xuejz
 * @description
 * @Time 2021/3/1 11:54
 */
@Path("/register")
public class RegisterController implements PageController {

    /**
     * @param request  HTTP 请求
     * @param response HTTP 相应
     * @return 视图地址路径
     * @throws Throwable 异常发生时
     */
    @POST
    public String execute(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        // 获取form表单参数, 转换成实体类
        String name =request.getParameter("name");
        String email =request.getParameter("email");
        String password =request.getParameter("password");
        String phoneNumber =request.getParameter("phoneNumber");
        User user = new User();
        user.setName(name);
        user.setPhoneNumber(phoneNumber);
        user.setEmail(email);
        user.setPassword(password);
        // 调用register方法
        UserServiceImpl userService = new UserServiceImpl();
        boolean isSuccess = userService.register(user);
        return isSuccess? "registerSuccess.jsp" : "registerFailed.jsp";
    }
}
