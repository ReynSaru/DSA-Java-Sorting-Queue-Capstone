# IT Help Desk Ticketing System

## Project Description

The IT Help Desk Ticketing System is a console-based Java application for recording and managing technical-support requests. New tickets are added to a first-in, first-out (FIFO) queue so the oldest waiting request is handled first. Staff can also search and sort the ticket records without changing the queue's processing order.

## Objectives

- Demonstrate two manually implemented sorting algorithms on useful ticket data.
- Use a Java queue to enqueue, peek at, display, and process help desk tickets in FIFO order.
- Store ticket information in synchronized `ArrayList` collections.
- Apply procedural methods, loops, conditional statements, and input validation.
- Integrate ticket records, searching, sorting, and queue operations in one console application.

## Use Cases and Features

- Create a ticket with a requester name and issue description.
- Automatically assign a unique ticket number and submission timestamp.
- Add each new ticket to the waiting queue.
- Display all ticket records, including their status.
- Search for a ticket using its number (for example, `HD-1001` or `1001`).
- Display the waiting queue in FIFO order.
- Peek at the next ticket without removing it.
- Process the next ticket, marking it as processed.
- Sort ticket records by requester name or ticket number.
- Reject blank ticket details and invalid menu or ticket-number input with clear messages.
- Handle empty record lists and empty queues without terminating unexpectedly.

## Data Structure Explanation

Ticket records are stored in five `ArrayList`s:

- `ticketNumbers`
- `requesterNames`
- `issueDescriptions`
- `submissionTimes`
- `ticketStatuses`

The values at a given index in all five lists describe the same ticket. The sorting methods swap every field at that index together to keep records synchronized.

The waiting queue is a `Queue<Integer>` backed by `ArrayDeque<Integer>`. It contains ticket numbers in arrival order, not copies of ticket records. Queue display and peek operations use each queued number to find its record. Processing removes only the queue's head ticket and marks that ticket as processed. Sorting changes the order of records in the `ArrayList`s, but never changes the queue.

The application uses only static methods and built-in Java data types and collections for its procedural logic. It does not define custom ticket/entity classes or use custom objects.

## Sorting Algorithm Explanation

Both algorithms are implemented directly in `Main.java`; no built-in sorting methods are used.

### Bubble Sort — requester name

Bubble Sort compares adjacent requester names without regard to letter case and swaps records when they are out of order. It is a straightforward way to demonstrate sorting a small set of help desk records alphabetically. Its average and worst-case time complexity is O(n²); the implementation stops early if a pass makes no swaps.

### Insertion Sort — ticket number

Insertion Sort places each ticket number into its proper position among the preceding records. It demonstrates ordering the records by their numeric ticket identifiers. It has O(n²) average and worst-case time complexity and performs well on small or nearly sorted lists.

Each sort displays the records before and after sorting. Since each sort swaps the fields in all five record lists together, ticket details remain associated. The separate FIFO queue is unaffected.

## How to Run the Program

Install a Java Development Kit (JDK) version 8 or later. From the project root, compile and run:

```text
javac -d out src\Main.java
java -cp out Main
```

On macOS or Linux, use forward slashes for the source path:

```text
javac -d out src/Main.java
java -cp out Main
```

Select a menu option by entering its number. Creating a ticket automatically enqueues it. Choose **Queue operations** to display the waiting list, peek at the next ticket, or process it.

## Screenshots of the System

Save clear screenshots captured from the running application in the `screenshots/` folder and keep these filenames in sync with the README:

| Screenshot | What it should demonstrate |
| --- | --- |
| `screenshots/main-menu.png` | The main menu and available operations. |
| `screenshots/queue-operations.png` | Enqueueing through ticket creation, queue display, peek, and processing. |
| `screenshots/fifo-behavior.png` | Multiple tickets processed in the same order they were added. |
| `screenshots/sorting-algorithm-1.png` | Bubble Sort by requester name, showing records before and after. |
| `screenshots/sorting-algorithm-2.png` | Insertion Sort by ticket number, showing records before and after. |
| `screenshots/input-validation.png` | Invalid menu input, blank field handling, or an empty-queue message. |

Each screenshot should be readable and accompanied by a short caption in the final submission. Capture it from the actual running application; do not use mockups.

## Repository Contents

```text
Java-Sorting-Queue-Capstone/
├── src/
│   └── Main.java
├── screenshots/
│   ├── main-menu.png
│   ├── queue-operations.png
│   ├── fifo-behavior.png
│   ├── sorting-algorithm-1.png
│   ├── sorting-algorithm-2.png
│   └── input-validation.png
└── README.md
```
