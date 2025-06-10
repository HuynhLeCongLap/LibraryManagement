package com.library.servlet;

import com.library.dao.MemberDAO;
import com.library.model.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Servlet quản lý các chức năng liên quan đến thành viên cho Admin
@WebServlet("/admin/members")
public class MemberServlet extends HttpServlet {
    private MemberDAO memberDAO; // Đối tượng DAO để thao tác với thành viên

    @Override
    // Phương thức khởi tạo Servlet, khởi tạo MemberDAO
    public void init() {
        memberDAO = new MemberDAO();
    }

    @Override
    // Xử lý các yêu cầu GET (hiển thị danh sách, chỉnh sửa, xóa thành viên)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Lấy session hiện tại
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        // Kiểm tra quyền truy cập (chỉ Admin mới được phép)
        if (session == null || session.getAttribute("user") == null || !((Member) session.getAttribute("user")).getRole().equals("ADMIN")) {
            resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập nếu không có quyền
            return;
        }

        String action = req.getParameter("action"); // Lấy tham số 'action'

        try {
            // Xử lý hành động "edit" (chỉnh sửa thành viên)
            if ("edit".equals(action)) {
                try {
                    int id = Integer.parseInt(req.getParameter("id")); // Lấy ID thành viên
                    Member member = memberDAO.getMemberById(id); // Lấy thành viên theo ID
                    if (member == null) {
                        req.setAttribute("error", "Không tìm thấy thành viên với ID: " + id); // Đặt thông báo lỗi
                    } else {
                        req.setAttribute("member", member); // Đặt đối tượng thành viên vào request
                    }
                    req.getRequestDispatcher("/admin/editmember.jsp").forward(req, resp); // Chuyển tiếp đến trang chỉnh sửa
                } catch (NumberFormatException e) {
                    req.setAttribute("error", "ID không hợp lệ."); // Đặt thông báo lỗi ID
                    req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý thành viên
                }
                return;
            }
            // Xử lý hành động "delete" (xóa thành viên)
            else if ("delete".equals(action)) {
                try {
                    int id = Integer.parseInt(req.getParameter("id")); // Lấy ID thành viên
                    memberDAO.deleteMember(id); // Xóa thành viên khỏi DB
                    resp.sendRedirect(req.getContextPath() + "/admin/members?success=delete"); // Chuyển hướng với thông báo thành công
                    return;
                } catch (NumberFormatException e) {
                    req.setAttribute("error", "ID không hợp lệ."); // Đặt thông báo lỗi ID
                } catch (SQLException e) {
                    req.setAttribute("error", "Lỗi khi xóa thành viên: " + e.getMessage()); // Đặt thông báo lỗi SQL
                }
            }

            // Xử lý tìm kiếm thành viên
            String idParam = req.getParameter("id");
            String username = req.getParameter("username");
            String fullName = req.getParameter("fullName");
            String role = req.getParameter("role");

            Integer id = null;
            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    id = Integer.parseInt(idParam.trim()); // Chuyển đổi ID sang số nguyên
                } catch (NumberFormatException e) {
                    req.setAttribute("error", "ID tìm kiếm không hợp lệ."); // Đặt thông báo lỗi ID tìm kiếm
                }
            }

            List<Member> members;

            // Thực hiện tìm kiếm hoặc lấy tất cả thành viên
            if ((id != null) ||
                    (username != null && !username.trim().isEmpty()) ||
                    (fullName != null && !fullName.trim().isEmpty()) ||
                    (role != null && !role.trim().isEmpty())) {
                members = memberDAO.searchMembers(id, username, fullName, role); // Tìm kiếm thành viên
            } else {
                members = memberDAO.getAllMembers(); // Lấy tất cả thành viên
            }

            req.setAttribute("members", members); // Đặt danh sách thành viên vào request
            req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý thành viên

        } catch (SQLException e) {
            throw new ServletException("Database error", e); // Ném ngoại lệ nếu có lỗi DB nghiêm trọng
        }
    }

    @Override
    // Xử lý các yêu cầu POST (thêm hoặc cập nhật thành viên)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Lấy session hiện tại
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        // Kiểm tra quyền truy cập (chỉ Admin mới được phép)
        if (session == null || session.getAttribute("user") == null ||
                !((Member) session.getAttribute("user")).getRole().equals("ADMIN")) {
            resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập nếu không có quyền
            return;
        }

        String idParam = req.getParameter("id"); // Lấy ID thành viên từ form (nếu có, là cập nhật)

        try {
            // Nếu có ID, đây là yêu cầu cập nhật thành viên
            if (idParam != null && !idParam.trim().isEmpty()) {
                int id = Integer.parseInt(idParam);
                String username = req.getParameter("username");
                String password = req.getParameter("password");
                String fullName = req.getParameter("fullName");
                String role = req.getParameter("role");

                // Kiểm tra trùng tên đăng nhập khi cập nhật
                Member existingMember = memberDAO.getMemberById(id);
                if (!existingMember.getUsername().equals(username) && memberDAO.isUsernameExists(username)) {
                    req.setAttribute("error", "Tên đăng nhập '" + username + "' đã tồn tại. Vui lòng chọn tên khác."); // Đặt thông báo lỗi
                    req.setAttribute("member", existingMember); // Đặt lại thông tin thành viên cũ để hiển thị trên form
                    req.getRequestDispatcher("/admin/editmember.jsp").forward(req, resp); // Chuyển tiếp về trang chỉnh sửa
                    return;
                }

                Member member = new Member(id, username, password, fullName, role); // Tạo đối tượng thành viên
                memberDAO.updateMember(member); // Cập nhật thành viên trong DB
            }
            // Nếu không có ID, đây là yêu cầu thêm thành viên mới
            else {
                String name = req.getParameter("name");
                String username = req.getParameter("username");
                String password = req.getParameter("password");

                // Kiểm tra các trường bắt buộc
                if (name == null || name.trim().isEmpty()) {
                    req.setAttribute("error", "Họ và tên không được để trống.");
                    req.getRequestDispatcher("/admin/members.jsp").forward(req, resp);
                    return;
                }
                if (username == null || username.trim().isEmpty()) {
                    req.setAttribute("error", "Tên đăng nhập không được để trống.");
                    req.getRequestDispatcher("/admin/members.jsp").forward(req, resp);
                    return;
                }
                if (password == null || password.trim().isEmpty()) {
                    req.setAttribute("error", "Mật khẩu không được để trống.");
                    req.getRequestDispatcher("/admin/members.jsp").forward(req, resp);
                    return;
                }

                // Kiểm tra trùng tên đăng nhập khi thêm mới
                if (memberDAO.isUsernameExists(username)) {
                    req.setAttribute("error", "Tên đăng nhập '" + username + "' đã tồn tại. Vui lòng chọn tên khác."); // Đặt thông báo lỗi
                    req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý thành viên
                    return;
                }

                Member member = new Member(0, username.trim(), password.trim(), name.trim(), "MEMBER"); // Tạo đối tượng thành viên mới
                memberDAO.addMember(member); // Thêm thành viên vào DB

                req.setAttribute("success", "Thêm thành viên thành công!"); // Đặt thông báo thành công
            }

            resp.sendRedirect(contextPath + "/admin/members"); // Chuyển hướng về trang quản lý thành viên

        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi CSDL: " + e.getMessage()); // Đặt thông báo lỗi SQL
            try {
                List<Member> members = memberDAO.getAllMembers(); // Lấy lại danh sách thành viên nếu có lỗi
                req.setAttribute("members", members); // Đặt danh sách thành viên vào request
                req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý thành viên
            } catch (SQLException ex) {
                throw new ServletException("Lỗi CSDL", ex); // Ném ngoại lệ nếu có lỗi DB nghiêm trọng
            }
        } catch (NumberFormatException e) {
            req.setAttribute("error", "ID không hợp lệ: " + e.getMessage()); // Đặt thông báo lỗi định dạng số
            try {
                List<Member> members = memberDAO.getAllMembers(); // Lấy lại danh sách thành viên
                req.setAttribute("members", members); // Đặt danh sách thành viên vào request
                req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý thành viên
            } catch (SQLException ex) {
                throw new ServletException("Lỗi CSDL", ex); // Ném ngoại lệ nếu có lỗi DB nghiêm trọng
            }
        }
    }
}
