# OS_Assignments

* Author : Aditya Manikashetti - 1001819
* Author : Tenzin Chan - 1001522
* Date: 09/03/2017 

***

## OS Assignment 1

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


***

## Network Assignment 1

**Purpose**  
To implement a secure file upload application from a client to an Internet file server. Secure concentrates on two properties: *authenticating the ID of the file server to prevent leakage of data to unauthorized sources* and *protecting the confidentiality during upload*. 
  
**Compilation**  
> In the works.

**Working**  
> Start SecStore first to allow it to listen on port 6789.
> Client will send a nonce (50-digit BigInteger) to the IP address entered.
> SecStore encrypts nonce with its private key and sends it back to Client.
> Client requests for server certificate, and uses public key in certificate to decrypt the encrypted nonce. If it is the same as the
> nonce sent, Client proceeds to encrypt the file with SecStore's public key and sends it. If it is not, connection is closed.
>
> For CP2, once the SecStore is verified, Client will request for a symmetric key. SecStore will send it over and Client will encrypt
> the file with the symmetric key using AES before sending it to SecStore.
