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

// Servlet xử lý chức năng đăng nhập
@WebServlet("/login") // Ánh xạ URL cho servlet này
public class LoginServlet extends HttpServlet {
    private MemberDAO memberDAO; // Đối tượng DAO để thao tác với thành viên

    @Override
    // Phương thức khởi tạo Servlet, khởi tạo MemberDAO
    public void init() {
        memberDAO = new MemberDAO();
    }

    @Override
    // Xử lý yêu cầu GET (hiển thị trang đăng nhập)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp); // Chuyển tiếp đến trang JSP đăng nhập
    }

    @Override
    // Xử lý yêu cầu POST (xử lý dữ liệu đăng nhập)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username"); // Lấy tên đăng nhập từ form
        String password = req.getParameter("password"); // Lấy mật khẩu từ form
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng
        HttpSession session = req.getSession(); // Lấy session hiện tại

        try {
            Member member = memberDAO.getMemberByUsername(username); // Lấy thành viên theo tên đăng nhập
            // Kiểm tra thông tin đăng nhập
            if (member != null && member.getPassword().equals(password)) {
                session.setAttribute("user", member); // Lưu thông tin người dùng vào session
                session.setAttribute("success", "Đăng nhập thành công!"); // Thêm thông báo thành công
                session.removeAttribute("error"); // Xóa thông báo lỗi nếu có

                // Chuyển hướng người dùng dựa trên vai trò
                if (member.getRole().equals("ADMIN")) {
                    resp.sendRedirect(contextPath + "/admin/books"); // Chuyển hướng Admin đến trang quản lý sách
                } else {
                    resp.sendRedirect(contextPath + "/member/dashboard"); // Chuyển hướng thành viên thường đến trang dashboard
                }
            } else {
                session.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu."); // Đặt thông báo lỗi vào session
                resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập
            }
        } catch (SQLException e) {
            session.setAttribute("error", "Lỗi hệ thống: " + e.getMessage()); // Đặt thông báo lỗi hệ thống
            resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập
        }
    }
}
