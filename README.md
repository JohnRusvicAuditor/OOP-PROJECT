# Library Management System (Java Swing)

A GUI application that demonstrates the four pillars of OOP: encapsulation, inheritance,
polymorphism and abstraction.

## Requirements
JDK 17 or newer (developed and tested with JDK 21). No external libraries.

## Build and run

Linux / macOS:
    ./build.sh          # compiles into ./out
    ./run.sh            # starts the application
    ./test.sh           # runs the 31 automated checks

Windows:
    build.bat
    run.bat
    test.bat

Or manually:
    javac -encoding UTF-8 -d out (all .java files under src)
    java -cp out library.Main

## Project layout
    src/library/model     Borrowable, LibraryItem, Book, Magazine, DVD, Member, Student, Faculty, Loan, enums
    src/library/service   LibraryService (business rules), LibraryException, SampleData
    src/library/util      Validator, Money
    src/library/ui        RefreshablePanel, ItemsPanel, MembersPanel, CirculationPanel, ReportsPanel, MainFrame
    src/library/Main.java entry point
    test/                 LibraryServiceTest (plain Java, no JUnit needed)
    docs/                 Project-Documentation (.docx / .md), UML diagrams, screenshots

Demo data is loaded at startup (9 items, 3 members, 3 loans - one of them overdue).
Data is kept in memory only; it resets when the program closes.
