<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Phòng học (Room Management) - F-CAMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css?v=20261025a">
</head>
<body>
    <div class="container-fluid px-0">
        <jsp:include page="/includes/header.jsp"/>

        <h1>Quản lý Phòng học (Room Management - CRUD)</h1>

        <!-- Bộ lọc phòng học -->
        <div class="filter-box">
            <form action="${pageContext.request.contextPath}/rooms" method="GET" class="row g-2 align-items-center" style="margin: 0;">
                <div class="col-auto">
                    <label for="buildingFilter"><b>Tòa nhà:</b></label>
                    <select id="buildingFilter" name="building" onchange="this.form.submit()">
                        <option value="ALL" ${selectedBuilding == 'ALL' ? 'selected' : ''}>-- Tất cả các tòa --</option>
                        <option value="Alpha" ${selectedBuilding == 'Alpha' ? 'selected' : ''}>Tòa Alpha</option>
                        <option value="Beta" ${selectedBuilding == 'Beta' ? 'selected' : ''}>Tòa Beta</option>
                        <option value="Delta" ${selectedBuilding == 'Delta' ? 'selected' : ''}>Tòa Delta</option>
                        <option value="Epsilon" ${selectedBuilding == 'Epsilon' ? 'selected' : ''}>Tòa Epsilon</option>
                        <option value="Gamma" ${selectedBuilding == 'Gamma' ? 'selected' : ''}>Tòa Gamma</option>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="statusFilter"><b>Trạng thái:</b></label>
                    <select id="statusFilter" name="status" onchange="this.form.submit()">
                        <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>-- Tất cả phòng --</option>
                        <option value="ISSUES" ${selectedStatus == 'ISSUES' ? 'selected' : ''}>Phòng có vấn đề (Thiếu ghế / Hỏng máy)</option>
                        <option value="OK" ${selectedStatus == 'OK' ? 'selected' : ''}>Phòng hoạt động bình thường</option>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="searchInput"><b>Tìm kiếm:</b></label>
                    <input type="text" id="searchInput" name="search" value="<c:out value='${searchQuery}'/>" placeholder="Mã phòng, mục đích...">
                    <button type="submit">Tìm kiếm</button>
                    <a href="${pageContext.request.contextPath}/rooms" class="btn-classic">Tất cả</a>
                </div>
            </form>
        </div>

        <div style="margin-bottom: 10px; font-size: 13px; font-weight: bold;">
            Hiển thị ${rooms.size()} phòng (Tổng số trong hệ thống: ${totalSystemRooms} phòng).
            <c:if test="${sessionScope.currentUser.admin}">
                &nbsp;&mdash;&nbsp;
                <a href="javascript:void(0)" onclick="document.getElementById('addRoomBox').style.display = (document.getElementById('addRoomBox').style.display === 'none' ? 'block' : 'none');">[+ Thêm phòng học mới]</a>
            </c:if>
        </div>

        <!-- Form Thêm phòng học mới (Tiêu chí 4: Thêm / Sửa / Xóa phòng - Chỉ Admin) -->
        <c:if test="${sessionScope.currentUser.admin}">
            <fieldset id="addRoomBox" style="display: none; margin-bottom: 15px;">
                <legend>Thêm phòng học mới (Cập nhật thông tin, chỗ ngồi &amp; mục đích sử dụng)</legend>
                <form action="${pageContext.request.contextPath}/rooms" method="POST">
                    <input type="hidden" name="action" value="create">
                    <div class="form-group">
                        <label for="room_id">Mã phòng (VD: BE-303):</label>
                        <input type="text" id="room_id" name="id" pattern="[A-Za-z]{2}-[0-9]{3}" placeholder="BE-303" required style="width: 160px;">
                    </div>
                    <div class="form-group">
                        <label for="room_building">Tòa nhà:</label>
                        <select id="room_building" name="building" style="width: 180px;">
                            <option value="Alpha">Tòa Alpha</option>
                            <option value="Beta" selected>Tòa Beta</option>
                            <option value="Delta">Tòa Delta</option>
                            <option value="Epsilon">Tòa Epsilon</option>
                            <option value="Gamma">Tòa Gamma</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="room_floor">Tầng (1 - 10):</label>
                        <input type="number" id="room_floor" name="floor" value="3" min="1" max="10" required style="width: 90px;">
                    </div>
                    <div class="form-group">
                        <label for="room_purpose">Mục đích sử dụng:</label>
                        <select id="room_purpose" name="purpose" style="width: 280px;">
                            <option value="Lý thuyết tiêu chuẩn">Lý thuyết tiêu chuẩn</option>
                            <option value="Phòng Lab thực hành CNTT">Phòng Lab thực hành CNTT</option>
                            <option value="Phòng Seminar &amp; Thảo luận nhóm">Phòng Seminar &amp; Thảo luận nhóm</option>
                            <option value="Hội trường đa năng">Hội trường đa năng</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="room_stdChairs">Định mức chỗ ngồi (Ghế):</label>
                        <input type="number" id="room_stdChairs" name="stdChairs" value="35" min="10" max="100" required style="width: 90px;"> chỗ
                    </div>
                    <div class="form-group">
                        <label for="room_curChairs">Số chỗ ngồi hiện có:</label>
                        <input type="number" id="room_curChairs" name="curChairs" value="35" min="0" max="150" required style="width: 90px;"> chỗ
                    </div>

                    <div style="margin-top: 10px; margin-bottom: 5px; font-weight: bold; font-size: 13px;">
                        Tài sản &amp; Trang thiết bị khởi tạo ban đầu cho phòng:
                    </div>
                    <div class="form-group">
                        <label for="room_proj">Máy chiếu:</label>
                        <input type="text" id="room_proj" name="projName" value="Máy chiếu Sony Laser 4K" style="width: 280px;">
                    </div>
                    <div class="form-group">
                        <label for="room_acLeft">Điều hòa 1 (Trái):</label>
                        <input type="text" id="room_acLeft" name="acLeftName" value="Điều hòa Daikin 18000BTU" style="width: 280px;">
                    </div>
                    <div class="form-group">
                        <label for="room_acRight">Điều hòa 2 (Phải):</label>
                        <input type="text" id="room_acRight" name="acRightName" value="Điều hòa Daikin 18000BTU" style="width: 280px;">
                    </div>
                    <div class="form-group">
                        <label for="room_tv">Smart TV:</label>
                        <input type="text" id="room_tv" name="tvName" value="Smart TV Samsung 65 inch" style="width: 280px;">
                    </div>
                    <div style="margin-left: 185px;">
                        <input type="submit" value="Lưu phòng học mới">
                        <button type="button" onclick="document.getElementById('addRoomBox').style.display='none'">Hủy</button>
                    </div>
                </form>
            </fieldset>

            <!-- Form Sửa phòng học (Chỉ Admin) -->
            <c:if test="${not empty editRoom}">
                <fieldset id="editRoomBox" style="margin-bottom: 15px; border-color: #000;">
                    <legend><b>Cập nhật thông tin phòng học [${editRoom.id}]</b></legend>
                    <form action="${pageContext.request.contextPath}/rooms" method="POST">
                        <input type="hidden" name="action" value="update">
                        <input type="hidden" name="id" value="${editRoom.id}">

                        <div class="form-group">
                            <label>Mã phòng:</label>
                            <b>${editRoom.id}</b>
                        </div>
                        <div class="form-group">
                            <label for="edit_building">Tòa nhà:</label>
                            <select id="edit_building" name="building" style="width: 180px;">
                                <option value="Alpha" ${editRoom.building == 'Alpha' ? 'selected' : ''}>Tòa Alpha</option>
                                <option value="Beta" ${editRoom.building == 'Beta' ? 'selected' : ''}>Tòa Beta</option>
                                <option value="Delta" ${editRoom.building == 'Delta' ? 'selected' : ''}>Tòa Delta</option>
                                <option value="Epsilon" ${editRoom.building == 'Epsilon' ? 'selected' : ''}>Tòa Epsilon</option>
                                <option value="Gamma" ${editRoom.building == 'Gamma' ? 'selected' : ''}>Tòa Gamma</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="edit_floor">Tầng:</label>
                            <input type="number" id="edit_floor" name="floor" value="${editRoom.floor}" min="1" max="10" required style="width: 90px;">
                        </div>
                        <div class="form-group">
                            <label for="edit_purpose">Mục đích sử dụng:</label>
                            <select id="edit_purpose" name="purpose" style="width: 280px;">
                                <option value="Lý thuyết tiêu chuẩn" ${editRoom.purpose == 'Lý thuyết tiêu chuẩn' ? 'selected' : ''}>Lý thuyết tiêu chuẩn</option>
                                <option value="Phòng Lab thực hành CNTT" ${editRoom.purpose == 'Phòng Lab thực hành CNTT' ? 'selected' : ''}>Phòng Lab thực hành CNTT</option>
                                <option value="Phòng Seminar &amp; Thảo luận nhóm" ${editRoom.purpose == 'Phòng Seminar &amp; Thảo luận nhóm' ? 'selected' : ''}>Phòng Seminar &amp; Thảo luận nhóm</option>
                                <option value="Hội trường đa năng" ${editRoom.purpose == 'Hội trường đa năng' ? 'selected' : ''}>Hội trường đa năng</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="edit_stdChairs">Định mức chỗ ngồi (Ghế):</label>
                            <input type="number" id="edit_stdChairs" name="stdChairs" value="${editRoom.std}" min="10" max="100" required style="width: 90px;"> chỗ
                        </div>
                        <div class="form-group">
                            <label for="edit_curChairs">Số chỗ ngồi hiện có:</label>
                            <input type="number" id="edit_curChairs" name="curChairs" value="${editRoom.cur}" min="0" max="150" required style="width: 90px;"> chỗ
                        </div>
                        <div style="margin-left: 185px;">
                            <input type="submit" value="Cập nhật phòng">
                            <a href="${pageContext.request.contextPath}/rooms" class="btn-classic">Hủy</a>
                        </div>
                    </form>
                </fieldset>
            </c:if>
        </c:if>

        <!-- BẢNG DANH MỤC PHÒNG HỌC (Tiêu chí 4) -->
        <div class="table-responsive">
            <table class="data-table" id="roomTable">
                <thead>
                    <tr>
                        <th style="width: 35px; text-align: center;">No.</th>
                        <th style="width: 100px;">Mã phòng</th>
                        <th style="width: 90px;">Tòa nhà</th>
                        <th style="width: 60px; text-align: center;">Tầng</th>
                        <th style="width: 220px;">Mục đích sử dụng</th>
                        <th style="width: 165px; text-align: center;">Chỗ ngồi (Hiện có / Chuẩn)</th>
                        <th style="width: 125px; text-align: center;">Tổng tài sản</th>
                        <th>Trang thiết bị chính</th>
                        <th style="width: 180px; text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty rooms}">
                            <tr>
                                <td colspan="9" style="text-align: center; padding: 20px;">Không tìm thấy phòng nào phù hợp điều kiện lọc.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="r" items="${rooms}" varStatus="loop">
                                <tr>
                                    <td style="text-align: center;">${loop.index + 1}</td>
                                    <td><a href="${pageContext.request.contextPath}/room-detail?id=${r.id}"><b>${r.id}</b></a></td>
                                    <td>Tòa ${r.building}</td>
                                    <td style="text-align: center;">Tầng ${r.floor}</td>
                                    <td><c:out value="${r.purpose}"/></td>
                                    <td style="text-align: center;"><fcams:chairStatus cur="${r.cur}" std="${r.std}"/></td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/room-detail?id=${r.id}"><b>${r.assetCount} tài sản</b></a>
                                    </td>
                                    <td>
                                        <small>
                                            Máy chiếu: <fcams:assetStatus status="${r.projector.status}"/> |
                                            ĐH: <fcams:assetStatus status="${r.acLeft.status}"/> |
                                            TV: <fcams:assetStatus status="${r.tv.status}"/>
                                        </small>
                                    </td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/room-detail?id=${r.id}">[Xem chi tiết]</a>
                                        <!-- SỬA & XÓA PHÒNG: CHỨC NĂNG NGUY HIỂM CHỈ DÀNH CHO ADMIN -->
                                        <c:if test="${sessionScope.currentUser.admin}">
                                            &nbsp;|&nbsp;
                                            <a href="${pageContext.request.contextPath}/rooms?editId=${r.id}">[Sửa]</a>
                                            &nbsp;|&nbsp;
                                            <form action="${pageContext.request.contextPath}/rooms" method="POST" style="display:inline;" onsubmit="return confirm('Xác nhận xóa phòng học ${r.id}? Tất cả tài sản trong phòng sẽ bị xóa theo.');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${r.id}">
                                                <button type="submit" style="background:none;border:none;padding:0;color:#c00;cursor:pointer;font-size:12px;">[Xóa]</button>
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

        <jsp:include page="/includes/footer.jsp"/>
    </div>
</body>
</html>
