package OSLab1;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.*;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by aditya on 10/3/2017.
 */
public class Sample {

    public static int edgeCount = 0;

    public static void main(String[] args) {
        // Graph<V, E> where V is the type of the vertices
        // and E is the type of the edges
        //Graph<Integer, String> g = new DirectedSparseMultigraph<Integer, String>();

        Graph<MyNode, MyLink> g = new DirectedSparseMultigraph<MyNode, MyLink>();

        // Create some MyNode objects to use as vertices
        MyNode n1 = new MyNode(1);
        MyNode n2 = new MyNode(2);
        MyNode n3 = new MyNode(3);
        MyNode n4 = new MyNode(4);
        MyNode n5 = new MyNode(5); // note n1-n5 declared elsewhere.

        // Add some directed edges along with the vertices to the graph
        g.addEdge(new MyLink(2.0, 48),n1, n2, EdgeType.DIRECTED); // This method
        g.addEdge(new MyLink(2.0, 48),n2, n3, EdgeType.DIRECTED);
        g.addEdge(new MyLink(3.0, 192), n3, n5, EdgeType.DIRECTED);
        g.addEdge(new MyLink(2.0, 48), n5, n4, EdgeType.DIRECTED); // or we can use
        //g.addEdge(new MyLink(2.0, 48), n4, n2); // In a directed graph the
        //g.addEdge(new MyLink(2.0, 48), n3, n1); // first node is the source
        //g.addEdge(new MyLink(10.0, 48), n2, n5);// and the second the destination



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



        JFrame frame = new JFrame("Simple Graph View");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv, new GridBagConstraints());
        frame.pack();
        frame.setVisible(true);

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
