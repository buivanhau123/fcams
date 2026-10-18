<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Tài sản (Asset Management) - F-CAMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css">
</head>
<body>
    <div class="container-fluid px-0">
        <jsp:include page="/includes/header.jsp"/>

        <h1>Quản lý Tài sản (Asset Management - CRUD)</h1>

        <!-- Bộ lọc và Tìm kiếm tài sản -->
        <div class="filter-box">
            <form action="${pageContext.request.contextPath}/assets" method="GET" class="row g-2 align-items-center" style="margin: 0;">
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
                    <label for="categoryFilter"><b>Danh mục:</b></label>
                    <select id="categoryFilter" name="category" onchange="this.form.submit()">
                        <option value="ALL" ${selectedCategory == 'ALL' ? 'selected' : ''}>-- Tất cả danh mục --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat}" ${selectedCategory == cat ? 'selected' : ''}>${cat}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="statusFilter"><b>Trạng thái:</b></label>
                    <select id="statusFilter" name="status" onchange="this.form.submit()">
                        <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>-- Tất cả trạng thái --</option>
                        <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Hoạt động tốt</option>
                        <option value="REPAIRING" ${selectedStatus == 'REPAIRING' ? 'selected' : ''}>Đang bảo trì</option>
                        <option value="FAULTY" ${selectedStatus == 'FAULTY' ? 'selected' : ''}>Hỏng hóc</option>
                    </select>
                </div>

                <div class="col-auto">
                    <label for="keywordInput"><b>Tìm kiếm:</b></label>
                    <input type="text" id="keywordInput" name="keyword" value="<c:out value='${keyword}'/>" placeholder="Mã, tên, số serial...">
                    <button type="submit">Tìm kiếm</button>
                    <a href="${pageContext.request.contextPath}/assets" class="btn-classic">Tất cả</a>
                </div>
            </form>
        </div>

        <div style="margin-bottom: 10px; font-size: 13px; font-weight: bold;">
            Tìm thấy ${assets.size()} tài sản phù hợp.
            &nbsp;&mdash;&nbsp;
            <a href="javascript:void(0)" onclick="document.getElementById('addAssetBox').style.display = (document.getElementById('addAssetBox').style.display === 'none' ? 'block' : 'none');">[+ Ghi nhận tài sản mới]</a>
        </div>

        <!-- FORM THÊM MỚI TÀI SẢN (Admin / Staff) -->
        <fieldset id="addAssetBox" style="display: none;">
            <legend>Ghi nhận tài sản mới vào hệ thống</legend>
            <form action="${pageContext.request.contextPath}/assets" method="POST">
                <input type="hidden" name="action" value="create">

                <div class="form-group">
                    <label for="code">Mã tài sản (*):</label>
                    <input type="text" id="code" name="code" placeholder="VD: AST-BE303-PJ" required style="width: 220px;">
                </div>

                <div class="form-group">
                    <label for="name">Tên tài sản (*):</label>
                    <input type="text" id="name" name="name" placeholder="VD: Máy chiếu Sony Laser 4K" required style="width: 380px;">
                </div>

                <div class="form-group">
                    <label for="category">Danh mục:</label>
                    <select id="category" name="category" style="width: 220px;">
                        <option value="Máy chiếu">Máy chiếu</option>
                        <option value="Điều hòa">Điều hòa</option>
                        <option value="Smart TV">Smart TV</option>
                        <option value="Âm thanh">Hệ thống Âm thanh</option>
                        <option value="Bàn ghế">Bàn ghế sinh viên</option>
                        <option value="Thiết bị Lab">Thiết bị Lab / Máy tính</option>
                        <option value="Khác">Khác</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="serialNumber">Số serial:</label>
                    <input type="text" id="serialNumber" name="serialNumber" placeholder="VD: SN-SNY-2024-99" style="width: 220px;">
                </div>

                <div class="form-group">
                    <label for="handoverDate">Ngày bàn giao:</label>
                    <input type="date" id="handoverDate" name="handoverDate" value="<%= java.time.LocalDate.now().toString() %>" style="width: 180px;">
                </div>

                <div class="form-group">
                    <label for="price">Đơn giá (VNĐ):</label>
                    <input type="number" id="price" name="price" value="15000000" min="0" step="100000" style="width: 180px;"> VNĐ
                </div>

                <div class="form-group">
                    <label for="roomId">Phòng bố trí:</label>
                    <select id="roomId" name="roomId" style="width: 220px;">
                        <c:forEach var="r" items="${rooms}">
                            <option value="${r.id}">${r.id} (Tòa ${r.building} - Tầng ${r.floor})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label for="status">Trạng thái:</label>
                    <select id="status" name="status" style="width: 220px;">
                        <option value="ACTIVE" selected>Hoạt động tốt</option>
                        <option value="REPAIRING">Đang bảo trì</option>
                        <option value="FAULTY">Hỏng hóc</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="note">Ghi chú sự cố / tình trạng:</label>
                    <input type="text" id="note" name="note" placeholder="Tình trạng kỹ thuật, vị trí lắp đặt..." style="width: 380px;">
                </div>

                <div style="margin-left: 185px;">
                    <input type="submit" value="Lưu tài sản mới">
                    <input type="button" value="Đóng" onclick="document.getElementById('addAssetBox').style.display='none'">
                </div>
            </form>
        </fieldset>

        <!-- FORM CẬP NHẬT TÀI SẢN (nếu nhấn [Sửa]) -->
        <c:if test="${not empty editAsset}">
            <fieldset>
                <legend>Cập nhật thông tin tài sản: ${editAsset.code}</legend>
                <form action="${pageContext.request.contextPath}/assets" method="POST">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="code" value="${editAsset.code}">

                    <div class="form-group">
                        <label>Mã tài sản:</label>
                        <b>${editAsset.code}</b>
                    </div>

                    <div class="form-group">
                        <label for="edit_name">Tên tài sản (*):</label>
                        <input type="text" id="edit_name" name="name" value="<c:out value='${editAsset.name}'/>" required style="width: 380px;">
                    </div>

                    <div class="form-group">
                        <label for="edit_category">Danh mục:</label>
                        <select id="edit_category" name="category" style="width: 220px;">
                            <option value="Máy chiếu" ${editAsset.category == 'Máy chiếu' ? 'selected' : ''}>Máy chiếu</option>
                            <option value="Điều hòa" ${editAsset.category == 'Điều hòa' ? 'selected' : ''}>Điều hòa</option>
                            <option value="Smart TV" ${editAsset.category == 'Smart TV' ? 'selected' : ''}>Smart TV</option>
                            <option value="Âm thanh" ${editAsset.category == 'Âm thanh' ? 'selected' : ''}>Hệ thống Âm thanh</option>
                            <option value="Bàn ghế" ${editAsset.category == 'Bàn ghế' ? 'selected' : ''}>Bàn ghế sinh viên</option>
                            <option value="Thiết bị Lab" ${editAsset.category == 'Thiết bị Lab' ? 'selected' : ''}>Thiết bị Lab / Máy tính</option>
                            <option value="Khác" ${editAsset.category == 'Khác' ? 'selected' : ''}>Khác</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="edit_serialNumber">Số serial:</label>
                        <input type="text" id="edit_serialNumber" name="serialNumber" value="<c:out value='${editAsset.serialNumber}'/>" style="width: 220px;">
                    </div>

                    <div class="form-group">
                        <label for="edit_handoverDate">Ngày bàn giao:</label>
                        <input type="date" id="edit_handoverDate" name="handoverDate" value="${editAsset.handoverDate}" style="width: 180px;">
                    </div>

                    <div class="form-group">
                        <label for="edit_price">Đơn giá (VNĐ):</label>
                        <input type="number" id="edit_price" name="price" value="${editAsset.price}" min="0" step="100000" style="width: 180px;"> VNĐ
                    </div>

                    <div class="form-group">
                        <label for="edit_roomId">Phòng bố trí:</label>
                        <select id="edit_roomId" name="roomId" style="width: 220px;">
                            <c:forEach var="r" items="${rooms}">
                                <option value="${r.id}" ${editAsset.roomId == r.id ? 'selected' : ''}>${r.id} (Tòa ${r.building} - Tầng ${r.floor})</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="edit_status">Trạng thái:</label>
                        <select id="edit_status" name="status" style="width: 220px;">
                            <option value="ACTIVE" ${editAsset.status == 'ACTIVE' ? 'selected' : ''}>Hoạt động tốt</option>
                            <option value="REPAIRING" ${editAsset.status == 'REPAIRING' ? 'selected' : ''}>Đang bảo trì</option>
                            <option value="FAULTY" ${editAsset.status == 'FAULTY' ? 'selected' : ''}>Hỏng hóc</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="edit_note">Ghi chú sự cố:</label>
                        <input type="text" id="edit_note" name="note" value="<c:out value='${editAsset.note}'/>" style="width: 380px;">
                    </div>

                    <div style="margin-left: 185px;">
                        <input type="submit" value="Lưu cập nhật">
                        <a href="${pageContext.request.contextPath}/assets" class="btn-classic">Hủy</a>
                    </div>
                </form>
            </fieldset>
        </c:if>

        <!-- FORM CHUYỂN DỜI TÀI SẢN GIỮA CÁC PHÒNG (nếu nhấn [Chuyển phòng]) -->
        <c:if test="${not empty transferAsset}">
            <fieldset>
                <legend>Chuyển dời tài sản sang phòng học khác</legend>
                <form action="${pageContext.request.contextPath}/assets" method="POST">
                    <input type="hidden" name="action" value="transfer">
                    <input type="hidden" name="assetCode" value="${transferAsset.code}">

                    <div class="form-group">
                        <label>Tài sản chuyển dời:</label>
                        <b>${transferAsset.code} &mdash; <c:out value="${transferAsset.name}"/></b>
                    </div>

                    <div class="form-group">
                        <label>Phòng hiện tại:</label>
                        <b>Phòng ${transferAsset.roomId}</b>
                    </div>

                    <div class="form-group">
                        <label for="targetRoom">Chuyển đến phòng mới:</label>
                        <select id="targetRoom" name="targetRoom" style="width: 250px;">
                            <c:forEach var="r" items="${rooms}">
                                <c:if test="${r.id != transferAsset.roomId}">
                                    <option value="${r.id}">Phòng ${r.id} (Tòa ${r.building} - Tầng ${r.floor})</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>

                    <div style="margin-left: 185px;">
                        <input type="submit" value="Xác nhận chuyển phòng">
                        <a href="${pageContext.request.contextPath}/assets" class="btn-classic">Hủy</a>
                    </div>
                </form>
            </fieldset>
        </c:if>

        <!-- BẢNG DANH SÁCH TÀI SẢN (Asset Management - CRUD) -->
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th style="width: 35px; text-align: center;">No.</th>
                        <th style="width: 120px;">Mã tài sản</th>
                        <th>Tên tài sản</th>
                        <th style="width: 110px;">Danh mục</th>
                        <th style="width: 125px;">Số serial</th>
                        <th style="width: 100px; text-align: center;">Ngày giao</th>
                        <th style="width: 115px; text-align: right;">Đơn giá</th>
                        <th style="width: 90px; text-align: center;">Phòng</th>
                        <th style="width: 120px; text-align: center;">Trạng thái</th>
                        <th style="width: 190px; text-align: center;">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${empty assets}">
                            <tr>
                                <td colspan="10" style="text-align: center; padding: 18px;">Không tìm thấy tài sản nào phù hợp điều kiện lọc.</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="a" items="${assets}" varStatus="loop">
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
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/room-detail?id=${a.roomId}"><b>${a.roomId}</b></a>
                                    </td>
                                    <td style="text-align: center;"><fcams:assetStatus status="${a.status}"/></td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/assets?editCode=${a.code}">[Sửa]</a>
                                        <a href="${pageContext.request.contextPath}/assets?transferCode=${a.code}">[Chuyển phòng]</a>

                                        <!-- Nút XÓA CHỈ DÀNH CHO ADMIN -->
                                        <c:if test="${sessionScope.currentUser.admin}">
                                            <form action="${pageContext.request.contextPath}/assets" method="POST" style="display:inline;" onsubmit="return confirm('Xác nhận xóa tài sản ${a.code} (${a.name})?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="code" value="${a.code}">
                                                <button type="submit" style="background:none;border:none;padding:0;color:#0000ee;text-decoration:underline;font-size:13px;">[Xóa]</button>
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

