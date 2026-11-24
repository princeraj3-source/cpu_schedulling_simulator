import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Scanner;

public final class CpuSchedulingSimulator {
    private CpuSchedulingSimulator() {
    }

    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            String algorithm;
            if (args.length > 0) {
                algorithm = args[0].toLowerCase();
            } else {
                System.out.print("Scheduling algorithm (priority/rr): ");
                algorithm = input.next().toLowerCase();
            }

            if (!algorithm.equals("priority") && !algorithm.equals("rr")) {
                throw new IllegalArgumentException("Choose 'priority' or 'rr'.");
            }

            System.out.print("Number of processes: ");
            int processCount = readInt(input, "number of processes");
            if (processCount < 1) {
                throw new IllegalArgumentException("At least one process is required.");
            }

            ProcessList processes = new ProcessList();
            Set<Integer> usedIds = new HashSet<>();
            for (int i = 0; i < processCount; i++) {
                System.out.printf(
                        "Process %d (ID, arrival time, burst time, priority): ",
                        i + 1);
                int id = readInt(input, "process ID");
                long arrivalTime = readLong(input, "arrival time");
                long burstTime = readLong(input, "burst time");
                int priority = readInt(input, "priority");

                if (id < 1 || !usedIds.add(id)) {
                    throw new IllegalArgumentException(
                            "Process IDs must be unique positive integers.");
                }
                if (arrivalTime < 0) {
                    throw new IllegalArgumentException("Arrival times cannot be negative.");
                }
                if (burstTime < 1) {
                    throw new IllegalArgumentException("Burst times must be positive.");
                }

                processes.add(new Process(id, arrivalTime, burstTime, priority));
            }

            Schedule schedule;
            if (algorithm.equals("priority")) {
                schedule = scheduleByPriority(processes);
                System.out.println("\nNon-Preemptive Priority Scheduling");
                System.out.println("Lower priority number means higher priority.");
            } else {
                System.out.print("Round-robin time quantum: ");
                long quantum = readLong(input, "time quantum");
                if (quantum < 1) {
                    throw new IllegalArgumentException("The time quantum must be positive.");
                }
                schedule = scheduleRoundRobin(processes, quantum);
                System.out.println("\nRound Robin Scheduling (quantum = " + quantum + ")");
            }

            printSchedule(schedule);
        } catch (IllegalArgumentException exception) {
            System.err.println("Input error: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static int readInt(Scanner input, String description) {
        long value = readLong(input, description);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(description + " is outside the supported range.");
        }
        return (int) value;
    }

    private static long readLong(Scanner input, String description) {
        if (!input.hasNextLong()) {
            throw new IllegalArgumentException("Expected a valid " + description + ".");
        }
        return input.nextLong();
    }

    private static Schedule scheduleByPriority(ProcessList processList) {
        Process[] processes = sortByArrival(processList.toArray());
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt((Process process) -> process.priority)
                        .thenComparingLong(process -> process.arrivalTime)
                        .thenComparingInt(process -> process.id));
        Schedule schedule = new Schedule(processes);
        long currentTime = 0;
        int nextProcess = 0;
        int completed = 0;

        while (completed < processes.length) {
            while (nextProcess < processes.length
                    && processes[nextProcess].arrivalTime <= currentTime) {
                readyQueue.add(processes[nextProcess++]);
            }

            if (readyQueue.isEmpty()) {
                long nextArrival = processes[nextProcess].arrivalTime;
                schedule.addSegment("IDLE", currentTime, nextArrival);
                currentTime = nextArrival;
                continue;
            }

            Process process = readyQueue.remove();
            long endTime = currentTime + process.burstTime;
            schedule.addSegment(process.label(), currentTime, endTime);
            process.completionTime = endTime;
            currentTime = endTime;
            completed++;
        }
        return schedule;
    }

    private static Schedule scheduleRoundRobin(ProcessList processList, long quantum) {
        Process[] processes = sortByArrival(processList.toArray());
        CircularQueue readyQueue = new CircularQueue(processes.length);
        Schedule schedule = new Schedule(processes);
        long currentTime = 0;
        int nextProcess = 0;
        int completed = 0;

        while (completed < processes.length) {
            if (readyQueue.isEmpty() && nextProcess < processes.length
                    && currentTime < processes[nextProcess].arrivalTime) {
                long nextArrival = processes[nextProcess].arrivalTime;
                schedule.addSegment("IDLE", currentTime, nextArrival);
                currentTime = nextArrival;
            }

            while (nextProcess < processes.length
                    && processes[nextProcess].arrivalTime <= currentTime) {
                readyQueue.add(processes[nextProcess++]);
            }

            Process process = readyQueue.remove();
            long runTime = Math.min(quantum, process.remainingTime);
            long endTime = currentTime + runTime;
            schedule.addSegment(process.label(), currentTime, endTime);
            currentTime = endTime;
            process.remainingTime -= runTime;

            while (nextProcess < processes.length
                    && processes[nextProcess].arrivalTime <= currentTime) {
                readyQueue.add(processes[nextProcess++]);
            }

            if (process.remainingTime == 0) {
                process.completionTime = currentTime;
                completed++;
            } else {
                readyQueue.add(process);
            }
        }
        return schedule;
    }

    private static Process[] sortByArrival(Process[] processes) {
        Process[] sorted = Arrays.copyOf(processes, processes.length);
        Process[] buffer = new Process[sorted.length];
        mergeSort(sorted, buffer, 0, sorted.length);
        return sorted;
    }

    private static void mergeSort(Process[] processes, Process[] buffer, int start, int end) {
        if (end - start < 2) {
            return;
        }
        int middle = start + (end - start) / 2;
        mergeSort(processes, buffer, start, middle);
        mergeSort(processes, buffer, middle, end);

        int left = start;
        int right = middle;
        int index = start;
        while (left < middle && right < end) {
            if (compareArrival(processes[left], processes[right]) <= 0) {
                buffer[index++] = processes[left++];
            } else {
                buffer[index++] = processes[right++];
            }
        }
        while (left < middle) {
            buffer[index++] = processes[left++];
        }
        while (right < end) {
            buffer[index++] = processes[right++];
        }
        System.arraycopy(buffer, start, processes, start, end - start);
    }

    private static int compareArrival(Process first, Process second) {
        int byArrival = Long.compare(first.arrivalTime, second.arrivalTime);
        return byArrival != 0 ? byArrival : Integer.compare(first.id, second.id);
    }

    private static void printSchedule(Schedule schedule) {
        System.out.println("\nGantt chart:");
        for (Segment segment : schedule.segments) {
            System.out.printf("| %-8s ", segment.label);
        }
        System.out.println("|");
        if (!schedule.segments.isEmpty()) {
            System.out.print(schedule.segments.get(0).startTime);
            for (Segment segment : schedule.segments) {
                System.out.printf("%11d", segment.endTime);
            }
            System.out.println();
        }

        Process[] results = Arrays.copyOf(schedule.processes, schedule.processes.length);
        Arrays.sort(results, Comparator.comparingInt(process -> process.id));
        System.out.println("\nProcess\tArrival\tBurst\tPriority\tCompletion\tTurnaround\tWaiting");
        long totalTurnaround = 0;
        long totalWaiting = 0;
        for (Process process : results) {
            long turnaroundTime = process.completionTime - process.arrivalTime;
            long waitingTime = turnaroundTime - process.burstTime;
            totalTurnaround += turnaroundTime;
            totalWaiting += waitingTime;
            System.out.printf("%s\t%d\t%d\t%d\t\t%d\t\t%d\t\t%d%n",
                    process.label(), process.arrivalTime, process.burstTime, process.priority,
                    process.completionTime, turnaroundTime, waitingTime);
        }
        System.out.printf("Average turnaround time: %.2f%n",
                (double) totalTurnaround / results.length);
        System.out.printf("Average waiting time: %.2f%n",
                (double) totalWaiting / results.length);
    }

    private static final class Process {
        private final int id;
        private final long arrivalTime;
        private final long burstTime;
        private final int priority;
        private long remainingTime;
        private long completionTime;

        private Process(int id, long arrivalTime, long burstTime, int priority) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
            this.remainingTime = burstTime;
        }

        private String label() {
            return "P" + id;
        }
    }

    private static final class ProcessList {
        private Node head;
        private Node tail;
        private int size;

        private void add(Process process) {
            Node node = new Node(process);
            if (head == null) {
                head = node;
            } else {
                tail.next = node;
            }
            tail = node;
            size++;
        }

        private Process[] toArray() {
            Process[] processes = new Process[size];
            Node current = head;
            for (int i = 0; i < size; i++) {
                processes[i] = current.process;
                current = current.next;
            }
            return processes;
        }

        private static final class Node {
            private final Process process;
            private Node next;

            private Node(Process process) {
                this.process = process;
            }
        }
    }

    private static final class CircularQueue {
        private final Process[] elements;
        private int front;
        private int rear;
        private int size;

        private CircularQueue(int capacity) {
            elements = new Process[capacity];
        }

        private boolean isEmpty() {
            return size == 0;
        }

        private void add(Process process) {
            if (size == elements.length) {
                throw new IllegalStateException("Ready queue capacity exceeded.");
            }
            elements[rear] = process;
            rear = (rear + 1) % elements.length;
            size++;
        }

        private Process remove() {
            if (isEmpty()) {
                throw new IllegalStateException("Cannot remove from an empty ready queue.");
            }
            Process process = elements[front];
            elements[front] = null;
            front = (front + 1) % elements.length;
            size--;
            return process;
        }
    }

    private static final class Schedule {
        private final Process[] processes;
        private final List<Segment> segments = new ArrayList<>();

        private Schedule(Process[] processes) {
            this.processes = processes;
        }

        private void addSegment(String label, long startTime, long endTime) {
            if (startTime == endTime) {
                return;
            }
            if (!segments.isEmpty()) {
                Segment previous = segments.get(segments.size() - 1);
                if (previous.label.equals(label) && previous.endTime == startTime) {
                    previous.endTime = endTime;
                    return;
                }
            }
            segments.add(new Segment(label, startTime, endTime));
        }
    }

    private static final class Segment {
        private final String label;
        private final long startTime;
        private long endTime;

        private Segment(String label, long startTime, long endTime) {
            this.label = label;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
