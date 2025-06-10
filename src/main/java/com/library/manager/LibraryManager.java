package com.library.manager;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Loan;
import java.util.ArrayList;
import java.util.List;

public class LibraryManager {
    private static LibraryManager instance;
    private List<Observer> observers = new ArrayList<>();
    private List<Book> books = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    // Constructor riêng tư để triển khai Singleton Pattern
    private LibraryManager() {}

    // Lấy thể hiện duy nhất của LibraryManager (Singleton Pattern)
    public static LibraryManager getInstance() {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }

    // Thêm một Observer (người quan sát) vào danh sách
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    // Thông báo cho tất cả các Observer về một tin nhắn
    public void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    // Methods to manage books, members, loans

    // Thêm một cuốn sách mới vào danh sách
    public void addBook(Book book) {
        books.add(book);
        notifyObservers("Book added: " + book.getTitle());
    }

    // Thêm một thành viên mới vào danh sách
    public void addMember(Member member) {
        members.add(member);
        notifyObservers("Member added: " + member.getFullName());
    }

    // Thêm một bản ghi mượn sách mới vào danh sách
    public void addLoan(Loan loan) {
        loans.add(loan);
        notifyObservers("Loan added for member ID: " + loan.getMemberId());
    }

    // Getters

    // Lấy danh sách tất cả sách
    public List<Book> getBooks() { return books; }
    // Lấy danh sách tất cả thành viên
    public List<Member> getMembers() { return members; }
    // Lấy danh sách tất cả bản ghi mượn sách
    public List<Loan> getLoans() { return loans; }
}
