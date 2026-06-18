
package rw.rndl;

import rw.rndl.model.Book;
import rw.rndl.model.Member;
import rw.rndl.service.BorrowTask;
import rw.rndl.service.dao.LibraryService;
import rw.rndl.util.DatabaseConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LibrarySimulator {

    public static void main(String[] args) {
        LibraryService libraryService = new LibraryService(true);

        try {
            System.out.println("=== Rwanda National Digital Library - Library Simulator ===\n");

            // Reset database
            DatabaseConfig.initializeDatabaseForSimulator();
            initializeTestData(libraryService);

            System.out.println("\nStart simulation ....");
            Thread.sleep(5000);

            System.out.println("\n--- PHASE 1: Display All Books ---");
            displayAllBooks(libraryService);

            System.out.println("\n--- PHASE 2: Normal Borrowing Process ---");
            testNormalBorrowing(libraryService);
            displayAllBooks(libraryService);

            System.out.println("\n--- PHASE 3: Member Borrow Limit ---");
            testBorrowLimit(libraryService);
            displayAllBooks(libraryService);

            System.out.println("\n--- PHASE 4: Simulating Concurrent Librarian Threads ---");
            testConcurrentBorrowing(libraryService);
            displayAllBooks(libraryService);

            System.out.println("\n--- PHASE 5: Testing Return Logic & DB Update ---");
            testReturningBooks(libraryService);
            displayAllBooks(libraryService);

            System.out.println("\n--- FINAL SYSTEM STATUS ---");
            displayAllBooks(libraryService);

            System.out.println("\n=== SIMULATION COMPLETE - ALL TASKS DEMONSTRATED ===");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeTestData(LibraryService libraryService) throws SQLException {
        System.out.println("Initializing test data...");

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

        System.out.println("Test data initialized.");
    }

    private static void displayAllBooks(LibraryService libraryService) throws SQLException {
        List<Book> books = libraryService.getAllBooks();
        System.out.println("All books in the library:");
        if (books.isEmpty()) {
            System.out.println("  No books in the library.");
        } else {
            for (Book book : books) {
                String status = book.isAvailable() ? "Available" : "Borrowed";
                System.out.println("  Book [ISBN: " + book.getIsbn() + ", Title: " + book.getTitle() + ", Author: " + book.getAuthor() + ", Year: " + book.getPublicationYear() + ", Status: " + status + "]");
            }
        }
        int availableCount = (int) books.stream().filter(Book::isAvailable).count();
        System.out.println("Total: " + books.size() + " books (" + availableCount + " available, " + (books.size() - availableCount) + " borrowed)");
    }

    private static void testNormalBorrowing(LibraryService libraryService) throws SQLException {
        System.out.println("Borrowing 'Clean Code' for M001 (Jean Paul)...");
        try {
            boolean success = libraryService.borrowBook("M001", "978-0132350884");
            System.out.println("Result: " + (success ? "Success" : "Failed"));
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void testBorrowLimit(LibraryService libraryService) throws SQLException {
        String memberId = "M002";
        System.out.println("Testing borrow limit for member " + memberId + "...");

        Book[] booksToBorrow = {
                new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018, true),
                new Book("978-1617294945", "Java Persistence with Spring", "Catalin Tudose", 2023, true),
                new Book("978-1491950296", "Designing Data-Intensive Applications", "Martin Kleppmann", 2017, true),
                new Book("978-1098118736", "Learning Go", "Jon Bodner", 2021, true),
                new Book("978-0134757599", "Java Concurrency in Practice", "Brian Goetz", 2006, true),
                new Book("978-1098101226", "Software Architecture", "Neal Ford", 2022, true)
        };

        for (int i = 0; i < booksToBorrow.length; i++) {
            Book book = booksToBorrow[i];
            System.out.println("  Attempting to borrow book " + (i + 1) + " (" + book.getTitle() + ")...");
            try {
                boolean success = libraryService.borrowBook(memberId, book.getIsbn());
                System.out.println("    Result: " + (success ? "Success" : "Failed"));
            } catch (IllegalStateException e) {
                System.out.println("    Error: " + e.getMessage());
            }
        }
    }

    private static void testConcurrentBorrowing(LibraryService libraryService) throws InterruptedException {
        System.out.println("Simulating concurrent borrowing by multiple librarians...");

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        executorService.submit(new BorrowTask(libraryService, "M003", "978-1492040347", "Librarian-1"));
        executorService.submit(new BorrowTask(libraryService, "M004", "978-1492040347", "Librarian-2"));
        executorService.submit(new BorrowTask(libraryService, "M003", "978-1098101226", "Librarian-3"));

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Concurrent borrowing simulation completed.");
    }

    private static void testReturningBooks(LibraryService libraryService) throws SQLException {
        System.out.println("Returning 'Clean Code' from M001...");
        try {
            boolean success = libraryService.returnBook("M001", "978-0132350884");
            System.out.println("Result: " + (success ? "Success" : "Failed"));
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("Returning 'Effective Java' from M002...");
        try {
            boolean success = libraryService.returnBook("M002", "978-0134685991");
            System.out.println("Result: " + (success ? "Success" : "Failed"));
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

