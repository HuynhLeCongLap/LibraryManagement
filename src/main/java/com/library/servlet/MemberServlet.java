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
                    session.setAttribute("success", "Xóa thành viên thành công!"); // Đặt thông báo thành công vào session
                    resp.sendRedirect(req.getContextPath() + "/admin/members"); // Chuyển hướng với thông báo thành công
                    return;
                } catch (NumberFormatException e) {
                    session.setAttribute("error", "ID không hợp lệ."); // Đặt thông báo lỗi ID vào session
                } catch (SQLException e) {
                    session.setAttribute("error", "Lỗi khi xóa thành viên: " + e.getMessage()); // Đặt thông báo lỗi SQL vào session
                }
                // Quan trọng: Sau khi xóa hoặc gặp lỗi xóa, vẫn cần tải lại danh sách thành viên
            }

            // --- Phần quan trọng để đảm bảo hiển thị danh sách thành viên ---
            String idParam = req.getParameter("id");
            String username = req.getParameter("username");
            String fullName = req.getParameter("fullName");
            String role = req.getParameter("role");

            Integer id = null;
            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    id = Integer.parseInt(idParam.trim()); // Chuyển đổi ID sang số nguyên
                } catch (NumberFormatException e) {
                    // Nếu ID không hợp lệ, thông báo lỗi đã được đặt ở trên.
                    // id vẫn là null, nên các điều kiện tìm kiếm khác sẽ được xem xét.
                    // hoặc nếu không có điều kiện nào khác, sẽ lấy tất cả thành viên.
                    req.setAttribute("error", "ID tìm kiếm không hợp lệ.");
                }
            }

            List<Member> members;

            // Logic tải danh sách thành viên:
            // Nếu có bất kỳ tham số tìm kiếm hợp lệ nào, thực hiện tìm kiếm.
            // Ngược lại (bao gồm cả trường hợp redirect từ RegisterServlet không có tham số),
            // lấy TẤT CẢ thành viên.
            if ((id != null) ||
                    (username != null && !username.trim().isEmpty()) ||
                    (fullName != null && !fullName.trim().isEmpty()) ||
                    (role != null && !role.trim().isEmpty())) {
                members = memberDAO.searchMembers(id, username, fullName, role); // Tìm kiếm thành viên
            } else {
                members = memberDAO.getAllMembers(); // Lấy TẤT CẢ thành viên theo mặc định
            }

            req.setAttribute("members", members); // Đặt danh sách thành viên vào request

            // Lấy thông báo từ session (từ RegisterServlet hoặc từ chính MemberServlet)
            String successMsg = (String) session.getAttribute("success");
            String errorMsg = (String) session.getAttribute("error");
            if (successMsg != null) {
                req.setAttribute("success", successMsg);
                session.removeAttribute("success"); // Xóa khỏi session sau khi đọc
            }
            if (errorMsg != null) {
                req.setAttribute("error", errorMsg);
                session.removeAttribute("error"); // Xóa khỏi session sau khi đọc
            }

            req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý thành viên

        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi CSDL: " + e.getMessage()); // Đặt thông báo lỗi SQL
            // Trong trường hợp lỗi SQL nghiêm trọng, vẫn cố gắng tải lại trang
            // và cung cấp thông báo lỗi. Danh sách members có thể sẽ rỗng.
            try {
                List<Member> members = memberDAO.getAllMembers(); // Thử lại để tránh trang trống hoàn toàn
                req.setAttribute("members", members);
            } catch (SQLException ex) {
                // Nếu vẫn lỗi, ném ngoại lệ Runtime hoặc chỉ hiển thị lỗi
                throw new ServletException("Lỗi CSDL nghiêm trọng khi tải thành viên: " + ex.getMessage(), ex);
            }
            req.getRequestDispatcher("/admin/members.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();

        if (session == null || session.getAttribute("user") == null ||
                !((Member) session.getAttribute("user")).getRole().equals("ADMIN")) {
            resp.sendRedirect(contextPath + "/login");
            return;
        }

        String idParam = req.getParameter("id");

        try {
            if (idParam != null && !idParam.trim().isEmpty()) {
                // Cập nhật thành viên
                int id = Integer.parseInt(idParam);
                String username = req.getParameter("username");
                String password = req.getParameter("password");
                String fullName = req.getParameter("fullName");
                String role = req.getParameter("role");

                // Kiểm tra username khi cập nhật
                Member existingMember = memberDAO.getMemberById(id);
                if (!existingMember.getUsername().equals(username) && memberDAO.isUsernameExists(username)) {
                    req.setAttribute("error", "Tên đăng nhập '" + username + "' đã tồn tại. Vui lòng chọn tên khác.");
                    req.setAttribute("member", existingMember);
                    req.getRequestDispatcher("/admin/editmember.jsp").forward(req, resp);
                    return;
                }

                Member member = new Member(id, username, password, fullName, role);
                memberDAO.updateMember(member);
                session.setAttribute("success", "Cập nhật thành viên thành công!"); // Đặt thông báo thành công vào session
            } else {
                // Thêm thành viên mới
                String name = req.getParameter("name");
                String username = req.getParameter("username");
                String password = req.getParameter("password");

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

                // Kiểm tra username khi thêm mới
                if (memberDAO.isUsernameExists(username)) {
                    req.setAttribute("error", "Tên đăng nhập '" + username + "' đã tồn tại. Vui lòng chọn tên khác."); // Đặt thông báo lỗi
                    req.getRequestDispatcher("/admin/members.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý thành viên
                    return;
                }

                Member member = new Member(0, username.trim(), password.trim(), name.trim(), "MEMBER");
                memberDAO.addMember(member);

                session.setAttribute("success", "Thêm thành viên thành công!"); // Đặt thông báo thành công vào session
            }

            resp.sendRedirect(contextPath + "/admin/members");

        } catch (SQLException e) {
            session.setAttribute("error", "Lỗi CSDL: " + e.getMessage()); // Đặt lỗi vào session
            try {
                List<Member> members = memberDAO.getAllMembers(); // Cố gắng tải lại danh sách
                req.setAttribute("members", members);
            } catch (SQLException ex) {
                throw new ServletException("Lỗi CSDL nghiêm trọng khi xử lý POST thành viên: " + ex.getMessage(), ex);
            }
            req.getRequestDispatcher("/admin/members.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            session.setAttribute("error", "ID không hợp lệ: " + e.getMessage()); // Đặt lỗi vào session
            try {
                List<Member> members = memberDAO.getAllMembers(); // Cố gắng tải lại danh sách
                req.setAttribute("members", members);
            } catch (SQLException ex) {
                throw new ServletException("Lỗi CSDL nghiêm trọng khi xử lý POST thành viên: " + ex.getMessage(), ex);
            }
            req.getRequestDispatcher("/admin/members.jsp").forward(req, resp);
        }
    }
}

