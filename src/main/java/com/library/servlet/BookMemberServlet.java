package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.Member;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

// Servlet xử lý yêu cầu liên quan đến sách có sẵn và thành viên
@WebServlet("/admin/getAvailableBooks") // Ánh xạ URL cho servlet này
public class BookMemberServlet extends HttpServlet {
    private BookDAO bookDAO = new BookDAO(); // Đối tượng DAO để thao tác với sách
    private MemberDAO memberDAO = new MemberDAO(); // Đối tượng DAO để thao tác với thành viên

    @Override
    // Phương thức xử lý các yêu cầu GET
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json"); // Đặt kiểu nội dung phản hồi là JSON
        PrintWriter out = resp.getWriter(); // Lấy đối tượng PrintWriter để gửi phản hồi
        String action = req.getParameter("action"); // Lấy tham số 'action' từ yêu cầu

        Gson gson = new Gson(); // Khởi tạo đối tượng Gson để chuyển đổi đối tượng Java sang JSON

        // Xử lý hành động "books" để lấy danh sách sách có sẵn
        if ("books".equals(action)) {
            try {
                List<Book> books = bookDAO.getAvailableBooks(); // Lấy danh sách sách có sẵn
                out.print(gson.toJson(books)); // Chuyển đổi danh sách sách thành JSON và gửi đi
            } catch (SQLException e) {
                out.print("[]"); // Trả về mảng JSON rỗng nếu có lỗi
                e.printStackTrace(); // In lỗi ra console
            }
            // Xử lý hành động "members" để lấy danh sách tất cả thành viên
        } else if ("members".equals(action)) {
            try {
                List<Member> members = memberDAO.getAllMembers(); // Lấy danh sách tất cả thành viên
                out.print(gson.toJson(members)); // Chuyển đổi danh sách thành viên thành JSON và gửi đi
            } catch (SQLException e) {
                out.print("[]"); // Trả về mảng JSON rỗng nếu có lỗi
                e.printStackTrace(); // In lỗi ra console
            }
        }
        out.flush(); // Đẩy tất cả dữ liệu còn lại trong bộ đệm ra phản hồi
    }
}
