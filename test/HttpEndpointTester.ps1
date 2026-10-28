$baseUrl = "http://localhost:8080/FCAMS-Simple"
$ErrorActionPreference = "Continue"

Write-Output "================================================================================"
Write-Output "  BẮT ĐẦU KIỂM THỬ GIAO DIỆN & PHÂN QUYỀN HTTP SERVLET / FILTER (PORT 8080)"
Write-Output "================================================================================`n"

$totalHttp = 0
$passedHttp = 0
$failedHttp = 0

function Assert-Http($name, $condition, $details) {
    $script:totalHttp++
    if ($condition) {
        $script:passedHttp++
        Write-Output ("  [PASS] {0,-60} | {1}" -f $name, $details)
    } else {
        $script:failedHttp++
        Write-Output ("  [FAIL] {0,-60} | {1}" -f $name, $details)
    }
}

# 1. Chưa đăng nhập -> Tự động chuyển hướng về trang /login
$unauth = Invoke-WebRequest -Uri "$baseUrl/dashboard" -UseBasicParsing -MaximumRedirection 0 -ErrorAction SilentlyContinue
$isRedirectLogin = ($unauth.StatusCode -eq 302 -and $unauth.Headers.Location -match "/login")
Assert-Http "Chưa đăng nhập truy cập /dashboard bị redirect về /login" $isRedirectLogin "Status: $($unauth.StatusCode), Location: $($unauth.Headers.Location)"

# 2. ĐĂNG NHẬP GIẢNG VIÊN (hungnq / 123)
$lecturerSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginLecturer = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body @{ username = "hungnq"; password = "123" } -WebSession $lecturerSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
$loggedLecturer = ($loginLecturer.StatusCode -eq 302 -and $loginLecturer.Headers.Location -match "/dashboard")
Assert-Http "Giảng viên đăng nhập thành công vào /dashboard" $loggedLecturer "Redirect to: $($loginLecturer.Headers.Location)"

# 2.1 Giảng viên vào /dashboard
$dashL = Invoke-WebRequest -Uri "$baseUrl/dashboard" -WebSession $lecturerSession -UseBasicParsing
$hasLecturerName = $dashL.Content -match "Thầy Nguyễn Quốc Hùng"
$hasLecturerRole = $dashL.Content -match "Giảng viên"
Assert-Http "Dashboard hiển thị đúng tên và vai trò Giảng viên" ($hasLecturerName -and $hasLecturerRole) "Tên: hungnq, Role: LECTURER"

# 2.2 Giảng viên vào /assets -> Xem được nhưng không có quyền Thêm/Xóa
$assetsL = Invoke-WebRequest -Uri "$baseUrl/assets" -WebSession $lecturerSession -UseBasicParsing
$hasReportDamageBtn = $assetsL.Content -match "\[Báo hỏng\]"
$noAddAssetBtn = -not ($assetsL.Content -match "Ghi nhận tài sản mới vào hệ thống")
$noDeleteAssetBtn = -not ($assetsL.Content -match "action=""delete""")
Assert-Http "Trang Tài sản của Giảng viên có nút [Báo hỏng]" $hasReportDamageBtn ""
Assert-Http "Trang Tài sản của Giảng viên ẩn nút Thêm & Xóa tài sản" ($noAddAssetBtn -and $noDeleteAssetBtn) "Bảo mật RBAC đạt chuẩn"

# 2.3 Giảng viên xem /room-detail và gửi báo cáo hỏng hóc
$roomL = Invoke-WebRequest -Uri "$baseUrl/room-detail?id=BE-301" -WebSession $lecturerSession -UseBasicParsing
$hasRoomReportForm = $roomL.Content -match "Gửi báo cáo đồ hỏng hóc trong phòng BE-301 cho Cán bộ Quản lý"
Assert-Http "Trang Chi tiết phòng có sẵn form báo hỏng cho Giảng viên" $hasRoomReportForm "Room: BE-301"

$postDamage = Invoke-WebRequest -Uri "$baseUrl/assets" -Method POST -Body @{
    action = "reportDamage";
    assetCode = "AST-BE301-TV";
    damageNote = "Smart TV Samsung bị sọc màn hình ngang, không hiển thị HDMI";
    returnRoomId = "BE-301"
} -WebSession $lecturerSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
$reportedOk = ($postDamage.StatusCode -eq 302 -and $postDamage.Headers.Location -match "msg=")
Assert-Http "Giảng viên gửi báo cáo đồ hỏng hóc thành công" $reportedOk "Redirect with msg confirmation"

# 2.4 Giảng viên cố truy cập /history -> BỊ CHẶN BỞI AUTHFILTER
$historyL = Invoke-WebRequest -Uri "$baseUrl/history" -WebSession $lecturerSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
$isBlockedHistory = ($historyL.StatusCode -eq 302 -and $historyL.Headers.Location -match "error=")
Assert-Http "AuthFilter chặn Giảng viên truy cập trang Lịch sử /history" $isBlockedHistory "Blocked & redirect with error"


# 3. ĐĂNG NHẬP CÁN BỘ QUẢN LÝ (it_beta / 123 - STAFF)
$staffSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginStaff = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body @{ username = "it_beta"; password = "123" } -WebSession $staffSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Http "Staff (it_beta) đăng nhập thành công" ($loginStaff.StatusCode -eq 302) ""

# 3.1 Staff vào /history -> ĐƯỢC PHÉP TRUY CẬP
$historyS = Invoke-WebRequest -Uri "$baseUrl/history" -WebSession $staffSession -UseBasicParsing
$canViewHistory = ($historyS.StatusCode -eq 200 -and $historyS.Content -match "Nhật ký Lịch sử Thay đổi")
$seesLecturerDamageReport = $historyS.Content -match "Smart TV Samsung bị sọc màn hình"
Assert-Http "Staff truy cập được trang /history" $canViewHistory "HTTP 200 OK"
Assert-Http "Lịch sử hiển thị ngay báo cáo hỏng của Giảng viên" $seesLecturerDamageReport "Tích hợp đồng bộ LECTURER -> STAFF"

# 3.2 Staff vào /assets có quyền Thêm/Sửa nhưng không có nút Xóa
$assetsS = Invoke-WebRequest -Uri "$baseUrl/assets" -WebSession $staffSession -UseBasicParsing
$staffCanAdd = $assetsS.Content -match "Ghi nhận tài sản mới"
$staffNoDelete = -not ($assetsS.Content -match "Xác nhận xóa tài sản")
Assert-Http "Staff có quyền Thêm/Sửa tài sản nhưng không có nút Xóa" ($staffCanAdd -and $staffNoDelete) "Hạn chế chức năng nguy hiểm"

# 3.3 Staff cố gửi POST Xóa tài sản -> BỊ AUTHFILTER CHẶN
$staffDeleteTry = Invoke-WebRequest -Uri "$baseUrl/assets" -Method POST -Body @{
    action = "delete";
    code = "AST-BE301-PJ"
} -WebSession $staffSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
$staffDeleteBlocked = ($staffDeleteTry.StatusCode -eq 302 -and $staffDeleteTry.Headers.Location -match "error=")
Assert-Http "AuthFilter chặn Staff cố tình thực hiện thao tác Xóa tài sản" $staffDeleteBlocked "Chặn thao tác nguy hiểm"


# 4. ĐĂNG NHẬP QUẢN TRỊ VIÊN (admin / 123)
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginAdmin = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body @{ username = "admin"; password = "123" } -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Assert-Http "Admin (admin) đăng nhập thành công" ($loginAdmin.StatusCode -eq 302) ""

# 4.1 Admin có nút Xóa tài sản và nút Xóa phòng
$assetsA = Invoke-WebRequest -Uri "$baseUrl/assets" -WebSession $adminSession -UseBasicParsing
$adminHasDeleteAsset = $assetsA.Content -match "Xác nhận xóa tài sản"
$roomsA = Invoke-WebRequest -Uri "$baseUrl/rooms" -WebSession $adminSession -UseBasicParsing
$adminHasRoomCrud = ($roomsA.Content -match "Thêm phòng học mới" -and $roomsA.Content -match "Xác nhận xóa phòng học")
Assert-Http "Admin có toàn quyền CRUD tài sản (bao gồm Xóa)" $adminHasDeleteAsset "Full CRUD"
Assert-Http "Admin có toàn quyền CRUD phòng học (Thêm/Sửa/Xóa phòng)" $adminHasRoomCrud "Full CRUD"

Write-Output "`n================================================================================"
Write-Output ("  KẾT QUẢ KIỂM THỬ HTTP: {0}/{1} passed (Thành công: {2:N1}%)" -f $passedHttp, $totalHttp, ($passedHttp * 100.0 / $totalHttp))
if ($failedHttp -eq 0) {
    Write-Output "  >>> TẤT CẢ 13/13 KỊCH BẢN KIỂM THỬ HTTP/SECURITY ĐỀU ĐẠT CHUẨN 100%! <<<"
} else {
    Write-Output ("  >>> CẢNH BÁO: CÓ {0} BÀI TEST HTTP THẤT BẠI! <<<" -f $failedHttp)
}
Write-Output "================================================================================"
