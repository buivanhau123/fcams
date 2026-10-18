package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Custom Tag hiển thị tên vai trò tiếng Việt của người dùng.
 * Đáp ứng tiêu chí 1.4 (JSP CustomTag).
 */
public class RoleNameTag extends SimpleTagSupport {
    private String role;

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        if ("ADMIN".equalsIgnoreCase(role)) {
            out.print("Quản trị viên (Admin)");
        } else if ("IT_STAFF".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role)) {
            out.print("Cán bộ Quản lý (Staff)");
        } else {
            out.print("Giảng viên");
        }
    }
}

