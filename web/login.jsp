<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Page - F-CAMS Simple</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
    <div class="container-fluid px-0">
        <h1>Login Page</h1>

        <c:if test="${not empty errorMsg}">
            <div class="alert-box" style="max-width: 500px; font-weight: bold;">
                <c:out value="${errorMsg}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="POST" style="margin-bottom: 15px;">
            <div style="margin-bottom: 8px;">
                <label for="username" style="display: inline-block; width: 90px; font-weight: bold;">Username</label>
                <input type="text" id="username" name="username" value="${enteredUsername}" required>
            </div>

            <div style="margin-bottom: 12px;">
                <label for="password" style="display: inline-block; width: 90px; font-weight: bold;">Password</label>
                <input type="password" id="password" name="password" value="" required>
            </div>

            <div>
                <input type="submit" value="Login">
                <input type="reset" value="Reset">
            </div>
        </form>

        <p>Need a new account? <a href="javascript:alert('Chức năng đăng ký tài khoản mới do Quản trị viên cấp phép.')">Register here</a></p>
        <p><a href="javascript:alert('Hệ thống Quản lý tài sản phòng học Đại học FPT Hòa Lạc (PRJ301 - MVC Servlet/JSP/SQL Server).')">Click here to get more knowledge</a></p>
    </div>
</body>
</html>
