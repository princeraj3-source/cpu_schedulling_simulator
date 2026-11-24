# CPU Scheduling Simulator (PS No. 80)

A beginner-friendly, console-based Java project that simulates:
1. **Priority Scheduling (Non-Preemptive)** — a smaller priority number means a higher priority.
2. **Round Robin Scheduling** — each ready process receives a fixed time quantum.

The program prints a Gantt chart, completion time (CT), waiting time (WT), turnaround time (TAT), and their averages.

## DSA concepts used
- Custom singly linked list to store processes.
- Insertion sort (written by us) to order processes by arrival time.
- Min-heap priority queue for non-preemptive priority scheduling.
- Array-based circular queue for Round Robin scheduling.
- ArrayList to store and print Gantt chart segments.

## Requirements
- JDK 8 or above
- Command Prompt / Terminal / VS Code / IntelliJ IDEA

## Run
Open a terminal in this folder and run:

```bash
javac CPUSchedulingSimulator.java
java CPUSchedulingSimulator
```

## Input rules
- Number of processes: greater than 0.
- Arrival time: zero or greater.
- Burst time: greater than 0.
- Priority: greater than 0; smaller number means higher priority.
- Round Robin quantum: greater than 0.
- Enter a unique process ID (for example P1, P2, P3) for each process. IDs are kept unchanged when the list is sorted.

## Formulas
- Turnaround Time = Completion Time - Arrival Time
- Waiting Time = Turnaround Time - Burst Time
- Average WT = Sum of WT / Number of processes
- Average TAT = Sum of TAT / Number of processes

## Test cases from the project brief

### Test Case 1 — Priority (normal)
Input processes:
- P1: AT 0, BT 3, Priority 3
- P2: AT 1, BT 2, Priority 1
- P3: AT 2, BT 1, Priority 2

Expected:
- Gantt: P1(0-3) P2(3-5) P3(5-6)
- WT: P1=0, P2=2, P3=3
- TAT: P1=3, P2=4, P3=4
- Average WT=1.67, Average TAT=3.67

### Test Case 2 — Priority (single process)
- P1: AT 0, BT 4, Priority 1
- Gantt: P1(0-4)
- WT=0, TAT=4
- Average WT=0.00, Average TAT=4.00

### Test Case 3 — Invalid input
- Burst time = -2
- The program rejects it and asks again: `Invalid burst time. It must be greater than 0.`

### Test Case 4 — Round Robin (quantum 2)
Enter P2 first and P1 second:
- P2: AT 1, BT 3
- P1: AT 0, BT 4
- Quantum: 2

After sorting by arrival:
- Gantt: P1(0-2) P2(2-4) P1(4-6) P2(6-7)
- P1: WT=2, TAT=6
- P2: WT=3, TAT=6
- Average WT=2.50, Average TAT=6.00

## Project structure

```text
CPU_Scheduling_Simulator/
├── CPUSchedulingSimulator.java
├── README.md
├── PROJECT_REPORT.pdf
└── .gitignore
```

## GitHub upload
Create a new GitHub repository, then upload these files or use:

```bash
git init
git add .
git commit -m "Add CPU Scheduling Simulator project"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
git push -u origin main
```

Replace the remote URL with your own repository URL.

## Limitations
This is a console-based educational simulator. It does not implement process creation in the operating system, context-switching overhead, or a graphical interface.
