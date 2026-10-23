import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final ArrayList<Integer> ticketNumbers = new ArrayList<Integer>();
    private static final ArrayList<String> requesterNames = new ArrayList<String>();
    private static final ArrayList<String> issueDescriptions = new ArrayList<String>();
    private static final ArrayList<String> submissionTimes = new ArrayList<String>();
    private static final ArrayList<String> ticketStatuses = new ArrayList<String>();
    private static final Queue<Integer> ticketQueue = new ArrayDeque<Integer>();
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int END_OF_INPUT = Integer.MIN_VALUE;
    private static int nextTicketNumber = 1001;

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            displayMainMenu();
            int choice = readIntegerInRange("Choose an option: ", 1, 7);

            if (choice == END_OF_INPUT) {
                break;
            }

            switch (choice) {
                case 1:
                    addTicket();
                    break;
                case 2:
                    displayAllTickets();
                    break;
                case 3:
                    searchTicket();
                    break;
                case 4:
                    displayQueueMenu();
                    break;
                case 5:
                    sortByRequesterName();
                    break;
                case 6:
                    sortByTicketNumber();
                    break;
                case 7:
                    running = false;
                    System.out.println("Thank you for using the IT Help Desk Ticketing System.");
                    break;
                default:
                    System.out.println("Invalid menu selection.");
            }
        }

        if (INPUT.ioException() != null) {
            System.out.println("Input ended because of an input error.");
        } else if (running) {
            System.out.println("\nInput ended. Exiting the application.");
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n============================================");
        System.out.println("       IT HELP DESK TICKETING SYSTEM");
        System.out.println("============================================");
        System.out.println("1. Create ticket (add to FIFO queue)");
        System.out.println("2. Display all ticket records");
        System.out.println("3. Search for a ticket");
        System.out.println("4. Queue operations");
        System.out.println("5. Sort records by requester name (Bubble Sort)");
        System.out.println("6. Sort records by ticket number (Insertion Sort)");
        System.out.println("7. Exit");
    }

    private static void addTicket() {
        System.out.println("\n--- Create Help Desk Ticket ---");
        String requesterName = readNonBlankLine("Requester name: ");
        if (requesterName == null) {
            return;
        }

        String issueDescription = readNonBlankLine("Issue description: ");
        if (issueDescription == null) {
            return;
        }

        int ticketNumber = nextTicketNumber;
        String submittedAt = LocalDateTime.now().format(DATE_FORMAT);

        ticketNumbers.add(ticketNumber);
        requesterNames.add(requesterName);
        issueDescriptions.add(issueDescription);
        submissionTimes.add(submittedAt);
        ticketStatuses.add("Pending");

        if (!ticketQueue.offer(ticketNumber)) {
            int lastIndex = ticketNumbers.size() - 1;
            ticketNumbers.remove(lastIndex);
            requesterNames.remove(lastIndex);
            issueDescriptions.remove(lastIndex);
            submissionTimes.remove(lastIndex);
            ticketStatuses.remove(lastIndex);
            System.out.println("Unable to add the ticket to the queue. No ticket was saved.");
            return;
        }

        nextTicketNumber++;
        System.out.println("Ticket created and added to the queue.");
        displayTicket(ticketNumbers.size() - 1);
    }

    private static void displayAllTickets() {
        System.out.println("\n--- All Ticket Records ---");
        if (ticketNumbers.isEmpty()) {
            System.out.println("No ticket records are available.");
            return;
        }

        for (int index = 0; index < ticketNumbers.size(); index++) {
            displayTicket(index);
        }
    }

    private static void searchTicket() {
        if (ticketNumbers.isEmpty()) {
            System.out.println("\nNo ticket records are available to search.");
            return;
        }

        int ticketNumber = readTicketNumber("Enter a ticket number (for example, HD-1001): ");
        if (ticketNumber == END_OF_INPUT) {
            return;
        }

        int index = findTicketIndex(ticketNumber);
        if (index < 0) {
            System.out.println("No ticket found with number " + formatTicketNumber(ticketNumber) + ".");
            return;
        }

        System.out.println("\n--- Ticket Found ---");
        displayTicket(index);
    }

    private static void displayQueueMenu() {
        boolean inQueueMenu = true;

        while (inQueueMenu) {
            System.out.println("\n--- Queue Operations ---");
            System.out.println("1. Display waiting queue");
            System.out.println("2. Peek at next ticket");
            System.out.println("3. Process next ticket (dequeue)");
            System.out.println("4. Return to main menu");

            int choice = readIntegerInRange("Choose an option: ", 1, 4);
            if (choice == END_OF_INPUT) {
                return;
            }

            switch (choice) {
                case 1:
                    displayWaitingQueue();
                    break;
                case 2:
                    peekAtNextTicket();
                    break;
                case 3:
                    processNextTicket();
                    break;
                case 4:
                    inQueueMenu = false;
                    break;
                default:
                    System.out.println("Invalid queue menu selection.");
            }
        }
    }

    private static void displayWaitingQueue() {
        System.out.println("\n--- Waiting Queue (FIFO order) ---");
        if (ticketQueue.isEmpty()) {
            System.out.println("The waiting queue is empty.");
            return;
        }

        int position = 1;
        for (Integer ticketNumber : ticketQueue) {
            int index = findTicketIndex(ticketNumber);
            if (index < 0) {
                System.out.println("Queue error: ticket " + formatTicketNumber(ticketNumber)
                        + " has no matching record.");
                continue;
            }
            System.out.println(position + ". " + formatTicketNumber(ticketNumber)
                    + " | " + requesterNames.get(index)
                    + " | " + issueDescriptions.get(index)
                    + " | Submitted: " + submissionTimes.get(index));
            position++;
        }
    }

    private static void peekAtNextTicket() {
        Integer ticketNumber = ticketQueue.peek();
        if (ticketNumber == null) {
            System.out.println("\nThe waiting queue is empty. There is no next ticket.");
            return;
        }

        int index = findTicketIndex(ticketNumber);
        if (index < 0) {
            System.out.println("Queue error: ticket " + formatTicketNumber(ticketNumber)
                    + " has no matching record.");
            return;
        }

        System.out.println("\n--- Next Ticket (still in queue) ---");
        displayTicket(index);
    }

    private static void processNextTicket() {
        Integer ticketNumber = ticketQueue.peek();
        if (ticketNumber == null) {
            System.out.println("\nThe waiting queue is empty. No ticket was processed.");
            return;
        }

        int index = findTicketIndex(ticketNumber);
        if (index < 0) {
            System.out.println("Queue error: ticket " + formatTicketNumber(ticketNumber)
                    + " has no matching record. No ticket was removed.");
            return;
        }

        ticketQueue.remove();
        ticketStatuses.set(index, "Processed");
        System.out.println("\nProcessed the next ticket in FIFO order:");
        displayTicket(index);
    }

    private static void sortByRequesterName() {
        if (ticketNumbers.isEmpty()) {
            System.out.println("\nNo ticket records are available to sort.");
            return;
        }

        System.out.println("\n--- Bubble Sort by Requester Name ---");
        System.out.println("Before sorting:");
        displayAllTickets();

        for (int end = requesterNames.size() - 1; end > 0; end--) {
            boolean swapped = false;
            for (int index = 0; index < end; index++) {
                if (requesterNames.get(index).compareToIgnoreCase(requesterNames.get(index + 1)) > 0) {
                    swapRecords(index, index + 1);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }

        System.out.println("After sorting by requester name:");
        displayAllTickets();
        System.out.println("The FIFO queue order was not changed.");
    }

    private static void sortByTicketNumber() {
        if (ticketNumbers.isEmpty()) {
            System.out.println("\nNo ticket records are available to sort.");
            return;
        }

        System.out.println("\n--- Insertion Sort by Ticket Number ---");
        System.out.println("Before sorting:");
        displayAllTickets();

        for (int index = 1; index < ticketNumbers.size(); index++) {
            int position = index;
            while (position > 0
                    && ticketNumbers.get(position - 1) > ticketNumbers.get(position)) {
                swapRecords(position - 1, position);
                position--;
            }
        }

        System.out.println("After sorting by ticket number:");
        displayAllTickets();
        System.out.println("The FIFO queue order was not changed.");
    }

    private static void swapRecords(int firstIndex, int secondIndex) {
        Integer ticketNumber = ticketNumbers.get(firstIndex);
        ticketNumbers.set(firstIndex, ticketNumbers.get(secondIndex));
        ticketNumbers.set(secondIndex, ticketNumber);

        String requesterName = requesterNames.get(firstIndex);
        requesterNames.set(firstIndex, requesterNames.get(secondIndex));
        requesterNames.set(secondIndex, requesterName);

        String issueDescription = issueDescriptions.get(firstIndex);
        issueDescriptions.set(firstIndex, issueDescriptions.get(secondIndex));
        issueDescriptions.set(secondIndex, issueDescription);

        String submissionTime = submissionTimes.get(firstIndex);
        submissionTimes.set(firstIndex, submissionTimes.get(secondIndex));
        submissionTimes.set(secondIndex, submissionTime);

        String status = ticketStatuses.get(firstIndex);
        ticketStatuses.set(firstIndex, ticketStatuses.get(secondIndex));
        ticketStatuses.set(secondIndex, status);
    }

    private static void displayTicket(int index) {
        System.out.println("Ticket: " + formatTicketNumber(ticketNumbers.get(index))
                + " | Requester: " + requesterNames.get(index)
                + " | Issue: " + issueDescriptions.get(index)
                + " | Submitted: " + submissionTimes.get(index)
                + " | Status: " + ticketStatuses.get(index));
    }

    private static int findTicketIndex(int ticketNumber) {
        for (int index = 0; index < ticketNumbers.size(); index++) {
            if (ticketNumbers.get(index) == ticketNumber) {
                return index;
            }
        }
        return -1;
    }

    private static String formatTicketNumber(int ticketNumber) {
        return String.format(Locale.ROOT, "HD-%04d", ticketNumber);
    }

    private static int readTicketNumber(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return END_OF_INPUT;
            }

            String normalized = value.trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("HD-")) {
                normalized = normalized.substring(3);
            }

            try {
                int ticketNumber = Integer.parseInt(normalized);
                if (ticketNumber > 0) {
                    return ticketNumber;
                }
            } catch (NumberFormatException exception) {
            }
            System.out.println("Invalid ticket number. Enter a positive number such as HD-1001.");
        }
    }

    private static int readIntegerInRange(String prompt, int minimum, int maximum) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return END_OF_INPUT;
            }

            try {
                int number = Integer.parseInt(value.trim());
                if (number >= minimum && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException exception) {
            }
            System.out.println("Invalid input. Enter a number from " + minimum + " to " + maximum + ".");
        }
    }

    private static String readNonBlankLine(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value == null) {
                return null;
            }
            value = value.trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be blank. Please try again.");
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!INPUT.hasNextLine()) {
            return null;
        }
        return INPUT.nextLine();
    }
}
