package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.Loan;
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
import java.util.stream.Collectors;

// Servlet xử lý việc tạo và hiển thị các báo cáo quản lý thư viện
@WebServlet("/admin/reports") // Ánh xạ URL cho servlet này
public class ReportServlet extends HttpServlet {
    private LoanDAO loanDAO; // Đối tượng DAO để thao tác với các khoản mượn
    private BookDAO bookDAO; // Đối tượng DAO để thao tác với sách
    private MemberDAO memberDAO; // Đối tượng DAO để thao tác với thành viên

    @Override
    // Phương thức khởi tạo Servlet, khởi tạo các đối tượng DAO
    public void init() {
        loanDAO = new LoanDAO();
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
    }

    @Override
    // Xử lý các yêu cầu GET để lấy và hiển thị dữ liệu báo cáo
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(); // Lấy session hiện tại
        // Kiểm tra quyền truy cập (chỉ Admin mới được phép xem báo cáo)
        if (session.getAttribute("user") == null || !"ADMIN".equals(((Member) session.getAttribute("user")).getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login"); // Chuyển hướng về trang đăng nhập nếu không có quyền
            return;
        }

        try {
            List<Loan> loans = loanDAO.getAllLoans(); // Lấy tất cả các khoản mượn
            List<Book> books = bookDAO.getAllBooks(); // Lấy tất cả sách
            List<Member> members = memberDAO.getAllMembers(); // Lấy tất cả thành viên

            // Ghi log để kiểm tra dữ liệu (có thể xóa trong production)
            System.out.println("Số lượng loans (ReportServlet): " + loans.size());
            long currentlyBorrowed = loans.stream().filter(loan -> loan.getReturnDate() == null).count();
            System.out.println("Số lượng sách đang mượn (ReportServlet): " + currentlyBorrowed);

            // Lọc ra các khoản mượn quá hạn (chưa trả và đã quá ngày đến hạn)
            List<Loan> overdueLoans = loans.stream()
                    .filter(loan -> loan.getReturnDate() == null && loan.getDueDate().isBefore(java.time.LocalDate.now()))
                    .collect(Collectors.toList());

            // Đặt các danh sách dữ liệu vào request để JSP có thể hiển thị
            req.setAttribute("loans", loans);
            req.setAttribute("overdueLoans", overdueLoans);
            req.setAttribute("books", books);
            req.setAttribute("members", members);
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp); // Chuyển tiếp đến trang báo cáo JSP
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi lấy dữ liệu báo cáo: " + e.getMessage()); // Đặt thông báo lỗi
            req.getRequestDispatcher("/admin/reports.jsp").forward(req, resp); // Chuyển tiếp về trang báo cáo với lỗi
        }
    }
}
