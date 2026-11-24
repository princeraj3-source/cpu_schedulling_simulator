# CPU Scheduling Simulator

A Java command-line simulator for non-preemptive priority scheduling and
round-robin scheduling. It prints a Gantt chart, each process's completion,
turnaround, and waiting times, plus the averages of all three metrics.

## Requirements

Install a JDK (Java 8 or later). No external dependencies are required.

## Compile and run

```sh
javac CpuSchedulingSimulator.java
java CpuSchedulingSimulator
```

Choose `priority` or `rr` when prompted. You can also pass the algorithm as an
argument:

```sh
java CpuSchedulingSimulator priority
java CpuSchedulingSimulator rr
```

Enter the number of processes, then one row per process containing its ID,
arrival time, burst time, and priority, separated by spaces. IDs must be unique
positive integers; arrival times must be non-negative and burst times positive.
The round-robin mode additionally asks for a positive time quantum. Priority
mode treats a smaller priority number as higher priority; ties are broken by
arrival time and then process ID.

The implementation uses a linked list to collect input processes, merge sort
to order arrivals, a min-heap priority queue for priority scheduling, and a
fixed-size array circular queue for round robin. CPU idle periods appear as
`IDLE` in the Gantt chart.