package com.library.model;

// Lớp trừu tượng Book: Đại diện cho một cuốn sách trong thư viện
public abstract class Book {
    private int id; // ID của sách
    private String title; // Tiêu đề của sách
    private String author; // Tác giả của sách

    // Constructor: Khởi tạo một đối tượng Book với ID, tiêu đề và tác giả
    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    // Getters and setters
    // Lấy ID của sách
    public int getId() { return id; }
    // Đặt ID cho sách
    public void setId(int id) { this.id = id; }
    // Lấy tiêu đề của sách
    public String getTitle() { return title; }
    // Đặt tiêu đề cho sách
    public void setTitle(String title) { this.title = title; }
    // Lấy tác giả của sách
    public String getAuthor() { return author; }
    // Đặt tác giả cho sách
    public void setAuthor(String author) { this.author = author; }
    // Phương thức trừu tượng: Trả về loại sách (ví dụ: "Printed", "EBook")
    public abstract String getType();
}
