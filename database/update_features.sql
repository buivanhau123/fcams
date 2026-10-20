USE FCAMS_DB;
GO

-- 1. Bổ sung cột purpose vào dbo.rooms nếu chưa có
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.rooms') AND name = 'purpose')
BEGIN
    ALTER TABLE dbo.rooms ADD purpose NVARCHAR(100) NOT NULL CONSTRAINT DF_rooms_purpose DEFAULT N'Lý thuyết tiêu chuẩn';
END
GO

-- Cập nhật dữ liệu mục đích sử dụng cho các phòng hiện tại
UPDATE dbo.rooms SET purpose = N'Lý thuyết tiêu chuẩn' WHERE id IN ('BE-301', 'BE-302', 'GA-301');
UPDATE dbo.rooms SET purpose = N'Phòng Lab thực hành CNTT' WHERE id IN ('AL-101', 'AL-201', 'EP-201');
UPDATE dbo.rooms SET purpose = N'Phòng Seminar & Thảo luận nhóm' WHERE id = 'DE-101';
GO

-- 2. Tạo bảng dbo.assets phục vụ Quản lý tài sản (Asset Management - CRUD)
IF OBJECT_ID('dbo.assets', 'U') IS NOT NULL DROP TABLE dbo.assets;
GO

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
    CONSTRAINT CK_assets_status CHECK (status IN ('ACTIVE', 'REPAIRING', 'FAULTY')),
    CONSTRAINT CK_assets_price CHECK (price >= 0)
);
GO

-- 3. Nạp dữ liệu tài sản mẫu đầy đủ thông số
INSERT INTO dbo.assets (code, name, category, serial_number, handover_date, price, room_id, status, note) VALUES
-- BE-301
('AST-BE301-PJ', N'Máy chiếu Sony Laser VPL-PHZ50', N'Máy chiếu', 'SN-SNY-2023-01', '2023-08-15', 22500000, 'BE-301', 'ACTIVE', N'Hoạt động tốt, hình ảnh sắc nét'),
('AST-BE301-AC1', N'Điều hòa Daikin Inverter 18000BTU (Trái)', N'Điều hòa', 'SN-DKN-8821', '2022-04-10', 14200000, 'BE-301', 'ACTIVE', N'Làm lạnh nhanh'),
('AST-BE301-AC2', N'Điều hòa Daikin Inverter 18000BTU (Phải)', N'Điều hòa', 'SN-DKN-8822', '2022-04-10', 14200000, 'BE-301', 'ACTIVE', N'Làm lạnh bình thường'),
('AST-BE301-TV', N'Smart TV Samsung 65 inch Crystal UHD', N'Smart TV', 'SN-SSG-6501', '2023-09-01', 16800000, 'BE-301', 'ACTIVE', N'Màn hình hiển thị tốt'),
('AST-BE301-SPK', N'Hệ thống Loa trợ giảng và Micro không dây', N'Âm thanh', 'SN-JBL-301', '2023-08-20', 5500000, 'BE-301', 'ACTIVE', N'Âm thanh rõ ràng'),
('AST-BE301-CHR', N'Bộ bàn ghế sinh viên tiêu chuẩn (35 bộ)', N'Bàn ghế', 'SN-FUR-301', '2022-01-15', 18500000, 'BE-301', 'ACTIVE', N'Hiện đang thiếu 5 ghế chuyển sang BE-302'),

-- BE-302
('AST-BE302-PJ', N'Máy chiếu Panasonic PT-VMZ51', N'Máy chiếu', 'SN-PNS-2023-02', '2023-08-15', 24000000, 'BE-302', 'ACTIVE', N'Hoạt động bình thường'),
('AST-BE302-AC1', N'Điều hòa Daikin Inverter 18000BTU (Trái)', N'Điều hòa', 'SN-DKN-8823', '2022-04-10', 14200000, 'BE-302', 'ACTIVE', N'Bình thường'),
('AST-BE302-AC2', N'Điều hòa Daikin Inverter 18000BTU (Phải)', N'Điều hòa', 'SN-DKN-8824', '2022-04-10', 14200000, 'BE-302', 'ACTIVE', N'Bình thường'),
('AST-BE302-TV', N'Smart TV LG 65 inch 4K NanoCell', N'Smart TV', 'SN-LGE-6502', '2023-09-01', 17200000, 'BE-302', 'ACTIVE', N'Kết nối HDMI ổn định'),
('AST-BE302-CHR', N'Bộ bàn ghế sinh viên tiêu chuẩn (35 bộ)', N'Bàn ghế', 'SN-FUR-302', '2022-01-15', 18500000, 'BE-302', 'ACTIVE', N'Đang mượn thêm 5 ghế từ BE-301'),

-- AL-101
('AST-AL101-PJ', N'Máy chiếu Epson EB-2250U', N'Máy chiếu', 'SN-EPS-101', '2021-11-20', 19800000, 'AL-101', 'FAULTY', N'Bóng đèn chập chờn, mờ không nhìn rõ chữ - Cần thay bóng gấp'),
('AST-AL101-AC1', N'Điều hòa Casper 24000BTU (Trái)', N'Điều hòa', 'SN-CSP-101A', '2022-06-12', 12500000, 'AL-101', 'ACTIVE', N'Hoạt động bình thường'),
('AST-AL101-AC2', N'Điều hòa Casper 24000BTU (Phải)', N'Điều hòa', 'SN-CSP-101B', '2022-06-12', 12500000, 'AL-101', 'ACTIVE', N'Hoạt động bình thường'),
('AST-AL101-TV', N'Smart TV Sony Bravia 75 inch 4K', N'Smart TV', 'SN-SNY-7501', '2023-10-05', 26500000, 'AL-101', 'ACTIVE', N'Hiển thị độ phân giải cao'),
('AST-AL101-NET', N'Switch mạng Cisco Catalyst 24 Port Gigabit', N'Thiết bị Lab', 'SN-CSC-24P', '2023-03-18', 9200000, 'AL-101', 'ACTIVE', N'Hạ tầng mạng Lab ổn định'),

-- AL-201
('AST-AL201-PJ', N'Máy chiếu Sony Laser VPL-PHZ10', N'Máy chiếu', 'SN-SNY-201', '2022-05-10', 21000000, 'AL-201', 'ACTIVE', N'Tốt'),
('AST-AL201-AC1', N'Điều hòa Daikin Inverter (Trái)', N'Điều hòa', 'SN-DKN-201A', '2022-05-10', 14200000, 'AL-201', 'REPAIRING', N'Đang nạp gas và vệ sinh lưới lọc dàn lạnh'),
('AST-AL201-AC2', N'Điều hòa Daikin Inverter (Phải)', N'Điều hòa', 'SN-DKN-201B', '2022-05-10', 14200000, 'AL-201', 'ACTIVE', N'Tốt'),
('AST-AL201-TV', N'Smart TV Sony 65 inch 4K', N'Smart TV', 'SN-SNY-6502', '2023-09-01', 17500000, 'AL-201', 'ACTIVE', N'Tốt'),

-- DE-101
('AST-DE101-PJ', N'Máy chiếu Panasonic Full HD PT-LB386', N'Máy chiếu', 'SN-PNS-101', '2023-01-10', 15800000, 'DE-101', 'ACTIVE', N'Sử dụng tốt cho hội thảo'),
('AST-DE101-AC1', N'Điều hòa Daikin 24000BTU (Trái)', N'Điều hòa', 'SN-DKN-DE1', '2022-03-25', 16500000, 'DE-101', 'ACTIVE', N'Tốt'),
('AST-DE101-AC2', N'Điều hòa Daikin 24000BTU (Phải)', N'Điều hòa', 'SN-DKN-DE2', '2022-03-25', 16500000, 'DE-101', 'ACTIVE', N'Tốt'),
('AST-DE101-TV', N'Smart TV Samsung 75 inch Crystal UHD', N'Smart TV', 'SN-SSG-7502', '2023-11-01', 28000000, 'DE-101', 'ACTIVE', N'Màn hình lớn cho phòng Seminar'),

-- EP-201
('AST-EP201-PJ', N'Máy chiếu Epson tương tác 4K EB-1485Fi', N'Máy chiếu', 'SN-EPS-EP201', '2024-02-20', 32000000, 'EP-201', 'ACTIVE', N'Công nghệ cảm ứng bảng trắng'),
('AST-EP201-AC1', N'Điều hòa Casper 18000BTU (Trái)', N'Điều hòa', 'SN-CSP-EP1', '2023-05-15', 11500000, 'EP-201', 'ACTIVE', N'Tốt'),
('AST-EP201-AC2', N'Điều hòa Casper 18000BTU (Phải)', N'Điều hòa', 'SN-CSP-EP2', '2023-05-15', 11500000, 'EP-201', 'ACTIVE', N'Tốt'),
('AST-EP201-TV', N'Smart TV LG OLED 65 inch Gallery Design', N'Smart TV', 'SN-LGE-EP65', '2024-01-15', 35000000, 'EP-201', 'ACTIVE', N'Chất lượng hình ảnh Lab AI'),

-- GA-301
('AST-GA301-PJ', N'Máy chiếu Sony VPL-EW435', N'Máy chiếu', 'SN-SNY-GA3', '2022-08-10', 16500000, 'GA-301', 'ACTIVE', N'Bình thường'),
('AST-GA301-AC1', N'Điều hòa Daikin 18000BTU (Trái)', N'Điều hòa', 'SN-DKN-GA1', '2022-08-10', 13800000, 'GA-301', 'ACTIVE', N'Bình thường'),
('AST-GA301-AC2', N'Điều hòa Daikin 18000BTU (Phải)', N'Điều hòa', 'SN-DKN-GA2', '2022-08-10', 13800000, 'GA-301', 'ACTIVE', N'Bình thường'),
('AST-GA301-TV', N'Smart TV Samsung 65 inch', N'Smart TV', 'SN-SSG-GA65', '2023-04-12', 15500000, 'GA-301', 'ACTIVE', N'Bình thường');
GO

PRINT N'Database FCAMS_DB updated successfully with assets table and room purpose!';
GO

