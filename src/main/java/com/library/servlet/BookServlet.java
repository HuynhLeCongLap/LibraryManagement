package com.library.servlet;

import com.library.dao.BookDAO;
import com.library.model.Book;
import com.library.model.book.AcademicBookFactory;
import com.library.model.book.EntertainmentBookFactory;
import com.library.model.book.BookFactory;
import com.library.model.decorator.FavoriteBookDecorator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Servlet xử lý các yêu cầu liên quan đến quản lý sách cho Admin
@WebServlet({"/admin/books", "/admin/toggle-favorite"})
public class BookServlet extends HttpServlet {
    private BookDAO bookDAO; // Đối tượng DAO để thao tác với sách

    @Override
    // Phương thức khởi tạo Servlet
    public void init() {
        bookDAO = new BookDAO();
    }

    @Override
    // Phương thức xử lý các yêu cầu GET
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Lấy session hiện tại
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        // Kiểm tra quyền truy cập của người dùng (chỉ Admin mới được phép)
        if (session == null || session.getAttribute("user") == null ||
                !((com.library.model.Member) session.getAttribute("user")).getRole().equals("ADMIN")) {
            resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập nếu không có quyền
            return;
        }

        String action = req.getParameter("action"); // Lấy tham số 'action' từ yêu cầu

        try {
            // Xử lý hành động "edit" (chỉnh sửa sách)
            if ("edit".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id")); // Lấy ID sách từ yêu cầu
                Book book = bookDAO.getBookById(id); // Lấy sách theo ID
                if (book == null) {
                    req.setAttribute("error", "Không tìm thấy sách với ID: " + id); // Đặt thông báo lỗi
                    List<Book> books = bookDAO.getAllBooks(); // Lấy lại tất cả sách
                    req.setAttribute("books", books); // Đặt danh sách sách vào request
                    req.getRequestDispatcher("/admin/books.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý sách
                    return;
                }
                req.setAttribute("book", book); // Đặt đối tượng sách vào request
                req.getRequestDispatcher("/admin/editbook.jsp").forward(req, resp); // Chuyển tiếp đến trang chỉnh sửa sách
                return;

            }
            // Xử lý hành động "delete" (xóa sách)
            else if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id")); // Lấy ID sách từ yêu cầu
                bookDAO.deleteBook(id); // Xóa sách khỏi DB
                session.setAttribute("success", "Xóa sách thành công!"); // Đặt thông báo thành công vào session
                resp.sendRedirect(contextPath + "/admin/books"); // Chuyển hướng về trang quản lý sách
                return;

            }
            // Xử lý hành động "favorite" (đánh dấu yêu thích)
            else if ("favorite".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id")); // Lấy ID sách từ yêu cầu
                bookDAO.markAsFavorite(id); // Đánh dấu sách là yêu thích
                session.setAttribute("success", "Đã đánh dấu sách là yêu thích!"); // Đặt thông báo thành công vào session
                resp.sendRedirect(contextPath + "/admin/books"); // Chuyển hướng về trang quản lý sách
                return;
            }

            // Xử lý tìm kiếm sách
            String searchId = req.getParameter("id"); // Lấy ID tìm kiếm
            Integer id = null;
            if (searchId != null && !searchId.trim().isEmpty()) {
                try {
                    id = Integer.parseInt(searchId); // Chuyển đổi ID sang số nguyên
                } catch (NumberFormatException e) {
                    req.setAttribute("error", "ID phải là số nguyên hợp lệ."); // Đặt thông báo lỗi
                    List<Book> books = bookDAO.getAllBooks(); // Lấy tất cả sách
                    req.setAttribute("books", books); // Đặt danh sách sách vào request
                    req.getRequestDispatcher("/admin/books.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý sách
                    return;
                }
            }

            String title = req.getParameter("title"); // Lấy tiêu đề tìm kiếm
            String author = req.getParameter("author"); // Lấy tác giả tìm kiếm
            String type = req.getParameter("type"); // Lấy loại sách tìm kiếm
            String favorite = req.getParameter("favorite"); // Lấy trạng thái yêu thích tìm kiếm

            List<Book> books;

            // Thực hiện tìm kiếm hoặc lấy tất cả sách
            if ((searchId != null && !searchId.isEmpty()) ||
                    (title != null && !title.isEmpty()) ||
                    (author != null && !author.isEmpty()) ||
                    (type != null && !type.isEmpty()) ||
                    (favorite != null && !favorite.isEmpty())) {
                books = bookDAO.searchBooks(searchId, title, author, type, favorite); // Tìm kiếm sách
            } else {
                books = bookDAO.getAllBooks(); // Lấy tất cả sách
            }

            req.setAttribute("books", books); // Đặt danh sách sách vào request
            req.getRequestDispatcher("/admin/books.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý sách

        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi: " + e.getMessage()); // Đặt thông báo lỗi
            try {
                List<Book> books = bookDAO.getAllBooks(); // Lấy lại tất cả sách nếu có lỗi
                req.setAttribute("books", books); // Đặt danh sách sách vào request
                req.getRequestDispatcher("/admin/books.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý sách
            } catch (SQLException ex) {
                throw new ServletException("Database error", ex); // Ném ngoại lệ nếu có lỗi DB nghiêm trọng
            }
        }
    }

    @Override
    // Phương thức xử lý các yêu cầu POST
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false); // Lấy session hiện tại
        String contextPath = req.getContextPath(); // Lấy đường dẫn context của ứng dụng

        // Kiểm tra quyền truy cập của người dùng (chỉ Admin mới được phép)
        if (session == null || session.getAttribute("user") == null || !((com.library.model.Member) session.getAttribute("user")).getRole().equals("ADMIN")) {
            resp.sendRedirect(contextPath + "/login"); // Chuyển hướng về trang đăng nhập nếu không có quyền
            return;
        }

        String path = req.getServletPath(); // Lấy đường dẫn servlet

        try {
            // Xử lý các yêu cầu POST đến "/admin/books"
            if ("/admin/books".equals(path)) {
                String action = req.getParameter("action"); // Lấy tham số 'action'

                // Xử lý hành động "update" (cập nhật sách)
                if ("update".equals(action)) {
                    int id = Integer.parseInt(req.getParameter("id")); // Lấy ID sách
                    String title = req.getParameter("title"); // Lấy tiêu đề
                    String author = req.getParameter("author"); // Lấy tác giả
                    String type = req.getParameter("type"); // Lấy loại sách

                    // Tạo đối tượng BookFactory và Book dựa trên loại sách
                    BookFactory factory = type.equals("Printed") ? new AcademicBookFactory() : new EntertainmentBookFactory();
                    Book book = factory.createBook(id, title, author);
                    bookDAO.updateBook(book); // Cập nhật sách trong DB
                    session.setAttribute("success", "Cập nhật sách thành công!"); // Đặt thông báo thành công
                    resp.sendRedirect(contextPath + "/admin/books"); // Chuyển hướng
                    return;
                }

                // Xử lý hành động thêm sách (mặc định nếu không có action hoặc action không phải update)
                String title = req.getParameter("title"); // Lấy tiêu đề
                String author = req.getParameter("author"); // Lấy tác giả
                String type = req.getParameter("type"); // Lấy loại sách

                // Tạo đối tượng BookFactory và Book dựa trên loại sách
                BookFactory factory = type.equals("Printed") ? new AcademicBookFactory() : new EntertainmentBookFactory();
                Book book = factory.createBook(0, title, author); // ID là 0 vì sẽ được DB tự tạo
                bookDAO.addBook(book); // Thêm sách vào DB
                session.setAttribute("success", "Thêm sách thành công!"); // Đặt thông báo thành công
                resp.sendRedirect(contextPath + "/admin/books"); // Chuyển hướng

            }
            // Xử lý yêu cầu POST đến "/admin/toggle-favorite" (bật/tắt trạng thái yêu thích)
            else if ("/admin/toggle-favorite".equals(path)) {
                String bookId = req.getParameter("bookId"); // Lấy ID sách
                if (bookId != null) {
                    int bookIdInt = Integer.parseInt(bookId); // Chuyển đổi ID sách sang số nguyên
                    Book book = bookDAO.getBookById(bookIdInt); // Lấy sách theo ID

                    if (book != null) {
                        // Kiểm tra nếu sách đang là FavoriteBookDecorator (đã yêu thích)
                        if (book instanceof FavoriteBookDecorator) {
                            bookDAO.unmarkAsFavorite(bookIdInt); // Hủy yêu thích
                            session.setAttribute("success", "Đã hủy yêu thích sách!"); // Đặt thông báo
                        } else {
                            bookDAO.markAsFavorite(bookIdInt); // Đánh dấu yêu thích
                            session.setAttribute("success", "Đã đánh dấu sách là yêu thích!"); // Đặt thông báo
                        }
                    } else {
                        req.setAttribute("error", "Sách không tồn tại."); // Đặt thông báo lỗi
                    }
                    List<Book> books = bookDAO.getAllBooks(); // Lấy lại tất cả sách
                    req.setAttribute("books", books); // Đặt danh sách sách vào request
                    req.getRequestDispatcher("/admin/books.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý sách
                }
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Lỗi khi xử lý yêu cầu: " + e.getMessage()); // Đặt thông báo lỗi
            List<Book> books;
            try {
                books = bookDAO.getAllBooks(); // Lấy lại tất cả sách nếu có lỗi
            } catch (SQLException ex) {
                throw new ServletException("Database error", ex); // Ném ngoại lệ nếu có lỗi DB nghiêm trọng
            }
            req.setAttribute("books", books); // Đặt danh sách sách vào request
            req.getRequestDispatcher("/admin/books.jsp").forward(req, resp); // Chuyển tiếp đến trang quản lý sách
        }
    }
}
