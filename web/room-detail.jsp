<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết Phòng học &amp; Tình trạng Đồ vật - F-CAMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css?v=20261025a">
</head>
<body>
    <div class="container-fluid px-0">
        <jsp:include page="/includes/header.jsp"/>

        <div style="margin-bottom: 12px; font-size: 13px;">
            <a href="${pageContext.request.contextPath}/rooms">&laquo; Quay lại Danh mục phòng học</a>
            <span>&nbsp;|&nbsp;</span>
            <a href="${pageContext.request.contextPath}/assets?roomId=${room.id}">Xem trên trang Tài sản &raquo;</a>
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
                    • <b>Tổng số đồ vật &amp; trang thiết bị trong phòng:</b> <b>${room.assetCount} tài sản</b>
                    <c:if test="${sessionScope.currentUser.admin}">
                        &nbsp;&mdash;&nbsp;
                        <a href="${pageContext.request.contextPath}/rooms?editId=${room.id}">[Sửa thông tin phòng]</a>
                    </c:if>
                </div>

                <!-- BẢNG TOÀN BỘ CÁC TÀI SẢN & ĐỒ VẬT BÊN TRONG PHÒNG HỌC -->
                <h2>1. Tình trạng các đồ vật và trang thiết bị bên trong phòng ${room.id}</h2>
                <c:if test="${sessionScope.currentUser.staffOrAdmin}">
                    <div style="margin-bottom: 10px; font-size: 13px;">
                        <a href="${pageContext.request.contextPath}/assets?roomId=${room.id}">[+ Thêm tài sản mới vào phòng ${room.id}]</a>
                    </div>
                </c:if>

                <div class="table-responsive" style="max-width: 1080px;">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 35px; text-align: center;">No.</th>
                                <th style="width: 130px;">Mã đồ vật</th>
                                <th>Tên đồ vật / Thiết bị &amp; Ghi chú tình trạng</th>
                                <th style="width: 120px;">Danh mục</th>
                                <th style="width: 130px;">Số serial</th>
                                <th style="width: 100px; text-align: center;">Ngày bàn giao</th>
                                <th style="width: 120px; text-align: right;">Đơn giá</th>
                                <th style="width: 125px; text-align: center;">Tình trạng</th>
                                <th style="width: 185px; text-align: center;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty room.assetList}">
                                    <tr>
                                        <td colspan="9" style="text-align: center; padding: 20px;">
                                            Phòng học <b>${room.id}</b> hiện chưa được gán tài sản nào.
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
                                                <a href="#reportDamageSection"
                                                   onclick="selectAssetForReport('${a.code}')"
                                                   style="color: #900; font-weight: bold;">[Báo hỏng]</a>
                                                <c:if test="${sessionScope.currentUser.staffOrAdmin}">
                                                    &nbsp;
                                                    <a href="${pageContext.request.contextPath}/assets?editCode=${a.code}">[Sửa]</a>
                                                    <a href="${pageContext.request.contextPath}/assets?transferCode=${a.code}">[Chuyển]</a>
                                                </c:if>
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

                <!-- PHẦN 2: FORM BÁO CÁO ĐỒ HỎNG HÓC TRONG PHÒNG HỌC CHO STAFF (Dành cho Giảng viên / Staff / Admin) -->
                <c:if test="${not empty room.assetList}">
                    <h2 id="reportDamageSection" style="margin-top: 22px;">2. Gửi báo cáo đồ hỏng hóc trong phòng ${room.id} cho Cán bộ Quản lý (Staff)</h2>
                    <fieldset style="max-width: 820px; border: 1px solid #000;">
                        <legend>Phiếu báo hỏng thiết bị / đồ vật phòng ${room.id}</legend>
                        <form action="${pageContext.request.contextPath}/assets" method="POST">
                            <input type="hidden" name="action" value="reportDamage">
                            <input type="hidden" name="returnRoomId" value="${room.id}">

                            <div class="form-group">
                                <label>Người gửi báo cáo:</label>
                                <b><c:out value="${sessionScope.currentUser.fullName}"/></b> (<fcams:roleName role="${sessionScope.currentUser.role}"/>)
                            </div>

                            <div class="form-group">
                                <label for="repAssetSelect">Chọn đồ vật bị hỏng (*):</label>
                                <select id="repAssetSelect" name="assetCode" required style="width: 440px;">
                                    <c:forEach var="a" items="${room.assetList}">
                                        <option value="${a.code}" ${selectedReportCode == a.code ? 'selected' : ''}>
                                            [${a.code}] ${a.name} (${a.category} - Hiện tại: ${a.statusDisplayName})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="repDamageNote">Mô tả tình trạng hỏng (*):</label>
                                <input type="text" id="repDamageNote" name="damageNote" required
                                       placeholder="Ví dụ: Máy chiếu không lên hình, điều hòa chảy nước, loa bị rè..."
                                       style="width: 440px;">
                            </div>

                            <div style="margin-left: 185px;">
                                <input type="submit" value="Gửi báo cáo đồ hỏng cho Staff">
                            </div>
                        </form>
                    </fieldset>
                </c:if>

                <div style="margin-top: 15px; font-size: 13px;">
                    <a href="${pageContext.request.contextPath}/rooms">&laquo; Quay lại Danh mục phòng học</a>
                </div>
            </c:otherwise>
        </c:choose>

        <jsp:include page="/includes/footer.jsp"/>
    </div>

    <script>
        function selectAssetForReport(code) {
            var sel = document.getElementById('repAssetSelect');
            if (sel) {
                sel.value = code;
            }
            var input = document.getElementById('repDamageNote');
            if (input) {
                input.focus();
            }
        }
    </script>
</body>
</html>
