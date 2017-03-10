package OSLab1;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.*;


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



    // For monitoring
    static class Monitoring extends JFrame implements Runnable {

        static int edgeCount = 0;
        static ArrayList<Integer[]> nodeCenters;


        @Override
        public void run() {
            runGUI();
        }
         private void runGUI(){

            while(true){
                int numofEdges=0;
                ArrayList<Sample.MyNode> nds = new ArrayList<>();
                Graph<Sample.MyNode, Sample.MyLink> g = new DirectedSparseMultigraph<Sample.MyNode, Sample.MyLink>();
                Sample.MyLink stdlink = new Sample.MyLink(1.0,1.0);
                for(int i =0; i < nodes.size();i++){
                    nds.add(new Sample.MyNode(nodes.get(i).getNodeId()));
                }

                for(ProcessGraphNode node: nodes){
                    int indexFrom=-1;
                    int indexto=-1;
                    for(int i = 0;i < nds.size();i++){
                        if(nds.get(i).id == node.getNodeId())
                            indexFrom = i;
                    }
                    for(int i = 0; i < node.getChildren().size(); i ++){
                        for(int k = 0;k < nds.size();k++){
                            if(nds.get(k).id == node.getChildren().get(i).getNodeId())
                                indexto = k;
                        }

                       g.addEdge(stdlink, nds.get(indexFrom),nds.get(indexto),EdgeType.DIRECTED);
                    }
                }

                // The Layout<V, E> is parameterized by the vertex and edge types
                Layout<Integer, String> layout = new CircleLayout(g);
                //Layout<MyNode,MyLink> layout = new DAGLayout<MyNode,MyLink>(g);
                layout.setSize(new Dimension(300,300)); // sets the initial size of the space

                // The BasicVisualizationServer<V,E> is parameterized by the edge types
                BasicVisualizationServer<Integer, String> vv = new BasicVisualizationServer<Integer, String>(layout);
                vv.setPreferredSize(new Dimension(500,500)); //Sets the viewing area size

                vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
                //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
                vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);



                JFrame frame = new JFrame("Monitoring Tool");
                frame.setLayout(new GridBagLayout());
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(vv, new GridBagConstraints());
                frame.pack();
                frame.setVisible(true);

            }


         }

        static class MyNode {
            int id; // good coding practice would have this as private
            public MyNode(int id) {
                this.id = id;
            }
            public String toString() { // Always a good idea for debuging
                return "V"+id; // JUNG2 makes good use of these.
            }
        }

        static class MyLink {
            double capacity; // should be private
            double weight; // should be private for good practice
            int id;

            public MyLink(double weight, double capacity) {
                this.id = edgeCount++; // This is defined in the outer class.
                this.weight = weight;
                this.capacity = capacity;
            }
            public String toString() { // Always good for debugging
                return "E"+id;
            }

        }

    }


    // Class that starts IPC for monitoring interface
//    class IPCServer extends Thread {
//        private ServerSocket server;
//        private Socket socket;
//        private PrintWriter writer;
//
//        public IPCServer() {
//            try {
//                server = new ServerSocket(4321);
//                server.setSoTimeout(5000);
//            } catch (IOException e) {
//                System.out.println("Could not create server for IPC.");
//                System.exit(-1);
//            }
//        }
//
//        public void startServer() {
//            try {
//                socket = server.accept();
//                writer = new PrintWriter(socket.getOutputStream(), true);
//            } catch (IOException e) {
//                System.out.println("Could not start server for IPC.");
//                System.exit(-1);
//            }
//        }
//
//        public void updateIPCListener(ArrayList<ProcessGraphNode> graph) {
//            for (ProcessGraphNode node :
//                    graph) {
//                sendChild(node);
//            }
//
//            for (ProcessGraphNode node :
//                    graph) {
//                sendRelationships(node);
//            }
//        }
//
//        private void sendChild(ProcessGraphNode node) {
//            sendData(node.getNodeData());
//        }
//
//        private void sendRelationships(ProcessGraphNode node) {
//            String data = Integer.toString(node.getNodeId()) + ":";
//            for (ProcessGraphNode child :
//                    node.getChildren()) {
//                data += Integer.toString(child.getNodeId());
//            }
//
//            data += ":";
//
//            for (ProcessGraphNode parent :
//                    node.getParents()) {
//                data += Integer.toString(parent.getNodeId());
//            }
//
//            sendData(data);
//        }
//
//        private void sendData(String data) {
//            writer.println(data);
//        }
//    }

}
