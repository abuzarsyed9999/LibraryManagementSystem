package com.abuzar.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowingService {
    private BookService bookService = new BookService();
    private MemberService memberService = new MemberService();

    // CORE BUSINESS LOGIC: Borrow a book
    public boolean borrowBook(int bookId, int memberId, int loanDays) {
        // 1. Validate book exists and is available
        Book book = bookService.getBookById(bookId);
        if (book == null) {
            System.err.println(" Book not found: ID=" + bookId);
            return false;
        }
        if (book.getAvailableCopies() <= 0) {
            System.err.println(" Book not available: " + book.getTitle());
            return false;
        }

        // 2. Validate member exists and is active
        Member member = memberService.getMemberById(memberId);
        if (member == null) {
            System.err.println(" Member not found: ID=" + memberId);
            return false;
        }
        if (!member.isActive()) {
            System.err.println("Member inactive: " + member.getName());
            return false;
        }

        // 3. Create borrowing record
        String sql = "INSERT INTO borrowings (book_id, member_id, due_date) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            
            // Calculate due date (today + loanDays)
            java.util.Date today = new java.util.Date();
            java.util.Date dueDateUtil = new java.util.Date(today.getTime() + loanDays * 24L * 60 * 60 * 1000);
            stmt.setDate(3, new java.sql.Date(dueDateUtil.getTime()));
            
            if (stmt.executeUpdate() > 0) {
                // 4.  CRITICAL: Decrement available copies
                book.setAvailableCopies(book.getAvailableCopies() - 1);
                bookService.updateBook(book);
                
                System.out.println(" Book borrowed: " + book.getTitle() + " by " + member.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Borrow failed: " + e.getMessage());
        }
        return false;
    }

    //  CORE BUSINESS LOGIC: Return a book
    public boolean returnBook(int borrowingId) {
        // 1. Get borrowing record
        Borrowing borrowing = getBorrowingById(borrowingId);
        if (borrowing == null) {
            System.err.println(" Borrowing not found: ID=" + borrowingId);
            return false;
        }
        if (borrowing.getReturnDate() != null) {
            System.err.println(" Book already returned!");
            return false;
        }

        // 2. Update borrowing record
        String sql = "UPDATE borrowings SET return_date = CURRENT_DATE, status = 'returned' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, borrowingId);
            if (stmt.executeUpdate() > 0) {
                // 3.  CRITICAL: Increment available copies
                Book book = bookService.getBookById(borrowing.getBookId());
                if (book != null) {
                    book.setAvailableCopies(book.getAvailableCopies() + 1);
                    bookService.updateBook(book);
                }
                
                System.out.println(" Book returned: ID=" + borrowing.getBookId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Return failed: " + e.getMessage());
        }
        return false;
    }

    // Get single borrowing with relationships
    public Borrowing getBorrowingById(int id) {
        String sql = "SELECT * FROM borrowings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Borrowing borrowing = new Borrowing();
                    borrowing.setId(rs.getInt("id"));
                    borrowing.setBookId(rs.getInt("book_id"));
                    borrowing.setMemberId(rs.getInt("member_id"));
                    borrowing.setBorrowDate(rs.getTimestamp("borrow_date"));
                    borrowing.setDueDate(rs.getDate("due_date"));
                    borrowing.setReturnDate(rs.getDate("return_date"));
                    borrowing.setStatus(rs.getString("status"));
                    
                    // Attach related objects (for GUI)
                    borrowing.setBook(bookService.getBookById(borrowing.getBookId()));
                    borrowing.setMember(memberService.getMemberById(borrowing.getMemberId()));
                    
                    return borrowing;
                }
            }
        } catch (SQLException e) {
            System.err.println(" Fetch borrowing failed: " + e.getMessage());
        }
        return null;
    }

    // Get all borrowings (with relationships for GUI)
    public List<Borrowing> getAllBorrowings() {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT * FROM borrowings ORDER BY borrow_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Borrowing borrowing = new Borrowing();
                borrowing.setId(rs.getInt("id"));
                borrowing.setBookId(rs.getInt("book_id"));
                borrowing.setMemberId(rs.getInt("member_id"));
                borrowing.setBorrowDate(rs.getTimestamp("borrow_date"));
                borrowing.setDueDate(rs.getDate("due_date"));
                borrowing.setReturnDate(rs.getDate("return_date"));
                borrowing.setStatus(rs.getString("status"));
                
                // Attach relationships (critical for GUI display)
                borrowing.setBook(bookService.getBookById(borrowing.getBookId()));
                borrowing.setMember(memberService.getMemberById(borrowing.getMemberId()));
                
                borrowings.add(borrowing);
            }
        } catch (SQLException e) {
            System.err.println(" Fetch borrowings failed: " + e.getMessage());
        }
        return borrowings;
    }

    // Get borrowings for a specific member (active + history)
    public List<Borrowing> getBorrowingsByMember(int memberId) {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT * FROM borrowings WHERE member_id = ? ORDER BY borrow_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = mapResultSetToBorrowing(rs);
                    borrowings.add(borrowing);
                }
            }
        } catch (SQLException e) {
            System.err.println(" Fetch member borrowings failed: " + e.getMessage());
        }
        return borrowings;
    }

    // Get overdue borrowings (critical for library management!)
    public List<Borrowing> getOverdueBorrowings() {
        List<Borrowing> borrowings = new ArrayList<>();
        String sql = "SELECT * FROM borrowings WHERE return_date IS NULL AND due_date < CURRENT_DATE ORDER BY due_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Borrowing borrowing = mapResultSetToBorrowing(rs);
                borrowings.add(borrowing);
            }
        } catch (SQLException e) {
            System.err.println(" Fetch overdue borrowings failed: " + e.getMessage());
        }
        return borrowings;
    }

    // Helper: Map ResultSet to Borrowing + relationships
    private Borrowing mapResultSetToBorrowing(ResultSet rs) throws SQLException {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(rs.getInt("id"));
        borrowing.setBookId(rs.getInt("book_id"));
        borrowing.setMemberId(rs.getInt("member_id"));
        borrowing.setBorrowDate(rs.getTimestamp("borrow_date"));
        borrowing.setDueDate(rs.getDate("due_date"));
        borrowing.setReturnDate(rs.getDate("return_date"));
        borrowing.setStatus(rs.getString("status"));
        
        // Attach relationships
        borrowing.setBook(bookService.getBookById(borrowing.getBookId()));
        borrowing.setMember(memberService.getMemberById(borrowing.getMemberId()));
        
        return borrowing;
    }
}