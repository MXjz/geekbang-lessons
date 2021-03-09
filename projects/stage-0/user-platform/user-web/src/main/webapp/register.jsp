<%--
  Created by IntelliJ IDEA.
  User: jzxue
  Date: 2021/2/28
  Time: 23:20
  To change this template use File | Settings | File Templates.
--%>
<head>
    <jsp:directive.include file="/WEB-INF/jsp/prelude/include-head-meta.jspf" />
    <title>注册</title>
</head>
<body>
    <div class="container">
        <form class="form-signup" method="post" action="/register">
            <h1 class="h3 mb-3 font-weight-normal">注册</h1>
            <div class="form-group">
                <label for="inputName" class="sr-only">请输入姓名</label>
                <input id="inputName" class="form-control" name="name" placeholder="请输入姓名" autofocus>
            </div>
            <div class="form-group">
                <label for="inputhoneNumber" class="sr-only">请输入电子邮件</label>
                <input id="inputhoneNumber" class="form-control" name="phoneNumber" placeholder="请输入电话号码">
            </div>
            <div class="form-group">
                <label for="inputEmail" class="sr-only">请输入电子邮件</label>
                <input type="email" id="inputEmail" class="form-control" name="email" placeholder="请输入电子邮件">
            </div>
            <div class="form-group">
                <label for="inputPassword" class="sr-only">Password</label>
                <input type="password" id="inputPassword" class="form-control" name="password" placeholder="请输入密码">
            </div>
            <div class="form-group">
                <label for="confirmPassword" class="sr-only">Confirm Password</label>
                <input type="password" id="confirmPassword" class="form-control" name="confirmPassword" placeholder="请确认密码">
            </div>
            <button class="btn btn-lg btn-primary btn-block" type="submit">注册</button>
            <button class="btn btn-lg btn-secondary btn-block" type="button" onclick="back()">返回</button>
        </form>
    </div>
    <script>
        /*function pageInit() {
            $('form').bootstrapValidator({
                message: 'This value is not valid',
                feedbackIcons: {
                    valid: 'glyphicon glyphicon-ok',
                    invalid: 'glyphicon glyphicon-remove',
                    validating: 'glyphicon glyphicon-refresh'
                },
                fields: {
                    name: {
                        validators: {
                            notEmpty: {
                                message: '姓名不能为空'
                            }
                        }
                    },
                    phoneNumber: {
                        validators: {
                            notEmpty: {
                                message: '电话号码不能为空'
                            },
                            regexp: {
                                regexp: /\d{8}|\d{4}-\{7,8}/,
                                message: '电话号码格式不正确'
                            }
                        }
                    },
                    email: {
                        validators: {
                            notEmpty: {
                                message: '邮箱地址不能为空'
                            },
                            emailAddress: {
                                message: '邮箱地址格式有误'
                            }
                        }
                    },
                    password: {
                        validators: {
                            notEmpty: {
                                message: '密码不能为空'
                            }
                        }
                    },
                    confirmPassword: {
                        validators: {
                            notEmpty: {
                                message: '密码不能为空'
                            },
                            identical : {
                                field : 'password',
                                message : '两次填写的密码不一致！'
                            }
                        }
                    }
                }
            })
        }*/

        function back() {
            window.location.href='login-form.jsp'
        }
    </script>
</body>
