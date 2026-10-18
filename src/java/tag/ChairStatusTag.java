package tag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Custom Tag hiển thị số lượng ghế thực tế so với định mức chuẩn của phòng học.
 * Đáp ứng tiêu chí 1.4 (JSP CustomTag).
 */
public class ChairStatusTag extends SimpleTagSupport {
    private int cur;
    private int std;

    public void setCur(int cur) {
        this.cur = cur;
    }

    public void setStd(int std) {
        this.std = std;
    }

    @Override
    public void doTag() throws JspException, IOException {
        JspWriter out = getJspContext().getOut();
        if (cur < std) {
            out.print("<b>" + cur + "/" + std + " (Thiếu " + (std - cur) + ")</b>");
        } else if (cur > std) {
            out.print(cur + "/" + std + " (+" + (cur - std) + ")");
        } else {
            out.print(cur + "/" + std + " (Đủ)");
        }
    }
}

