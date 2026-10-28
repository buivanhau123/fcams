package model;

import java.io.Serializable;

/**
 * Tác nhân người dùng của hệ thống (Tiêu chí 1.1):
 * - LECTURER (Giảng viên)
 * - STAFF (Cán bộ Quản lý)
 * - ADMIN (Quản trị viên Hệ thống)
 */
public class User implements Serializable {
    private String username;
    private String password;
    private String fullName;
    private String role;
    private String description;

    public User() {
    }

    public User(String username, String password, String fullName, String role, String description) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(role);
    }

    public boolean isLecturer() {
        return "LECTURER".equalsIgnoreCase(role);
    }

    public boolean isStaffOrAdmin() {
        return isStaff() || isAdmin();
    }

    public String getRoleDisplayName() {
        if (isAdmin()) {
            return "Quản trị viên (Admin)";
        }
        if (isStaff()) {
            return "Cán bộ Quản lý (Staff)";
        }
        return "Giảng viên (Lecturer)";
    }
}
