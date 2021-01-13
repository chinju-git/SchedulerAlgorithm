package controller;

import model.GanttRecord;
import model.Process;
import model.ReadyQueue;

import java.util.ArrayList;

public class PreemptiveScheduling {
    //Gantt
    private ArrayList<GanttRecord> gantt;
    private int currentTime;
    private int exeTime;
    private ReadyQueue readyQueue;

    public PreemptiveScheduling(){
        gantt = new ArrayList<>();
        currentTime = 0;
        exeTime = 0;
        readyQueue = new ReadyQueue();
    }

    public ArrayList<GanttRecord> getGantt(ArrayList<Process> processes){
        //finds first arriving time from the process array
        currentTime = this.getFirstArrivingTime(processes);
        int in = currentTime ,out = currentTime;

        //based on the arriving time moves the first arrived process to a new array list
        ArrayList<Process> processes1 = this.getFirstArrivingProcesses(processes);

        for(Process process : processes1){
            //added the first process to ready queue and removed the same process from processes array list
            readyQueue.enqueue(process);
            processes.remove(process);
        }

        //created new array list sorted based on arriving time (1st arrived process is not present in this list) 
        ArrayList<Process> orderedByArrivingTime = this.orderProcessesByArrivingTime(processes);

        //iterate through the the ready queue
        while(!readyQueue.isEmpty()){
            //Taking the first process from ready queue
            Process process = readyQueue.dequeue();


            //Iterate over the Sorted array list 
            //
            if(orderedByArrivingTime.size() > 0) {
                //Handle of all arriving processes while one process has the control of CPU
                for (int i = 0; i < orderedByArrivingTime.size(); i++) {
                    Process p = orderedByArrivingTime.get(i);
                    //the new process that comes when the CPU is busy is compared to the priority and if the priority is 
                    //lower than the priority of the process that is in control then the new process is added to the ready queue
                    if (p.getArrivingTime() >= process.getArrivingTime()
                            && p.getArrivingTime() < (process.getBurstTime() + currentTime)
                            && p.getPriority() >= process.getPriority()) {
                        readyQueue.enqueue(p);
                        orderedByArrivingTime.remove(p);
                        i--;
                    }

                    //the new process that comes when the CPU is busy is compared to the priority and 
                    //if the priority is greater than the priority of the process that is in control then the old process is added to the ready queue
                    //with  reduced burst time, CPU control is taken over by the new process
                    else if (p.getArrivingTime() >= process.getArrivingTime()
                            && p.getArrivingTime() < (process.getBurstTime() + currentTime)
                            && p.getPriority() < process.getPriority()) {
                        in = currentTime;
                        currentTime = p.getArrivingTime();
                        process.reduceTime(currentTime - in);
                        out = currentTime;
                        readyQueue.enqueue(process);
                        GanttRecord gR = new GanttRecord(in, out, process.getProcessID());
                        gantt.add(gR);

                        readyQueue.enqueue(p);
                        orderedByArrivingTime.remove(p);
                        i--;

                        break;
                    }
                    
                    //if the whole list of new processes is checked and none of them is valid
                  // to take control of the CPU, the process that has control continues with the time it takes
                    if (i == orderedByArrivingTime.size() - 1) {
                        in = currentTime;
                        currentTime += process.getBurstTime();
                        out = currentTime;
                        gantt.add(new GanttRecord(in, out, process.getProcessID()));
                        //checks if at the end of the uninterrupted execution of a process we have a new process that
                        // comes and goes in the ready row
                        if(orderedByArrivingTime.size() > 0
                                && readyQueue.isEmpty()) {
                            readyQueue.enqueue(orderedByArrivingTime.get(0));
                        }
                    }
                }
            }
            
            //the other case when we do not have new processes that follow but only those that are in the ready queue are treated
            else{
                in = currentTime;
                currentTime += process.getBurstTime();
                out = currentTime;
                gantt.add(new GanttRecord(in, out, process.getProcessID()));
            }
        }
        return gantt;
    }

    public static int getCompletionTime(Process p, ArrayList<GanttRecord> gantt) {
        int completionTime = 0;
        for(GanttRecord gR : gantt){
            if(gR.getProcessId() == p.getProcessID())
                completionTime = gR.getOutTime();
        }
        return completionTime;
    }

    public static int getTurnAroundTime(Process p, ArrayList<GanttRecord> gantt) {
        int completionTime = PreemptiveScheduling.getCompletionTime(p,gantt);
        return completionTime-p.getArrivingTime();
    }

    public static int getWaitingTime(Process p, ArrayList<GanttRecord> gantt) {
        int turnAroundTime = PreemptiveScheduling.getTurnAroundTime(p,gantt);
        return turnAroundTime-p.getBurstTime();
    }

    private ArrayList<Process> orderProcessesByArrivingTime(ArrayList<Process> processes){
        ArrayList<Process> newProcesses = new ArrayList<>();
        while(processes.size() != 0) {
            Process p = this.getFirstArrivingProcess(processes);
            processes.remove(p);
            newProcesses.add(p);
        }
        return newProcesses;
    }

    private Process getFirstArrivingProcess(ArrayList<Process> processes){
        int min = Integer.MAX_VALUE;
        Process process = null;
        for(Process p : processes){
            if(p.getArrivingTime() < min){
                min = p.getArrivingTime();
                process = p;
            }
        }
        return process;
    }

    private ArrayList<Process> getFirstArrivingProcesses(ArrayList<Process> processes){
        int min = this.getFirstArrivingTime(processes);
        ArrayList<Process> processes1 = new ArrayList<>();
        for(Process p : processes){
            if(p.getArrivingTime() == min){
                processes1.add(p);
            }
        }
        return processes1;
    }

    private int getFirstArrivingTime(ArrayList<Process> processes){
        int min = Integer.MAX_VALUE;
        for(Process p : processes){
            if(p.getArrivingTime() < min){
                min = p.getArrivingTime();
            }
        }
        return min;
    }

}