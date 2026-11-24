import java.util.Scanner;

/*
 * Project: CPU Scheduling Simulator
 * Algorithms: Priority Scheduling (Non-Preemptive) and Round Robin
 * Data structures: Custom Linked List, Min Heap Priority Queue,
 *                  and Array-based Circular Queue
 */
public class CPUSchedulingSimulator {

    static class Process {
        String id;
        int arrival, burst, priority, remaining, completion;
        Process(String id, int arrival, int burst, int priority) {
            this.id = id;
            this.arrival = arrival;
            this.burst = burst;
            this.priority = priority;
            this.remaining = burst;
            this.completion = 0;
        }
    }

    // Custom singly linked list used to store and sort processes.
    static class Node {
        Process data;
        Node next;
        Node(Process data) { this.data = data; }
    }

    static class ProcessList {
        Node head;

        void add(Process p) {
            Node node = new Node(p);
            if (head == null) {
                head = node;
                return;
            }
            Node current = head;
            while (current.next != null) current = current.next;
            current.next = node;
        }

        // Insertion sort by arrival time (stable for equal arrival times).
        void sortByArrival() {
            Node sorted = null;
            Node current = head;
            while (current != null) {
                Node next = current.next;
                if (sorted == null || current.data.arrival < sorted.data.arrival) {
                    current.next = sorted;
                    sorted = current;
                } else {
                    Node temp = sorted;
                    while (temp.next != null &&
                           temp.next.data.arrival <= current.data.arrival) {
                        temp = temp.next;
                    }
                    current.next = temp.next;
                    temp.next = current;
                }
                current = next;
            }
            head = sorted;
        }

        int size() {
            int count = 0;
            for (Node n = head; n != null; n = n.next) count++;
            return count;
        }

        Process get(int index) {
            Node n = head;
            for (int i = 0; i < index && n != null; i++) n = n.next;
            return n == null ? null : n.data;
        }

        void print() {
            for (Node n = head; n != null; n = n.next) {
                System.out.println(n.data.id + "  AT=" + n.data.arrival +
                    "  BT=" + n.data.burst + "  Priority=" + n.data.priority);
            }
        }
    }

    static class Segment {
        String process;
        int start, end;
        Segment(String process, int start, int end) {
            this.process = process; this.start = start; this.end = end;
        }
    }

    // Min-heap priority queue: smaller priority number means higher priority.
    static class MinHeap {
        Process[] heap;
        int size;
        MinHeap(int capacity) {
            heap = new Process[Math.max(1, capacity)];
            size = 0;
        }
        boolean higher(Process a, Process b) {
            if (a.priority != b.priority) return a.priority < b.priority;
            if (a.arrival != b.arrival) return a.arrival < b.arrival;
            return a.id.compareTo(b.id) < 0;
        }
        void offer(Process p) {
            int i = size++;
            heap[i] = p;
            while (i > 0) {
                int parent = (i - 1) / 2;
                if (!higher(heap[i], heap[parent])) break;
                swap(i, parent); i = parent;
            }
        }
        Process poll() {
            if (size == 0) return null;
            Process result = heap[0];
            heap[0] = heap[--size];
            int i = 0;
            while (true) {
                int left = 2 * i + 1, right = 2 * i + 2, best = i;
                if (left < size && higher(heap[left], heap[best])) best = left;
                if (right < size && higher(heap[right], heap[best])) best = right;
                if (best == i) break;
                swap(i, best); i = best;
            }
            return result;
        }
        boolean isEmpty() { return size == 0; }
        void swap(int a, int b) {
            Process temp = heap[a]; heap[a] = heap[b]; heap[b] = temp;
        }
    }

    // Array-based circular queue for Round Robin.
    static class CircularQueue {
        Process[] queue;
        int front = 0, rear = 0, size = 0;
        CircularQueue(int capacity) { queue = new Process[Math.max(2, capacity + 1)]; }
        boolean isEmpty() { return size == 0; }
        void offer(Process p) {
            if (size == queue.length) {
                System.out.println("Queue capacity reached.");
                return;
            }
            queue[rear] = p;
            rear = (rear + 1) % queue.length;
            size++;
        }
        Process poll() {
            if (isEmpty()) return null;
            Process p = queue[front];
            front = (front + 1) % queue.length;
            size--;
            return p;
        }
    }

    static void addSegment(java.util.ArrayList<Segment> chart,
                           String id, int start, int end) {
        if (end > start) chart.add(new Segment(id, start, end));
    }

    static void printResults(String algorithm, ProcessList list,
                             java.util.ArrayList<Segment> chart) {
        System.out.println("\n===== " + algorithm + " =====");
        System.out.print("Gantt: ");
        for (int i = 0; i < chart.size(); i++) {
            Segment s = chart.get(i);
            System.out.print(s.process + "(" + s.start + "-" + s.end + ")");
            if (i < chart.size() - 1) System.out.print(" ");
        }
        double totalWT = 0, totalTAT = 0;
        System.out.println("\n\nProcess\tAT\tBT\tCT\tWT\tTAT");
        for (int i = 0; i < list.size(); i++) {
            Process p = list.get(i);
            int tat = p.completion - p.arrival;
            int wt = tat - p.burst;
            totalWT += wt; totalTAT += tat;
            System.out.println(p.id + "\t" + p.arrival + "\t" + p.burst +
                "\t" + p.completion + "\t" + wt + "\t" + tat);
        }
        int n = list.size();
        System.out.printf("Average WT: %.2f%n", totalWT / n);
        System.out.printf("Average TAT: %.2f%n", totalTAT / n);
    }

    static void priorityScheduling(ProcessList list) {
        list.sortByArrival();
        MinHeap ready = new MinHeap(list.size());
        java.util.ArrayList<Segment> chart = new java.util.ArrayList<>();
        int index = 0, completed = 0, time = 0, n = list.size();

        while (completed < n) {
            while (index < n && list.get(index).arrival <= time)
                ready.offer(list.get(index++));
            if (ready.isEmpty()) {
                int nextArrival = list.get(index).arrival;
                addSegment(chart, "IDLE", time, nextArrival);
                time = nextArrival;
                continue;
            }
            Process p = ready.poll();
            int start = time;
            time += p.burst; // Non-preemptive: process runs until completion.
            p.completion = time;
            completed++;
            addSegment(chart, p.id, start, time);
        }
        printResults("Priority Scheduling (Non-Preemptive)", list, chart);
    }

    static void roundRobin(ProcessList list, int quantum) {
        list.sortByArrival();
        CircularQueue ready = new CircularQueue(list.size() + 2);
        java.util.ArrayList<Segment> chart = new java.util.ArrayList<>();
        int index = 0, completed = 0, time = 0, n = list.size();

        while (completed < n) {
            // Add all processes that have arrived by the current time.
            while (index < n && list.get(index).arrival <= time)
                ready.offer(list.get(index++));
            if (ready.isEmpty()) {
                int nextArrival = list.get(index).arrival;
                addSegment(chart, "IDLE", time, nextArrival);
                time = nextArrival;
                continue;
            }

            Process p = ready.poll();
            int start = time;
            int runTime = Math.min(quantum, p.remaining);
            time += runTime;
            p.remaining -= runTime;
            addSegment(chart, p.id, start, time);

            // Arrivals during this time slice join before the running process
            // is placed at the back of the ready queue.
            while (index < n && list.get(index).arrival <= time)
                ready.offer(list.get(index++));

            if (p.remaining > 0) {
                ready.offer(p);
            } else {
                p.completion = time;
                completed++;
            }
        }
        printResults("Round Robin (Quantum = " + quantum + ")", list, chart);
    }

    static ProcessList readProcesses(Scanner sc) {
        ProcessList list = new ProcessList();
        System.out.print("Enter number of processes: ");
        int n = readPositive(sc, "Number of processes");
        for (int i = 1; i <= n; i++) {
            System.out.println("\nProcess " + i);
            System.out.print("Process ID (for example P1): ");
            String id = readProcessId(sc, list);
            System.out.print("Arrival time: ");
            int at = readNonNegative(sc, "Arrival time");
            System.out.print("Burst time: ");
            int bt = readPositive(sc, "Burst time");
            System.out.print("Priority (smaller number = higher priority): ");
            int pr = readPositive(sc, "Priority");
            list.add(new Process(id, at, bt, pr));
        }
        return list;
    }

    static String readProcessId(Scanner sc, ProcessList list) {
        while (true) {
            String id = sc.nextLine().trim();
            if (id.isEmpty()) {
                System.out.print("Process ID cannot be blank. Enter again: ");
                continue;
            }
            boolean duplicate = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id.equalsIgnoreCase(id)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) return id;
            System.out.print("Process ID already exists. Enter a unique ID: ");
        }
    }

    static int readPositive(Scanner sc, String field) {
        while (true) {
            String input = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.print("Invalid " + field.toLowerCase() +
                ". It must be greater than 0. Enter again: ");
        }
    }

    static int readNonNegative(Scanner sc, String field) {
        while (true) {
            String input = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= 0) return value;
            } catch (NumberFormatException ignored) { }
            System.out.print("Invalid " + field.toLowerCase() +
                ". It must be zero or greater. Enter again: ");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("==================================");
        System.out.println("       CPU SCHEDULING SIMULATOR");
        System.out.println("==================================");
        System.out.println("1. Priority Scheduling (Non-Preemptive)");
        System.out.println("2. Round Robin");
        System.out.print("Choose algorithm: ");

        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid algorithm choice.");
            sc.close();
            return;
        }

        if (choice != 1 && choice != 2) {
            System.out.println("Invalid algorithm choice.");
            sc.close();
            return;
        }

        ProcessList processes = readProcesses(sc);
        if (choice == 1) {
            priorityScheduling(processes);
        } else {
            System.out.print("Enter time quantum: ");
            int quantum = readPositive(sc, "Time quantum");
            roundRobin(processes, quantum);
        }
        sc.close();
    }
}
