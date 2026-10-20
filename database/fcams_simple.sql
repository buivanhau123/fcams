-- ============================================================================
-- F-CAMS SIMPLE: HỆ THỐNG QUẢN LÝ TÀI SẢN PHÒNG HỌC (PRJ301 - JAVA WEB)
-- CSDL: Microsoft SQL Server (T-SQL) - Database: FCAMS_DB
-- ============================================================================

USE master;
GO

IF DB_ID('FCAMS_DB') IS NULL
BEGIN
    CREATE DATABASE FCAMS_DB;
END
GO

USE FCAMS_DB;
GO

-- Xóa các bảng và stored procedure cũ nếu tồn tại
IF OBJECT_ID('dbo.sp_ResetFcamsData', 'P') IS NOT NULL DROP PROCEDURE dbo.sp_ResetFcamsData;
IF OBJECT_ID('dbo.assets', 'U') IS NOT NULL DROP TABLE dbo.assets;
IF OBJECT_ID('dbo.tickets', 'U') IS NOT NULL DROP TABLE dbo.tickets;
IF OBJECT_ID('dbo.borrows', 'U') IS NOT NULL DROP TABLE dbo.borrows;
IF OBJECT_ID('dbo.transfers', 'U') IS NOT NULL DROP TABLE dbo.transfers;
IF OBJECT_ID('dbo.room_assets', 'U') IS NOT NULL DROP TABLE dbo.room_assets;
IF OBJECT_ID('dbo.rooms', 'U') IS NOT NULL DROP TABLE dbo.rooms;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
GO

-- ============================================================================
-- 1. BẢNG USERS (Xác thực & Phân quyền: ADMIN, STAFF / IT_STAFF)
-- ============================================================================
CREATE TABLE dbo.users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    full_name NVARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    description NVARCHAR(500) NULL,
    CONSTRAINT CK_users_role CHECK (role IN ('ADMIN', 'STAFF', 'IT_STAFF', 'LECTURER')),
    CONSTRAINT CK_users_username_len CHECK (LEN(LTRIM(RTRIM(username))) >= 3)
);
GO

-- ============================================================================
-- 2. BẢNG ROOMS (Quản lý Phòng học: Mã, Tòa, Tầng, Chỗ ngồi, Mục đích)
-- ============================================================================
CREATE TABLE dbo.rooms (
    id VARCHAR(20) NOT NULL PRIMARY KEY,
    building NVARCHAR(50) NOT NULL,
    floor INT NOT NULL,
    std_chairs INT NOT NULL DEFAULT 35,
    cur_chairs INT NOT NULL DEFAULT 35,
    purpose NVARCHAR(100) NOT NULL DEFAULT N'Lý thuyết tiêu chuẩn',
    CONSTRAINT CK_rooms_building CHECK (building IN (N'Alpha', N'Beta', N'Delta', N'Epsilon', N'Gamma')),
    CONSTRAINT CK_rooms_floor CHECK (floor BETWEEN 1 AND 10),
    CONSTRAINT CK_rooms_std_chairs CHECK (std_chairs BETWEEN 10 AND 100),
    CONSTRAINT CK_rooms_cur_chairs CHECK (cur_chairs BETWEEN 0 AND 150)
);
GO

-- ============================================================================
-- 3. BẢNG ASSETS (Quản lý Tài sản - CRUD đầy đủ thông số)
-- ============================================================================
CREATE TABLE dbo.assets (
    code VARCHAR(50) NOT NULL PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    category NVARCHAR(50) NOT NULL,
    serial_number VARCHAR(100) NULL,
    handover_date DATE NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    price DECIMAL(15,2) NOT NULL DEFAULT 0,
    room_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    note NVARCHAR(300) NULL DEFAULT N'',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_assets_rooms FOREIGN KEY (room_id) REFERENCES dbo.rooms(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT CK_assets_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'BROKEN', 'REPAIRING', 'FAULTY')),
    CONSTRAINT CK_assets_price CHECK (price >= 0)
);
GO

-- ============================================================================
-- 4. BẢNG ROOM_ASSETS (Trang thiết bị chính gắn với phòng học)
-- ============================================================================
CREATE TABLE dbo.room_assets (
    asset_id INT IDENTITY(1,1) PRIMARY KEY,
    room_id VARCHAR(20) NOT NULL,
    asset_key VARCHAR(20) NOT NULL,
    asset_name NVARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    note NVARCHAR(255) NOT NULL DEFAULT N'',
    CONSTRAINT FK_room_assets_rooms FOREIGN KEY (room_id) REFERENCES dbo.rooms(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_room_assets_room_key UNIQUE (room_id, asset_key),
    CONSTRAINT CK_room_assets_key CHECK (asset_key IN ('projector', 'acLeft', 'acRight', 'tv')),
    CONSTRAINT CK_room_assets_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'BROKEN', 'REPAIRING', 'FAULTY'))
);
GO

-- ============================================================================
-- STORED PROCEDURE KHỞI TẠO & ĐẶT LẠI DỮ LIỆU HOÀN CHỈNH (sp_ResetFcamsData)
-- ============================================================================
CREATE PROCEDURE dbo.sp_ResetFcamsData
AS
BEGIN
    SET NOCOUNT ON;

    DELETE FROM dbo.assets;
    DELETE FROM dbo.room_assets;
    DELETE FROM dbo.rooms;
    DELETE FROM dbo.users;

    -- 1. NẠP USERS (Phân quyền Admin / Staff)
    INSERT INTO dbo.users (username, password, full_name, role, description) VALUES
    ('admin',   '123', N'Quản trị viên Hệ thống', 'ADMIN',    N'Quản trị viên (Admin): Toàn quyền Dashboard, Quản lý Tài sản (Xem, Thêm, Sửa, XÓA), Quản lý Phòng học (Xem, Thêm, Sửa, XÓA).'),
    ('it_beta', '123', N'Trần Văn Tuấn (Cán bộ IT)', 'STAFF',   N'Cán bộ Quản lý (Staff): Dashboard, Quản lý Tài sản (Xem, Thêm, Sửa, Chuyển phòng), Quản lý Phòng học (Xem chi tiết). Bị hạn chế quyền Xóa/Sửa phòng.'),
    ('hungnq',  '123', N'Thầy Nguyễn Quốc Hùng',    'STAFF',   N'Cán bộ Quản lý Phòng học & Thiết bị');

    -- 2. NẠP ROOMS (Danh mục các phòng học toàn trường)
    INSERT INTO dbo.rooms (id, building, floor, std_chairs, cur_chairs, purpose) VALUES
    ('BE-301', N'Beta',    3, 35, 35, N'Lý thuyết tiêu chuẩn'),
    ('BE-302', N'Beta',    3, 35, 35, N'Lý thuyết tiêu chuẩn'),
    ('BE-303', N'Beta',    3, 40, 40, N'Phòng Seminar & Thảo luận nhóm'),
    ('AL-101', N'Alpha',   1, 35, 35, N'Phòng Lab thực hành CNTT'),
    ('AL-102', N'Alpha',   1, 35, 35, N'Phòng Lab Đồ họa & AI'),
    ('AL-201', N'Alpha',   2, 35, 35, N'Phòng Lab thực hành CNTT'),
    ('DE-101', N'Delta',   1, 45, 45, N'Phòng Seminar & Thảo luận nhóm'),
    ('EP-201', N'Epsilon', 2, 30, 30, N'Phòng Lab thực hành CNTT'),
    ('GA-301', N'Gamma',   3, 60, 60, N'Hội trường đa năng');

    -- 3. NẠP ROOM_ASSETS (Thiết bị phòng cơ bản)
    INSERT INTO dbo.room_assets (room_id, asset_key, asset_name, status, note) VALUES
    ('BE-301', 'projector', N'Máy chiếu Sony Laser 4K',       'ACTIVE', N''),
    ('BE-301', 'acLeft',    N'Điều hòa Daikin 18000BTU (Trái)', 'ACTIVE', N''),
    ('BE-301', 'acRight',   N'Điều hòa Daikin 18000BTU (Phải)', 'ACTIVE', N''),
    ('BE-301', 'tv',        N'Smart TV Samsung 65 inch',       'ACTIVE', N''),

    ('BE-302', 'projector', N'Máy chiếu Panasonic PT-VMZ50',  'ACTIVE', N''),
    ('BE-302', 'acLeft',    N'Điều hòa Daikin 18000BTU (Trái)', 'ACTIVE', N''),
    ('BE-302', 'acRight',   N'Điều hòa Daikin 18000BTU (Phải)', 'ACTIVE', N''),
    ('BE-302', 'tv',        N'Smart TV LG QNED 65 inch',       'ACTIVE', N''),

    ('BE-303', 'projector', N'Máy chiếu Epson EB-2250U',      'ACTIVE', N''),
    ('BE-303', 'acLeft',    N'Điều hòa Panasonic 24000BTU',    'ACTIVE', N''),
    ('BE-303', 'acRight',   N'Điều hòa Panasonic 24000BTU',    'ACTIVE', N''),
    ('BE-303', 'tv',        N'Smart TV Sony Bravia 75 inch',   'ACTIVE', N''),

    ('AL-101', 'projector', N'Máy chiếu ViewSonic Laser',     'ACTIVE', N''),
    ('AL-101', 'acLeft',    N'Điều hòa Daikin Inverter',       'ACTIVE', N''),
    ('AL-101', 'acRight',   N'Điều hòa Daikin Inverter',       'ACTIVE', N''),
    ('AL-101', 'tv',        N'Smart TV Samsung 75 inch',       'ACTIVE', N''),

    ('AL-102', 'projector', N'Máy chiếu BenQ 4K HDR',         'ACTIVE', N''),
    ('AL-102', 'acLeft',    N'Điều hòa Daikin 24000BTU',       'ACTIVE', N''),
    ('AL-102', 'acRight',   N'Điều hòa Daikin 24000BTU',       'ACTIVE', N''),
    ('AL-102', 'tv',        N'Smart TV LG OLED 65 inch',       'ACTIVE', N''),

    ('AL-201', 'projector', N'Máy chiếu Sony VPL-PHZ60',      'ACTIVE', N''),
    ('AL-201', 'acLeft',    N'Điều hòa Panasonic Inverter',    'ACTIVE', N''),
    ('AL-201', 'acRight',   N'Điều hòa Panasonic Inverter',    'ACTIVE', N''),
    ('AL-201', 'tv',        N'Smart TV Samsung Crystal 65"',   'ACTIVE', N''),

    ('DE-101', 'projector', N'Máy chiếu Epson Laser Full HD', 'ACTIVE', N''),
    ('DE-101', 'acLeft',    N'Điều hòa Daikin 18000BTU',       'ACTIVE', N''),
    ('DE-101', 'acRight',   N'Điều hòa Daikin 18000BTU',       'ACTIVE', N''),
    ('DE-101', 'tv',        N'Smart TV TCL 65 inch 4K',        'ACTIVE', N''),

    ('EP-201', 'projector', N'Máy chiếu Panasonic PT-VZ580',  'ACTIVE', N''),
    ('EP-201', 'acLeft',    N'Điều hòa Daikin 18000BTU',       'ACTIVE', N''),
    ('EP-201', 'acRight',   N'Điều hòa Daikin 18000BTU',       'ACTIVE', N''),
    ('EP-201', 'tv',        N'Smart TV Samsung 65 inch',       'ACTIVE', N''),

    ('GA-301', 'projector', N'Máy chiếu Laser Công suất lớn', 'ACTIVE', N''),
    ('GA-301', 'acLeft',    N'Hệ thống Điều hòa trung tâm 01', 'ACTIVE', N''),
    ('GA-301', 'acRight',   N'Hệ thống Điều hòa trung tâm 02', 'ACTIVE', N''),
    ('GA-301', 'tv',        N'Màn hình LED Hội trường P2.5',   'ACTIVE', N'');

    -- 4. NẠP ASSETS (Toàn bộ tài sản trang thiết bị chi tiết với đơn giá, serial, ngày bàn giao)
    INSERT INTO dbo.assets (code, name, category, serial_number, handover_date, price, room_id, status, note) VALUES
    -- Phòng BE-301
    ('AST-BE301-PJ',  N'Máy chiếu Sony Laser 4K VPL-XW5000', N'Máy chiếu', 'SN-SNY-BE301-01', '2025-09-01', 25000000, 'BE-301', 'ACTIVE',      N'Đang hoạt động tốt, hình ảnh sắc nét'),
    ('AST-BE301-AC1', N'Điều hòa Daikin Inverter 18000BTU (Trái)', N'Điều hòa', 'SN-DKN-BE301-01', '2025-08-15', 14500000, 'BE-301', 'ACTIVE',      N'Làm lạnh nhanh, đã bảo dưỡng định kỳ'),
    ('AST-BE301-AC2', N'Điều hòa Daikin Inverter 18000BTU (Phải)', N'Điều hòa', 'SN-DKN-BE301-02', '2025-08-15', 14500000, 'BE-301', 'ACTIVE',      N'Hoạt động bình thường'),
    ('AST-BE301-TV',  N'Smart TV Samsung 65 inch 4K Crystal', N'Smart TV', 'SN-SSG-BE301-01', '2025-10-10', 16200000, 'BE-301', 'ACTIVE',      N'Hiển thị tốt, kết nối HDMI ổn định'),
    ('AST-BE301-AU',  N'Bộ Micro không dây & Amply Shure', N'Âm thanh', 'SN-SHR-BE301-01', '2025-11-05', 8500000, 'BE-301', 'ACTIVE',      N'Micro rõ tiếng, không rè'),

    -- Phòng BE-302
    ('AST-BE302-PJ',  N'Máy chiếu Panasonic PT-VMZ50 Laser', N'Máy chiếu', 'SN-PAN-BE302-01', '2025-09-02', 22000000, 'BE-302', 'ACTIVE',      N'Hoạt động tốt'),
    ('AST-BE302-AC1', N'Điều hòa Daikin 18000BTU (Trái)', N'Điều hòa', 'SN-DKN-BE302-01', '2025-08-16', 14500000, 'BE-302', 'MAINTENANCE', N'Đang vệ sinh màng lọc và kiểm tra ga'),
    ('AST-BE302-AC2', N'Điều hòa Daikin 18000BTU (Phải)', N'Điều hòa', 'SN-DKN-BE302-02', '2025-08-16', 14500000, 'BE-302', 'ACTIVE',      N'Làm mát tốt'),
    ('AST-BE302-TV',  N'Smart TV LG QNED 65 inch 4K', N'Smart TV', 'SN-LGE-BE302-01', '2025-10-11', 17800000, 'BE-302', 'ACTIVE',      N'Chất lượng hình ảnh xuất sắc'),

    -- Phòng BE-303
    ('AST-BE303-PJ',  N'Máy chiếu Epson EB-2250U Siêu nét', N'Máy chiếu', 'SN-EPS-BE303-01', '2025-09-05', 24000000, 'BE-303', 'ACTIVE',      N'Độ sáng 5000 Ansi Lumens'),
    ('AST-BE303-AC1', N'Điều hòa Panasonic 24000BTU Inverter', N'Điều hòa', 'SN-PAN-BE303-01', '2025-08-20', 19500000, 'BE-303', 'ACTIVE',      N'Làm lạnh cực nhanh'),
    ('AST-BE303-AC2', N'Điều hòa Panasonic 24000BTU Inverter', N'Điều hòa', 'SN-PAN-BE303-02', '2025-08-20', 19500000, 'BE-303', 'ACTIVE',      N'Hoạt động bình thường'),
    ('AST-BE303-TV',  N'Smart TV Sony Bravia 75 inch 4K', N'Smart TV', 'SN-SNY-BE303-01', '2025-10-15', 28000000, 'BE-303', 'ACTIVE',      N'Phục vụ hội thảo và thảo luận chuyên sâu'),
    ('AST-BE303-AU',  N'Hệ thống Loa thanh Soundbar Bose', N'Âm thanh', 'SN-BOS-BE303-01', '2025-11-12', 12500000, 'BE-303', 'ACTIVE',      N'Âm thanh vòm trong trẻo'),

    -- Phòng AL-101 (Phòng Lab CNTT)
    ('AST-AL101-PJ',  N'Máy chiếu ViewSonic Laser LS700HD', N'Máy chiếu', 'SN-VSN-AL101-01', '2025-09-10', 21500000, 'AL-101', 'ACTIVE',      N'Đang chiếu bài giảng mượt mà'),
    ('AST-AL101-AC1', N'Điều hòa Daikin 18000BTU (Cửa sổ)', N'Điều hòa', 'SN-DKN-AL101-01', '2025-08-18', 14500000, 'AL-101', 'ACTIVE',      N'Hoạt động ổn định'),
    ('AST-AL101-AC2', N'Điều hòa Daikin 18000BTU (Cửa chính)', N'Điều hòa', 'SN-DKN-AL101-02', '2025-08-18', 14500000, 'AL-101', 'ACTIVE',      N'Hoạt động ổn định'),
    ('AST-AL101-TV',  N'Smart TV Samsung 75 inch UHD', N'Smart TV', 'SN-SSG-AL101-01', '2025-10-20', 23000000, 'AL-101', 'ACTIVE',      N'Màn hình to rõ cho toàn phòng Lab'),
    ('AST-AL101-SW',  N'Cisco Switch 48 Ports Gigabit Layer 3', N'Thiết bị Lab', 'SN-CSCO-AL101-01', '2025-07-15', 32000000, 'AL-101', 'ACTIVE', N'Trung tâm mạng phòng Lab CNTT'),
    ('AST-AL101-SRV', N'Máy chủ Server Dell PowerEdge R650', N'Thiết bị Lab', 'SN-DLL-AL101-01', '2025-07-20', 48000000, 'AL-101', 'ACTIVE',    N'Chạy máy ảo thực hành cho sinh viên'),

    -- Phòng AL-102 (Phòng Lab Đồ họa & AI)
    ('AST-AL102-PJ',  N'Máy chiếu BenQ 4K HDR Chuyên đồ họa', N'Máy chiếu', 'SN-BNQ-AL102-01', '2025-09-12', 31000000, 'AL-102', 'ACTIVE',      N'Độ chuẩn màu 100% sRGB'),
    ('AST-AL102-AC1', N'Điều hòa Daikin Inverter 24000BTU', N'Điều hòa', 'SN-DKN-AL102-01', '2025-08-22', 19500000, 'AL-102', 'ACTIVE',      N'Làm mát phòng đồ họa tải nhiệt cao'),
    ('AST-AL102-AC2', N'Điều hòa Daikin Inverter 24000BTU', N'Điều hòa', 'SN-DKN-AL102-02', '2025-08-22', 19500000, 'AL-102', 'ACTIVE',      N'Làm mát ổn định'),
    ('AST-AL102-TV',  N'Smart TV LG OLED 65 inch AI ThinQ', N'Smart TV', 'SN-LGE-AL102-01', '2025-10-25', 29000000, 'AL-102', 'ACTIVE',      N'Chuẩn màu HDR cao cấp'),
    ('AST-AL102-WS1', N'Máy tính Workstation HP Z2 G9 i9-13900K RTX 4080', N'Thiết bị Lab', 'SN-HPZ-AL102-01', '2026-01-15', 45000000, 'AL-102', 'ACTIVE', N'Máy giảng viên giảng dạy AI'),
    ('AST-AL102-WS2', N'Máy tính Apple iMac 24 inch M3 16GB', N'Thiết bị Lab', 'SN-APL-AL102-01', '2026-02-10', 38500000, 'AL-102', 'ACTIVE',    N'Thực hành lập trình macOS & iOS'),

    -- Phòng AL-201
    ('AST-AL201-PJ',  N'Máy chiếu Sony VPL-PHZ60 Laser 6000 Lumens', N'Máy chiếu', 'SN-SNY-AL201-01', '2025-09-15', 27000000, 'AL-201', 'ACTIVE', N'Chiếu sáng rõ trong ban ngày'),
    ('AST-AL201-AC1', N'Điều hòa Panasonic Inverter 18000BTU', N'Điều hòa', 'SN-PAN-AL201-01', '2025-08-25', 15200000, 'AL-201', 'ACTIVE',      N'Vận hành êm ái'),
    ('AST-AL201-AC2', N'Điều hòa Panasonic Inverter 18000BTU', N'Điều hòa', 'SN-PAN-AL201-02', '2025-08-25', 15200000, 'AL-201', 'BROKEN',      N'Bị rò rỉ nước ngưng, đang chờ thay ống xả'),
    ('AST-AL201-TV',  N'Smart TV Samsung Crystal UHD 65 inch', N'Smart TV', 'SN-SSG-AL201-01', '2025-10-30', 16500000, 'AL-201', 'ACTIVE',      N'Hoạt động bình thường'),

    -- Phòng DE-101
    ('AST-DE101-PJ',  N'Máy chiếu Epson Laser Full HD 4000lm', N'Máy chiếu', 'SN-EPS-DE101-01', '2025-09-18', 21000000, 'DE-101', 'MAINTENANCE', N'Bóng đèn báo thay thế định kỳ'),
    ('AST-DE101-AC1', N'Điều hòa Daikin 18000BTU (Phía trước)', N'Điều hòa', 'SN-DKN-DE101-01', '2025-08-28', 14500000, 'DE-101', 'ACTIVE',     N'Hoạt động tốt'),
    ('AST-DE101-AC2', N'Điều hòa Daikin 18000BTU (Phía sau)', N'Điều hòa', 'SN-DKN-DE101-02', '2025-08-28', 14500000, 'DE-101', 'ACTIVE',       N'Hoạt động tốt'),
    ('AST-DE101-TV',  N'Smart TV TCL 65 inch 4K HDR', N'Smart TV', 'SN-TCL-DE101-01', '2025-11-01', 13500000, 'DE-101', 'ACTIVE',              N'Hình ảnh sắc nét'),

    -- Phòng EP-201
    ('AST-EP201-PJ',  N'Máy chiếu Panasonic PT-VZ580 WUXGA', N'Máy chiếu', 'SN-PAN-EP201-01', '2025-09-20', 23500000, 'EP-201', 'ACTIVE',       N'Độ phân giải 1920x1200'),
    ('AST-EP201-AC1', N'Điều hòa Daikin 18000BTU', N'Điều hòa', 'SN-DKN-EP201-01', '2025-08-30', 14500000, 'EP-201', 'ACTIVE',                N'Vận hành ổn định'),
    ('AST-EP201-AC2', N'Điều hòa Daikin 18000BTU', N'Điều hòa', 'SN-DKN-EP201-02', '2025-08-30', 14500000, 'EP-201', 'ACTIVE',                N'Vận hành ổn định'),
    ('AST-EP201-TV',  N'Smart TV Samsung 65 inch', N'Smart TV', 'SN-SSG-EP201-01', '2025-11-05', 16000000, 'EP-201', 'BROKEN',                 N'Cháy cổng HDMI 1 do sốc điện, cần sửa'),

    -- Phòng GA-301 (Hội trường lớn)
    ('AST-GA301-PJ',  N'Máy chiếu Laser Công suất lớn 10000 Lumens', N'Máy chiếu', 'SN-LSR-GA301-01', '2025-09-25', 65000000, 'GA-301', 'ACTIVE', N'Phục vụ sự kiện toàn trường'),
    ('AST-GA301-AC1', N'Hệ thống Điều hòa trung tâm Daikin VRV 01', N'Điều hòa', 'SN-VRV-GA301-01', '2025-08-01', 55000000, 'GA-301', 'ACTIVE', N'Công suất cực lớn cho hội trường'),
    ('AST-GA301-AC2', N'Hệ thống Điều hòa trung tâm Daikin VRV 02', N'Điều hòa', 'SN-VRV-GA301-02', '2025-08-01', 55000000, 'GA-301', 'ACTIVE', N'Công suất cực lớn cho hội trường'),
    ('AST-GA301-TV',  N'Màn hình LED Hội trường P2.5 Indoor (150 inch)', N'Smart TV', 'SN-LED-GA301-01', '2025-10-01', 85000000, 'GA-301', 'ACTIVE', N'Màn hình LED trung tâm bục diễn'),
    ('AST-GA301-AU',  N'Dàn Âm thanh Hội trường Line Array Yamaha & Bàn Mixer', N'Âm thanh', 'SN-YMH-GA301-01', '2025-10-05', 42000000, 'GA-301', 'ACTIVE', N'Hệ thống loa và micro hội nghị chuyên nghiệp');

END;
GO

-- Thực thi thủ tục khởi tạo dữ liệu mẫu chuẩn ngay lập tức
EXEC dbo.sp_ResetFcamsData;
GO

-- ============================================================================
-- TRUY VẤN KIỂM TRA TỔNG QUAN SAU KHI NẠP DỮ LIỆU
-- ============================================================================
SELECT 'users' AS TableName, COUNT(*) AS TotalRows FROM dbo.users
UNION ALL
SELECT 'rooms' AS TableName, COUNT(*) AS TotalRows FROM dbo.rooms
UNION ALL
SELECT 'room_assets' AS TableName, COUNT(*) AS TotalRows FROM dbo.room_assets
UNION ALL
SELECT 'assets' AS TableName, COUNT(*) AS TotalRows FROM dbo.assets;

SELECT 
    COUNT(*) AS TotalAssets,
    SUM(price) AS TotalEstimatedValue_VND,
    SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) AS ActiveCount,
    SUM(CASE WHEN status = 'MAINTENANCE' THEN 1 ELSE 0 END) AS MaintenanceCount,
    SUM(CASE WHEN status = 'BROKEN' THEN 1 ELSE 0 END) AS BrokenCount
FROM dbo.assets;
GO
