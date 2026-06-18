
package rw.rndl;

import rw.rndl.model.Book;
import rw.rndl.model.Member;
import rw.rndl.service.BorrowTask;
import rw.rndl.service.dao.LibraryService;
import rw.rndl.util.DatabaseConfig;

import java.sql.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LibraryApplication {

    public static void main(String[] args) {
        LibraryService libraryService = new LibraryService();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Rwanda National Digital Library - Library Management System ===\n");

            // Initialize database
            DatabaseConfig.initializeDatabaseForApp();

            boolean exit = false;
            while (!exit) {
                printMainMenu();
                System.out.print("\nChoose option (0-11): ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        viewAllBooks(libraryService);
                        break;
                    case "2":
                        viewAvailableBooks(libraryService);
                        break;
                    case "3":
                        viewAllMembers(libraryService);
                        break;
                    case "4":
                        addNewBook(libraryService, scanner);
                        break;
                    case "5":
                        registerNewMember(libraryService, scanner);
                        break;
                    case "6":
                        borrowBook(libraryService, scanner);
                        break;
                    case "7":
                        returnBook(libraryService, scanner);
                        break;
                    case "8":
                        viewMemberBorrowedBooks(libraryService, scanner);
                        break;
                    case "9":
                        viewBorrowingHistory();
                        break;
                    case "10":
                        viewLibraryStatistics(libraryService);
                        break;
                    case "11":
                        runAutoDemo(libraryService);
                        break;
                    case "0":
                        exit = true;
                        System.out.println("\nThank you for using Rwanda National Digital Library!");
                        break;
                    default:
                        System.out.println("\nInvalid option! Please choose a number between 0 and 11.");
                }

                if (!exit) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void printMainMenu() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║                    MAIN MENU                              ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║ 1. View All Books                                         ║");
        System.out.println("║ 2. View Available Books                                   ║");
        System.out.println("║ 3. View All Members                                       ║");
        System.out.println("║ 4. Add New Book                                           ║");
        System.out.println("║ 5. Register New Member                                    ║");
        System.out.println("║ 6. Borrow Book                                            ║");
        System.out.println("║ 7. Return Book                                            ║");
        System.out.println("║ 8. View Member's Borrowed Books                           ║");
        System.out.println("║ 9. View Borrowing History                                 ║");
        System.out.println("║ 10. Library Statistics                                    ║");
        System.out.println("║ 11. Run Auto Demo (All 5 Tasks)                           ║");
        System.out.println("║ 0. Exit                                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    private static void viewAllBooks(LibraryService libraryService) throws SQLException {
        System.out.println("\n--- All Books in Library ---");
        List<Book> allBooks = libraryService.getAllBooks();
        if (allBooks.isEmpty()) {
            System.out.println("No books in the library.");
        } else {
            for (Book book : allBooks) {
                String status = book.isAvailable() ? "Available" : "Borrowed";
                System.out.println("  - ISBN: " + book.getIsbn() + ", Title: " + book.getTitle() + ", Author: " + book.getAuthor() +
                        ", Year: " + book.getPublicationYear() + ", Status: " + status);
            }
        }
    }

    private static void viewAvailableBooks(LibraryService libraryService) throws SQLException {
        System.out.println("\n--- Available Books ---");
        List<Book> books = libraryService.getAllAvailableBooks();
        if (books.isEmpty()) {
            System.out.println("No available books.");
        } else {
            for (Book book : books) {
                System.out.println("  - ISBN: " + book.getIsbn() + ", Title: " + book.getTitle() + ", Author: " + book.getAuthor() + ", Year: " + book.getPublicationYear());
            }
        }
        System.out.println("Total available: " + books.size());
    }

    private static void viewAllMembers(LibraryService libraryService) throws SQLException {
        System.out.println("\n--- All Members ---");
        List<Member> members = libraryService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("No members registered.");
        } else {
            for (Member member : members) {
                System.out.println("  - ID: " + member.getMemberId() + ", Name: " + member.getName());
            }
        }
    }

    private static void addNewBook(LibraryService libraryService, Scanner scanner) throws SQLException {
        System.out.println("\n--- Add New Book ---");
        String isbn;
        do {
            System.out.print("Enter ISBN: ");
            isbn = scanner.nextLine().trim();
            if (isbn.isEmpty()) {
                System.out.println("ISBN cannot be empty!");
            }
        } while (isbn.isEmpty());

        String title;
        do {
            System.out.print("Enter Title: ");
            title = scanner.nextLine().trim();
            if (title.isEmpty()) {
                System.out.println("Title cannot be empty!");
            }
        } while (title.isEmpty());

        String author;
        do {
            System.out.print("Enter Author: ");
            author = scanner.nextLine().trim();
            if (author.isEmpty()) {
                System.out.println("Author cannot be empty!");
            }
        } while (author.isEmpty());

        int year = -1;
        boolean validYear = false;
        while (!validYear) {
            System.out.print("Enter Publication Year: ");
            String yearStr = scanner.nextLine().trim();
            try {
                year = Integer.parseInt(yearStr);
                if (year > 0 && year <= 2100) {
                    validYear = true;
                } else {
                    System.out.println("Year must be a positive number (1-2100)!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid year! Please enter a valid integer!");
            }
        }

        Book book = new Book(isbn, title, author, year, true);
        try {
            libraryService.addBook(book);
            System.out.println("Book added successfully!");
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }

    private static void registerNewMember(LibraryService libraryService, Scanner scanner) throws SQLException {
        System.out.println("\n--- Register New Member ---");
        String memberId;
        do {
            System.out.print("Enter Member ID: ");
            memberId = scanner.nextLine().trim();
            if (memberId.isEmpty()) {
                System.out.println("Member ID cannot be empty!");
            }
        } while (memberId.isEmpty());

        String name;
        do {
            System.out.print("Enter Member Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Member Name cannot be empty!");
            }
        } while (name.isEmpty());

        Member member = new Member(memberId, name);
        try {
            libraryService.addMember(member);
            System.out.println("Member registered successfully!");
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Error registering member: " + e.getMessage());
        }
    }

    private static void borrowBook(LibraryService libraryService, Scanner scanner) throws SQLException {
        System.out.println("\n--- Borrow Book ---");
        String memberId;
        do {
            System.out.print("Enter Member ID: ");
            memberId = scanner.nextLine().trim();
            if (memberId.isEmpty()) {
                System.out.println("Member ID cannot be empty!");
            }
        } while (memberId.isEmpty());

        String isbn;
        do {
            System.out.print("Enter Book ISBN: ");
            isbn = scanner.nextLine().trim();
            if (isbn.isEmpty()) {
                System.out.println("Book ISBN cannot be empty!");
            }
        } while (isbn.isEmpty());

        try {
            boolean success = libraryService.borrowBook(memberId, isbn);
            if (success) {
                System.out.println("Book borrowed successfully!");
            }
        } catch (IllegalStateException e) {
            System.out.println("Cannot borrow book: " + e.getMessage());
        }
    }

    private static void returnBook(LibraryService libraryService, Scanner scanner) throws SQLException {
        System.out.println("\n--- Return Book ---");
        String memberId;
        do {
            System.out.print("Enter Member ID: ");
            memberId = scanner.nextLine().trim();
            if (memberId.isEmpty()) {
                System.out.println("Member ID cannot be empty!");
            }
        } while (memberId.isEmpty());

        String isbn;
        do {
            System.out.print("Enter Book ISBN: ");
            isbn = scanner.nextLine().trim();
            if (isbn.isEmpty()) {
                System.out.println("Book ISBN cannot be empty!");
            }
        } while (isbn.isEmpty());

        try {
            boolean success = libraryService.returnBook(memberId, isbn);
            if (success) {
                System.out.println("Book returned successfully!");
            }
        } catch (IllegalStateException e) {
            System.out.println("Cannot return book: " + e.getMessage());
        }
    }

    private static void viewMemberBorrowedBooks(LibraryService libraryService, Scanner scanner) throws SQLException {
        System.out.println("\n--- View Member's Borrowed Books ---");
        String memberId;
        do {
            System.out.print("Enter Member ID: ");
            memberId = scanner.nextLine().trim();
            if (memberId.isEmpty()) {
                System.out.println("Member ID cannot be empty!");
            }
        } while (memberId.isEmpty());

        Member member = libraryService.getMember(memberId);
        if (member == null) {
            System.out.println("Member not found!");
            return;
        }

        List<Book> borrowedBooks = getBorrowedBooksForMember(memberId);
        System.out.println("Member: " + member.getName() + " (ID: " + member.getMemberId() + ")");
        if (borrowedBooks.isEmpty()) {
            System.out.println("No borrowed books.");
        } else {
            System.out.println("Borrowed books:");
            for (Book book : borrowedBooks) {
                System.out.println("  - " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
            }
        }
    }

    private static List<Book> getBorrowedBooksForMember(String memberId) throws SQLException {
        List<Book> books = new java.util.ArrayList<>();
        String sql = "SELECT b.* FROM books b " +
                "JOIN borrowing_records br ON b.isbn = br.isbn " +
                "WHERE br.member_id = ? AND br.return_date IS NULL";
        try (Connection conn = DatabaseConfig.getAppConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                            rs.getString("isbn"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("publication_year"),
                            rs.getBoolean("available")
                    ));
                }
            }
        }
        return books;
    }

    private static void viewBorrowingHistory() throws SQLException {
        System.out.println("\n--- View Borrowing History ---");
        String sql = "SELECT br.*, m.name as member_name, b.title as book_title " +
                "FROM borrowing_records br " +
                "JOIN members m ON br.member_id = m.member_id " +
                "JOIN books b ON br.isbn = b.isbn " +
                "ORDER BY br.borrow_date DESC";
        try (Connection conn = DatabaseConfig.getAppConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean hasRecords = false;
            while (rs.next()) {
                hasRecords = true;
                System.out.println("  - Record ID: " + rs.getInt("record_id") +
                        ", Member: " + rs.getString("member_name") + " (" + rs.getString("member_id") + ")" +
                        ", Book: " + rs.getString("book_title") + " (" + rs.getString("isbn") + ")" +
                        ", Borrow Date: " + rs.getDate("borrow_date") +
                        ", Return Date: " + (rs.getDate("return_date") != null ? rs.getDate("return_date") : "Not returned yet"));
            }
            if (!hasRecords) {
                System.out.println("No borrowing history found.");
            }
        }
    }

    private static void viewLibraryStatistics(LibraryService libraryService) throws SQLException {
        System.out.println("\n--- Library Statistics ---");
        int totalBooks = countAllBooks();
        int availableBooks = libraryService.getAllAvailableBooks().size();
        int totalMembers = countAllMembers();
        int activeBorrows = countActiveBorrows();

        System.out.println("Total Books: " + totalBooks);
        System.out.println("Available Books: " + availableBooks);
        System.out.println("Borrowed Books: " + (totalBooks - availableBooks));
        System.out.println("Total Members: " + totalMembers);
        System.out.println("Active Borrows: " + activeBorrows);
    }

    private static int countAllBooks() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = DatabaseConfig.getAppConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static int countAllMembers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM members";
        try (Connection conn = DatabaseConfig.getAppConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static int countActiveBorrows() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowing_records WHERE return_date IS NULL";
        try (Connection conn = DatabaseConfig.getAppConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private static void runAutoDemo(LibraryService libraryService) throws SQLException, InterruptedException {
        System.out.println("\n--- Running Auto Demo (All Tasks) ---");

        // Initialize test data
        initializeTestData(libraryService);

        // Task 1 & 3: Display available books
        System.out.println("\n--- Task 1 & 3: Display All Available Books ---");
        viewAvailableBooks(libraryService);

        // Normal borrowing
        System.out.println("\n--- Normal Borrowing ---");
        try {
            libraryService.borrowBook("M001", "978-0132350884");
            System.out.println("M001 borrowed Clean Code successfully!");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // Borrow limit
        System.out.println("\n--- Task 3: Borrow Limit Test ---");
        String[] isbns = {"978-0134685991", "978-1617294945", "978-1491950296", "978-1098118736", "978-0134757599", "978-1098101226"};
        for (int i = 0; i < isbns.length; i++) {
            try {
                libraryService.borrowBook("M002", isbns[i]);
                System.out.println("M002 borrowed book " + (i + 1) + " successfully!");
            } catch (IllegalStateException e) {
                System.out.println("Failed to borrow book " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Concurrent borrowing
        System.out.println("\n--- Task 4: Concurrent Borrowing ---");
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(new BorrowTask(libraryService, "M003", "978-1492040347", "Librarian 1"));
        executor.submit(new BorrowTask(libraryService, "M004", "978-1492040347", "Librarian 2"));
        executor.submit(new BorrowTask(libraryService, "M003", "978-1098101226", "Librarian 3"));
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Return books
        System.out.println("\n--- Task 5: Returning Books & DB Update ---");
        try {
            libraryService.returnBook("M001", "978-0132350884");
            System.out.println("M001 returned Clean Code successfully!");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        try {
            libraryService.returnBook("M002", "978-0134685991");
            System.out.println("M002 returned Effective Java successfully!");
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\n--- Final Available Books ---");
        viewAvailableBooks(libraryService);
        System.out.println("\n--- Auto Demo Complete! ---");
    }

    private static void initializeTestData(LibraryService libraryService) throws SQLException {
        Book[] books = {
                new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018, true),
                new Book("978-1617294945", "Java Persistence with Spring", "Catalin Tudose", 2023, true),
                new Book("978-0132350884", "Clean Code", "Robert C. Martin", 2008, true),
                new Book("978-1491950296", "Designing Data-Intensive Applications", "Martin Kleppmann", 2017, true),
                new Book("978-1098118736", "Learning Go", "Jon Bodner", 2021, true),
                new Book("978-0134757599", "Java Concurrency in Practice", "Brian Goetz", 2006, true),
                new Book("978-1098101226", "Software Architecture", "Neal Ford", 2022, true),
                new Book("978-1492040347", "Head First Design Patterns", "Eric Freeman", 2020, true)
        };

        for (Book book : books) {
            try {
                libraryService.addBook(book);
            } catch (SQLException | IllegalStateException e) {
                // Skip if exists
            }
        }

        Member[] members = {
                new Member("M001", "Jean Paul"),
                new Member("M002", "Marie Claire"),
                new Member("M003", "Patrick Ndayisaba"),
                new Member("M004", "Alice Uwimana")
        };

        for (Member member : members) {
            try {
                libraryService.addMember(member);
            } catch (SQLException | IllegalStateException e) {
                // Skip if exists
            }
        }
    }
}

