package OSLab1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ProcessManagement {

    private static File currentDirectory = new File(System.getProperty("user.dir"));  //set the working directory
    private static File instructionSet = new File("graph-file");            //set the instructions file
    public static Object lock=new Object();

    public static void main(String[] args) throws InterruptedException, IOException{

        //parse the instruction file and construct a data structure, stored inside ProcessGraph class
        ParseFile.generateGraph(new File(currentDirectory + "/src/OSLab1/"+instructionSet));

        //mark initial runnable
        for(int i =0; i < ProcessGraph.nodes.size(); i++) {
            if (ProcessGraph.nodes.get(i).getParents().isEmpty()) {
                ProcessGraph.nodes.get(i).setRunnable();
            }
        }

        //print initial state of graph
        ProcessGraph.printGraph();

        //check to see if all processes have been executed
        boolean allNotExec = true;

        //MAIN WHILE LOOP
        while(allNotExec) {
            allNotExec = false;
            ArrayList<ProcessBuilder> pbs = new ArrayList<>();  //Processbuilder array list
            ArrayList<Process> processes = new ArrayList<>();   //Process array list
            ArrayList<Integer> indices = new ArrayList<>();     //Indices to store nodeIDs that will be run in current iteration

            //Check if all nodes have been executed
            for(ProcessGraphNode node: ProcessGraph.nodes){
                if(!node.isExecuted() && !node.isRunnable())
                    allNotExec = true;
            }

            //Set up processes that can be run, and run them
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

           //Run the processes one by one
            for(int i =0; i < pbs.size();i++){
                System.out.println("Running process: " + indices.get(i));
                processes.add(pbs.get(i).start());
            }

            //Wait for the processes
            for(int i = 0; i < processes.size();i++){
                processes.get(i).waitFor();
                if (processes.get(i).exitValue() != 0) {
                    Scanner in = new Scanner(processes.get(i).getErrorStream());
                    while (in.hasNext()) System.out.print(in.next());
                }
            }

            //For processes that finished execution, set the appropriate parameters
            for(Integer i: indices) {
                ProcessGraph.nodes.get(i).setExecuted();
                ProcessGraph.nodes.get(i).setNotRunable();

            }


            //Only if all processes haven't been executed, set nodes that can now be run to runnable
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
                        }

                    }
                }
            }



        }

        System.out.println("\nAll process finished successfully");
        ProcessGraph.printBasic();
    }

}
