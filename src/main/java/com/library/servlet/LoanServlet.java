package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.dao.MemberDAO;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.model.book.AcademicBookFactory;
import com.library.model.book.BookFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Servlet quản lý các khoản mượn sách
@WebServlet("/admin/loans")
public class LoanServlet extends HttpServlet {
    private LoanDAO loanDAO; // Đối tượng DAO để thao tác với các khoản mượn
    private BookDAO bookDAO; // Đối tượng DAO để thao tác với sách
    private MemberDAO memberDAO; // Đối tượng DAO để thao tác với thành viên
    private static final int ITEMS_PER_PAGE = 10; // Số lượng mục hiển thị trên mỗi trang

    @Override
    // Phương thức khởi tạo Servlet, khởi tạo các đối tượng DAO
    public void init() {
        loanDAO = new LoanDAO();
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
    }

    @Override
    // Xử lý các yêu cầu GET (hiển thị danh sách, xử lý trả sách)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(); // Lấy session
        Member user = (Member) session.getAttribute("user"); // Lấy thông tin người dùng từ session

        // Kiểm tra quyền truy cập (chỉ Admin mới được phép)
        if (user == null || !user.getRole().equals("ADMIN")) {
            resp.sendRedirect(req.getContextPath() + "/login"); // Chuyển hướng về trang đăng nhập
            return;
        }

        String action = req.getParameter("action"); // Lấy tham số hành động

        // Nếu hành động là "return", xử lý trả sách
        if ("return".equals(action)) {
            handleReturn(req, resp);
            return; // handleReturn đã redirect, nên thoát ngay
        }

        // Nếu không phải hành động "return", tải dữ liệu và chuyển tiếp trang
        loadDataAndForward(req, resp);
    }

    // Tải dữ liệu các khoản mượn, sách, thành viên và chuyển tiếp đến trang JSP
    private void loadDataAndForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Loan> allLoans = loanDAO.getAllLoans(); // Lấy tất cả các khoản mượn
            List<Book> allBooks = bookDAO.getAllBooks(); // Lấy tất cả sách
            List<Member> allMembers = memberDAO.getAllMembers(); // Lấy tất cả thành viên

            // Gắn thông tin Book và Member vào từng đối tượng Loan
            for (Loan loan : allLoans) {
                Book book = bookDAO.getBookById(loan.getBookId());
                loan.setBook(book != null ? book : createDefaultBook(loan.getBookId()));

                Member member = memberDAO.getMemberById(loan.getMemberId());
                loan.setMember(member != null ? member : createDefaultMember(loan.getMemberId()));
            }

            List<Book> availableBooks = bookDAO.getAvailableBooks(); // Lấy sách đang có sẵn

            // Sắp xếp danh sách khoản mượn: sách đã trả trước, sau đó theo ngày mượn giảm dần
            allLoans.sort(Comparator.comparing(
                            Loan::getReturnDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(Loan::getBorrowDate, Comparator.reverseOrder())
                    .thenComparing(Loan::getReturnDate, Comparator.nullsLast(Comparator.reverseOrder())));

            // Xử lý phân trang
            int page = 1;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    page = 1; // Mặc định trang 1 nếu lỗi
                }
            }

            int totalItems = allLoans.size(); // Tổng số khoản mượn
            int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE); // Tổng số trang
            if (page > totalPages && totalPages > 0) page = totalPages; // Điều chỉnh trang hiện tại nếu vượt quá

            int start = (page - 1) * ITEMS_PER_PAGE; // Chỉ mục bắt đầu của trang hiện tại
            int end = Math.min(start + ITEMS_PER_PAGE, totalItems); // Chỉ mục kết thúc của trang hiện tại
            List<Loan> loansForPage = (totalItems > 0) ? allLoans.subList(start, end) : List.of(); // Lấy danh sách khoản mượn cho trang

            // Đặt các thuộc tính vào request để JSP hiển thị
            req.setAttribute("loans", loansForPage);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("availableBooks", availableBooks);
            req.setAttribute("allMembers", allMembers);

            // Lấy và xóa thông báo thành công/lỗi từ session
            HttpSession session = req.getSession();
            String success = (String) session.getAttribute("success");
            String error = (String) session.getAttribute("error");
            if (success != null) {
                req.setAttribute("success", success);
                session.removeAttribute("success");
            }
            if (error != null) {
                req.setAttribute("error", error);
                session.removeAttribute("error");
            }

            req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp); // Chuyển tiếp đến trang JSP
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi lấy danh sách khoản vay: " + e.getMessage()); // Đặt thông báo lỗi
            req.setAttribute("loans", List.of()); // Trả về danh sách rỗng
            req.setAttribute("currentPage", 1);
            req.setAttribute("totalPages", 1);
            req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp); // Chuyển tiếp đến trang JSP
        }
    }


    @Override
    // Xử lý các yêu cầu POST (thêm khoản mượn, cập nhật chiến lược phí)
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(); // Lấy session
        Member user = (Member) session.getAttribute("user"); // Lấy thông tin người dùng

        // Kiểm tra quyền truy cập (chỉ Admin mới được phép)
        if (user == null || !user.getRole().equals("ADMIN")) {
            resp.sendRedirect(req.getContextPath() + "/login"); // Chuyển hướng về trang đăng nhập
            return;
        }

        String action = req.getParameter("action"); // Lấy tham số hành động

        // Nếu hành động là "updateFeeStrategy", xử lý cập nhật chiến lược phí
        if ("updateFeeStrategy".equals(action)) {
            handleUpdateFeeStrategy(req, resp);
            return;
        }

        // Nếu không phải "updateFeeStrategy", xử lý thêm khoản mượn mới
        String bookId = req.getParameter("bookId"); // Lấy ID sách từ request
        String memberId = req.getParameter("memberId"); // Lấy ID thành viên từ request

        try {
            // Lấy đối tượng Book và Member từ DB
            Book book = bookDAO.getBookById(Integer.parseInt(bookId));
            Member member = memberDAO.getMemberById(Integer.parseInt(memberId));
            if (book == null || member == null) {
                req.setAttribute("error", "Sách hoặc thành viên không tồn tại."); // Đặt thông báo lỗi
                req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý khoản mượn
                return;
            }

            // Tạo và thêm một khoản mượn mới vào DB
            Loan loan = new Loan();
            loan.setBookId(book.getId());
            loan.setMemberId(member.getId());
            loan.setBorrowDate(LocalDate.now()); // Ngày mượn là hôm nay
            loan.setDueDate(LocalDate.now().plusDays(14)); // Ngày đến hạn là sau 14 ngày
            loanDAO.addLoan(loan); // Thêm khoản mượn vào DB

            resp.sendRedirect(req.getContextPath() + "/admin/loans"); // Chuyển hướng về trang quản lý khoản mượn
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi thêm khoản vay: " + e.getMessage()); // Đặt thông báo lỗi
            req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp); // Chuyển tiếp về trang quản lý khoản mượn
        }
    }

    // Xử lý cập nhật chiến lược tính phí cho một khoản mượn
    private void handleUpdateFeeStrategy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String loanId = req.getParameter("loanId"); // Lấy ID khoản mượn
        String feeStrategy = req.getParameter("feeStrategy"); // Lấy chiến lược phí mới
        try {
            Loan loan = loanDAO.getLoanById(Integer.parseInt(loanId)); // Lấy khoản mượn theo ID
            if (loan == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // Đặt trạng thái lỗi 404
                resp.getWriter().write("Khoản vay không tồn tại."); // Gửi thông báo lỗi
                return;
            }
            loan.setFeeStrategy(feeStrategy); // Cập nhật chiến lược phí
            loan.calculateOverdueFee(); // Tính lại phí quá hạn
            loanDAO.updateLoan(loan); // Cập nhật khoản mượn trong DB
            resp.setStatus(HttpServletResponse.SC_OK); // Đặt trạng thái thành công 200
            resp.getWriter().write("Cập nhật chiến lược phí thành công."); // Gửi thông báo thành công
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Đặt trạng thái lỗi 500
            resp.getWriter().write("Lỗi khi cập nhật chiến lược phí: " + e.getMessage()); // Gửi thông báo lỗi
        }
    }

    // Xử lý việc trả sách
    private void handleReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String loanId = req.getParameter("id"); // Lấy ID khoản mượn cần trả
        try {
            Loan loan = loanDAO.getLoanById(Integer.parseInt(loanId)); // Lấy khoản mượn theo ID
            if (loan == null) {
                req.getSession().setAttribute("error", "Khoản vay không tồn tại."); // Đặt thông báo lỗi vào session
            } else {
                loan.setReturnDate(LocalDate.now()); // Đặt ngày trả là hôm nay
                loanDAO.updateLoan(loan); // Cập nhật khoản mượn trong DB
                String strategyDisplay = "daily".equalsIgnoreCase(loan.getFeeStrategy()) ? "Theo ngày" : "Theo số lượng";
                req.getSession().setAttribute("success", "Trả sách thành công! Phí trễ hạn: " + loan.getOverdueFee() + " USD (" + strategyDisplay + ")"); // Đặt thông báo thành công
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("error", "Lỗi khi trả sách: " + e.getMessage()); // Đặt thông báo lỗi vào session
        }
        // Chuyển hướng về trang danh sách khoản vay (không kèm tham số action)
        resp.sendRedirect(req.getContextPath() + "/admin/loans");
    }

    // Phương thức trợ giúp để tạo một đối tượng Book mặc định khi không tìm thấy
    private Book createDefaultBook(int id) {
        BookFactory factory = new AcademicBookFactory(); // Sử dụng AcademicBookFactory làm mặc định
        return factory.createBook(id, "Không có tên", "");
    }
    // Phương thức trợ giúp để tạo một đối tượng Member mặc định khi không tìm thấy
    private Member createDefaultMember(int id) {
        return new Member(id, "user" + id, "", "Không có tên", "MEMBER");
    }
}
