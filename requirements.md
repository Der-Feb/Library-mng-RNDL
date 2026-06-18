The Rwanda National Digital Library (RNDL) is developing a computerized Library Management System to modernize how books are managed and accessed by members. The system will allow librarians to register books and members, track borrowing activities, and ensure accurate record keeping in a relational db.

The system must prevent members from borrowing more than the allowed number of books, maintain correct book availability status, and permanently sstore all borrowing transactions. As a java developer, you are required to design and implement a simplified version of this ssystem using OOP, java collections, Multithreading and JDBC

Task 1: Database schema Design:
Design a relational database using the following structure:

- Books Table: ISBN (Primary key), Title, Author, and Publication year.
- Members table: member id (primary key), and name
- borrowing_records table: record id (primary key), member id (foreign key), ISBN (foreign key), borrow date, return date(nullable)

Task 2: Perfoming encapsulation:
Encapsulate the following:

- Book: encapsulates ISBN, Title and name
- Member: Encapsulates ID, name, and a list of currently borrowed books
- Library Service: A service / DAO class responsible for managing system operations, such ass borrowing, returning and listing books

Task 3: Core logic & collections
Implement the following rules using the java collections

- A member can't borrow more than 5 books at a time
- Ensure the system can display a list of all available books in the library

Task 4: Simulate concurrent borrowing operations where multiple librarians process requests at the same time

> create a task class:

- Borrow task implements runnable
  > use executor service:
- manage multiple libraries threads
- simulate concurrent borrowing requests

> ensure threads safety

- use synchronized or lock
- prevent two libraries borrowing the same book at the same time

Task 5: Data Persistence & JDBC
Using the java sql API, implement the following (LO6):
-Borrowing Logic: A method that records a borrowing transaction in the db and ensures the book is marked as unavailable
-Returning Logic: A method to return a book and update the db records to available
-Exception Handling: Property handle SQLException and ensure connections are closed
