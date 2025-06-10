package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
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
import java.time.LocalDate;
import java.util.List;

// Servlet xử lý các chức năng cho bảng điều khiển của thành viên (dashboard)
@WebServlet({"/member/dashboard", "/member/return", "/member/borrow"})
public class MemberDashboardServlet extends HttpServlet {
    private BookDAO bookDAO; // Đối tượng DAO để thao tác với sách
    private LoanDAO loanDAO; // Đối tượng DAO để thao tác với các khoản mượn

    @Override
    // Phương thức khởi tạo Servlet, khởi tạo các đối tượng DAO
    public void init() {
        bookDAO = new BookDAO();
        loanDAO = new LoanDAO();
    }

    @Override
    // Xử lý các yêu cầu GET (hiển thị dashboard, trang mượn/trả sách)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Lấy session hiện tại
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        // Kiểm tra quyền truy cập (chỉ thành viên mới được phép)
        if (session == null || session.getAttribute("user") == null || !((Member) session.getAttribute("user")).getRole().equals("MEMBER")) {
            resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập
            return;
        }

        String path = req.getServletPath(); // Lấy đường dẫn servlet được yêu cầu
        try {
            // Hiển thị dashboard của thành viên
            if ("/member/dashboard".equals(path)) {
                List<Book> books = bookDAO.getAllBooks(); // Lấy tất cả sách
                req.setAttribute("books", books); // Đặt danh sách sách vào request
                req.getRequestDispatcher("/member/dashboard.jsp").forward(req, resp); // Chuyển tiếp đến trang dashboard
            }
            // Hiển thị trang mượn sách
            else if ("/member/borrow".equals(path)) {
                List<Book> books = bookDAO.getAllBooks(); // Lấy tất cả sách
                req.setAttribute("books", books); // Đặt danh sách sách vào request
                req.getRequestDispatcher("/member/borrow.jsp").forward(req, resp); // Chuyển tiếp đến trang mượn sách
            }
            // Hiển thị trang trả sách
            else if ("/member/return".equals(path)) {
                String action = req.getParameter("action"); // Lấy tham số hành động

                // Nếu hành động là "return", xử lý trả sách
                if ("return".equals(action)) {
                    handleReturn(req, resp);
                    return; // handleReturn đã chuyển hướng, nên thoát ngay
                }

                // Tải danh sách sách đã mượn của thành viên
                Member member = (Member) session.getAttribute("user");
                List<Loan> loans = loanDAO.getLoansByMember(member.getId());
                for (Loan loan : loans) {
                    try {
                        Book book = bookDAO.getBookById(loan.getBookId());
                        loan.setBook(book); // Gắn đối tượng sách vào khoản mượn
                    } catch (SQLException e) {
                        e.printStackTrace(); // In lỗi nếu không tìm thấy sách
                    }
                }
                req.setAttribute("loans", loans); // Đặt danh sách khoản mượn vào request
                req.getRequestDispatcher("/member/return.jsp").forward(req, resp); // Chuyển tiếp đến trang trả sách
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi hệ thống: " + e.getMessage()); // Đặt thông báo lỗi

            // Chuyển tiếp về trang tương ứng nếu có lỗi SQL
            if ("/member/dashboard".equals(path)) {
                req.getRequestDispatcher("/member/dashboard.jsp").forward(req, resp);
            } else if ("/member/borrow".equals(path)) {
                req.getRequestDispatcher("/member/borrow.jsp").forward(req, resp);
            } else {
                req.getRequestDispatcher("/member/return.jsp").forward(req, resp);
            }
        }
    }

    @Override
    // Xử lý các yêu cầu POST (ví dụ: mượn sách)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Lấy session hiện tại
        Member user = (Member) session.getAttribute("user"); // Lấy thông tin người dùng

        // Kiểm tra quyền truy cập (chỉ thành viên mới được phép)
        if (user == null || !"MEMBER".equals(user.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login"); // Chuyển hướng về trang đăng nhập
            return;
        }

        String path = req.getServletPath(); // Lấy đường dẫn servlet
        try {
            // Xử lý yêu cầu mượn sách
            if ("/member/borrow".equals(path)) {
                String bookId = req.getParameter("bookId"); // Lấy ID sách từ form
                if (bookId == null || bookId.trim().isEmpty()) {
                    throw new IllegalArgumentException("Vui lòng chọn một sách để mượn."); // Ném lỗi nếu không chọn sách
                }
                int bookIdInt = Integer.parseInt(bookId); // Chuyển đổi ID sách sang số nguyên
                Book book = bookDAO.getBookById(bookIdInt); // Lấy thông tin sách

                if (book == null) {
                    throw new IllegalArgumentException("Sách không tồn tại."); // Ném lỗi nếu sách không tồn tại
                }

                // Kiểm tra xem sách đã được mượn hay chưa
                List<Loan> loans = loanDAO.getAllLoans();
                boolean isBorrowed = loans.stream()
                        .anyMatch(loan -> loan.getBookId() == bookIdInt && loan.getReturnDate() == null);
                if (isBorrowed) {
                    throw new IllegalArgumentException("Sách này đã được mượn. Vui lòng chọn sách khác."); // Ném lỗi nếu sách đã mượn
                }

                // Tạo và thêm bản ghi mượn sách mới
                Loan loan = new Loan();
                loan.setBookId(bookIdInt);
                loan.setMemberId(user.getId());
                loan.setBorrowDate(LocalDate.now()); // Ngày mượn là hôm nay
                loan.setDueDate(LocalDate.now().plusDays(14)); // Ngày đến hạn là sau 14 ngày
                loanDAO.addLoan(loan); // Thêm bản ghi mượn vào DB

                req.setAttribute("success", "Mượn sách thành công!"); // Đặt thông báo thành công
                List<Book> books = bookDAO.getAllBooks(); // Lấy lại danh sách sách
                req.setAttribute("books", books); // Đặt danh sách sách vào request
                req.getRequestDispatcher("/member/borrow.jsp").forward(req, resp); // Chuyển tiếp đến trang mượn sách
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi mượn sách: " + e.getMessage()); // Đặt thông báo lỗi SQL
            List<Book> books = null;
            try {
                books = bookDAO.getAllBooks(); // Thử lấy lại danh sách sách
            } catch (SQLException ex) {
                throw new RuntimeException(ex); // Ném lỗi nếu không thể lấy danh sách sách
            }
            req.setAttribute("books", books); // Đặt danh sách sách vào request
            req.getRequestDispatcher("/member/borrow.jsp").forward(req, resp); // Chuyển tiếp đến trang mượn sách
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage()); // Đặt thông báo lỗi logic
            try {
                List<Book> books = bookDAO.getAllBooks(); // Thử lấy lại danh sách sách
                req.setAttribute("books", books); // Đặt danh sách sách vào request
                req.getRequestDispatcher("/member/borrow.jsp").forward(req, resp); // Chuyển tiếp đến trang mượn sách
            } catch (SQLException ex) {
                req.setAttribute("error", e.getMessage() + ". Không thể tải danh sách sách: " + ex.getMessage()); // Đặt thông báo lỗi nếu không thể tải sách
                req.setAttribute("books", null); // Đặt sách là null
                req.getRequestDispatcher("/member/borrow.jsp").forward(req, resp); // Chuyển tiếp đến trang mượn sách
            }
        }
    }

    // Phương thức xử lý việc trả sách
    private void handleReturn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loanId = req.getParameter("id"); // Lấy ID khoản mượn
        String feeType = req.getParameter("feeType"); // Lấy chiến lược phí từ giao diện người dùng
        try {
            Loan loan = loanDAO.getLoanById(Integer.parseInt(loanId)); // Lấy khoản mượn theo ID
            if (loan == null) {
                req.setAttribute("error", "Khoản vay không tồn tại."); // Đặt thông báo lỗi
            } else {
                loan.setReturnDate(LocalDate.now()); // Đặt ngày trả là ngày hiện tại
                loan.setFeeStrategy(feeType); // Áp dụng chiến lược phí
                loan.calculateOverdueFee(); // Tính lại phí quá hạn dựa trên chiến lược mới
                loanDAO.updateLoan(loan); // Cập nhật khoản mượn trong DB (bao gồm cả ngày trả và phí)
                String strategyDisplay = "daily".equalsIgnoreCase(loan.getFeeStrategy()) ? "Theo ngày" : "Theo số lượng";
                req.setAttribute("success", "Trả sách thành công! Phí trễ hạn: " + loan.getOverdueFee() + " USD (" + strategyDisplay + ")"); // Đặt thông báo thành công
            }

            // Tải lại danh sách sách đã mượn của thành viên sau khi trả
            Member member = (Member) req.getSession().getAttribute("user");
            List<Loan> loans = loanDAO.getLoansByMember(member.getId());
            for (Loan l : loans) {
                try {
                    Book book = bookDAO.getBookById(l.getBookId());
                    l.setBook(book); // Gắn đối tượng sách vào khoản mượn
                } catch (SQLException e) {
                    e.printStackTrace(); // In lỗi nếu không tìm thấy sách
                }
            }
            req.setAttribute("loans", loans); // Đặt danh sách khoản mượn vào request
            req.getRequestDispatcher("/member/return.jsp").forward(req, resp); // Chuyển tiếp đến trang trả sách
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi trả sách: " + e.getMessage()); // Đặt thông báo lỗi SQL
            req.getRequestDispatcher("/member/return.jsp").forward(req, resp); // Chuyển tiếp đến trang trả sách
        }
    }
}
