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

@WebServlet("/admin/loans")
public class LoanServlet extends HttpServlet {
    private LoanDAO loanDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public void init() {
        loanDAO = new LoanDAO();
        bookDAO = new BookDAO();
        memberDAO = new MemberDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Member user = (Member) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        if ("return".equals(action)) {
            // Xử lý trả sách
            handleReturn(req, resp);
            return; // handleReturn đã redirect rồi nên thoát luôn
        }

        // Không phải trả sách -> tải dữ liệu và forward trang
        loadDataAndForward(req, resp);
    }


    private void loadDataAndForward(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Loan> allLoans = loanDAO.getAllLoans();
            List<Book> allBooks = bookDAO.getAllBooks();
            List<Member> allMembers = memberDAO.getAllMembers();

            for (Loan loan : allLoans) {
                Book book = bookDAO.getBookById(loan.getBookId());
                loan.setBook(book != null ? book : createDefaultBook(loan.getBookId()));

                Member member = memberDAO.getMemberById(loan.getMemberId());
                loan.setMember(member != null ? member : createDefaultMember(loan.getMemberId()));
            }

            List<Book> availableBooks = bookDAO.getAvailableBooks();

            // Sắp xếp theo yêu cầu
            allLoans.sort(Comparator.comparing(
                            Loan::getReturnDate, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(Loan::getBorrowDate, Comparator.reverseOrder())
                    .thenComparing(Loan::getReturnDate, Comparator.nullsLast(Comparator.reverseOrder())));

            int page = 1;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try {
                    page = Integer.parseInt(pageParam);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    page = 1;
                }
            }

            int totalItems = allLoans.size();
            int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            if (page > totalPages && totalPages > 0) page = totalPages;

            int start = (page - 1) * ITEMS_PER_PAGE;
            int end = Math.min(start + ITEMS_PER_PAGE, totalItems);
            List<Loan> loansForPage = (totalItems > 0) ? allLoans.subList(start, end) : List.of();

            req.setAttribute("loans", loansForPage);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("availableBooks", availableBooks);
            req.setAttribute("allMembers", allMembers);

            // Lấy thông báo success/error từ session rồi set vào request, sau đó xoá trong session
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

            req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp);
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi lấy danh sách khoản vay: " + e.getMessage());
            req.setAttribute("loans", List.of());
            req.setAttribute("currentPage", 1);
            req.setAttribute("totalPages", 1);
            req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp);
        }
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Member user = (Member) session.getAttribute("user");
        if (user == null || !user.getRole().equals("ADMIN")) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("updateFeeStrategy".equals(action)) {
            handleUpdateFeeStrategy(req, resp);
            return;
        }

        String bookId = req.getParameter("bookId");
        String memberId = req.getParameter("memberId");

        try {
            Book book = bookDAO.getBookById(Integer.parseInt(bookId));
            Member member = memberDAO.getMemberById(Integer.parseInt(memberId));
            if (book == null || member == null) {
                req.setAttribute("error", "Sách hoặc thành viên không tồn tại.");
                req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp);
                return;
            }

            Loan loan = new Loan();
            loan.setBookId(book.getId());
            loan.setMemberId(member.getId());
            loan.setBorrowDate(LocalDate.now());
            loan.setDueDate(LocalDate.now().plusDays(14));
            loanDAO.addLoan(loan);

            resp.sendRedirect(req.getContextPath() + "/admin/loans");
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi thêm khoản vay: " + e.getMessage());
            req.getRequestDispatcher("/admin/loans.jsp").forward(req, resp);
        }
    }

    private void handleUpdateFeeStrategy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String loanId = req.getParameter("loanId");
        String feeStrategy = req.getParameter("feeStrategy");
        try {
            Loan loan = loanDAO.getLoanById(Integer.parseInt(loanId));
            if (loan == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Khoản vay không tồn tại.");
                return;
            }
            loan.setFeeStrategy(feeStrategy);
            loan.calculateOverdueFee();
            loanDAO.updateLoan(loan);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Cập nhật chiến lược phí thành công.");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Lỗi khi cập nhật chiến lược phí: " + e.getMessage());
        }
    }

    private void handleReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String loanId = req.getParameter("id");
        try {
            Loan loan = loanDAO.getLoanById(Integer.parseInt(loanId));
            if (loan == null) {
                req.getSession().setAttribute("error", "Khoản vay không tồn tại.");
            } else {
                loan.setReturnDate(LocalDate.now());
                loanDAO.updateLoan(loan);
                String strategyDisplay = "daily".equalsIgnoreCase(loan.getFeeStrategy()) ? "Theo ngày" : "Theo số lượng";
                req.getSession().setAttribute("success", "Trả sách thành công! Phí trễ hạn: " + loan.getOverdueFee() + " USD (" + strategyDisplay + ")");
            }
        } catch (SQLException e) {
            req.getSession().setAttribute("error", "Lỗi khi trả sách: " + e.getMessage());
        }
        // Chuyển hướng về trang danh sách khoản vay (không kèm tham số action)
        resp.sendRedirect(req.getContextPath() + "/admin/loans");
    }

    // Phương thức tạo sách mặc định bằng BookFactory
    private Book createDefaultBook(int id) {
        BookFactory factory = new AcademicBookFactory();
        return factory.createBook(id, "Không có tên", "");
    }
    // Phương thức tạo thành viên mặc định
    private Member createDefaultMember(int id) {
        return new Member(id, "user" + id, "", "Không có tên", "MEMBER");
    }
}