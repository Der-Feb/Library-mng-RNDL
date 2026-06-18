package rw.rndl.model;

import java.util.ArrayList;
import java.util.List;

public class Member {
    private String memberId;
    private String name;
    private List<Book> currentlyBorrowedBooks;

    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
        this.currentlyBorrowedBooks = new ArrayList<>();
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Book> getCurrentlyBorrowedBooks() {
        return new ArrayList<>(currentlyBorrowedBooks);
    }

    public void setCurrentlyBorrowedBooks(List<Book> currentlyBorrowedBooks) {
        this.currentlyBorrowedBooks = new ArrayList<>(currentlyBorrowedBooks);
    }

    public void addBorrowedBook(Book book) {
        currentlyBorrowedBooks.add(book);
    }

    public void removeBorrowedBook(Book book) {
        currentlyBorrowedBooks.remove(book);
    }

    public int getBorrowedCount() {
        return currentlyBorrowedBooks.size();
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId='" + memberId + '\'' +
                ", name='" + name + '\'' +
                ", borrowedCount=" + getBorrowedCount() +
                '}';
    }
}
