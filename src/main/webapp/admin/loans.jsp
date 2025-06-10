<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.library.model.Loan, com.library.model.Book, com.library.model.Member, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý mượn sách</title>
    <base href="${pageContext.request.contextPath}/">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/style.css" rel="stylesheet">
    <style>
        .pagination {
            margin-top: 1rem;
            justify-content: center;
        }
        /* CSS mới để làm nhỏ cỡ chữ của select và option */
        .fee-strategy {
            font-size: 0.85em; /* Giảm cỡ chữ của hộp chọn */
            padding: 0.25rem 0.5rem; /* Tùy chỉnh padding để trông gọn gàng hơn */
        }
        .fee-strategy option {
            font-size: 0.85em; /* Đảm bảo cỡ chữ của từng option bên trong */
        }
    </style>
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid">
        <a class="navbar-brand" href="#">Thư viện</a>
        <div class="navbar-nav">
            <a class="nav-link" href="admin/books">Sách</a>
            <a class="nav-link" href="admin/members">Thành viên</a>
            <a class="nav-link" href="admin/loans">Mượn sách</a>
            <a class="nav-link" href="admin/reports">Báo cáo</a>
            <a class="nav-link" href="logout">Đăng xuất</a>
        </div>
    </div>
</nav>
<div class="container mt-5">
    <h2>Quản lý mượn sách</h2>
    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <%= request.getAttribute("error") %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <% } %>
    <% if (session.getAttribute("success") != null) { %>
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <%= session.getAttribute("success") %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <% session.removeAttribute("success"); %>
    <% } %>
    <form action="admin/loans" method="post" class="mb-4">
        <div class="row">
            <div class="col">
                <select class="form-select" name="bookId" required>
                    <option value="" disabled selected>Chọn sách</option>
                    <% List<Book> availableBooks = (List<Book>) request.getAttribute("availableBooks"); %>
                    <% if (availableBooks != null && !availableBooks.isEmpty()) { %>
                    <% for (Book book : availableBooks) { %>
                    <option value="<%= book.getId() %>"><%= book.getTitle() %> (ID: <%= book.getId() %>)</option>
                    <% } %>
                    <% } else { %>
                    <option value="" disabled>Không có sách nào khả dụng</option>
                    <% } %>
                </select>
            </div>
            <div class="col">
                <select class="form-select" name="memberId" required>
                    <option value="" disabled selected>Chọn thành viên</option>
                    <% List<Member> allMembers = (List<Member>) request.getAttribute("allMembers"); %>
                    <% if (allMembers != null && !allMembers.isEmpty()) { %>
                    <% for (Member member : allMembers) { %>
                    <option value="<%= member.getId() %>"><%= member.getFullName() %> (ID: <%= member.getId() %>)</option>
                    <% } %>
                    <% } else { %>
                    <option value="" disabled>Không có thành viên nào</option>
                    <% } %>
                </select>
            </div>
            <div class="col">
                <button type="submit" class="btn btn-primary">Mượn sách</button>
            </div>
        </div>
    </form>
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>ID</th>
            <th>Tên sách</th>
            <th>Thành viên mượn</th>
            <th>Ngày mượn</th>
            <th>Hạn trả</th>
            <th>Ngày trả</th>
            <th>Phí quá hạn</th>
            <th>Chiến lược phí</th>
            <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <% List<Loan> loans = (List<Loan>) request.getAttribute("loans"); %>
        <% if (loans != null && !loans.isEmpty()) { %>
        <% for (Loan loan : loans) { %>
        <tr>
            <td><%= loan.getId() %></td>
            <td><%= loan.getBook() != null ? loan.getBook().getTitle() : "Không có tên (ID: " + loan.getBookId() + ")" %></td>
            <td><%= loan.getMember() != null ? loan.getMember().getFullName() : "Không có tên (ID: " + loan.getMemberId() + ")" %></td>
            <td><%= loan.getBorrowDate() %></td>
            <td><%= loan.getDueDate() %></td>
            <td><%= loan.getReturnDate() != null ? loan.getReturnDate() : "Chưa trả" %></td>
            <td><%= loan.getOverdueFee() %> USD</td>
            <td>
                <% if (loan.getReturnDate() == null) { %>
                <select class="form-select fee-strategy" data-loan-id="<%= loan.getId() %>">
                    <option value="daily" <%= "daily".equals(loan.getFeeStrategy()) ? "selected" : "" %>>Theo ngày</option>
                    <option value="quantity" <%= "quantity".equals(loan.getFeeStrategy()) ? "selected" : "" %>>Theo số lượng</option>
                </select>
                <% } else { %>
                <%= loan.getFeeStrategy() != null ? loan.getFeeStrategy() : "Chưa tính" %>
                <% } %>
            </td>
            <td>
                <% if (loan.getReturnDate() == null) { %>
                <a href="admin/loans?action=return&id=<%= loan.getId() %>&feeType=<%= loan.getFeeStrategy() %>"
                   class="btn btn-sm btn-success">
                    Trả
                </a>
                <% } %>
            </td>
        </tr>
        <% } %>
        <% } else { %>
        <tr>
            <td colspan="9" class="text-center">Không có khoản mượn nào.</td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% Integer currentPage = (Integer) request.getAttribute("currentPage"); %>
    <% Integer totalPages = (Integer) request.getAttribute("totalPages"); %>
    <% if (totalPages != null && totalPages > 1) { %>
    <nav aria-label="Page navigation">
        <ul class="pagination">
            <% if (currentPage > 1) { %>
            <li class="page-item">
                <a class="page-link" href="admin/loans?page=<%= currentPage - 1 %>">Trước</a>
            </li>
            <% } %>
            <% for (int i = 1; i <= totalPages; i++) { %>
            <li class="page-item <%= (i == currentPage) ? "active" : "" %>">
                <a class="page-link" href="admin/loans?page=<%= i %>"><%= i %></a>
            </li>
            <% } %>
            <% if (currentPage < totalPages) { %>
            <li class="page-item">
                <a class="page-link" href="admin/loans?page=<%= currentPage + 1 %>">Sau</a>
            </li>
            <% } %>
        </ul>
    </nav>
    <% } %>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        $('.fee-strategy').change(function() {
            var loanId = $(this).data('loan-id');
            var feeStrategy = $(this).val();
            $.ajax({
                url: 'admin/loans',
                type: 'POST',
                data: {
                    action: 'updateFeeStrategy',
                    loanId: loanId,
                    feeStrategy: feeStrategy
                },
                success: function(response) {
                    alert('Cập nhật chiến lược phí thành công!');
                    location.reload();
                },
                error: function(xhr, status, error) {
                    alert('Lỗi khi cập nhật chiến lược phí: ' + error);
                }
            });
        });
    });
</script>
</body>
</html>
