package com.abuzar.library;

import java.sql.Date;
import java.sql.Timestamp;

public class Borrowing {
    private int id;
    private int bookId;      // Foreign key
    private int memberId;    // Foreign key
    private Timestamp borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // 'borrowed', 'returned', 'overdue'

    // For relationships (optional but useful for GUI)
    private Book book;
    private Member member;

    // Default constructor
    public Borrowing() {}

    // Constructor for new borrowing (without ID)
    public Borrowing(int bookId, int memberId, Date dueDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.dueDate = dueDate;
        this.status = "borrowed";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public Timestamp getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Timestamp borrowDate) { this.borrowDate = borrowDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Relationship helpers
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    // Helper: Is this borrowing overdue?
    public boolean isOverdue() {
        if (returnDate != null) return false; // Already returned
        return new java.util.Date().after(dueDate);
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", memberId=" + memberId +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                '}';
    }
}