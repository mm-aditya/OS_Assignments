# OS_Assignments

***

#**OS Lab 1**

* Programming Assignment 1
* Author : Aditya Manikashetti - 1001819
* Author : Tenzin Chan - 1001522
* Date: 10/03/2017 

##**Purpose**  
To learn process management. We model processes and their dependencies with a Directed Acyclic Graph and manage and execute
processes by traversing the graph. Independent processes take first priority and child processes wait on them and execute subsequently.
The processes are executed using Java's ProcessBuilder. The program also spawns a thread which displays a graph that updates in realtime according to the status of the nodes.
  
##**Compilation**    

Since this code contains a visualizer you're going to have to include these JAR files in your depencies: <https://nchc.dl.sourceforge.net/project/jung/jung/jung-2.0.1/jung2-2_0_1.zip>  
  
As we are using the IntelliJ IDE for Java, our directory must be set to `/src/OSLab1`. You can change this in your code to whichever directory you wish to use.  
 
Program can be run as a java program with
input file as first command.  Output files will be saved in `\src\OSLab1`, which you can ofcourse change in the program files.  
  
Once the program runs you will notice a Java applet window pop up with the DAG on it. Once all the nodes have turned green it means that the program
has finished traversing the graph. Simply close this window to complete the program execution.

##**Working**  
> There are comments accompanying each class that explains the working in detail. Please refer to these
comments.  

* *ParseFile.java* - Parses the input file into a DAG. The node structure of the DAG is defined in another class.  
* *ProcessGraphNode.java* - Node structure is defined in this class.  
* *ProcessGraph.java* - Contains a static ArrayList which houses the DAG to be traversed. Also contains the visualizer class that generates a new window in which you can view the current states of the nodes.  
* *ProcessManagement.java* - Traverses the graph.  
* *Monitoring.class* - This is situated in ProcessGraph.java. With the help of Swing and JUNG we manage to visualize and update the graph in realtime with the state of the nodes. There isn't 
a detailed explanation for this section as it would require a lot of space to explain the concepts of Swing and JUNG. So we have just commented it lightly.
  
##Visualizer
  
The visualizer is simply a window with the DAG printed on it. The update function for the node colors runs continuously in a while(true)
loop.

**Color Legend**:  
>Red - Not ready  
Yellow - Ready  
Green - Executed  
  
