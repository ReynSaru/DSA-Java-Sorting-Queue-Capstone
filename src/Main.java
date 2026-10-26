import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
    private static final Path DATA_FILE = Paths.get("tickets.txt");
    private static final Path TEMP_DATA_FILE = Paths.get("tickets.txt.tmp");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int END_OF_INPUT = Integer.MIN_VALUE;
    private static final int CANCEL_OPERATION = Integer.MIN_VALUE + 1;
    private static int nextTicketNumber = 1001;

    public static void main(String[] args) {
        try {
            loadTickets();
        } catch (IOException exception) {
            System.out.println("Unable to load saved tickets from " + DATA_FILE
                    + ": " + exception.getMessage());
            System.out.println("The application will exit to avoid overwriting saved data.");
            return;
        }

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
        if (nextTicketNumber < 1) {
            System.out.println("\nNo more ticket numbers are available.");
            return;
        }

        System.out.println("\n--- Create Help Desk Ticket ---");
        System.out.println("Enter 0 at either prompt to return to the main menu.");
        String requesterName = readRequesterName();
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

        try {
            saveTickets();
        } catch (IOException exception) {
            ticketQueue.remove(Integer.valueOf(ticketNumber));
            removeTicketRecord(ticketNumbers.size() - 1);
            System.out.println("Unable to save the new ticket. It was not added: "
                    + exception.getMessage());
            return;
        }

        nextTicketNumber++;
        System.out.println("Ticket created and added to the queue.");
        displayTicket(ticketNumbers.size() - 1);
    }

    private static String readRequesterName() {
        while (true) {
            String value = readLine("Requester name: ");
            if (value == null) {
                return null;
            }

            value = value.trim();
            if (value.equals("0")) {
                System.out.println("Returning to the main menu.");
                return null;
            }
            if (value.matches("\\p{L}[\\p{L}\\p{M}]*(?:(?: +|['’\\-])\\p{L}[\\p{L}\\p{M}]*)*")) {
                return value;
            }
            System.out.println("Invalid name. Use letters, spaces, apostrophes, or hyphens only.");
        }
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

        System.out.println("\nEnter 0 to return to the main menu.");
        int ticketNumber = readTicketNumber("Enter a ticket number (for example, HD-1001): ");
        if (ticketNumber == END_OF_INPUT || ticketNumber == CANCEL_OPERATION) {
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
            System.out.println("0. Return to main menu");

            int choice = readIntegerInRange("Choose an option: ", 0, 3);
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
                case 0:
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

        ticketStatuses.set(index, "Processed");
        try {
            saveTickets();
        } catch (IOException exception) {
            ticketStatuses.set(index, "Pending");
            System.out.println("Unable to save the processed ticket. It remains in the queue: "
                    + exception.getMessage());
            return;
        }

        ticketQueue.remove();
        System.out.println("\nProcessed the next ticket in FIFO order:");
        displayTicket(index);
    }

    private static void loadTickets() throws IOException {
        if (!Files.exists(DATA_FILE)) {
            return;
        }

        List<String> savedLines = Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8);
        int highestTicketNumber = 0;
        int loadedCount = 0;

        for (int lineIndex = 0; lineIndex < savedLines.size(); lineIndex++) {
            try {
                ArrayList<String> fields = splitSavedRecord(savedLines.get(lineIndex));
                if (fields.size() != 5) {
                    throw new IllegalArgumentException("expected five fields");
                }

                int ticketNumber = Integer.parseInt(fields.get(0));
                String status = fields.get(1);
                String submittedAt = fields.get(2);
                String requesterName = fields.get(3);
                String issueDescription = fields.get(4);

                if (ticketNumber < 1 || findTicketIndex(ticketNumber) >= 0) {
                    throw new IllegalArgumentException("ticket number is invalid or duplicated");
                }
                if (!status.equals("Pending") && !status.equals("Processed")) {
                    throw new IllegalArgumentException("ticket status is invalid");
                }
                if (submittedAt.trim().isEmpty()
                        || requesterName.trim().isEmpty()
                        || issueDescription.trim().isEmpty()) {
                    throw new IllegalArgumentException("ticket details cannot be blank");
                }

                ticketNumbers.add(ticketNumber);
                ticketStatuses.add(status);
                submissionTimes.add(submittedAt);
                requesterNames.add(requesterName);
                issueDescriptions.add(issueDescription);
                highestTicketNumber = Math.max(highestTicketNumber, ticketNumber);
                loadedCount++;
            } catch (IllegalArgumentException exception) {
                System.out.println("Warning: Skipping invalid saved ticket on line "
                        + (lineIndex + 1) + ": " + exception.getMessage() + ".");
            }
        }

        rebuildWaitingQueue();
        nextTicketNumber = highestTicketNumber == Integer.MAX_VALUE
                ? -1 : highestTicketNumber + 1;

        System.out.println("Loaded " + loadedCount + " saved ticket record(s); "
                + ticketQueue.size() + " pending ticket(s) restored to the FIFO queue.");
    }

    private static void rebuildWaitingQueue() {
        int previousTicketNumber = 0;
        int pendingCount = 0;

        for (String status : ticketStatuses) {
            if (status.equals("Pending")) {
                pendingCount++;
            }
        }

        for (int count = 0; count < pendingCount; count++) {
            int nextIndex = -1;
            for (int index = 0; index < ticketNumbers.size(); index++) {
                int ticketNumber = ticketNumbers.get(index);
                if (ticketStatuses.get(index).equals("Pending")
                        && ticketNumber > previousTicketNumber
                        && (nextIndex < 0 || ticketNumber < ticketNumbers.get(nextIndex))) {
                    nextIndex = index;
                }
            }

            if (nextIndex < 0) {
                break;
            }

            int ticketNumber = ticketNumbers.get(nextIndex);
            if (!ticketQueue.offer(ticketNumber)) {
                System.out.println("Warning: Unable to restore ticket "
                        + formatTicketNumber(ticketNumber) + " to the waiting queue.");
                break;
            }
            previousTicketNumber = ticketNumber;
        }
    }

    private static void saveTickets() throws IOException {
        ArrayList<String> savedLines = new ArrayList<String>();
        for (int index = 0; index < ticketNumbers.size(); index++) {
            savedLines.add(ticketNumbers.get(index)
                    + "|" + escapeSavedField(ticketStatuses.get(index))
                    + "|" + escapeSavedField(submissionTimes.get(index))
                    + "|" + escapeSavedField(requesterNames.get(index))
                    + "|" + escapeSavedField(issueDescriptions.get(index)));
        }

        Files.write(TEMP_DATA_FILE, savedLines, StandardCharsets.UTF_8);
        try {
            Files.move(TEMP_DATA_FILE, DATA_FILE, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(TEMP_DATA_FILE, DATA_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String escapeSavedField(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '\\') {
                escaped.append("\\\\");
            } else if (character == '|') {
                escaped.append("\\|");
            } else if (character == '\n') {
                escaped.append("\\n");
            } else if (character == '\r') {
                escaped.append("\\r");
            } else {
                escaped.append(character);
            }
        }
        return escaped.toString();
    }

    private static ArrayList<String> splitSavedRecord(String line) {
        ArrayList<String> fields = new ArrayList<String>();
        StringBuilder field = new StringBuilder();
        boolean escaped = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (escaped) {
                if (character == 'n') {
                    field.append('\n');
                } else if (character == 'r') {
                    field.append('\r');
                } else if (character == '\\' || character == '|') {
                    field.append(character);
                } else {
                    throw new IllegalArgumentException("invalid escape sequence");
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '|') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }

        if (escaped) {
            throw new IllegalArgumentException("unfinished escape sequence");
        }
        fields.add(field.toString());
        return fields;
    }

    private static void removeTicketRecord(int index) {
        ticketNumbers.remove(index);
        requesterNames.remove(index);
        issueDescriptions.remove(index);
        submissionTimes.remove(index);
        ticketStatuses.remove(index);
    }

    private static void sortByRequesterName() {
        if (ticketNumbers.isEmpty()) {
            System.out.println("\nNo ticket records are available to sort.");
            return;
        }

        System.out.println("\n--- Bubble Sort by Requester Name ---");
        displaySortSection("BEFORE SORTING - ORIGINAL RECORD ORDER");

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

        displaySortSection("AFTER SORTING - SORTED BY REQUESTER NAME");
        System.out.println("The FIFO queue order was not changed.");
    }

    private static void sortByTicketNumber() {
        if (ticketNumbers.isEmpty()) {
            System.out.println("\nNo ticket records are available to sort.");
            return;
        }

        System.out.println("\n--- Insertion Sort by Ticket Number ---");
        displaySortSection("BEFORE SORTING - ORIGINAL RECORD ORDER");

        for (int index = 1; index < ticketNumbers.size(); index++) {
            int position = index;
            while (position > 0
                    && ticketNumbers.get(position - 1) > ticketNumbers.get(position)) {
                swapRecords(position - 1, position);
                position--;
            }
        }

        displaySortSection("AFTER SORTING - SORTED BY TICKET NUMBER");
        System.out.println("The FIFO queue order was not changed.");
    }

    private static void displaySortSection(String heading) {
        System.out.println("\n========== " + heading + " ==========");
        for (int index = 0; index < ticketNumbers.size(); index++) {
            displayTicket(index);
        }
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
                if (ticketNumber == 0) {
                    System.out.println("Returning to the main menu.");
                    return CANCEL_OPERATION;
                }
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
            if (value.equals("0")) {
                System.out.println("Returning to the main menu.");
                return null;
            }
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