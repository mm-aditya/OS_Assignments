package OSLab1;

import OSLab1.ProcessGraph.Monitoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ProcessManagement {
    private static File currentDirectory = new File(System.getProperty("user.dir"));  // Set the working directory
    private static File instructionSet = new File("graph-file2");            // Set the instructions file

    public static void main(String[] args) throws InterruptedException, IOException {

        // Parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory + "/src/OSLab1/" + instructionSet));

        // Mark initial runnable
        for (int i = 0; i < ProcessGraph.nodes.size(); i++) {
            if (ProcessGraph.nodes.get(i).getParents().isEmpty()) {
                ProcessGraph.nodes.get(i).setRunnable();
            }
        }

        // Print initial state of graph
        ProcessGraph.printGraph();

        boolean allNotExec = true;
        ArrayList<ProcessBuilder> pbs = new ArrayList<>();  // Processbuilder array list
        ArrayList<Process> processes = new ArrayList<>();   // Process array list
        ArrayList<Integer> indices = new ArrayList<>();     // Indices to store nodeIDs that will be run in current iteration

        Thread monitor = new Thread(new Monitoring());  //
        monitor.start();

        // MAIN WHILE LOOP
        // Check to see if all processes have been executed
        while (allNotExec) {
            allNotExec = false;
            pbs.clear();
            processes.clear();
            indices.clear();

            // Check if all nodes have been executed
            for (ProcessGraphNode node : ProcessGraph.nodes) {
                if (!node.isExecuted() && !node.isRunnable())
                    allNotExec = true;
            }

            // Set up processes that can be run, and run them
            buildRunnableProcList(pbs, indices);

            // Run the processes one by one
            for (int i = 0; i < pbs.size(); i++) {
                System.out.println("Running process: " + indices.get(i));
                processes.add(pbs.get(i).start());
            }

            // Wait for the processes
            for (int i = 0; i < processes.size(); i++) {
                processes.get(i).waitFor();
                // Catch errors in process and print out error message
                if (processes.get(i).exitValue() != 0) {
                    printError(processes.get(i));
                }
            }

            // For processes that finished execution, set the appropriate parameters
            for (Integer i : indices) {
                ProcessGraph.nodes.get(i).setExecuted();
                ProcessGraph.nodes.get(i).setNotRunable();
            }

            // Only if all processes haven't been executed, set nodes that can now be run to runnable
            if (allNotExec) {
                setNewRunnable(indices);
            }
        }

        System.out.println("\nAll process finished successfully");
        ProcessGraph.printBasic();
    }

    // Print the error message from a process that failed to complete
    private static void printError(Process process) {
        Scanner in = new Scanner(process.getErrorStream());
        while (in.hasNext()) System.out.print(in.next());
    }

    // Set up processes that can be run, and run them
    private static void buildRunnableProcList(ArrayList<ProcessBuilder> builder, ArrayList<Integer> indices) {
        for (int i = 0; i < ProcessGraph.nodes.size(); i++) {
            ProcessGraphNode node = ProcessGraph.nodes.get(i);
            if (node.isRunnable()) {
                indices.add(i);
                ProcessBuilder temp = new ProcessBuilder();
                temp.command(node.getCommand().split(" "));
                temp.directory(currentDirectory);
                String inpFile = node.getInputFile().getName();
                if (!inpFile.equals("stdin")) temp.redirectInput(node.getInputFile());
                if (!inpFile.equals("stdout")) temp.redirectOutput(node.getOutputFile());
                builder.add(temp);
            }
        }
    }

    // Set nodes that can now be run to runnable
    private static void setNewRunnable(ArrayList<Integer> indices) {
        for (Integer i : indices) {
            ArrayList<ProcessGraphNode> children = ProcessGraph.nodes.get(i).getChildren();
            for (int p = 0; p < children.size(); p++) {
                boolean parentsIsExec = true;
                ArrayList<ProcessGraphNode> parents = children.get(p).getParents();
                for (int k = 0; k < parents.size(); k++) {
                    if (!parents.get(k).isExecuted()) parentsIsExec = false;
                }
                if (parentsIsExec) children.get(p).setRunnable();
            }
        }
    }
}
