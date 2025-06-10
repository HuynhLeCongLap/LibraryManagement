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

// Servlet xử lý chức năng đăng ký thành viên mới
@WebServlet("/register") // Ánh xạ URL cho servlet này
public class RegisterServlet extends HttpServlet {
    private MemberDAO memberDAO; // Đối tượng DAO để thao tác với thành viên

    @Override
    // Phương thức khởi tạo Servlet, khởi tạo MemberDAO
    public void init() {
        memberDAO = new MemberDAO();
    }

    @Override
    // Xử lý yêu cầu GET (hiển thị trang đăng ký)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp); // Chuyển tiếp đến trang JSP đăng ký
    }

    @Override
    // Xử lý yêu cầu POST (xử lý dữ liệu đăng ký thành viên mới)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username"); // Lấy tên đăng nhập từ form
        String password = req.getParameter("password"); // Lấy mật khẩu từ form
        String fullName = req.getParameter("fullName"); // Lấy họ và tên đầy đủ từ form
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        try {
            // Kiểm tra xem tên đăng nhập đã tồn tại trong cơ sở dữ liệu chưa
            Member existingMember = memberDAO.getMemberByUsername(username);
            if (existingMember != null) {
                req.setAttribute("error", "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác."); // Đặt thông báo lỗi
                req.getRequestDispatcher("/register.jsp").forward(req, resp); // Chuyển tiếp về trang đăng ký
                return;
            }

            // Tạo đối tượng thành viên mới với vai trò mặc định là "MEMBER"
            Member newMember = new Member(0, username, password, fullName, "MEMBER");
            memberDAO.addMember(newMember); // Thêm thành viên mới vào cơ sở dữ liệu

            // Đặt thông báo thành công vào session và chuyển hướng về trang đăng nhập
            HttpSession session = req.getSession();
            session.setAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            resp.sendRedirect(contextPath + "/login");
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi hệ thống: " + e.getMessage()); // Đặt thông báo lỗi hệ thống
            req.getRequestDispatcher("/register.jsp").forward(req, resp); // Chuyển tiếp về trang đăng ký nếu có lỗi
        }
    }
}
