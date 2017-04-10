# OS_Assignments

***

##OS Lab 1

* Programming Assignment 1
* Author : Aditya Manikashetti - 1001819
* Author : Tenzin Chan - 1001522
* Date: 09/03/2017 

**Purpose**  
To learn process management. We model processes and their dependencies with a Directed Acyclic Graph and manage and execute
processes by traversing the graph. Independent processes take first priority and child processes wait on them and execute subsequently.
The processes are executed using Java's ProcessBuilder.  
  
**Compilation**  
Code is compiled in the `\out\production\OS Assignments\OSLab1` directory. Program can be run as a java program with
input file as first command.  
Output files will be saved in `\src\OSLab1`.

**Working**  
> There are comments accompanying each class that explains the working in detail. Please refer to these
comments.  

*ParseFile.java* - Parses the input file into a DAG. The node structure of the DAG is defined in another class.  
*ProcessGraphNode.java* - Node structure is defined in this class.  
*ProcessGraph.java* - Contains a static ArrayList which houses the DAG to be traversed.  
*ProcessManagement.java* - Traverses the graph. 


