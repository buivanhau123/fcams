<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>

<div class="header-bar">
    <div class="header-top-row">
        <a class="brand" href="${pageContext.request.contextPath}/dashboard">
            <span class="brand-mark">F</span>
            <span class="brand-text"><b>F-CAMS</b><small>Quản lý tài sản phòng học</small></span>
        </a>
        <div class="header-user">
            <div class="welcome-msg">
                Xin chào, <b><c:out value="${sessionScope.currentUser.fullName}"/></b> (<fcams:roleName role="${sessionScope.currentUser.role}"/>)
            </div>
            <div class="header-logout">
                <a href="${pageContext.request.contextPath}/logout" class="logout-link">Đăng xuất</a>
            </div>
        </div>
    </div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/dashboard" class="${activeTab == 'dashboard' ? 'active' : ''}">Home</a>
        <a href="${pageContext.request.contextPath}/assets" class="${activeTab == 'assets' ? 'active' : ''}">Tài sản</a>
        <a href="${pageContext.request.contextPath}/rooms" class="${activeTab == 'rooms' ? 'active' : ''}">Phòng học</a>
        <c:if test="${sessionScope.currentUser.staffOrAdmin}">
            <a href="${pageContext.request.contextPath}/history" class="${activeTab == 'history' ? 'active' : ''}">Lịch sử</a>
        </c:if>
    </div>
</div>

<c:if test="${not empty param.msg}">
    <div class="alert-box" style="margin-top: 10px; border-left: 4px solid #000;">
        <b>Thông báo:</b> <c:out value="${param.msg}"/>
    </div>
</c:if>

<c:if test="${not empty param.error}">
    <div class="alert-box" style="margin-top: 10px; border: 2px solid #b00; color: #900; background-color: #fff5f5;">
        <b>Cảnh báo / Lỗi:</b> <c:out value="${param.error}"/>
    </div>
</c:if>
