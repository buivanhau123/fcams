<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Phòng học &amp; Tài sản trang bị - F-CAMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
    <div class="container-fluid px-0">
        <jsp:include page="/includes/header.jsp"/>

        <div style="margin-bottom: 12px; font-size: 13px;">
            <a href="${pageContext.request.contextPath}/rooms">&laquo; Quay lại Danh mục phòng học</a>
            <span>&nbsp;|&nbsp;</span>
            <a href="${pageContext.request.contextPath}/assets?roomId=${room.id}">Xem trên trang Quản lý Tài sản &raquo;</a>
        </div>

        <c:choose>
            <c:when test="${empty room}">
                <div class="alert-box">
                    <b>Lỗi:</b> Không tìm thấy thông tin phòng học có mã <b><c:out value="${roomId}"/></b>!
                    <br><a href="${pageContext.request.contextPath}/rooms">&laquo; Quay lại danh sách phòng học</a>
                </div>
            </c:when>
            <c:otherwise>
                <h1 id="roomTitle">
                    Chi tiết Phòng học: ${room.id} (Tòa ${room.building} &mdash; Tầng ${room.floor})
                </h1>

                <!-- Thông số cơ bản của phòng học (Tiêu chí 4) -->
                <div class="alert-box" style="max-width: 900px; font-size: 13px; line-height: 1.8;">
                    <b>Thông tin phòng học:</b><br>
                    • <b>Mục đích sử dụng:</b> <c:out value="${room.purpose}"/><br>
                    • <b>Sức chứa / Chỗ ngồi:</b> <fcams:chairStatus cur="${room.cur}" std="${room.std}"/><br>
                    • <b>Tổng số tài sản &amp; trang thiết bị đang trang bị:</b> <b>${room.assetCount} tài sản</b>
                    <c:if test="${sessionScope.currentUser.admin}">
                        &nbsp;&mdash;&nbsp;
                        <a href="${pageContext.request.contextPath}/rooms?editId=${room.id}">[Sửa thông tin phòng]</a>
                    </c:if>
                </div>

                <!-- BẢNG TOÀN BỘ CÁC TÀI SẢN & TRANG THIẾT BỊ TRANG BỊ BÊN TRONG PHÒNG HỌC (Tiêu chí 4) -->
                <h2>Toàn bộ tài sản và trang thiết bị hiện đang được trang bị bên trong phòng ${room.id}</h2>
                <div style="margin-bottom: 10px; font-size: 13px;">
                    <a href="${pageContext.request.contextPath}/assets?roomId=${room.id}">[+ Thêm tài sản mới vào phòng ${room.id}]</a>
                </div>

                <div class="table-responsive" style="max-width: 1050px;">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 35px; text-align: center;">No.</th>
                                <th style="width: 130px;">Mã tài sản</th>
                                <th>Tên tài sản &amp; Mô tả</th>
                                <th style="width: 120px;">Danh mục</th>
                                <th style="width: 130px;">Số serial</th>
                                <th style="width: 100px; text-align: center;">Ngày bàn giao</th>
                                <th style="width: 120px; text-align: right;">Đơn giá</th>
                                <th style="width: 125px; text-align: center;">Tình trạng</th>
                                <th style="width: 160px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty room.assetList}">
                                    <tr>
                                        <td colspan="9" style="text-align: center; padding: 20px;">
                                            Phòng học <b>${room.id}</b> hiện chưa được gán tài sản nào.
                                            <br>
                                            <a href="${pageContext.request.contextPath}/assets?roomId=${room.id}">[Nhấn vào đây để thêm tài sản vào phòng này]</a>
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="a" items="${room.assetList}" varStatus="loop">
                                        <tr>
                                            <td style="text-align: center;">${loop.index + 1}</td>
                                            <td><b>${a.code}</b></td>
                                            <td>
                                                <b><c:out value="${a.name}"/></b>
                                                <c:if test="${not empty a.note}">
                                                    <br><small style="color: #444;">Ghi chú: <c:out value="${a.note}"/></small>
                                                </c:if>
                                            </td>
                                            <td><c:out value="${a.category}"/></td>
                                            <td><code><c:out value="${not empty a.serialNumber ? a.serialNumber : '-'}"/></code></td>
                                            <td style="text-align: center;">${a.handoverDate}</td>
                                            <td style="text-align: right;">${a.formattedPrice}</td>
                                            <td style="text-align: center;"><fcams:assetStatus status="${a.status}"/></td>
                                            <td style="text-align: center;">
                                                <a href="${pageContext.request.contextPath}/assets?editCode=${a.code}">[Sửa]</a>
                                                <a href="${pageContext.request.contextPath}/assets?transferCode=${a.code}">[Chuyển]</a>
                                                <c:if test="${sessionScope.currentUser.admin}">
                                                    <form action="${pageContext.request.contextPath}/assets" method="POST" style="display:inline; margin:0;"
                                                          onsubmit="return confirm('Bạn có chắc chắn muốn xóa tài sản [${a.code}] khỏi hệ thống?');">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="code" value="${a.code}">
                                                        <button type="submit" class="btn-link" style="color:red; background:none; border:none; padding:0; font-size:12px; cursor:pointer;">[Xóa]</button>
                                                    </form>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <div style="margin-top: 15px; font-size: 13px;">
                    <a href="${pageContext.request.contextPath}/rooms">&laquo; Quay lại Danh mục phòng học</a>
                </div>
            </c:otherwise>
        </c:choose>

        <jsp:include page="/includes/footer.jsp"/>
    </div>
</body>
</html>
