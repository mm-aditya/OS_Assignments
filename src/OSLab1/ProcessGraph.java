package OSLab1;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import javax.swing.JFrame;
import java.awt.*;


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
        ArrayList<MyNode> nds;
        Graph<Integer, String> g;
        Layout<Integer, String> layout;
        BasicVisualizationServer<Integer, String> vv;
        JFrame frame;

        @Override
        public void run() {
            nds = new ArrayList<>();
            int edgectr = 0;
            g = new DirectedSparseMultigraph<Integer, String>();

            //Making a custom class for storing node info for the graph
            for(int i =0; i < nodes.size();i++){
                nds.add(new MyNode(nodes.get(i).getNodeId()));
            }

            //Loop to generate the graph by adding edges and vertices
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
                    g.addEdge( new String("edge "+ edgectr++),nds.get(indexFrom).id,nds.get(indexto).id,EdgeType.DIRECTED);
                }
            }

            // The Layout<V, E> is parameterized by the vertex and edge types
            layout = new CircleLayout(g);
            //Layout<MyNode,MyLink> layout = new DAGLayout<MyNode,MyLink>(g);
            layout.setSize(new Dimension(300,300)); // sets the initial size of the space
            // The BasicVisualizationServer<V,E> is parameterized by the edge types
            vv = new BasicVisualizationServer<Integer, String>(layout);
            vv.setPreferredSize(new Dimension(500,500)); //Sets the viewing area size
            vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
            //vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
            vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

            //Drawing the graph for the first time
            frame = new JFrame("Monitoring Tool");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(vv);
            frame.pack();
            frame.setVisible(true);

            runGUI();
        }
         private void runGUI(){

            while(true){

                //This loops infinitely, updating the graph colors as the program progresses
                Transformer<Integer,Paint> vertexColor = new Transformer<Integer,Paint>(){
                    public Paint transform(Integer i) {
                        if(nodeState(i)==0)
                            return Color.RED;       // RED for processes not ready to execute
                        else if(nodeState(i)==1)
                            return Color.YELLOW;    // YELLOW for processes ready to be executed
                        else if(nodeState(i) ==2)
                            return Color.GREEN;     // GREEN for processes that finish executing
                        return Color.RED;
                    }

                };

                vv.getRenderContext().setVertexFillPaintTransformer(vertexColor); // To paint the colors
                frame.validate();
                frame.repaint();
            }


         }

        // Method that returns an integer value representing the state of the node
        private int nodeState(int ip){
             int index = -1;
             for(int i =0; i < nds.size();i++)
                 if(nds.get(i).id == ip)
                     index = i;
             if(nodes.get(index).isRunnable() == false && nodes.get(index).isExecuted() == false)
                 return 0;
             else if (nodes.get(index).isRunnable()==true)
                 return 1;
             else if (nodes.get(index).isExecuted()==true)
                 return 2;
             return 1;
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
