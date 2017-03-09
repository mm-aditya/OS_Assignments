package OSLab1;

import java.io.File;
import java.util.Scanner;

public class ParseFile {
    //this method generates a ProcessGraph and store in ProcessGraph Class

    public static void generateGraph(File inputFile) {
        try{
            Scanner fileIn=new Scanner(inputFile);
            int index=0;
            while(fileIn.hasNext()){
                String line=fileIn.nextLine();
                String[] quatiles= line.split(":");
                for(String s:quatiles)
                    System.out.print(s+" ");
                System.out.print("\n");
                if (quatiles.length!=4) {
                    System.out.println("Wrong input format!");
                    System.out.println(line);
                    throw new Exception();
                }

                //add this node
                ProcessGraph.addNode(index);
                //handle Children
                //System.out.println(ProcessGraph.nodes.size());
//                if (!quatiles[1].equals("none")){
//                    String[] childrenStringArray=quatiles[1].split(" ");
//                    int[] childrenId=new int[childrenStringArray.length];
//                    for(String a:childrenStringArray)
//                        System.out.print(a + " ");
//                    System.out.print("\n");
//                    for (int i = 0; i < childrenId.length; i++) {
//                        childrenId[i]= Integer.parseInt(childrenStringArray[i]);
//                        ProcessGraph.addNode(childrenId[i]);
//                        //System.out.println("Index and i: "+ index + i);
//                        for(int p = 0; p < ProcessGraph.nodes.size();p++) {
//                            if(ProcessGraph.nodes.get(p).getNodeId() == childrenId[i])
//                                ProcessGraph.nodes.get(index).addChild(ProcessGraph.nodes.get(p));
//                        }
//                    }
//                }
                //setup command
                ProcessGraph.nodes.get(index).setCommand(quatiles[0]);
                //setup input
                ProcessGraph.nodes.get(index).setInputFile(new File(quatiles[2]));
                //setup output
                ProcessGraph.nodes.get(index).setOutputFile(new File(quatiles[3]));
                //setup parent
//                for(int i =0;i<ProcessGraph.nodes.size();i++){
//                    for(int p=0; p < ProcessGraph.nodes.get(i).getChildren().size();p++){
//                        ProcessGraph.nodes.get(i).getChildren().get(p).addParent(ProcessGraph.nodes.get(i));
//                    }
//                }

//                for (ProcessGraphNode node : ProcessGraph.nodes) {
//                    for (ProcessGraphNode childNode : node.getChildren()) {
//                        ProcessGraph.nodes.get(childNode.getNodeId()).addParent(ProcessGraph.nodes.get(node.getNodeId()));
//                    }
//                }

                //mark initial runnable
//                for (ProcessGraphNode node:ProcessGraph.nodes) {
//                    if (node.getParents().isEmpty()){
//                        node.setRunnable();
//                    }
//                }


                index++;
            }

            fileIn=new Scanner(inputFile);
            index = 0;
            while(fileIn.hasNext()) {
                String line = fileIn.nextLine();
                String[] quatiles = line.split(":");
//                for (String s : quatiles)
//                    System.out.print(s + " ");
//                System.out.print("\n");
                if (quatiles.length != 4) {
                    System.out.println("Wrong input format!");
                    System.out.println(line);
                    throw new Exception();
                }

                //handle Children
                //System.out.println(ProcessGraph.nodes.size());
                if (!quatiles[1].equals("none")) {
                    String[] childrenStringArray = quatiles[1].split(" ");
                    int[] childrenId = new int[childrenStringArray.length];
                    for (int i = 0; i < childrenId.length; i++) {
                        childrenId[i] = Integer.parseInt(childrenStringArray[i]);
                        ProcessGraph.nodes.get(index).addChild(ProcessGraph.nodes.get(childrenId[i]));
                    }
                }
                //setup parent
                for (int i = 0; i < ProcessGraph.nodes.size(); i++) {
                    for (int p = 0; p < ProcessGraph.nodes.get(i).getChildren().size(); p++) {
                        ProcessGraph.nodes.get(i).getChildren().get(p).addParent(ProcessGraph.nodes.get(i));
                    }
                }



                index++;
            }
        } catch (Exception e){
           e.printStackTrace();
        }
    }


}
