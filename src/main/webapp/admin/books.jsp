<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.library.model.Book, java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sách</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid">
        <a class="navbar-brand" href="#">Thư viện</a>
        <div class="navbar-nav">
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/books">Sách</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/members">Thành viên</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/loans">Mượn sách</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/admin/reports">Báo cáo</a>
            <a class="nav-link" href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
        </div>
    </div>
</nav>
<div class="container mt-5">
    <h2>Quản lý sách</h2>
    <% if (session.getAttribute("success") != null) { %>
    <div class="alert alert-success alert-dismissible fade show" role="alert" id="successAlert">
        <%= session.getAttribute("success") %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <% session.removeAttribute("success"); %>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger alert-dismissible fade show" role="alert" id="errorAlert">
        <%= request.getAttribute("error") %>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <% } %>
    <form action="${pageContext.request.contextPath}/admin/books" method="post" class="mb-4">
        <div class="row">
            <div class="col">
                <input type="text" class="form-control" name="title" placeholder="Tiêu đề" required>
            </div>
            <div class="col">
                <input type="text" class="form-control" name="author" placeholder="Tác giả" required>
            </div>
            <div class="col">
                <select class="form-control" name="type" required>
                    <option value="Printed">Sách giấy</option>
                    <option value="EBook">Sách điện tử</option>
                </select>
            </div>
            <div class="col">
                <button type="submit" class="btn btn-primary">Thêm sách</button>
            </div>
        </div>
    </form>

    <form action="${pageContext.request.contextPath}/admin/books" method="get" class="d-flex align-items-center gap-2 mb-4">
        <input type="text" class="form-control tiny-input" name="id" placeholder="ID" style="width: 80px;">
        <input type="text" class="form-control tiny-input" name="title" placeholder="Tiêu đề" style="width: 200px;">
        <input type="text" class="form-control tiny-input" name="author" placeholder="Tác giả" style="width: 200px;">



        <select class="form-control tiny-select" name="type" style="width: 130px;">
            <option value="">-- Loại sách --</option>
            <option value="Printed">Sách giấy</option>
            <option value="EBook">Sách điện tử</option>
        </select>

        <select class="form-control tiny-select" name="favorite" style="width: 130px;">
            <option value="">-- Yêu thích --</option>
            <option value="true">Có</option>
            <option value="false">Không</option>
        </select>

        <button type="submit" class="btn btn-success tiny-btn">Tìm kiếm</button>
    </form>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th>ID</th>
            <th>Tiêu đề</th>
            <th>Tác giả</th>
            <th>Loại</th>
            <th>Yêu thích</th>
            <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <% List<Book> books = (List<Book>) request.getAttribute("books"); %>
        <% if (books != null && !books.isEmpty()) { %>
        <% for (Book book : books) { %>
        <tr>
            <td><%= book.getId() %></td>
            <td><%= book.getTitle() %></td>
            <td><%= book.getAuthor() %></td>
            <td><%= book.getType() %></td>
            <td><%= book instanceof com.library.model.decorator.FavoriteBookDecorator ? "Có" : "Không" %></td>
            <td>
                <form action="${pageContext.request.contextPath}/admin/toggle-favorite" method="post" style="display:inline;">
                    <input type="hidden" name="bookId" value="<%= book.getId() %>">
                    <button type="submit" class="btn btn-sm <%= (book instanceof com.library.model.decorator.FavoriteBookDecorator) ? "btn-danger" : "btn-success" %>">
                        <%= (book instanceof com.library.model.decorator.FavoriteBookDecorator) ? "Hủy yêu thích" : "Yêu thích" %>
                    </button>
                </form>
                <a href="${pageContext.request.contextPath}/admin/books?action=edit&id=<%= book.getId() %>" class="btn btn-sm btn-warning">Sửa</a>
                <a href="${pageContext.request.contextPath}/admin/books?action=delete&id=<%= book.getId() %>" class="btn btn-sm btn-danger">Xóa</a>
            </td>
        </tr>
        <% } %>
        <% } else { %>
        <tr>
            <td colspan="6" class="text-center">Chưa có sách nào.</td>
        </tr>
        <% } %>
        </tbody>
    </table>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/script.js"></script>