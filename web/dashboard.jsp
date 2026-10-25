<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fcams" uri="/WEB-INF/tlds/fcams.tld"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng điều khiển Thống kê (Dashboard) - F-CAMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/style.css?v=20261025a">
</head>
<body>
    <div class="container-fluid px-0">
        <jsp:include page="/includes/header.jsp"/>

        <h1>Bảng điều khiển Thống kê (Dashboard)</h1>

        <!-- 1. CÁC CHỈ SỐ TỔNG QUAN (KPIs) -->
        <div class="row g-3 my-2">
            <div class="col-md-4">
                <div class="alert-box" style="margin-bottom: 0;">
                    <div style="font-size: 13px; text-transform: uppercase; font-weight: bold;">Tổng số tài sản</div>
                    <div style="font-size: 28px; font-weight: bold; margin: 5px 0;">${stats.totalAssets}</div>
                    <div style="font-size: 12px;">Thiết bị &amp; trang thiết bị trong khuôn viên campus</div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="alert-box" style="margin-bottom: 0;">
                    <div style="font-size: 13px; text-transform: uppercase; font-weight: bold;">Tổng số phòng học</div>
                    <div style="font-size: 28px; font-weight: bold; margin: 5px 0;">${stats.totalRooms}</div>
                    <div style="font-size: 12px;">Giảng đường lý thuyết, thực hành Lab &amp; Seminar</div>
                </div>
            </div>

            <div class="col-md-4">
                <div class="alert-box" style="margin-bottom: 0;">
                    <div style="font-size: 13px; text-transform: uppercase; font-weight: bold;">Giá trị tài sản ước tính</div>
                    <div style="font-size: 24px; font-weight: bold; margin: 7px 0;">${stats.formattedTotalValue}</div>
                    <div style="font-size: 12px;">Tổng nguyên giá trang thiết bị toàn trường</div>
                </div>
            </div>
        </div>

        <!-- 2. THỐNG KÊ TỶ LỆ TÌNH TRẠNG TÀI SẢN -->
        <h2>Tỷ lệ tình trạng tài sản toàn hệ thống</h2>
        <div class="table-responsive" style="max-width: 820px;">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Tình trạng tài sản</th>
                        <th style="width: 140px; text-align: center;">Số lượng</th>
                        <th style="width: 140px; text-align: center;">Tỷ lệ (%)</th>
                        <th>Mô tả nghiệp vụ</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><b>Đang hoạt động tốt (ACTIVE)</b></td>
                        <td style="text-align: center;"><b>${stats.activeCount}</b></td>
                        <td style="text-align: center;">${stats.activeRate}%</td>
                        <td>Thiết bị vận hành ổn định phục vụ giảng dạy hàng ngày</td>
                    </tr>
                    <tr>
                        <td><b>Đang bảo trì / sửa chữa (REPAIRING)</b></td>
                        <td style="text-align: center;"><b>${stats.repairingCount}</b></td>
                        <td style="text-align: center;">${stats.repairingRate}%</td>
                        <td>Đang nạp gas, vệ sinh, sửa linh kiện kỹ thuật</td>
                    </tr>
                    <tr>
                        <td><b>Hỏng hóc / Cần thay thế (FAULTY)</b></td>
                        <td style="text-align: center;"><b>${stats.faultyCount}</b></td>
                        <td style="text-align: center;">${stats.faultyRate}%</td>
                        <td>Gặp sự cố kỹ thuật, cần cán bộ kỹ thuật kiểm tra gấp</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="row g-4 my-2">
            <!-- 3. DANH SÁCH TÀI SẢN MỚI ĐƯỢC THÊM -->
            <div class="col-lg-6">
                <h2>Tài sản mới được ghi nhận gần đây</h2>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 110px;">Mã tài sản</th>
                                <th>Tên &amp; Danh mục</th>
                                <th style="width: 90px;">Phòng</th>
                                <th style="width: 115px; text-align: right;">Đơn giá</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty stats.recentAssets}">
                                    <tr>
                                        <td colspan="4" style="text-align: center; padding: 12px;">Chưa có tài sản mới.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="a" items="${stats.recentAssets}">
                                        <tr>
                                            <td><a href="${pageContext.request.contextPath}/assets?keyword=${a.code}"><b>${a.code}</b></a></td>
                                            <td>
                                                <b><c:out value="${a.name}"/></b><br>
                                                <small>Danh mục: <c:out value="${a.category}"/> | Ngày: ${a.handoverDate}</small>
                                            </td>
                                            <td><a href="${pageContext.request.contextPath}/room-detail?id=${a.roomId}"><b>${a.roomId}</b></a></td>
                                            <td style="text-align: right;">${a.formattedPrice}</td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                <div style="font-size: 13px;">
                    <a href="${pageContext.request.contextPath}/assets">[&raquo; Xem tất cả tài sản]</a>
                </div>
            </div>

            <!-- 4. DANH SÁCH TÀI SẢN CẦN BẢO TRÌ KHẨN CẤP -->
            <div class="col-lg-6">
                <h2>Tài sản cần bảo trì khẩn cấp / Hỏng hóc</h2>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 110px;">Mã tài sản</th>
                                <th>Tên &amp; Tình trạng sự cố</th>
                                <th style="width: 90px;">Phòng</th>
                                <th style="width: 100px; text-align: center;">Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty stats.urgentAssets}">
                                    <tr>
                                        <td colspan="4" style="text-align: center; padding: 12px;">Tất cả tài sản hiện đang hoạt động bình thường!</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="a" items="${stats.urgentAssets}">
                                        <tr>
                                            <td><a href="${pageContext.request.contextPath}/assets?keyword=${a.code}"><b>${a.code}</b></a></td>
                                            <td>
                                                <b><c:out value="${a.name}"/></b><br>
                                                <small style="color: #000;">Sự cố: <c:out value="${not empty a.note ? a.note : 'Cần kiểm tra kỹ thuật'}"/></small>
                                            </td>
                                            <td><a href="${pageContext.request.contextPath}/room-detail?id=${a.roomId}"><b>${a.roomId}</b></a></td>
                                            <td style="text-align: center;"><fcams:assetStatus status="${a.status}"/></td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                <div style="font-size: 13px;">
                    <a href="${pageContext.request.contextPath}/assets?status=BROKEN">[&raquo; Xem toàn bộ danh sách tài sản gặp sự cố trên trang Quản lý Tài sản]</a>
                </div>
            </div>
        </div>

        <jsp:include page="/includes/footer.jsp"/>
    </div>
</body>
</html>

