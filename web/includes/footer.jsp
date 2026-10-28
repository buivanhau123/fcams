<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="footer-text">
    Hệ thống Quản lý Tài sản Phòng học (F-CAMS Simple) — Dự án môn PRJ301 (Java Web: MVC + JSP/JSTL/CustomTag + Servlet + Filter + SQL Server JDBC + Bootstrap 5)<br>
    <c:if test="${sessionScope.currentUser.staffOrAdmin}">
        <form action="${pageContext.request.contextPath}/rooms" method="POST" style="display: inline; margin: 0;" onsubmit="return confirm('Đặt lại toàn bộ dữ liệu CSDL về trạng thái mẫu ban đầu?');">
            <input type="hidden" name="action" value="reset">
            <button type="submit" style="background: none; border: none; padding: 0; color: #0000ee; text-decoration: underline; font-size: 12px;">[Đặt lại dữ liệu ban đầu]</button>
        </form>
    </c:if>
</div>
