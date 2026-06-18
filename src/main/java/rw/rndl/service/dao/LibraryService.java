
package rw.rndl.service.dao;

import rw.rndl.model.Book;
import rw.rndl.model.Member;
import rw.rndl.util.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class LibraryService {
    private static final int MAX_BORROW_LIMIT = 5;
    private final boolean isSimulator;
    private final ReentrantLock borrowLock = new ReentrantLock();

    public LibraryService() {
        this(false);
    }

    public LibraryService(boolean isSimulator) {
        this.isSimulator = isSimulator;
    }

    private Connection getConnection() throws SQLException {
        if (isSimulator) {
            return DatabaseConfig.getSimulatorConnection();
        } else {
            return DatabaseConfig.getAppConnection();
        }
    }

    public void addBook(Book book) throws SQLException, IllegalStateException {
        if (getBook(book.getIsbn()) != null) {
            throw new IllegalStateException("Book with ISBN " + book.getIsbn() + " already exists");
        }

        String sql = "INSERT INTO books (isbn, title, author, publication_year, available) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setInt(4, book.getPublicationYear());
            stmt.setBoolean(5, book.isAvailable());
            stmt.executeUpdate();
        }
    }

    public Book getBook(String isbn) throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        }
        return null;
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    public List<Book> getAllAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE available = 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getInt("publication_year"),
                rs.getBoolean("available")
        );
    }

    public void addMember(Member member) throws SQLException, IllegalStateException {
        if (getMember(member.getMemberId()) != null) {
            throw new IllegalStateException("Member with ID " + member.getMemberId() + " already exists");
        }

        String sql = "INSERT INTO members (member_id, name) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getMemberId());
            stmt.setString(2, member.getName());
            stmt.executeUpdate();
        }
    }

    public Member getMember(String memberId) throws SQLException {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Member(rs.getString("member_id"), rs.getString("name"));
            }
        }
        return null;
    }

    public List<Member> getAllMembers() throws SQLException {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(new Member(rs.getString("member_id"), rs.getString("name")));
            }
        }
        return members;
    }

    public int getBorrowedBookCount(String memberId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowing_records WHERE member_id = ? AND return_date IS NULL";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean borrowBook(String memberId, String isbn) throws SQLException, IllegalStateException {
        borrowLock.lock();
        try {
            Connection conn = getConnection();
            try {
                conn.setAutoCommit(false);

                Member member = getMemberWithConnection(memberId, conn);
                if (member == null) {
                    throw new IllegalStateException("Member with ID " + memberId + " not found");
                }

                Book book = getBookWithConnection(isbn, conn);
                if (book == null) {
                    throw new IllegalStateException("Book with ISBN " + isbn + " not found");
                }

                if (!book.isAvailable()) {
                    throw new IllegalStateException("Book " + book.getTitle() + " is not available for borrowing");
                }

                int borrowedCount = getBorrowedBookCountWithConnection(memberId, conn);
                if (borrowedCount >= MAX_BORROW_LIMIT) {
                    throw new IllegalStateException("Member " + member.getName() + " has reached the maximum borrow limit of " + MAX_BORROW_LIMIT);
                }

                updateBookAvailabilityWithConnection(isbn, false, conn);
                createBorrowingRecordWithConnection(memberId, isbn, conn);

                conn.commit();
                return true;
            } catch (SQLException | IllegalStateException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.close();
            }
        } finally {
            borrowLock.unlock();
        }
    }

    private Member getMemberWithConnection(String memberId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM members WHERE member_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Member(rs.getString("member_id"), rs.getString("name"));
            }
        }
        return null;
    }

    private Book getBookWithConnection(String isbn, Connection conn) throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
        }
        return null;
    }

    private int getBorrowedBookCountWithConnection(String memberId, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrowing_records WHERE member_id = ? AND return_date IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private void updateBookAvailabilityWithConnection(String isbn, boolean available, Connection conn) throws SQLException {
        String sql = "UPDATE books SET available = ? WHERE isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, available);
            stmt.setString(2, isbn);
            stmt.executeUpdate();
        }
    }

    private void createBorrowingRecordWithConnection(String memberId, String isbn, Connection conn) throws SQLException {
        String sql = "INSERT INTO borrowing_records (member_id, isbn, borrow_date) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, isbn);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();
        }
    }

    public boolean returnBook(String memberId, String isbn) throws SQLException, IllegalStateException {
        borrowLock.lock();
        try {
            Connection conn = getConnection();
            try {
                conn.setAutoCommit(false);

                Member member = getMemberWithConnection(memberId, conn);
                if (member == null) {
                    throw new IllegalStateException("Member with ID " + memberId + " not found");
                }

                Book book = getBookWithConnection(isbn, conn);
                if (book == null) {
                    throw new IllegalStateException("Book with ISBN " + isbn + " not found");
                }

                int recordId = getActiveBorrowingRecordWithConnection(memberId, isbn, conn);
                if (recordId == -1) {
                    throw new IllegalStateException("No active borrowing record found for member " + member.getName() + " and book " + book.getTitle());
                }

                updateBookAvailabilityWithConnection(isbn, true, conn);
                updateBorrowingRecordWithConnection(recordId, conn);

                conn.commit();
                return true;
            } catch (SQLException | IllegalStateException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.close();
            }
        } finally {
            borrowLock.unlock();
        }
    }

    private int getActiveBorrowingRecordWithConnection(String memberId, String isbn, Connection conn) throws SQLException {
        String sql = "SELECT record_id FROM borrowing_records WHERE member_id = ? AND isbn = ? AND return_date IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("record_id");
            }
        }
        return -1;
    }

    private void updateBorrowingRecordWithConnection(int recordId, Connection conn) throws SQLException {
        String sql = "UPDATE borrowing_records SET return_date = ? WHERE record_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, recordId);
            stmt.executeUpdate();
        }
    }
}

