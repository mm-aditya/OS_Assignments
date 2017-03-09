package OSLab1;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class ParseFile {
    // Generate a ProcessGraph and store in ProcessGraph Class
    public static void generateGraph(File inputFile) {
        try {
            // Generate the nodes based on input file
            Scanner fileIn = new Scanner(inputFile);
            int index = 0;
            while (fileIn.hasNext()) {
                generateNode(fileIn.nextLine(), index);
                index++;
            }

            fileIn = new Scanner(inputFile);
            index = 0;
            // Run the file through again, to handle children and parents
            while (fileIn.hasNext()) {
                generateNodeRelationships(fileIn.nextLine(), index);
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Add a node based on input from file
    private static void generateNode(String line, int index) {
        String[] quatiles = line.split(":");
        for (String s : quatiles)
            System.out.print(s + " ");
        System.out.print("\n");
        if (quatiles.length != 4) {
            System.out.println("Wrong input format!");
            System.out.println(line);
            System.out.println("Halting program.");
            System.exit(-1);
        }

        //add this node
        ProcessGraph.addNode(index);
        ProcessGraphNode justAdded = ProcessGraph.nodes.get(index);
        //setup command
        justAdded.setCommand(quatiles[0]);
        //setup input
        justAdded.setInputFile(new File(quatiles[2]));
        //setup output
        justAdded.setOutputFile(new File(quatiles[3]));
    }

    // Generate the parent-child relationships for each node from input file
    private static void generateNodeRelationships(String line, int index) {
        String[] quatiles = line.split(":");

        // Add children
        if (!quatiles[1].equals("none")) {
            String[] childrenStringArray = quatiles[1].split(" ");
            int[] childrenId = new int[childrenStringArray.length];
            for (int i = 0; i < childrenId.length; i++) {
                if (childrenStringArray[i].matches("\\d+")) childrenId[i] = Integer.parseInt(childrenStringArray[i]);
                else {
                    System.out.println("Invalid children nodes (invalid PID).\nHalting program.");
                    System.exit(-1);
                }
                ProcessGraph.nodes.get(index).addChild(ProcessGraph.nodes.get(childrenId[i]));
            }
        }

        // Setup parent
        for (int i = 0; i < ProcessGraph.nodes.size(); i++) {
            ArrayList<ProcessGraphNode> children = ProcessGraph.nodes.get(i).getChildren();
            for (int p = 0; p < children.size(); p++) {
                children.get(p).addParent(ProcessGraph.nodes.get(i));
            }
        }
    }
}
