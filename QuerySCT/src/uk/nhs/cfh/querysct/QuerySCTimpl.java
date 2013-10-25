/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.cfh.querysct;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;



/**
 *
 * @author yoga
 */
@ServiceProvider(service = QuerySCT.class)
public class QuerySCTimpl implements QuerySCT {
    private static final String DB_PATH = System.getProperty("user.home")+"/neo4j-sct-store"; 
//    private static final String DB_PATH = "D:/neo4j-sct-store";
//    private static final File File_Store = new File("D:/neo4j-sct-store");
    private static GraphDatabaseService graphDB;
    private static final String SCTID_KEY = "sct_id";
    private static final String FSN_KEY = "fsn";
    private static final String CONSTATUS_KEY = "concept_status";
    private static Index<Node> nodeIndex;
//    private static String sctid;
//    private static Charset charset = Charset.forName("UTF-8");
//    private static java.nio.file.Path daisyfile = Paths.get("daisy.txt");
//    private static DataOutputStream writer;
    private InputOutput io = IOProvider.getDefault().getIO("Query Status", false);
    private Collection<Node> subNodes = new ArrayList<Node>();

 
    private static enum RelTypes implements RelationshipType
    {
        ISA,
        iISA,
        SameAs,
        MovedFrom,
        MayBeA,
        ReplacedBy,
        WasA
    }
    
     @Override
    public void startGraphDB() {
        if(graphDB != null){
            graphDB.shutdown();
        }
        graphDB = new EmbeddedGraphDatabase(DB_PATH);
        nodeIndex = graphDB.index().forNodes("nodes");
        registerShutdownHook( graphDB );
    }
    
        private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
        
    @Override
    public Index<Node> getIndex(){
        return nodeIndex;
    }
        
    @Override
    public GraphDatabaseService getGraphDB(){
        return graphDB;
    }
        
    @Override
    public String getFSNbyID(String sctid) {
         String fsn = null;
         Node findConcept = nodeIndex.get(SCTID_KEY, sctid).getSingle();
         if(findConcept != null){
            fsn = findConcept.getProperty(FSN_KEY).toString();
            io.getOut().println("Concept for Query: " + sctid + " | " + fsn);
            io.getOut().close();
            return fsn;
         }
         else {
            String error = "Node does not exist." ;
//            System.out.println(error);
            io.getOut().println(error);
            return fsn;
         }
    }
    
    
// Traverser list:
    
    private static Traverser getDirectSuperTraverser( final Node node )
        {
           TraversalDescription td = Traversal.description()
                  .breadthFirst()
                  .relationships(RelTypes.ISA, Direction.OUTGOING)
                  .evaluator(Evaluators.includingDepths(1, 1));
           return td.traverse(node);

        }
    
    private static Traverser getDirectSubTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ISA, Direction.INCOMING)
                .evaluator(Evaluators.includingDepths(1, 1));
        return td.traverse(node);
        
    }
       
    private static Traverser getISATraverser(final Node node){
         TraversalDescription td = Traversal.description()
                 .breadthFirst()
                 .relationships(RelTypes.ISA, Direction.INCOMING)
                 .evaluator(Evaluators.all());
         return td.traverse(node);
     }
    
    private static Traverser getISAExcludeStartTraverser(final Node node){
         TraversalDescription td = Traversal.description()
                 .breadthFirst()
                 .relationships(RelTypes.ISA, Direction.INCOMING)
                 .evaluator(Evaluators.excludeStartPosition());
         return td.traverse(node);
     } 
    
    
    private static Traverser getISAiTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ISA, Direction.INCOMING)
                .relationships(RelTypes.iISA, Direction.INCOMING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getAllHistoryTraverserI(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.SameAs, Direction.INCOMING)
                .relationships(RelTypes.MovedFrom, Direction.INCOMING)                
                .relationships(RelTypes.MayBeA, Direction.INCOMING)
                .relationships(RelTypes.ReplacedBy, Direction.INCOMING)
                .relationships(RelTypes.WasA, Direction.INCOMING);
        return td.traverse(node);

    }

    private static Traverser getAllHistoryTraverserO(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.SameAs, Direction.OUTGOING)
                .relationships(RelTypes.MovedFrom, Direction.OUTGOING)                
                .relationships(RelTypes.MayBeA, Direction.OUTGOING)
                .relationships(RelTypes.ReplacedBy, Direction.OUTGOING)
                .relationships(RelTypes.WasA, Direction.OUTGOING);
        return td.traverse(node);

    }
    
    
    private static Traverser getWasATraverserI(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.WasA, Direction.INCOMING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
   
    private static Traverser getWasATraverserO(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.WasA, Direction.OUTGOING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }    
    
    
    private static Traverser getReplacedByTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ReplacedBy, Direction.INCOMING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getReplacedByOutTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ReplacedBy, Direction.OUTGOING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getMayBeReplacedTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.MayBeA, Direction.INCOMING)
                .relationships(RelTypes.ReplacedBy, Direction.INCOMING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getMayBeReplacedOutTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.MayBeA, Direction.OUTGOING)
                .relationships(RelTypes.ReplacedBy, Direction.OUTGOING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getMayBeTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.MayBeA, Direction.INCOMING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getMayBeOutTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.MayBeA, Direction.OUTGOING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getSameAsTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.SameAs, Direction.INCOMING)
                .relationships(RelTypes.MovedFrom, Direction.INCOMING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getSameAsOutTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.SameAs, Direction.OUTGOING)
                .relationships(RelTypes.MovedFrom, Direction.OUTGOING)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
     
    private static Traverser getSameAsNonselfTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.SameAs, Direction.INCOMING)
                .relationships(RelTypes.MovedFrom, Direction.INCOMING)
                .evaluator(Evaluators.excludeStartPosition());
        return td.traverse(node);
    }
    
    private static Traverser getSameAsBothDirectTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.SameAs, Direction.BOTH)
                .relationships(RelTypes.MovedFrom, Direction.BOTH)
                .evaluator(Evaluators.excludeStartPosition());
        return td.traverse(node);
    }
    
    private static Traverser getSameAsSubTraverser(final Node node){
         TraversalDescription td = Traversal.description()
                 .breadthFirst()
                 .relationships(RelTypes.ISA, Direction.INCOMING)
                 .relationships(RelTypes.SameAs, Direction.INCOMING)
                 .relationships(RelTypes.MovedFrom, Direction.INCOMING)                 
                 .evaluator(Evaluators.all());
         return td.traverse(node);
     }
    
    private static Traverser getSameAsSubiTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ISA, Direction.INCOMING)
                .relationships(RelTypes.iISA, Direction.INCOMING)
                .relationships(RelTypes.SameAs, Direction.BOTH)
                .relationships(RelTypes.MovedFrom, Direction.BOTH)
                .evaluator(Evaluators.all());
        return td.traverse(node);
    }
    
    private static Traverser getSameAsSubnoStartiTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ISA, Direction.INCOMING)
                .relationships(RelTypes.iISA, Direction.INCOMING)
                .relationships(RelTypes.SameAs, Direction.INCOMING)
                .relationships(RelTypes.MovedFrom, Direction.INCOMING)
                .evaluator(Evaluators.excludeStartPosition());
        return td.traverse(node);
    }
    
    private static Traverser getMayBeDaisyTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ISA, Direction.INCOMING)
                .relationships(RelTypes.iISA, Direction.INCOMING)
                .relationships(RelTypes.SameAs, Direction.BOTH)
                .relationships(RelTypes.MovedFrom, Direction.BOTH)
                .relationships(RelTypes.MayBeA, Direction.INCOMING)
                .relationships(RelTypes.WasA, Direction.INCOMING)
                .relationships(RelTypes.ReplacedBy, Direction.INCOMING)
                .evaluator(Evaluators.all());               
             return td.traverse(node);
    }
    
    private static Traverser getMayBeNoStartTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.ISA, Direction.INCOMING)
                .relationships(RelTypes.iISA, Direction.INCOMING)
                .relationships(RelTypes.SameAs, Direction.INCOMING)
                .relationships(RelTypes.MovedFrom, Direction.INCOMING)
                .relationships(RelTypes.MayBeA, Direction.INCOMING)
                .relationships(RelTypes.WasA, Direction.INCOMING)
                .relationships(RelTypes.ReplacedBy, Direction.OUTGOING)
                .evaluator(Evaluators.excludeStartPosition());               
             return td.traverse(node);
    }
    
    private static Traverser getMayBeNonselfTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.MayBeA, Direction.INCOMING)
                .relationships(RelTypes.WasA, Direction.INCOMING)
                .relationships(RelTypes.ReplacedBy, Direction.OUTGOING)
                .evaluator(Evaluators.excludeStartPosition());
        return td.traverse(node);
    }
    
    

    

    @Override
    public void findDirectSuperNodes(String sctid) {
        int n = 0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        Traverser myTraverser = getDirectSuperTraverser(startNode);
//        System.out.println("========== direct super-concepts =============");
        for (Path subPath : myTraverser){                  
//                 System.out.println( 
            io.getOut().println("Direct super-concepts: "); 
            io.getOut().println(
                     subPath.endNode().getProperty(SCTID_KEY)+ " | "
                       + subPath.endNode().getProperty(FSN_KEY));
                  n = n+1;
              }
//              System.out.println("Totoal super-concept number: " + n);
            io.getOut().println("Total number of super-concept :" + n);
            io.getOut().close();
    }

    @Override
    public void findSuperNodes(String sctid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void findDirectSubNodes(String sctid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   
    @Override
    public Collection<Node> getDirectSubNodes(Node sctnode) {
        int n=0;
        Node startNode = sctnode;
        Traverser directSubTrav = getDirectSubTraverser(startNode);
        for(Path subPath : directSubTrav){
          subNodes.add(subPath.endNode());
          n = n+1;
        }
//        System.out.println("Number of subnodes: " + n);
        for(Node node : subNodes){
//            System.out.println(node.getProperty(SCTID_KEY).toString());
        }
        return subNodes;        
    }
    
    @Override
    public void findAllSubNodes(String sctid) {
              int n = 0;
              Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
              Traverser myTraverser = getISAExcludeStartTraverser(startNode);
              String fn = "Descendents_" + startNode.getProperty(SCTID_KEY)+".txt";
              File descendenttable = new File(fn);
//              try(BufferedWriter writer = Files.newBufferedWriter(daisyfile,charset, StandardOpenOption.CREATE)){
              try{
                BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
                writer.write("SubConcept" + "\t" + "SuperConcept" + "\n");
                for (Path subPath : myTraverser){                  
 //                 System.out.println(
 //                         "Has subconcepts at depth: " + subPath.length()+ " "
                    String conceptid = subPath.endNode().getProperty(SCTID_KEY).toString();                          
                    String conceptfsn = subPath.endNode().getProperty(FSN_KEY).toString();
//                    String daisytc = subPath.endNode().getProperty(SCTID_KEY).toString()+ " | "
//                                    + subPath.endNode().getProperty(FSN_KEY);
//                    System.out.println(sctid + " has descendent: " + conceptid + " | " + conceptfsn);
                         
//                   try{                           
//                         writer.write(conceptid + "  " + conceptfsn + "\n");
                    writer.write(conceptid + "\t" + sctid + "\n");
                       n = n+1;
                    }
                writer.close();
                }catch (IOException x) {
//                System.err.format("IOException: %s%n", x);
                  io.getErr().println("IOException: %s%n" + x);
                  io.getErr().close();
                }
                
//               }catch (IOException x){
//                  System.err.println(x.getStackTrace());
              
            io.select();
            io.getOut().println(" =============================== ");
            io.getOut().println("Generated file for descendent concepts at: ");
            io.getOut().println(descendenttable.getAbsolutePath());
            io.getOut().println("Totoal number of descendents: " + n);
            io.getOut().println(" =============================== ");
            io.getOut().println("");
            io.getOut().close();
    }
    
    
  
        @Override
    public void findAllSubandInactiveNodes(String sctid) {
        
                  int n = 0;
              Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
              Traverser myTraverser = getISAiTraverser(startNode);
              String fn = "Descendents_a+i_" + startNode.getProperty(SCTID_KEY)+".txt";
              File descendenttable = new File(fn);

//              io.getOut().println("=========== subconcepts =========== ");
            
//              try(BufferedWriter writer = Files.newBufferedWriter(daisyfile,charset, StandardOpenOption.CREATE)){
              try{
                BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
                writer.write("Concept" + "\t" + "Descendents" + "\n");
                for (Path subPath : myTraverser){                  
 //                 System.out.println(
 //                         "Has subconcepts at depth: " + subPath.length()+ " "
                         String conceptid = subPath.endNode().getProperty(SCTID_KEY).toString();                          
//                         String conceptfsn = subPath.endNode().getProperty(FSN_KEY).toString();
                      String descendentsai = subPath.endNode().getProperty(SCTID_KEY).toString()+ " | "
                      + subPath.endNode().getProperty(FSN_KEY);
//                    System.out.println(sctid + " has descendents a + i : " + descendentsai);
                    writer.write(sctid + "\t" + conceptid + "\n");
                       n = n+1;
                    }
                writer.close();
                }catch (IOException x) {
                System.err.format("IOException: %s%n", x);
                }
           
            io.select();
            io.getOut().println(" =============================== ");
            io.getOut().println("Generated file for descendent concepts at: ");
            io.getOut().println(descendenttable.getAbsolutePath());
            io.getOut().println("Totoal number of descendents a + i : " + n);
            io.getOut().println(" =============================== ");
            io.getOut().println("");
            io.getOut().close();        
            
    }
   
    
    @Override
    public void findSameAsNodes(String sctid) {
        int n = 0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        String fn = "SAME_" + sctid +".txt";
        File sameastable = new File(fn);
        Traverser myTraverser = getSameAsTraverser(startNode); 

        try{            
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept"+ "\n");
//            System.out.print("InactiveConcept" + "\t" + "ActiveConcept"+ "\n");            
            for(Path subPath : myTraverser){
                String sameas = subPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
                                + sctid + "\n";
                writer.write(sameas);

//                System.out.print(sameas);
                n = n+1;
            }
            writer.close();
        }catch (IOException e){
            io.getErr().println("IOException in findSameAsNode method :" + e.getMessage());
            io.getErr().close();
        }
            io.getOut().println(" ============================= ");
            io.getOut().println("Generated file for SAME concepts at: ");
            io.getOut().println(sameastable.getAbsolutePath());
            io.getOut().println("Total number of SAME concepts: " + n);
            io.getOut().println(" ============================= ");
            io.getOut().println("");
            io.getOut().close();
       
    }
    
        @Override
    public void findSameAsForAllNodes(String sctid) {
        int n =0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        int concept_status = Integer.parseInt(startNode.getProperty(CONSTATUS_KEY).toString());
//        System.out.println("start node status: " + concept_status);
        String fn = "Substitutation_Table_SameAs_ReplacedBy_" + sctid + ".txt";
        File substitfile = new File(fn);
        Traverser isaTraverser = getISAiTraverser(startNode);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept" + "\n");
//            System.out.print("InactiveConcept_ID" + "\t" + "InactiveConcept_FSN" + "\t" + "ActiveConcept_ID" + "\t" + "ActiveConcept_FSN" + "\n");
            for(Path isaPath : isaTraverser){
                Node myStartNode = isaPath.endNode();
                if(concept_status==0 || concept_status==11){
                    Traverser myTraverser = getSameAsTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String inactiveid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());
                        String activeid = myStartNode.getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());                        
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");
                        n = n+1; 
                        }
                    }
                }
                else {
                    Traverser myTraverser = getSameAsOutTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String activeid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String inactiveid = myStartNode.getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");

    //                    System.out.print(myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
    //                            + myPath.endNode().getProperty(FSN_KEY).toString() + "\t"
    //                            + myStartNode.getProperty(SCTID_KEY).toString() + "\t" 
    //                            + myStartNode.getProperty(FSN_KEY).toString() + "\n");

                        n = n+1;
                    }
               }
                }
            }
           writer.close(); 
        }catch(IOException e){
            io.getErr().println(e.getStackTrace().toString());
            io.getErr().close();
        }
        io.select();
        io.getOut().println(" ========================== ");
        io.getOut().println("Generated substitution table for SameAs and ReplacedBy relationships at :");        
        io.getOut().println(substitfile.getAbsolutePath());
        io.getOut().println("Total number of substitutions: " + n);
        io.getOut().println(" ========================== ");
        io.getOut().println("");
        io.getOut().close();        
        
    }
        
        
        
    @Override
    public void findMayBeForAllNodes(String sctid) {
        int n =0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        int concept_status = Integer.parseInt(startNode.getProperty(CONSTATUS_KEY).toString());
//        System.out.println("start node status: " + concept_status);
        String fn = "Substitutation_Table_MayBeA_" + sctid + ".txt";
        File substitfile = new File(fn);
        Traverser isaTraverser = getISAiTraverser(startNode);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept" + "\n");
//            System.out.print("InactiveConcept_ID" + "\t" + "InactiveConcept_FSN" + "\t" + "ActiveConcept_ID" + "\t" + "ActiveConcept_FSN" + "\n");
            for(Path isaPath : isaTraverser){
                Node myStartNode = isaPath.endNode();
                if(concept_status==0 || concept_status==11){
                    Traverser myTraverser = getMayBeTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String inactiveid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());
                        String activeid = myStartNode.getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());                        
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");
                        n = n+1; 
                        }
                    }
                }
                else {
                    Traverser myTraverser = getMayBeOutTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String activeid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String inactiveid = myStartNode.getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");

    //                    System.out.print(myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
    //                            + myPath.endNode().getProperty(FSN_KEY).toString() + "\t"
    //                            + myStartNode.getProperty(SCTID_KEY).toString() + "\t" 
    //                            + myStartNode.getProperty(FSN_KEY).toString() + "\n");

                        n = n+1;
                    }
               }
                }
            }
           writer.close(); 
        }catch(IOException e){
            io.getErr().println(e.getStackTrace().toString());
            io.getErr().close();
        }
        io.select();
        io.getOut().println(" ========================== ");
        io.getOut().println("Generated substitution table for MayBeA relationship at :");        
        io.getOut().println(substitfile.getAbsolutePath());
        io.getOut().println("Total number of substitutions: " + n);
        io.getOut().println(" ========================== ");
        io.getOut().println("");
        io.getOut().close();        
        
    }
    
    @Override
    public void findReplacedByForAllNodes(String sctid) {
        int n =0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        int concept_status = Integer.parseInt(startNode.getProperty(CONSTATUS_KEY).toString());
//        System.out.println("start node status: " + concept_status);
        String fn = "Substitutation_Table_ReplacedBy_" + sctid + ".txt";
        File substitfile = new File(fn);
        Traverser isaTraverser = getISAiTraverser(startNode);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept" + "\n");
//            System.out.print("InactiveConcept_ID" + "\t" + "InactiveConcept_FSN" + "\t" + "ActiveConcept_ID" + "\t" + "ActiveConcept_FSN" + "\n");
            for(Path isaPath : isaTraverser){
                Node myStartNode = isaPath.endNode();
                if(concept_status==0 || concept_status==11){
                    Traverser myTraverser = getReplacedByTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String inactiveid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());
                        String activeid = myStartNode.getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());                        
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");
                        n = n+1; 
                        }
                    }
                }
                else {
                    Traverser myTraverser = getReplacedByOutTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String activeid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String inactiveid = myStartNode.getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");

    //                    System.out.print(myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
    //                            + myPath.endNode().getProperty(FSN_KEY).toString() + "\t"
    //                            + myStartNode.getProperty(SCTID_KEY).toString() + "\t" 
    //                            + myStartNode.getProperty(FSN_KEY).toString() + "\n");

                        n = n+1;
                    }
               }
                }
            }
           writer.close(); 
        }catch(IOException e){
            io.getErr().println(e.getStackTrace().toString());
            io.getErr().close();
        }
        io.select();
        io.getOut().println(" ========================== ");
        io.getOut().println("Generated substitution table for ReplacedBy relationship at :");        
        io.getOut().println(substitfile.getAbsolutePath());
        io.getOut().println("Total number of substitutions: " + n);
        io.getOut().println(" ========================== ");
        io.getOut().println("");
        io.getOut().close();         
    }

    @Override
    public void findWasAForAllNodes(String sctid) {
        int n =0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        int concept_status = Integer.parseInt(startNode.getProperty(CONSTATUS_KEY).toString());
//        System.out.println("start node status: " + concept_status);
        String fn = "Substitutation_Table_WasA_" + sctid + ".txt";
        File substitfile = new File(fn);
        Traverser isaTraverser = getISAiTraverser(startNode);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept" + "\n");
//            System.out.print("InactiveConcept_ID" + "\t" + "InactiveConcept_FSN" + "\t" + "ActiveConcept_ID" + "\t" + "ActiveConcept_FSN" + "\n");
            for(Path isaPath : isaTraverser){
                Node myStartNode = isaPath.endNode();
                if(concept_status==0 || concept_status==11){
                    Traverser myTraverser = getWasATraverserI(myStartNode);
                    for(Path myPath : myTraverser){
                        String inactiveid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String activeid = myStartNode.getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());                         
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");
                        n = n+1; 
                        }
                    }
                }
                else {
                    Traverser myTraverser = getWasATraverserO(myStartNode);
                    for(Path myPath : myTraverser){
                        String activeid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String inactiveid = myStartNode.getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");

    //                    System.out.print(myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
    //                            + myPath.endNode().getProperty(FSN_KEY).toString() + "\t"
    //                            + myStartNode.getProperty(SCTID_KEY).toString() + "\t" 
    //                            + myStartNode.getProperty(FSN_KEY).toString() + "\n");

                        n = n+1;
                    }
               }
                }
            }
           writer.close(); 
        }catch(IOException e){
            io.getErr().println(e.getStackTrace().toString());
            io.getErr().close();
        }
        io.select();
        io.getOut().println(" ========================== ");
        io.getOut().println("Generated substitution table for WasA relationship at :");        
        io.getOut().println(substitfile.getAbsolutePath());
        io.getOut().println("Total number of substitutes: " + n);
        io.getOut().println(" ========================== ");
        io.getOut().println("");
        io.getOut().close();  
    }    

    @Override
    public void findMayBeReplacedForAllNodes(String sctid) {
        int n =0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        int concept_status = Integer.parseInt(startNode.getProperty(CONSTATUS_KEY).toString());
//        System.out.println("start node status: " + concept_status);
        String fn = "Substitutation_Table_MayBeA_ReplacedBy_" + sctid + ".txt";
        File substitfile = new File(fn);
        Traverser isaTraverser = getISAiTraverser(startNode);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept" + "\n");
//            System.out.print("InactiveConcept_ID" + "\t" + "InactiveConcept_FSN" + "\t" + "ActiveConcept_ID" + "\t" + "ActiveConcept_FSN" + "\n");
            for(Path isaPath : isaTraverser){
                Node myStartNode = isaPath.endNode();
                if(concept_status==0 || concept_status==11){
                    Traverser myTraverser = getMayBeReplacedTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String inactiveid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());
                        String activeid = myStartNode.getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());                        
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");
                        n = n+1; 
                        }
                    }
                }
                else {
                    Traverser myTraverser = getMayBeReplacedOutTraverser(myStartNode);
                    for(Path myPath : myTraverser){
                        String activeid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String inactiveid = myStartNode.getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");

    //                    System.out.print(myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
    //                            + myPath.endNode().getProperty(FSN_KEY).toString() + "\t"
    //                            + myStartNode.getProperty(SCTID_KEY).toString() + "\t" 
    //                            + myStartNode.getProperty(FSN_KEY).toString() + "\n");

                        n = n+1;
                    }
               }
                }
            }
           writer.close(); 
        }catch(IOException e){
            io.getErr().println(e.getStackTrace().toString());
            io.getErr().close();
        }
        io.select();
        io.getOut().println(" ========================== ");
        io.getOut().println("Generated substitution table for MayBeA and ReplacedBy relationships at :");        
        io.getOut().println(substitfile.getAbsolutePath());
        io.getOut().println("Total number of substitutions: " + n);
        io.getOut().println(" ========================== ");
        io.getOut().println("");
        io.getOut().close();         
        
    }

    @Override
    public void findAllHistoryForAllNodes(String sctid) {
        int n =0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        int concept_status = Integer.parseInt(startNode.getProperty(CONSTATUS_KEY).toString());
//        System.out.println("start node status: " + concept_status);
        String fn = "Substitutation_Table_AllHistorical_Relationships_" + sctid + ".txt";
        File substitfile = new File(fn);
        Traverser isaTraverser = getISAiTraverser(startNode);
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("InactiveConcept" + "\t" + "ActiveConcept" + "\n");
//            System.out.print("InactiveConcept_ID" + "\t" + "InactiveConcept_FSN" + "\t" + "ActiveConcept_ID" + "\t" + "ActiveConcept_FSN" + "\n");
            for(Path isaPath : isaTraverser){
                Node myStartNode = isaPath.endNode();
                if(concept_status==0 || concept_status==11){
                    Traverser myTraverser = getAllHistoryTraverserI(myStartNode);
                    for(Path myPath : myTraverser){
                        String inactiveid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());
                        String activeid = myStartNode.getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());                        
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");
                        n = n+1; 
                        }
                    }
                }
                else {
                    Traverser myTraverser = getAllHistoryTraverserO(myStartNode);
                    for(Path myPath : myTraverser){
                        String activeid = myPath.endNode().getProperty(SCTID_KEY).toString();
                        int active = Integer.parseInt(myPath.endNode().getProperty(CONSTATUS_KEY).toString());                        
                        String inactiveid = myStartNode.getProperty(SCTID_KEY).toString();
                        int inactive = Integer.parseInt(myStartNode.getProperty(CONSTATUS_KEY).toString());
                        if (!inactiveid.equals(activeid)&& (active == 0||active ==11) && inactive !=0 && inactive!=11 ){
                        writer.write(inactiveid + "\t" + activeid + "\n");

    //                    System.out.print(myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
    //                            + myPath.endNode().getProperty(FSN_KEY).toString() + "\t"
    //                            + myStartNode.getProperty(SCTID_KEY).toString() + "\t" 
    //                            + myStartNode.getProperty(FSN_KEY).toString() + "\n");

                        n = n+1;
                    }
               }
                }
            }
           writer.close(); 
        }catch(IOException e){
            io.getErr().println(e.getStackTrace().toString());
            io.getErr().close();
        }
        io.select();
        io.getOut().println(" ========================== ");
        io.getOut().println("Generated substitution table for all historical relationships at :");        
        io.getOut().println(substitfile.getAbsolutePath());
        io.getOut().println("Total number of substitutions: " + n);
        io.getOut().println(" ========================== ");
        io.getOut().println("");
        io.getOut().close();         
    }    
    
    
    
    @Override
    public void findSameAsAllSubNodes(String sctid) {
                int n = 0;
//         long sctid = Long.parseLong(id);
         Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
         String fn = "Same_Descendents_" + sctid + ".txt";
         
         Traverser myTraverser = getSameAsSubTraverser(startNode);
         
//           try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(fn), charset, StandardOpenOption.CREATE)){
         try{
             File sameasdescent = new File(fn);
             BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
             String firstline = "SubConcept"+ "\t" +"SuperConcept" + "\n"; 
             writer.write(firstline);
//             System.out.print(firstline);
            
            for(Path subPath : myTraverser){
//             System.out.println("At depth: " + subPath.length()+ " "
//                     + subPath.lastRelationship().getType() + " "
//                     + subPath.endNode().getProperty(SCTID_KEY)+ " | "
//                     + subPath.endNode().getProperty(FSN_KEY));
//                   System.out.println( 
                     String samedesoutput = subPath.endNode().getProperty(SCTID_KEY).toString() + "\t"                     
                     + sctid +"\n";                     
                     writer.write(samedesoutput);
//                     System.out.print(samedesoutput);
                  n = n+1;
                 }
                writer.close();
                io.getOut().println(" ======================== ");
                io.getOut().println("Generated file for SAME and Descendent concepts at :");
                io.getOut().println(sameasdescent.getAbsolutePath());         
                io.getOut().println("Total number of SAME and/or Descendent concepts: " + n);
                io.getOut().println(" ======================== ");
                io.getOut().println("");
                io.getOut().close();
                }catch (IOException e) {
                       io.getErr().println(e.getStackTrace().toString());
                  }             
    }

         
        

    @Override
    public void findSameAsDirectSubNodes(String sctid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    @Override
    public void findISATCNodes(String sctid){
         int n = 0;
         Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
         String fn = "TC_table_" + sctid + ".txt";
         File standardTCtable = new File(fn);
         Traverser TcTraverser = getISATraverser(startNode);
          try{
             
             BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
             writer.write("SubConcept" + "\t"+ "SuperConcept" + "\n");
//             System.out.println("Stanard TC Table:");
//             System.out.print("SubConcept" + "\t"+ "SuperConcept" + "\n");
             for (Path subPath : TcTraverser){
             Node myStartNode = subPath.endNode();
             Traverser myTraverser = getISATraverser(myStartNode);           
             
             for(Path myPath : myTraverser){
                 String tc = myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
                         + myStartNode.getProperty(SCTID_KEY).toString() + "\n";
                 writer.write(tc);
//                 System.out.print(tc);
                 n = n+1;
             }
             }
              writer.close();        
             }catch (IOException e){
                 io.getErr().println(e.getStackTrace().toString());
                 io.getErr().close();
             }
             io.select();
             io.getOut().println(" ============================ ");
             io.getOut().println("Generated standard TC table at:");
             io.getOut().println(standardTCtable.getAbsolutePath());
             io.getOut().println("Total number of row in standard TC: " + n);
             io.getOut().println(" ============================ ");
             io.getOut().println("");
             io.getOut().close();
                
     }
          
    @Override
    public void findSameAsISATCNodes(String sctid) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*
        int n = 0;
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        String fn = "SameAsDaisy_" + sctid + ".txt";
        Traverser samesubTraverser = getSameAsSubTraverser(startNode);
        try{
            File daisyfile = new File(fn);
            BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
            writer.write("subconcept" + "\t" + "superconcept" + "\n");
//            System.out.print("subconcept" + "\t" + "superconcept" + "\n");
            
            for(Path subPath : samesubTraverser){
                Node myStartNode = subPath.endNode();
                Traverser myTraverser = getSameAsTraverser(myStartNode);
                for(Path mySubPath : myTraverser){
                    String sameasdaisy = mySubPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
                            + myStartNode.getProperty(SCTID_KEY).toString() + "\n";                    
                    writer.write(sameasdaisy);
//                    System.out.print(sameasdaisy);
                 n = n+1;
                }
               
            }
            
            writer.close();
            
             
 
            io.getOut().println(" ========================== ");
            io.getOut().println("Generated file for role inclusion transitive clousre table at :");
            io.getOut().println(daisyfile.getAbsolutePath());
            io.getOut().println("Total number of rows: " + n);
            io.getOut().println(" ========================== ");
            io.getOut().println("");
            io.getOut().close();
                     
            
        }catch (IOException e){
            io.getErr().println(e.getStackTrace().toString());
        }
  */
    }
    
    @Override
    public void findDaisyNodes(String sctid) {
         int n = 0;
         Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
         String fn = "TC_SAME_extension_" + sctid + ".txt";
         File standardTCtable = new File(fn);
         Traverser TcTraverser = getSameAsSubiTraverser(startNode);
          try{
             
             BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
             writer.write("SubConcept" + "\t"+ "SuperConcept" + "\n");
//             System.out.println("TC with SameAs extension table :");
//             System.out.print("SubConcept" + "\t"+ "SuperConcept" + "\n");
             for (Path subPath : TcTraverser){
             Node myStartNode = subPath.endNode();
             Traverser myTraverser = getSameAsSubiTraverser(myStartNode);  
//             Traverser sameTraverser = getSameAsNonselfTraverser(myStartNode);
             
                for(Path myPath : myTraverser){
                    String tc = myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
                         + myStartNode.getProperty(SCTID_KEY).toString() + "\n";
                    writer.write(tc);
//                    System.out.print(tc);
                    n = n+1;
                }
 
//                for(Path samePath: sameTraverser){
//                    String same = myStartNode.getProperty(SCTID_KEY).toString() + "\t"
//                                  + samePath.endNode().getProperty(SCTID_KEY).toString() + "\n";
//                    writer.write(same);
//                    System.out.print(same);
//                    n = n+1;
//                 }

                            
             
//             Traverser samebiTraverser = getSameAsNonselfTraverser(myStartNode);
//             for(Path samePath : samebiTraverser){
//                String sameas = samePath.endNode().getProperty(SCTID_KEY).toString() + "\t"
//                         + myStartNode.getProperty(SCTID_KEY).toString() + "\n";  
//                writer.write(sameas);
//                System.out.print(sameas);
//                n = n + 1;
//             }
             
             }
              writer.close();        
             }catch (IOException e){
                 io.getErr().println(e.getStackTrace().toString());
                 io.getErr().close();
             }
             io.select();
             io.getOut().println(" ============================ ");
             io.getOut().println("Generated TC with SAME extension table at:");
             io.getOut().println(standardTCtable.getAbsolutePath());
             io.getOut().println("Total number of rows in table: " + n);
             io.getOut().println(" ============================ ");
             io.getOut().println("");
             io.getOut().close();
        
    }
    
        @Override
    public void findMayBeDaisyNodes(String sctid) {
                 int n = 0;
         Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
         String fn = "TC_MAY_extension_" + sctid + ".txt";
         File standardTCtable = new File(fn);
         Traverser TcTraverser = getMayBeDaisyTraverser(startNode);
          try{
             
             BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
             writer.write("SubConcept" + "\t"+ "SuperConcept" + "\n");
//             System.out.println("MayBe Daisy Table :");
//             System.out.print("SubConcept" + "\t"+ "SuperConcept" + "\n");
             for (Path subPath : TcTraverser){
             Node myStartNode = subPath.endNode();
             Traverser myTraverser = getMayBeDaisyTraverser(myStartNode);  
//                Traverser sameTraverser = getSameAsNonselfTraverser(myStartNode);
             
                    for(Path myPath : myTraverser){
                        String tc = myPath.endNode().getProperty(SCTID_KEY).toString() + "\t"
                         + myStartNode.getProperty(SCTID_KEY).toString() + "\n";
                        writer.write(tc);
//                        System.out.print(tc);
                        n = n+1;
                    }

/*                    
                    for(Path samePath: sameTraverser){
                        String same = myStartNode.getProperty(SCTID_KEY).toString() + "\t"
                                    + samePath.endNode().getProperty(SCTID_KEY).toString() + "\n";
                        writer.write(same);
                        System.out.print(same);
                        n = n+1;
                    }
                Traverser MayBeTraverser = getMayBeNonselfTraverser(myStartNode);
           
 
                    for(Path samePath: MayBeTraverser){
                        String maybe = myStartNode.getProperty(SCTID_KEY).toString() + "\t"
                                    + samePath.endNode().getProperty(SCTID_KEY).toString() + "\n";
                        writer.write(maybe);
                        System.out.print(maybe);
                                             
                    } 

*  
*/
             
//             Traverser samebiTraverser = getSameAsNonselfTraverser(myStartNode);
//             for(Path samePath : samebiTraverser){
//                String sameas = samePath.endNode().getProperty(SCTID_KEY).toString() + "\t"
//                         + myStartNode.getProperty(SCTID_KEY).toString() + "\n";  
//                writer.write(sameas);
//                System.out.print(sameas);
//                n = n + 1;
//             }
             
             }
              writer.close();        
             }catch (IOException e){
                 io.getErr().println(e.getStackTrace().toString());
                 io.getErr().close();
             }
             io.select();
             io.getOut().println(" ============================ ");
             io.getOut().println("Generated TC with MAY extension table at:");
             io.getOut().println(standardTCtable.getAbsolutePath());
             io.getOut().println("Total number of rows in the table: " + n);
             io.getOut().println(" ============================ ");
             io.getOut().println("");
             io.getOut().close();
    }
}
