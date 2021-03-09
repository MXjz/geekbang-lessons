<%--
  Created by IntelliJ IDEA.
  User: jzxue
  Date: 2021/3/1
  Time: 22:37
  To change this template use File | Settings | File Templates.
--%>
<head>
    <jsp:directive.include file="/WEB-INF/jsp/prelude/include-head-meta.jspf" />
    <title>注册</title>
    <style>
        .btn-pos {
            display: block;
            margin: 0 auto;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>注册失败, 失败原因:</h2>
        <ol>
            <c:forEach items="${errorMsg}" var="item">
                <li>${item}</li>
            </c:forEach>
        </ol>
        <button class="btn btn-default btn-secondary btn-pos" type="button" onclick="back()">返回</button>
    </div>
    <script>
        function back() {
            window.location.href='register.jsp'
        }
    </script>
</body>
