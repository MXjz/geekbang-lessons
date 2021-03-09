package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.domain.group.ValidateStrategy;
import org.geektimes.projects.user.service.impl.UserServiceImpl;
import org.geektimes.web.mvc.controller.PageController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注册controller
 * @author xuejz
 * @description
 * @Time 2021/3/1 11:54
 */
@Path("/register")
public class RegisterController implements PageController {

    @Resource(name = "bean/UserService")
    private UserServiceImpl userService;

    private static final Class<?> REGISTER_GROUP = ValidateStrategy.UserRegister.class;
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
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> validateRes = validator.validate(user, REGISTER_GROUP);
        if(!validateRes.isEmpty()) {
            List<String> errorMsg = validateRes.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
            request.setAttribute("errorMsg", errorMsg);
            //request.getRequestDispatcher("registerFailed.jsp").forward(request, response);
            return "registerFailed.jsp";
        }
        // 校验通过, 调用register方法
        userService.register(user);
        return "registerSuccess.jsp";
    }
}
