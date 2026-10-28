package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Custom Tag hiển thị trạng thái thiết bị phòng học theo phong cách trắng-đen của fcams-simple.
 * Đáp ứng tiêu chí 1.4 (JSP CustomTag).
 */
public class AssetStatusTag extends SimpleTagSupport {
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        if ("FAULTY".equalsIgnoreCase(status) || "BROKEN".equalsIgnoreCase(status)) {
            out.print("<b>Hỏng hóc</b>");
        } else if ("REPAIRING".equalsIgnoreCase(status) || "MAINTENANCE".equalsIgnoreCase(status)) {
            out.print("<i>Đang bảo trì</i>");
        } else {
            out.print("Tốt");
        }
    }
}
