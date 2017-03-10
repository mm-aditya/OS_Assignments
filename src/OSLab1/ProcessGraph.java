package OSLab1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ProcessGraph {
    // A static ArrayList of ProcessGraphNode containing all the node of the graph
    public static ArrayList<ProcessGraphNode> nodes = new ArrayList<>();

    // Add node if not yet created
    public static void addNode(int index) {
        if (index >= nodes.size()) {
            nodes.add(new ProcessGraphNode(index));
        }
    }

    // Print the information of ProcessGraph
    public static void printGraph() {
        System.out.println();
        System.out.println("Graph info:");
        try {
            for (ProcessGraphNode node :
                    nodes) {
                System.out.print("Node " + node.getNodeId() + ": \nParent: ");
                if (node.getParents().isEmpty()) System.out.print("none");
                for (ProcessGraphNode parentnode :
                        node.getParents()) {
                    System.out.print(parentnode.getNodeId() + " ");
                }
                System.out.print(" \nChildren: ");
                if (node.getChildren().isEmpty()) System.out.print("none");
                for (ProcessGraphNode childnode :
                        node.getChildren()) {
                    System.out.print(childnode.getNodeId() + " ");
                }
                System.out.print("\nCommand: " + node.getCommand() + "    ");
                System.out.print("\nInput File: " + node.getInputFile() + "    ");
                System.out.println("\nOutput File: " + node.getOutputFile() + "    ");
                System.out.println("Runnable: " + node.isRunnable());
                System.out.println("Executed: " + node.isExecuted());
                System.out.println("\n");
            }
        } catch (Exception e) {
            System.out.println("Exception !");
            return;
        }
    }

    // Print basic information of ProcessGraph
    public static void printBasic() {
        System.out.println("Basic info:");
        for (ProcessGraphNode node : nodes) {
            System.out.println("Node: " + node.getNodeId() + " Runable: " + node.isRunnable() + " Executed: " + node.isExecuted());
        }
    }

    // Class that starts IPC for monitoring interface
    class IPCServer extends Thread {
        private ServerSocket server;
        private Socket socket;
        private PrintWriter writer;

        public IPCServer() {
            try {
                server = new ServerSocket(4321);
                server.setSoTimeout(5000);
            } catch (IOException e) {
                System.out.println("Could not create server for IPC.");
                System.exit(-1);
            }
        }

        public void startServer() {
            try {
                socket = server.accept();
                writer = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("Could not start server for IPC.");
                System.exit(-1);
            }
        }

        public void updateIPCListener(ArrayList<ProcessGraphNode> graph) {
            for (ProcessGraphNode node :
                    graph) {
                sendChild(node);
            }

            for (ProcessGraphNode node :
                    graph) {
                sendRelationships(node);
            }
        }

        private void sendChild(ProcessGraphNode node) {
            sendData(node.getNodeData());
        }

        private void sendRelationships(ProcessGraphNode node) {
            String data = Integer.toString(node.getNodeId()) + ":";
            for (ProcessGraphNode child :
                    node.getChildren()) {
                data += Integer.toString(child.getNodeId());
            }

            data += ":";

            for (ProcessGraphNode parent :
                    node.getParents()) {
                data += Integer.toString(parent.getNodeId());
            }

            sendData(data);
        }

        private void sendData(String data) {
            writer.println(data);
        }
    }

}
