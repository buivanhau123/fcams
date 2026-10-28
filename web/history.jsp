<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử Thay đổi &amp; Báo cáo Hỏng hóc - F-CAMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css?v=20261025a">
</head>
<body>
    <div class="container-fluid px-0">
        <jsp:include page="/includes/header.jsp"/>

        <h1>Nhật ký Lịch sử Thay đổi &amp; Báo cáo Đồ Hỏng hóc</h1>
        <p style="font-size: 13px; margin-bottom: 10px;">
            <i>(Dành riêng cho Quản trị viên (Admin) và Cán bộ Quản lý (Staff) theo dõi toàn bộ thay đổi tài sản, phòng học và các báo cáo đồ hỏng hóc từ Giảng viên)</i>
        </p>

        <!-- Bộ lọc Lịch sử -->
        <div class="filter-box">
            <form action="${pageContext.request.contextPath}/history" method="GET" class="row g-2 align-items-center" style="margin: 0;">
                <div class="col-auto">
                    <label for="actionFilter"><b>Loại thay đổi:</b></label>
                    <select id="actionFilter" name="actionType" onchange="this.form.submit()">
                        <option value="ALL" ${selectedAction == 'ALL' ? 'selected' : ''}>-- Tất cả thao tác --</option>
                        <option value="REPORT_DAMAGE" ${selectedAction == 'REPORT_DAMAGE' ? 'selected' : ''}>Báo cáo đồ hỏng</option>
                        <option value="CREATE_ASSET" ${selectedAction == 'CREATE_ASSET' ? 'selected' : ''}>Thêm tài sản</option>
                        <option value="UPDATE_ASSET" ${selectedAction == 'UPDATE_ASSET' ? 'selected' : ''}>Cập nhật tài sản</option>
                        <option value="TRANSFER_ASSET" ${selectedAction == 'TRANSFER_ASSET' ? 'selected' : ''}>Chuyển phòng tài sản</option>
                        <option value="DELETE_ASSET" ${selectedAction == 'DELETE_ASSET' ? 'selected' : ''}>Xóa tài sản</option>
                        <option value="CREATE_ROOM" ${selectedAction == 'CREATE_ROOM' ? 'selected' : ''}>Thêm phòng học</option>
                        <option value="UPDATE_ROOM" ${selectedAction == 'UPDATE_ROOM' ? 'selected' : ''}>Cập nhật phòng học</option>
                        <option value="DELETE_ROOM" ${selectedAction == 'DELETE_ROOM' ? 'selected' : ''}>Xóa phòng học</option>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="roomFilter"><b>Phòng học:</b></label>
                    <select id="roomFilter" name="roomId" onchange="this.form.submit()">
                        <option value="ALL" ${selectedRoom == 'ALL' ? 'selected' : ''}>-- Tất cả các phòng --</option>
                        <c:forEach var="r" items="${rooms}">
                            <option value="${r.id}" ${selectedRoom == r.id ? 'selected' : ''}>${r.id} (Tòa ${r.building})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="roleFilter"><b>Vai trò thực hiện:</b></label>
                    <select id="roleFilter" name="roleFilter" onchange="this.form.submit()">
                        <option value="ALL" ${selectedRole == 'ALL' ? 'selected' : ''}>-- Tất cả vai trò --</option>
                        <option value="LECTURER" ${selectedRole == 'LECTURER' ? 'selected' : ''}>Giảng viên (Lecturer)</option>
                        <option value="STAFF" ${selectedRole == 'STAFF' ? 'selected' : ''}>Cán bộ Quản lý (Staff)</option>
                        <option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>Quản trị viên (Admin)</option>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="keywordInput"><b>Tìm kiếm:</b></label>
                    <input type="text" id="keywordInput" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Mã, tên đồ vật, người thực hiện...">
                    <button type="submit">Lọc</button>
                    <a href="${pageContext.request.contextPath}/history" class="btn-classic">Tất cả</a>
                </div>
            </form>
        </div>

        <div style="margin-bottom: 10px; font-size: 13px; font-weight: bold;">
            Hiển thị ${historyList.size()} bản ghi lịch sử thay đổi.
        </div>

        <!-- BẢNG LỊCH SỬ THAY ĐỔI -->
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 35px; text-align: center;">No.</th>
                        <th style="width: 120px; text-align: center;">Thời gian</th>
                        <th style="width: 130px; text-align: center;">Loại thao tác</th>
                        <th style="width: 180px;">Đối tượng (Mã &amp; Tên)</th>
                        <th style="width: 75px; text-align: center;">Phòng</th>
                        <th style="width: 165px;">Trước thay đổi</th>
                        <th style="width: 165px;">Sau thay đổi</th>
                        <th>Nội dung chi tiết / Mô tả báo hỏng</th>
                        <th style="width: 165px;">Người thực hiện</th>
                        <th style="width: 90px; text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty historyList}">
                            <tr>
                                <td colspan="10" style="text-align: center; padding: 20px;">
                                    Chưa có bản ghi lịch sử thay đổi nào phù hợp điều kiện lọc.
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="h" items="${historyList}" varStatus="loop">
                                <tr>
                                    <td style="text-align: center;">${loop.index + 1}</td>
                                    <td style="text-align: center;">${h.createdAt}</td>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${h.actionType == 'REPORT_DAMAGE'}">
                                                <b style="color: #900;">${h.actionDisplayName}</b>
                                            </c:when>
                                            <c:otherwise>
                                                <b>${h.actionDisplayName}</b>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <b><c:out value="${h.targetCode}"/></b><br>
                                        <small><c:out value="${h.targetName}"/></small>
                                    </td>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${not empty h.roomId}">
                                                <a href="${pageContext.request.contextPath}/room-detail?id=${h.roomId}"><b>${h.roomId}</b></a>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><small><c:out value="${h.oldValue}"/></small></td>
                                    <td><small><b><c:out value="${h.newValue}"/></b></small></td>
                                    <td><c:out value="${h.description}"/></td>
                                    <td>
                                        <b><c:out value="${h.performerName}"/></b><br>
                                        <small>(<c:out value="${h.performedBy}"/> &mdash; <fcams:roleName role="${h.performerRole}"/>)</small>
                                    </td>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${h.targetType == 'ASSET' and h.actionType != 'DELETE_ASSET'}">
                                                <a href="${pageContext.request.contextPath}/assets?editCode=${h.targetCode}">[Xử lý / Sửa]</a>
                                            </c:when>
                                            <c:when test="${h.targetType == 'ROOM' and h.actionType != 'DELETE_ROOM'}">
                                                <a href="${pageContext.request.contextPath}/room-detail?id=${h.targetCode}">[Xem phòng]</a>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <jsp:include page="/includes/footer.jsp"/>
    </div>
</body>
</html>
