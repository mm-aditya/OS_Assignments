package OSLab1;

import java.io.File;
import java.util.ArrayList;

public class ProcessGraphNode {

    //point to all the parents
    private ArrayList<ProcessGraphNode> parents=new ArrayList<>();
    //point to all the children
    private ArrayList<ProcessGraphNode> children=new ArrayList<>();
    //properties of ProcessGraphNode
    private int nodeId;
    private File inputFile;
    private File outputFile;
    private String command;
    private boolean runnable;
    private boolean executed;


    public ProcessGraphNode(int nodeId ) {
        this.nodeId = nodeId;
        this.runnable=false;
        this.executed=false;
    }

    public void setRunnable() {
        this.runnable = true;
    }

    public void setNotRunable() {this.runnable = false;}

    public void setExecuted() {
        this.executed = true;
    }

    public boolean isRunnable() {
        return runnable;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void addChild(ProcessGraphNode child){
        if (!children.contains(child)){
            children.add(child);
        }
    }

    public void addParent(ProcessGraphNode parent){
        if (!parents.contains(parent)){
            parents.add(parent);
        }
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getCommand() {
        return command;
    }

    public ArrayList<ProcessGraphNode> getParents() {
        return parents;
    }

    public ArrayList<ProcessGraphNode> getChildren() {
        return children;
    }

    public int getNodeId() {
        return nodeId;
    }


    //Custom print function to check all the info regarding single node
    public String toString(){
        String op = "~~~~~~~~~~~~~~~~~~~~~~~~~~~\n";
        String inpFile = "none";
        String opFile = "none";
        if (inputFile!=null)
            inpFile = inputFile.getName();
        if (outputFile!=null)
            inpFile = outputFile.getName();
        op = op + "Node ID: "+nodeId+"\n";
        op = op + String.format("Program name and arguments: %s\nInput File: %s\nOutput File: %s\n",command, inpFile, opFile);
        op = op + "Is runnable: " + isRunnable() + "\nIs executed: "+isExecuted()+"\n";
        op = op + "\nParent nodes: \n";
        for(ProcessGraphNode node: parents)
            op = op + node.getNodeId() + "\n";
        op = op + "\nChild nodes: \n";
        for(ProcessGraphNode node: children)
            op = op + node.getNodeId() + "\n";
        return op;
    }

    public synchronized boolean allParentsExecuted(){
        boolean ans=true;
        for (ProcessGraphNode child : this.getChildren()) {
            if (child.isExecuted()) {
                return false;
            }
        }
        for (ProcessGraphNode parent:this.getParents()) {
            if (!parent.isExecuted())
                ans=false;
        }

        return ans;
    }  //Didn't use this. Sorry.
}
