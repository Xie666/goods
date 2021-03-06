package cn.xie.goods.user.web.servlet;

import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;
import cn.xie.goods.user.domain.User;
import cn.xie.goods.user.service.UserService;
import cn.xie.goods.user.service.exception.UserException;
import com.sun.deploy.net.HttpRequest;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户模块web层
 * Created by baobiao on 2017/2/24.
 */
public class UserServlet extends BaseServlet {
    private UserService userService = new UserService();

    /**
     * ajax 用户名是否注册校验
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String ajaxValidateLoginname(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       /*
       * 1.获取用户名
       * */
        String username = req.getParameter("loginname");
        /*
       * 2.通过service得到校验结果
       * */
        boolean b = userService.ajaxValidateLoginname(username);
        /*
        * 3.发给客户端
        * */
        resp.getWriter().print(b);
        return null;
    }

    /*
   * ajax Email是否注册校验
   * */
    public String ajaxValidateEms(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*
       * 1.获取email
       * */
        String email = req.getParameter("email");
        /*
       * 2.通过service得到校验结果
       * */
        boolean b = userService.ajaxValidateEmail(email);
        /*
        * 3.发给客户端
        * */
        resp.getWriter().print(b);
        return null;
    }

    /**
     * ajax验证码是否正确校验
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String ajaxValidateVerifyCode(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 获取输入框中的验证码
		 */
        String verifyCode = req.getParameter("verifyCode");
        /*
         * 2. 获取图片上真实的校验码
		 */
        String vcode = (String) req.getSession().getAttribute("vCode");
        /*
         * 3. 进行忽略大小写比较，得到结果
		 */
        boolean b = verifyCode.equalsIgnoreCase(vcode);
        /*
         * 4. 发送给客户端
		 */
        resp.getWriter().print(b);
        return null;
    }

    /**
     * 注册功能
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String regist(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.封装表单到user对象
        User formUser = CommonUtils.toBean(req.getParameterMap(), User.class);
           /*
         * 2. 校验之, 如果校验失败，保存错误信息，返回到regist.jsp显示
		 */
        Map<String, String> errors = validateRegist(formUser, req.getSession());
        if (errors.size() > 0) {
            req.setAttribute("form", formUser);
            req.setAttribute("errors", errors);
            return "f:/jsps/user/regist.jsp";
        }
        /*
         * 3. 使用service完成业务
		 */
        userService.regist(formUser);
        /*
         * 4. 保存成功信息，转发到msg.jsp显示！
		 */
        req.setAttribute("code", "success");
        req.setAttribute("msg", "注册功能，请马上到邮箱激活！");
        return "f:/jsps/msg.jsp";
    }


    /**
     * 注册校验
     * 对表单的字段进行逐个校验，如果有错误，使用当前字段名称为key，错误信息为value，保存到map中
     * 返回map
     *
     * @param formUser
     * @param session
     * @return
     */
    private Map<String, String> validateRegist(User formUser, HttpSession session) {
        Map<String, String> errors = new HashMap<String, String>();
        /*
         * 1. 校验登录名
		 */
        String loginname = formUser.getLoginname();
        if (loginname == null || loginname.trim().isEmpty()) {
            errors.put("loginname", "用户名不能为空！");
        } else if (loginname.length() < 3 || loginname.length() > 20) {
            errors.put("loginname", "用户名长度必须在3~20之间！");
        } else if (!userService.ajaxValidateLoginname(loginname)) {
            errors.put("loginname", "用户名已被注册！");
        }

		/*
         * 2. 校验登录密码
		 */
        String loginpass = formUser.getLoginpass();
        if (loginpass == null || loginpass.trim().isEmpty()) {
            errors.put("loginpass", "密码不能为空！");
        } else if (loginpass.length() < 3 || loginpass.length() > 20) {
            errors.put("loginpass", "密码长度必须在3~20之间！");
        }

		/*
         * 3. 确认密码校验
		 */
        String reloginpass = formUser.getReloginpass();
        if (reloginpass == null || reloginpass.trim().isEmpty()) {
            errors.put("reloginpass", "确认密码不能为空！");
        } else if (!reloginpass.equals(loginpass)) {
            errors.put("reloginpass", "两次输入不一致！");
        }

		/*
         * 4. 校验email
		 */
        String email = formUser.getEmail();
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email不能为空！");
        } else if (!email.matches("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$")) {
            errors.put("email", "Email格式错误！");
        } else if (!userService.ajaxValidateEmail(email)) {
            errors.put("email", "Email已被注册！");
        }

		/*
         * 5. 验证码校验
		 */
        String verifyCode = formUser.getVerifyCode();
        String vcode = (String) session.getAttribute("vCode");
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            errors.put("verifyCode", "验证码不能为空！");
        } else if (!verifyCode.equalsIgnoreCase(vcode)) {
            errors.put("verifyCode", "验证码错误！");
        }

        return errors;
    }

    /**
     * 激活
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String activation(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String activitionCode = req.getParameter("code");
        try {
            userService.activatioin(activitionCode);
            req.setAttribute("code", "success");//通知msg.jsp显示对号
            req.setAttribute("msg", "恭喜，激活成功，请马上登录！");
        } catch (UserException e) {
            // 说明service抛出了异常
            req.setAttribute("msg", e.getMessage());
            req.setAttribute("code", "error");//通知msg.jsp显示X
        }
        return "f:/jsps/msg.jsp";
    }

    /**
     * 更改密码
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String updatePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 封装表单数据到User
		 * 2. 校验表单数据
		 * 3. 使用service查询，得到User
		 * 4. 查看用户是否存在，如果不存在：
		 *   * 保存错误信息：用户名或密码错误
		 *   * 保存用户数据：为了回显
		 *   * 转发到login.jsp
		 * 5. 如果存在，查看状态，如果状态为false：
		 *   * 保存错误信息：您没有激活
		 *   * 保存表单数据：为了回显
		 *   * 转发到login.jsp
		 * 6. 登录成功：
		 * 　　* 保存当前查询出的user到session中
		 *   * 保存当前用户的名称到cookie中，注意中文需要编码处理。
		 */
        /*
         * 1. 封装表单数据到user
		 */
        User formUser = CommonUtils.toBean(req.getParameterMap(), User.class);
        /*
         * 2. 校验
		 */
        Map<String, String> errors = validatePass(formUser);
        if (errors.size() > 0) {
            req.setAttribute("form", formUser);
            req.setAttribute("errors", errors);
            return "f:/jsps/user/login.jsp";
        }
        //3调用service
        try {
            userService.updatePass(formUser);
            // 说明service抛出了异常
            req.setAttribute("msg", "修改成功！");
            req.setAttribute("code", "success");//通知msg.jsp显示
            return "f:/jsps/msg.jsp";
        } catch (UserException e) {
            // 说明service抛出了异常
            req.setAttribute("msg", e.getMessage());
            req.setAttribute("code", "error");//通知msg.jsp显示X
        }

        return null;
    }

    /**
     * 登录功能
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        /*
         * 1. 封装表单数据到User
		 * 2. 校验表单数据
		 * 3. 使用service查询，得到User
		 * 4. 查看用户是否存在，如果不存在：
		 *   * 保存错误信息：用户名或密码错误
		 *   * 保存用户数据：为了回显
		 *   * 转发到login.jsp
		 * 5. 如果存在，查看状态，如果状态为false：
		 *   * 保存错误信息：您没有激活
		 *   * 保存表单数据：为了回显
		 *   * 转发到login.jsp
		 * 6. 登录成功：
		 * 　　* 保存当前查询出的user到session中
		 *   * 保存当前用户的名称到cookie中，注意中文需要编码处理。
		 */
        /*
         * 1. 封装表单数据到user
		 */
        User formUser = CommonUtils.toBean(req.getParameterMap(), User.class);
        /*
		 * 2. 校验
		 */
        Map<String, String> errors = validateLogin(formUser, req.getSession());

        if (errors.size() > 0) {
            req.setAttribute("form", formUser);
            req.setAttribute("errors", errors);
            return "f:/jsps/user/login.jsp";
        }

		/*
		 * 3. 调用userService#login()方法
		 */
        User user = userService.login(formUser);
		/*
		 * 4. 开始判断
		 */
        if (user == null) {
            req.setAttribute("msg", "用户名或密码错误！");
            req.setAttribute("user", formUser);
            return "f:/jsps/user/login.jsp";
        } else {
            if (!user.isStatus()) {
                req.setAttribute("msg", "您还没有激活！");
                req.setAttribute("user", formUser);
                return "f:/jsps/user/login.jsp";
            } else {
                // 保存用户到session
                req.getSession().setAttribute("sessionUser", user);
                // 获取用户名保存到cookie中
                String loginname = user.getLoginname();
                loginname = URLEncoder.encode(loginname, "utf-8");
                Cookie cookie = new Cookie("loginname", loginname);
                cookie.setMaxAge(60 * 60 * 24 * 10);//保存10天
                resp.addCookie(cookie);
                return "r:/index.jsp";//重定向到主页
            }
        }
    }

    /**
     * 登录验证
     *
     * @param formUser
     * @param session
     * @return
     */
    private Map<String, String> validateLogin(User formUser, HttpSession session) {
        Map<String, String> errors = new HashMap<String, String>();
        /*
		 * 1. 校验登录名
		 */
        String loginname = formUser.getLoginname();
        if (loginname == null || loginname.trim().isEmpty()) {
            errors.put("loginname", "用户名不能为空！");
        }
        if (loginname.length() < 3 || loginname.length() > 20) {
            errors.put("loginname", "用户名长度必须在3~20之间！");
        }

		/*
		 * 2. 校验登录密码
		 */
        String loginpass = formUser.getLoginpass();
        if (loginpass == null || loginpass.trim().isEmpty()) {
            errors.put("loginpass", "密码不能为空！");
        } else if (loginpass.length() < 3 || loginpass.length() > 20) {
            errors.put("loginpass", "密码长度必须在3~20之间！");
        }
        /**
         * 3.校验验证码
         */
        String verifyCode = formUser.getVerifyCode();
        String vcode = (String) session.getAttribute("vCode");
        boolean b = verifyCode.equalsIgnoreCase(vcode);
        if(!b){
            errors.put("vCode","错误的验证码！");
        }

        return errors;
    }

    /**
     * 登录验证
     *
     * @param formUser
     * @return
     */
    private Map<String, String> validatePass(User formUser) {
        Map<String, String> errors = new HashMap<String, String>();

		/*
		 *  校验登录密码
		 */
        String loginpass = formUser.getLoginpass();
        if (loginpass == null || loginpass.trim().isEmpty()) {
            errors.put("loginpass", "密码不能为空！");
        } else if (loginpass.length() < 3 || loginpass.length() > 20) {
            errors.put("loginpass", "密码长度必须在3~20之间！");
        }
        //校验旧密码
        User user = userService.findByNameAndPass(formUser);
        if (user == null) {
            errors.put("loginpass", "旧密码错误！");
        }
        return errors;
    }

    /**
     * 退出登录
     *
     * @param req
     * @param resp
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public String quit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.获取Session
        HttpSession session = req.getSession();
        //2.销毁Session
        session.invalidate();
        //3.重定向到登录页面
        return "r:/jsps/user/login.jsp";
    }
}
