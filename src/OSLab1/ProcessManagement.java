package OSLab1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProcessManagement {

    //set the working directory
    private static File currentDirectory = new File(System.getProperty("user.dir"));
    //set the instructions file
    private static File instructionSet = new File("graph-file");
    public static Object lock=new Object();

    public static void main(String[] args) throws InterruptedException, IOException{

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory + "/src/OSLab1/"+instructionSet));

        // Print the graph information
	    // WRITE YOUR CODE
        //mark initial runnable
        for(int i =0; i < ProcessGraph.nodes.size(); i++) {
            if (ProcessGraph.nodes.get(i).getParents().isEmpty()) {
                ProcessGraph.nodes.get(i).setRunnable();
            }
        }

        ProcessGraph.printGraph();
        //System.out.println(ProcessGraph.nodes.get(0).getParents().isEmpty());



        boolean allNotExec = true;

        while(allNotExec) {
            allNotExec = false;
            ArrayList<ProcessBuilder> pbs = new ArrayList<>();
            ArrayList<Process> processes = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            // Using index of ProcessGraph, loop through each ProcessGraphNode, to check whether it is ready to run
            // check if all the nodes are executed
            // WRITE YOUR CODE
            for(ProcessGraphNode node: ProcessGraph.nodes){
                if(!node.isExecuted() && !node.isRunnable())
                    allNotExec = true;
            }

            //mark all the runnable nodes
            // WRITE YOUR CODE
            for(int i =0; i < ProcessGraph.nodes.size();i++){
                if(ProcessGraph.nodes.get(i).isRunnable()){
                    indices.add(i);
                    ProcessBuilder temp = new ProcessBuilder();
                    temp.command(ProcessGraph.nodes.get(i).getCommand().split(" "));
                    temp.directory(currentDirectory);
                    if(!ProcessGraph.nodes.get(i).getInputFile().getName().equals("stdin"))
                        temp.redirectInput(ProcessGraph.nodes.get(i).getInputFile());
                    if(!ProcessGraph.nodes.get(i).getOutputFile().getName().equals("stdout"))
                        temp.redirectOutput(ProcessGraph.nodes.get(i).getOutputFile());
                    pbs.add(temp);
                }
            }

            //run the node if it is runnable
            // WRITE YOUR CODE
            for(int i =0; i < pbs.size();i++){
                System.out.println("Running process: " + indices.get(i));
                //System.out.println("Inp and op are " + ProcessGraph.nodes.get(i).getInputFile().getName() + ProcessGraph.nodes.get(i).getOutputFile().getName());
                processes.add(pbs.get(i).start());
            }

            //System.out.println("Waiting for processes now");
            for(int i = 0; i < processes.size();i++){
                processes.get(i).waitFor();
            }
            for(Integer i: indices) {
                ProcessGraph.nodes.get(i).setExecuted();
                ProcessGraph.nodes.get(i).setNotRunable();

            }

            if(allNotExec) {
                for (Integer i : indices) {
                    for (int p = 0; p < ProcessGraph.nodes.get(i).getChildren().size(); p++) {
                        boolean parents = true;
                        for (int k = 0; k < ProcessGraph.nodes.get(i).getChildren().get(p).getParents().size(); k++) {
                            if (!ProcessGraph.nodes.get(i).getChildren().get(p).getParents().get(k).isExecuted())
                                parents = false;
                        }
                        if (parents) {
                            ProcessGraph.nodes.get(i).getChildren().get(p).setRunnable();
                            //System.out.println(ProcessGraph.nodes.get(i).getChildren().get(p));
                        }

                    }
                }
            }
        }

        System.out.println("\nAll process finished successfully");
        ProcessGraph.printBasic();
    }

}
