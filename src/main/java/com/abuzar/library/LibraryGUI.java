package com.abuzar.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class LibraryGUI {
    private JFrame frame;
    private BookService bookService = new BookService();
    private MemberService memberService = new MemberService();
    private BorrowingService borrowingService = new BorrowingService();

    // Books Tab
    private JTextField bookTitleField, bookAuthorField, bookIsbnField, bookYearField, bookCategoryField, bookCopiesField;
    private JTable booksTable;
    private DefaultTableModel booksTableModel;

    // Members Tab
    private JTextField memberNameField, memberEmailField, memberPhoneField;
    private JTable membersTable;
    private DefaultTableModel membersTableModel;
 
    private JComboBox bookComboBox;       
    private JComboBox memberComboBox;     
    private JTextField loanDaysField;
    private JTable borrowingsTable;
    private DefaultTableModel borrowingsTableModel;

    public LibraryGUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame(" Library Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab(" Books", createBooksPanel());
        tabbedPane.addTab(" Members", createMembersPanel());
        tabbedPane.addTab(" Borrowings", createBorrowingsPanel());

        frame.add(tabbedPane);
        frame.setVisible(true);

        // Load initial data
        loadBooksTable();
        loadMembersTable();
        loadBorrowingsTable();
    }

    // ===== TAB 1: BOOKS =====
    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Edit Book"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        bookTitleField = new JTextField(20);
        formPanel.add(bookTitleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        bookAuthorField = new JTextField(20);
        formPanel.add(bookAuthorField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        bookIsbnField = new JTextField(20);
        formPanel.add(bookIsbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        bookYearField = new JTextField(5);
        formPanel.add(bookYearField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        bookCategoryField = new JTextField(15);
        formPanel.add(bookCategoryField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Total Copies:"), gbc);
        gbc.gridx = 1;
        bookCopiesField = new JTextField(5);
        bookCopiesField.setText("1");
        formPanel.add(bookCopiesField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveBookBtn = new JButton(" Save Book");
        JButton deleteBookBtn = new JButton("Delete Book");
        JButton searchBookBtn = new JButton("Search");
        JTextField searchBookField = new JTextField(15);

        styleButton(saveBookBtn, new Color(46, 204, 113));
        styleButton(deleteBookBtn, new Color(231, 76, 60));
        styleButton(searchBookBtn, new Color(52, 152, 219));

        saveBookBtn.addActionListener(e -> handleSaveBook());
        deleteBookBtn.addActionListener(e -> handleDeleteBook());
        searchBookBtn.addActionListener(e -> handleSearchBooks(searchBookField.getText()));

        buttonPanel.add(saveBookBtn);
        buttonPanel.add(deleteBookBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchBookField);
        buttonPanel.add(searchBookBtn);

        // Table
        String[] bookColumns = {"ID", "Title", "Author", "ISBN", "Year", "Category", "Available/Total"};
        booksTableModel = new DefaultTableModel(bookColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        booksTable = new JTable(booksTableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksTable.setRowHeight(25);
        booksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Row selection listener
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = booksTable.getSelectedRow();
                if (row >= 0) {
                    bookTitleField.setText((String) booksTableModel.getValueAt(row, 1));
                    bookAuthorField.setText((String) booksTableModel.getValueAt(row, 2));
                    bookIsbnField.setText((String) booksTableModel.getValueAt(row, 3));
                    bookYearField.setText(booksTableModel.getValueAt(row, 4).toString());
                    bookCategoryField.setText((String) booksTableModel.getValueAt(row, 5));
                    // Copies field not filled (to avoid accidental reduction)
                }
            }
        });

        // Assemble panel
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(booksTable), BorderLayout.CENTER);

        return panel;
    }

    // ===== TAB 2: MEMBERS =====
    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Edit Member"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        memberNameField = new JTextField(20);
        formPanel.add(memberNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        memberEmailField = new JTextField(20);
        formPanel.add(memberEmailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        memberPhoneField = new JTextField(15);
        formPanel.add(memberPhoneField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveMemberBtn = new JButton(" Save Member");
        JButton deleteMemberBtn = new JButton(" Delete Member");
        JButton searchMemberBtn = new JButton(" Search");
        JTextField searchMemberField = new JTextField(15);

        styleButton(saveMemberBtn, new Color(46, 204, 113));
        styleButton(deleteMemberBtn, new Color(231, 76, 60));
        styleButton(searchMemberBtn, new Color(52, 152, 219));

        saveMemberBtn.addActionListener(e -> handleSaveMember());
        deleteMemberBtn.addActionListener(e -> handleDeleteMember());
        searchMemberBtn.addActionListener(e -> handleSearchMembers(searchMemberField.getText()));

        buttonPanel.add(saveMemberBtn);
        buttonPanel.add(deleteMemberBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchMemberField);
        buttonPanel.add(searchMemberBtn);

        // Table
        String[] memberColumns = {"ID", "Name", "Email", "Phone", "Since", "Status"};
        membersTableModel = new DefaultTableModel(memberColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        membersTable = new JTable(membersTableModel);
        membersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        membersTable.setRowHeight(25);
        membersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Row selection listener
        membersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = membersTable.getSelectedRow();
                if (row >= 0) {
                    memberNameField.setText((String) membersTableModel.getValueAt(row, 1));
                    memberEmailField.setText((String) membersTableModel.getValueAt(row, 2));
                    memberPhoneField.setText((String) membersTableModel.getValueAt(row, 3));
                }
            }
        });

        // Assemble panel
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(membersTable), BorderLayout.CENTER);

        return panel;
    }

    // ===== TAB 3: BORROWINGS =====
    private JPanel createBorrowingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Borrow / Return Books"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Book selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Book:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        bookComboBox = new JComboBox<>(); // Raw type - accepts any object
        loadBookComboBox();
        formPanel.add(bookComboBox, gbc);

        // Member selection
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Member:"), gbc);
        gbc.gridx = 1;
        memberComboBox = new JComboBox<>(); // Raw type - accepts any object
        loadMemberComboBox();
        formPanel.add(memberComboBox, gbc);

        // Loan days
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Loan Days:"), gbc);
        gbc.gridx = 1;
        loanDaysField = new JTextField(5);
        loanDaysField.setText("14");
        formPanel.add(loanDaysField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton borrowBtn = new JButton(" Borrow Book");
        JButton returnBtn = new JButton(" Return Book");
        JButton refreshBtn = new JButton(" Refresh");
        JButton overdueBtn = new JButton("View Overdue");

        styleButton(borrowBtn, new Color(46, 204, 113));
        styleButton(returnBtn, new Color(231, 76, 60));
        styleButton(refreshBtn, new Color(52, 152, 219));
        styleButton(overdueBtn, new Color(241, 196, 15));

        borrowBtn.addActionListener(e -> handleBorrowBook());
        returnBtn.addActionListener(e -> handleReturnBook());
        refreshBtn.addActionListener(e -> loadBorrowingsTable());
        overdueBtn.addActionListener(e -> showOverdueBooks());

        buttonPanel.add(borrowBtn);
        buttonPanel.add(returnBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(overdueBtn);

        // Table
        String[] borrowingColumns = {"ID", "Book", "Member", "Borrowed", "Due Date", "Returned", "Status"};
        borrowingsTableModel = new DefaultTableModel(borrowingColumns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        borrowingsTable = new JTable(borrowingsTableModel);
        borrowingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowingsTable.setRowHeight(25);
        borrowingsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Assemble panel
        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(borrowingsTable), BorderLayout.CENTER);

        return panel;
    }

    // ===== ACTION HANDLERS =====

    private void handleSaveBook() {
        try {
            String title = bookTitleField.getText().trim();
            String author = bookAuthorField.getText().trim();
            String isbn = bookIsbnField.getText().trim();
            int year = Integer.parseInt(bookYearField.getText().trim());
            String category = bookCategoryField.getText().trim();
            int copies = Integer.parseInt(bookCopiesField.getText().trim());

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                showError("Title, Author, and ISBN are required!");
                return;
            }

            Book book = new Book(title, author, isbn, year, category, copies);
            bookService.saveBook(book);
            clearBookForm();
            loadBooksTable();
            loadBookComboBox(); // Refresh combo box
            showInfo("Book saved successfully!");
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for Year and Copies");
        }
    }

    private void handleDeleteBook() {
        int row = booksTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a book to delete");
            return;
        }

        int id = (int) booksTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(frame, 
            "Are you sure you want to delete this book?\nThis will fail if the book is currently borrowed.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (bookService.deleteBook(id)) {
                loadBooksTable();
                loadBookComboBox(); // Refresh combo box
                clearBookForm();
                showInfo("Book deleted successfully!");
            } else {
                showError("Cannot delete book (may have active borrowings)");
            }
        }
    }

    private void handleSaveMember() {
        String name = memberNameField.getText().trim();
        String email = memberEmailField.getText().trim();
        String phone = memberPhoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            showError("Name and Email are required!");
            return;
        }

        Member member = new Member(name, email, phone);
        memberService.saveMember(member);
        clearMemberForm();
        loadMembersTable();
        loadMemberComboBox(); // Refresh combo box
        showInfo("Member saved successfully!");
    }

    private void handleDeleteMember() {
        int row = membersTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a member to delete");
            return;
        }

        int id = (int) membersTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(frame,
            "Are you sure you want to delete this member?\nThis will fail if they have active borrowings.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (memberService.deleteMember(id)) {
                loadMembersTable();
                loadMemberComboBox(); // Refresh combo box
                clearMemberForm();
                showInfo("Member deleted successfully!");
            } else {
                showError("Cannot delete member (may have active borrowings)");
            }
        }
    }

    private void handleBorrowBook() {
        try {
            Object bookObj = bookComboBox.getSelectedItem();
            Object memberObj = memberComboBox.getSelectedItem();
            
            // Check for null selections
            if (bookObj == null || memberObj == null) {
                showError("Please select both book and member");
                return;
            }
            
            // Check for placeholder strings
            if (bookObj.toString().equals("No available books") || 
                memberObj.toString().equals("No active members")) {
                showError("Please add available books and active members first!");
                return;
            }

            // Now cast to proper types
            Book book = (Book) bookObj;
            Member member = (Member) memberObj;
            int loanDays = Integer.parseInt(loanDaysField.getText().trim());

            if (borrowingService.borrowBook(book.getId(), member.getId(), loanDays)) {
                loadBorrowingsTable();
                loadBooksTable();
                loadBookComboBox();
                loadMemberComboBox();
                showInfo("Book borrowed successfully!");
            } else {
                showError("Borrow failed (check availability/member status)");
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid loan days");
        } catch (Exception e) {
            showError("Borrow error: " + e.getMessage());
        }
    }
    private void handleReturnBook() {
        int row = borrowingsTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a borrowing to return");
            return;
        }

        int id = (int) borrowingsTableModel.getValueAt(row, 0);
        if (borrowingService.returnBook(id)) {
            loadBorrowingsTable();
            loadBooksTable(); // Refresh availability
            loadBookComboBox(); // Refresh combo box
            loadMemberComboBox(); // Refresh combo box
            showInfo("Book returned successfully!");
        } else {
            showError("Return failed (book may already be returned)");
        }
    }

    private void handleSearchBooks(String keyword) {
        if (keyword.trim().isEmpty()) {
            loadBooksTable();
            return;
        }
        List<Book> books = bookService.searchBooks(keyword.trim());
        displayBooks(books);
    }

    private void handleSearchMembers(String keyword) {
        if (keyword.trim().isEmpty()) {
            loadMembersTable();
            return;
        }
        List<Member> members = memberService.searchMembers(keyword.trim());
        displayMembers(members);
    }

    // ===== DATA LOADING METHODS =====

    private void loadBooksTable() {
        List<Book> books = bookService.getAllBooks();
        displayBooks(books);
    }

    private void displayBooks(List<Book> books) {
        booksTableModel.setRowCount(0);
        for (Book b : books) {
            booksTableModel.addRow(new Object[]{
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getIsbn(),
                b.getPublicationYear(),
                b.getCategory(),
                b.getAvailableCopies() + "/" + b.getTotalCopies()
            });
        }
    }

    private void loadMembersTable() {
        List<Member> members = memberService.getAllMembers();
        displayMembers(members);
    }

    private void displayMembers(List<Member> members) {
        membersTableModel.setRowCount(0);
        for (Member m : members) {
            membersTableModel.addRow(new Object[]{
                m.getId(),
                m.getName(),
                m.getEmail(),
                m.getPhone(),
                m.getMemberSinceDate(),
                m.getMembershipStatus()
            });
        }
    }

    private void loadBorrowingsTable() {
        List<Borrowing> borrowings = borrowingService.getAllBorrowings();
        borrowingsTableModel.setRowCount(0);
        for (Borrowing b : borrowings) {
            borrowingsTableModel.addRow(new Object[]{
                b.getId(),
                b.getBook() != null ? b.getBook().getTitle() : "Unknown",
                b.getMember() != null ? b.getMember().getName() : "Unknown",
                b.getBorrowDate(),
                b.getDueDate(),
                b.getReturnDate() != null ? b.getReturnDate() : "Not returned",
                b.getStatus()
            });
        }
    }

    private void loadBookComboBox() {
        bookComboBox.removeAllItems();
        List<Book> books = bookService.getAllBooks();
        boolean hasAvailable = false;
        
        for (Book book : books) {
            if (book.isAvailable()) {
                bookComboBox.addItem(book); // Book object - OK!
                hasAvailable = true;
            }
        }
        
        if (!hasAvailable) {
            bookComboBox.addItem("No available books"); // String - now OK with raw type!
        }
    }

    private void loadMemberComboBox() {
        memberComboBox.removeAllItems();
        List<Member> members = memberService.getAllMembers();
        boolean hasActive = false;
        
        for (Member member : members) {
            if (member.isActive()) {
                memberComboBox.addItem(member); // Member object - OK!
                hasActive = true;
            }
        }
        
        if (!hasActive) {
            memberComboBox.addItem("No active members"); // String - now OK!
        }
    }

    private void showOverdueBooks() {
        List<Borrowing> overdue = borrowingService.getOverdueBorrowings();
        if (overdue.isEmpty()) {
            showInfo("No overdue books found!");
            return;
        }

        StringBuilder message = new StringBuilder(" OVERDUE BOOKS:\n\n");
        for (Borrowing b : overdue) {
            message.append("• ").append(b.getBook().getTitle())
                   .append(" (borrowed by ").append(b.getMember().getName())
                   .append(") - Due: ").append(b.getDueDate()).append("\n");
        }
        JOptionPane.showMessageDialog(frame, message.toString(), 
            "Overdue Books", JOptionPane.WARNING_MESSAGE);
    }

    // ===== HELPER METHODS =====

    private void clearBookForm() {
        bookTitleField.setText("");
        bookAuthorField.setText("");
        bookIsbnField.setText("");
        bookYearField.setText("");
        bookCategoryField.setText("");
        bookCopiesField.setText("1");
        booksTable.clearSelection();
    }

    private void clearMemberForm() {
        memberNameField.setText("");
        memberEmailField.setText("");
        memberPhoneField.setText("");
        membersTable.clearSelection();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                Font baseFont = new Font("Segoe UI", Font.PLAIN, 13);
                UIManager.put("Label.font", baseFont);
                UIManager.put("TextField.font", baseFont);
                UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LibraryGUI();
        });
    }
}
