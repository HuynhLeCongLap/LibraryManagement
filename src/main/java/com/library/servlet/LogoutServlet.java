package com.library.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Servlet xử lý chức năng đăng xuất
@WebServlet("/logout") // Ánh xạ URL cho servlet này
public class LogoutServlet extends HttpServlet {
    @Override
    // Xử lý yêu cầu GET (thực hiện đăng xuất)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        // Mã hóa thông báo thành công để lưu vào cookie
        String successMessage = URLEncoder.encode("Đăng xuất thành công!", StandardCharsets.UTF_8.toString());
        // Tạo cookie chứa thông báo thành công
        Cookie successCookie = new Cookie("success", successMessage);
        successCookie.setMaxAge(300); // Đặt thời gian tồn tại của cookie là 300 giây
        successCookie.setPath(contextPath); // Đặt đường dẫn áp dụng của cookie (toàn bộ ứng dụng)
        resp.addCookie(successCookie); // Thêm cookie vào phản hồi

        // Hủy bỏ phiên làm việc (session) hiện tại
        req.getSession().invalidate();

        // Chuyển hướng người dùng về trang đăng nhập
        resp.sendRedirect(contextPath + "/login");
    }
}
