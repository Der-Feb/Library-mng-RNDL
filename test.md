# Library Management System - Testing Guide

## Quick Start

1. **Build the project** (compiles code and downloads dependencies):
   ```bash
   mvn clean compile
   ```

2. **Run the automated simulator** (tests all 5 tasks):
   ```bash
   mvn exec:java -Dexec.mainClass="rw.rndl.LibrarySimulator"
   ```

3. **Or run the interactive menu app**:
   ```bash
   mvn exec:java -Dexec.mainClass="rw.rndl.LibraryApplication"
   ```

---

## Prerequisites

### 1. Database Setup (SQLite)
No manual setup required! The application automatically creates a local SQLite database file named `library.db` in the project root directory when it starts.

### 2. Interacting with the SQLite Database
To view and interact with the `library.db` file, you can use:

#### Option 1: DB Browser for SQLite (GUI) - RECOMMENDED
Download and install from https://sqlitebrowser.org/
1. Open DB Browser for SQLite
2. Click "Open Database"
3. Select the `library.db` file from your project directory
4. Browse tables, run queries, and view data visually

#### Option 2: SQLite Command-Line Tool
**Installation on Windows**:
1. Download SQLite tools for Windows from https://www.sqlite.org/download.html
   - Look for "Precompiled Binaries for Windows"
   - Download the zip file (e.g., `sqlite-tools-win-x64-*.zip`)
2. Extract the zip file to a folder (e.g., `C:\sqlite`)
3. Add `C:\sqlite` to your system PATH:
   - Right-click "This PC" → Properties → Advanced system settings → Environment Variables
   - Under "System Variables", find "Path", click "Edit"
   - Click "New" and add `C:\sqlite`
   - Click "OK" to save all changes
4. Restart Git Bash (or your terminal) and test with `sqlite3 --version`

**Usage**:
```bash
# Open the database
sqlite3 library.db

# List all tables
.tables

# View all books
SELECT * FROM books;

# View all members
SELECT * FROM members;

# View borrowing history
SELECT * FROM borrowing_records;

# Exit SQLite
.exit
```

---

## Running the Applications

### Option 1: LibrarySimulator (Automated Test/Simulation)
Runs all 5 tasks automatically:
```bash
mvn exec:java -Dexec.mainClass="rw.rndl.LibrarySimulator"
```

### Option 2: LibraryApplication (Interactive Terminal Menu)
Provides a user-friendly menu to interact with the system:
```bash
mvn exec:java -Dexec.mainClass="rw.rndl.LibraryApplication"
```

---

## Test Scenarios

### LibrarySimulator - Automated Test
The `LibrarySimulator.java` runs comprehensive tests covering all 5 tasks:

#### Phase 1: Display All Available Books (Task 3)
**Purpose**: Verify that books can be listed from the database.

**Expected Output**:
```
--- PHASE 1: Display All Available Books (Task 3) ---
Available books in the library:
  Book [ISBN: 978-0134685991, Title: Effective Java, Author: Joshua Bloch, Year: 2018, Available: true]
  Book [ISBN: 978-1617294945, Title: Java Persistence with Spring, Author: Catalin Tudose, Year: 2023, Available: true]
  ...
Total available: 8
```

**Verification Points**:
- [ ] Books are retrieved from database
- [ ] Available status is correctly filtered
- [ ] All book fields are displayed

---

#### Phase 2: Normal Borrowing Process
**Purpose**: Verify that a member can borrow an available book.

**Expected Output**:
```
--- PHASE 2: Normal Borrowing Process ---
Borrowing 'Clean Code' for M001 (Jean Paul)...
Result: Success
```

**Verification Points**:
- [ ] Book is marked as unavailable in database
- [ ] Borrowing record is created with borrow_date
- [ ] return_date is NULL

**Database Verification (SQLite)**:
```sql
-- Check book availability
SELECT isbn, title, available FROM books WHERE isbn = '978-0132350884';
-- Expected: available = 0 (SQLite uses 0/1 for boolean)

-- Check borrowing record
SELECT * FROM borrowing_records WHERE isbn = '978-0132350884' AND return_date IS NULL;
-- Expected: 1 row with member_id = 'M001'
```

---

#### Phase 3: Member Borrow Limit (Max 5 Books) (Task 3)
**Purpose**: Verify that a member cannot borrow more than 5 books.

**Expected Output**:
```
--- PHASE 3: Member Borrow Limit (Task 3) ---
Testing borrow limit for member M002...
  Attempting to borrow book 1...
    Result: Success
  Attempting to borrow book 2...
    Result: Success
  ...
  Attempting to borrow book 6...
    Error: Member has reached the maximum borrow limit of 5
```

**Verification Points**:
- [ ] Member can borrow up to 5 books successfully
- [ ] 6th book borrowing fails with appropriate error message
- [ ] No new borrowing record is created for failed attempts

---

#### Phase 4: Simulating Concurrent Librarian Threads (Task 4)
**Purpose**: Verify thread safety when multiple librarians try to borrow books simultaneously.

**Expected Output**:
```
--- PHASE 4: Simulating Concurrent Librarian Threads (Task 4) ---
Simulating concurrent borrowing by multiple librarians...
[Librarian-1] Attempting to borrow book 978-1492040347 for member M003
[Librarian-2] Attempting to borrow book 978-1492040347 for member M004
[Librarian-3] Attempting to borrow book 978-1098101226 for member M003
[Librarian-1] Successfully borrowed book 978-1492040347 for member M003
[Librarian-2] Cannot borrow book: Book is not available
[Librarian-3] Successfully borrowed book 978-1098101226 for member M003
Concurrent borrowing simulation completed.
```

**Verification Points**:
- [ ] Only one librarian can borrow a specific book at a time
- [ ] No duplicate borrowing records are created
- [ ] Thread safety is maintained (no race conditions)

---

#### Phase 5: Testing Return Logic & DB Update (Task 5)
**Purpose**: Verify that books can be returned and records are updated.

**Expected Output**:
```
--- PHASE 5: Testing Return Logic & DB Update (Task 5) ---
Returning 'Clean Code' from M001...
Result: Success
Returning 'Effective Java' from M002...
Result: Success

--- FINAL BOOKS AVAILABLE AFTER RETURNS ---
Available books in the library:
  Book [ISBN: 978-0134685991, Title: Effective Java, Author: Joshua Bloch, Year: 2018, Available: true]
  Book [ISBN: 978-0132350884, Title: Clean Code, Author: Robert C. Martin, Year: 2008, Available: true]
Total available: 2
```

**Verification Points**:
- [ ] Book is marked as available in database
- [ ] Borrowing record is updated with return_date
- [ ] Member's borrowed count is decremented

**Database Verification (SQLite)**:
```sql
-- Check book availability
SELECT isbn, title, available FROM books WHERE isbn = '978-0132350884';
-- Expected: available = 1

-- Check borrowing record
SELECT * FROM borrowing_records WHERE isbn = '978-0132350884';
-- Expected: return_date is NOT NULL
```

---

### LibraryApplication - Interactive Menu
The `LibraryApplication.java` provides an interactive terminal menu with the following options:
- 1. View All Books
- 2. View Available Books
- 3. View All Members
- 4. Add New Book
- 5. Register New Member
- 6. Borrow Book
- 7. Return Book
- 8. View Member's Borrowed Books
- 9. View Borrowing History
- 10. Library Statistics
- 11. Run Auto Demo (All 5 Tasks)
- 0. Exit

---

## Final Verification Checklist
- [ ] **Task 1 - Database Schema**: Tables created (books, members, borrowing_records) in SQLite
- [ ] **Task 2 - Encapsulation**: Book, Member, and LibraryService properly encapsulated
- [ ] **Task 3 - Core Logic**: Max 5 books limit enforced, available books listing works
- [ ] **Task 4 - Multithreading**: BorrowTask implements Runnable, ExecutorService manages threads, thread safety with ReentrantLock
- [ ] **Task 5 - JDBC**: Proper CRUD operations, exception handling, connection closing
- [ ] **Local Database**: Uses SQLite database (library.db) created in project root
- [ ] **Interactive App**: LibraryApplication provides terminal menu interface
- [ ] **Simulator App**: LibrarySimulator runs all tasks automatically

---

## Troubleshooting
### Issue: "No suitable driver found" error
**Solution**: Always use the Maven commands (`mvn exec:java`) to run the application. Maven automatically handles the classpath and dependencies.

### Issue: Database file not created
**Solution**: The application creates `library.db` automatically on first run. Check the project root directory.

### Issue: Maven dependencies not found
**Solution**: Run `mvn clean install` to download dependencies.

### Issue: Concurrent borrowing shows inconsistent results
**Solution**: This is expected behavior - the lock ensures only one thread succeeds when borrowing the same book.

---

## Summary
This test guide covers all requirements from the project specification:
1. **Database Schema**: Proper relational design with foreign keys in SQLite
2. **OOP Principles**: Encapsulation, proper class design
3. **Java Collections**: Used in Member for tracking borrowed books
4. **Multithreading**: BorrowTask, ExecutorService, ReentrantLock for thread safety
5. **JDBC**: Complete data persistence with proper exception handling
6. **Local Development**: SQLite database file for easy marking and testing
7. **Two Applications**:
   - LibrarySimulator: Automated test for all tasks
   - LibraryApplication: Interactive terminal menu for user interaction

All test scenarios are designed to verify these requirements are met.
