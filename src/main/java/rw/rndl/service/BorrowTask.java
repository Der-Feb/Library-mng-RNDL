package rw.rndl.service;

import rw.rndl.service.dao.LibraryService;

import java.sql.SQLException;

public class BorrowTask implements Runnable {
    private final LibraryService libraryService;
    private final String memberId;
    private final String isbn;
    private final String librarianName;

    public BorrowTask(LibraryService libraryService, String memberId, String isbn, String librarianName) {
        this.libraryService = libraryService;
        this.memberId = memberId;
        this.isbn = isbn;
        this.librarianName = librarianName;
    }

    @Override
    public void run() {
        System.out.println("[" + librarianName + "] Attempting to borrow book " + isbn + " for member " + memberId);
        try {
            boolean success = libraryService.borrowBook(memberId, isbn);
            if (success) {
                System.out.println("[" + librarianName + "] Successfully borrowed book " + isbn + " for member " + memberId);
            } else {
                System.out.println("[" + librarianName + "] Failed to borrow book " + isbn + " for member " + memberId);
            }
        } catch (SQLException e) {
            System.out.println("[" + librarianName + "] Database error while borrowing book: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("[" + librarianName + "] Cannot borrow book: " + e.getMessage());
        }
    }
}
