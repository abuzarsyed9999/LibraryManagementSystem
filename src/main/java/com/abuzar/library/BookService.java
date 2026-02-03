package com.abuzar.library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    // 1. Save new book (NO throws SQLException)
    public void saveBook(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, publication_year, category, total_copies, available_copies) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setInt(4, book.getPublicationYear());
            stmt.setString(5, book.getCategory());
            stmt.setInt(6, book.getTotalCopies());
            stmt.setInt(7, book.getAvailableCopies());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Book saved: " + book.getTitle());
            }
        } catch (SQLException e) {
            System.err.println("❌ Save book failed: " + e.getMessage());
            // Do NOT throw - handle gracefully for GUI
        }
    }

    // 2. Get all books (NO throws SQLException)
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Book book = new Book();
                book.setId(rs.getInt("id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setPublicationYear(rs.getInt("publication_year"));
                book.setCategory(rs.getString("category"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("❌ Fetch books failed: " + e.getMessage());
            // Return empty list instead of crashing
        }
        return books;
    }

    // 3. Search books (already correct - no throws)
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR category LIKE ? ORDER BY title";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setPublicationYear(rs.getInt("publication_year"));
                    book.setCategory(rs.getString("category"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    books.add(book);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Search books failed: " + e.getMessage());
        }
        return books;
    }

    // 4. Get book by ID (already correct - no throws)
    public Book getBookById(int id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("id"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setIsbn(rs.getString("isbn"));
                    book.setPublicationYear(rs.getInt("publication_year"));
                    book.setCategory(rs.getString("category"));
                    book.setTotalCopies(rs.getInt("total_copies"));
                    book.setAvailableCopies(rs.getInt("available_copies"));
                    return book;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Fetch book failed: " + e.getMessage());
        }
        return null;
    }

    // 5. Update book (already correct - no throws)
    public void updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, publication_year = ?, category = ?, " +
                     "total_copies = ?, available_copies = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setInt(4, book.getPublicationYear());
            stmt.setString(5, book.getCategory());
            stmt.setInt(6, book.getTotalCopies());
            stmt.setInt(7, book.getAvailableCopies());
            stmt.setInt(8, book.getId());
            
            if (stmt.executeUpdate() > 0) {
                System.out.println("✅ Book updated: " + book.getTitle());
            } else {
                System.out.println("⚠️ Book not found: ID=" + book.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Update book failed: " + e.getMessage());
        }
    }

    // 6. Delete book (already correct - no throws)
    public boolean deleteBook(int id) {
        // First check if book has active borrowings
        String checkSql = "SELECT COUNT(*) FROM borrowings WHERE book_id = ? AND return_date IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("❌ Cannot delete book: It has active borrowings!");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Delete check failed: " + e.getMessage());
            return false;
        }

        // Safe to delete
        String deleteSql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            
            stmt.setInt(1, id);
            if (stmt.executeUpdate() > 0) {
                System.out.println("✅ Book deleted: ID=" + id);
                return true;
            } else {
                System.out.println("⚠️ Book not found: ID=" + id);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Delete book failed: " + e.getMessage());
            return false;
        }
    }
}