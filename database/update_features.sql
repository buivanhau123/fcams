USE FCAMS_DB;
GO

-- 1. Chuẩn hóa vai trò người dùng trong dbo.users (Đổi IT_STAFF sang STAFF, cập nhật hungnq thành LECTURER)
IF EXISTS (SELECT * FROM sys.check_constraints WHERE name = 'CK_users_role')
BEGIN
    ALTER TABLE dbo.users DROP CONSTRAINT CK_users_role;
END
GO

UPDATE dbo.users SET role = 'STAFF' WHERE role = 'IT_STAFF';
UPDATE dbo.users SET role = 'LECTURER', description = N'Giảng viên (Lecturer): Xem danh sách phòng học, xem tình trạng các đồ vật trong phòng và gửi báo cáo đồ hỏng hóc cho Cán bộ Quản lý (Staff).' WHERE username = 'hungnq';
GO

ALTER TABLE dbo.users ADD CONSTRAINT CK_users_role CHECK (role IN ('ADMIN', 'STAFF', 'LECTURER'));
GO

-- 2. Tạo bảng dbo.change_history phục vụ chức năng Lịch sử thay đổi & Báo cáo hỏng hóc
IF OBJECT_ID('dbo.change_history', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.change_history (
        id INT IDENTITY(1,1) PRIMARY KEY,
        action_type VARCHAR(30) NOT NULL,
        target_type VARCHAR(20) NOT NULL DEFAULT 'ASSET',
        target_code VARCHAR(50) NOT NULL,
        target_name NVARCHAR(150) NOT NULL,
        room_id VARCHAR(20) NULL,
        old_value NVARCHAR(300) NULL,
        new_value NVARCHAR(300) NULL,
        description NVARCHAR(500) NOT NULL,
        performed_by VARCHAR(50) NOT NULL,
        performer_name NVARCHAR(100) NOT NULL,
        performer_role VARCHAR(20) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END
GO

PRINT N'Database FCAMS_DB updated successfully with LECTURER/STAFF roles and change_history table!';
GO
