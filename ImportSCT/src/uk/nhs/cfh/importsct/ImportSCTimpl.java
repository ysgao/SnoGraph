/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.cfh.importsct;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.impl.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author yoga
 */
@ServiceProvider(service = ImportSCT.class)
public class ImportSCTimpl implements ImportSCT {
    
    private static final String DB_PATH = System.getProperty("user.home")+ "\\neo4j-sct-store";    
    private static final File File_Store = new File(DB_PATH);
    private static final FileObject File_Store_FO = FileUtil.toFileObject(File_Store);
    private static GraphDatabaseService graphDB;
    private static final String SCTID_KEY = "sct_id";
    private static final String FSN_KEY = "fsn";
    private static final String CONSTATUS_KEY = "concept_status";
    private static Index<Node> nodeIndex;
    private static long counter =0;
    private static long MAX = 10000;
    private static long nodeNo=0;
    private static long total;
    InputOutput io = IOProvider.getDefault().getIO("Import Status", true);
    
    private String release = null;
    private String relv;
    private String ukreldate = null;
    private String intreldate = null;
    private String conceptPathUK = null;
    private String relPathUK = null;
    private String conceptPathINT = null;
    private String relPathINT = null;

    @Override
    public long getImportedConceptTotal() {
        return total;
    }

    /*
     * @TODO need to catch exception if no release file avaiable.
     */
    
    @Override
    public void releaseFiles(FileObject folder) {
        FileObject releaseFiles[] = folder.getChildren();
        for (FileObject fileObject : releaseFiles) {
            
//            System.out.println(fileObject.getName().toString());
            if(fileObject.getName().startsWith("sct1_Concepts_Core_INT")){
               conceptPathINT = fileObject.getPath().toString();
               System.out.println("INT concept text file path: " + conceptPathINT);
            }
            if(fileObject.getName().startsWith("sct1_Relationships_Core_INT")){
                relPathINT = fileObject.getPath().toString();
                System.out.println("INT relationship text file path: " + relPathINT);
            }
            if(fileObject.getName().startsWith("sct1_Concepts_National_GB")){
                conceptPathUK = fileObject.getPath().toString();
                System.out.println("UK concept text file path: " + conceptPathUK);
            }
            if(fileObject.getName().startsWith("sct1_Relationships_National_GB")){
                relPathUK = fileObject.getPath().toString();
                System.out.println("UK relationship text file path: " + relPathUK);
            }
            
            releaseFiles(fileObject);    
            
        }
       
        
    }

    @Override
    public boolean deleteDir(FileObject folder) {
        try{
        FileObject neoDB[] = folder.getChildren();
        for (FileObject fileObject : neoDB) {
            boolean success = deleteDir(fileObject);
            if(!success){
                return false;
            }
            }    
            folder.delete();
            return true;  

    }catch(IOException e){
        return false;
    }
    }
    
    
    private static enum RelTypes implements RelationshipType{
        ISA,
        iISA,
        SameAs,
        MovedFrom,
        MayBeA,
        ReplacedBy,
        WasA
    }
    
    private static Traverser getISATraverser(final Node node){
         TraversalDescription td = Traversal.description()
                 .breadthFirst()
                 .relationships(RelTypes.ISA, Direction.INCOMING)
                 .evaluator(Evaluators.excludeStartPosition());
         return td.traverse(node);
     }
     
    private static Traverser getDirectSuperTraverser( final Node node )
        {
           TraversalDescription td = Traversal.description()
                  .breadthFirst()
                  .relationships(RelTypes.ISA, Direction.OUTGOING)
                  .evaluator(Evaluators.includingDepths(1, 1));
           return td.traverse(node);

        }
    
    private static Traverser getDirectiSuperTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.iISA, Direction.OUTGOING)
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
    
    private static Traverser getDirectiSubTraverser(final Node node){
        TraversalDescription td = Traversal.description()
                .breadthFirst()
                .relationships(RelTypes.iISA, Direction.INCOMING)
                .evaluator(Evaluators.includingDepths(1, 1));
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
    public void createGraphDB() {
       
        io.getOut().println("Start to create database ...");
        if(graphDB != null){
            graphDB.shutdown();
        }
        if (File_Store.exists()) {
//            FileUtils.deleteFile(File_Store);
            deleteDir(File_Store_FO);
        }
        graphDB = new EmbeddedGraphDatabase(DB_PATH);
        nodeIndex = graphDB.index().forNodes("nodes");
        registerShutdownHook(graphDB);
        io.getOut().println("GraphDB is ready for data import ... ");
        io.getOut().println("New database created at: ");
        io.getOut().println(DB_PATH);
        io.getOut().close();  
    }

    @Override
    public void importConcepts(String conceptfilepath) {
//       createGraphDB();
       Transaction tx = graphDB.beginTx();
      
       try{
           try{
            BufferedReader reader = new BufferedReader(new FileReader(conceptfilepath));
            String readline;
            int curLineNr = 1;
            int skipLines =1;
            while((readline = reader.readLine()) != null){
                if(curLineNr++ <= skipLines){
                    continue;
                }
                String datavalue[] = readline.split("\t");
                
                String conceptid = datavalue[0];
                int conceptstatus = Integer.parseInt(datavalue[1]);
                String fsn = datavalue[2];
                
                Node node = createNode(conceptid, fsn, conceptstatus);                
                counter=counter+1;
                storeData(counter,tx);
            }           
       }catch(IOException e){
         io.getErr().println(e.getStackTrace().toString());
         io.getErr().close();
       }
       tx.success(); 
       
    }
      finally{
       tx.finish();  
       total = total + counter;
       io.getOut().println("Total import number of concepts : " + total );
       io.getOut().println("Import of concept table commpleted.");
       io.getOut().close();
       }
       graphDB.shutdown();
       total = 0;
       counter = 0;        
         
    }

    @Override
    public void importRelationships(String relfilepath) {
       int totalNo = 0;
       int isaNo =0;
       int sameasNo =0;
       int movedfromNo =0;
       int maybeaNo =0;
       int replacedbyNo =0;
       int wasaNo = 0; 
     
//       startGraphDB();
       Transaction tx = graphDB.beginTx();      
       try{
//            ProgressHandle handle = ProgressHandleFactory.createHandle("Import Relationships task");
//            handle.start();    
            try{
            BufferedReader reader = new BufferedReader(new FileReader(relfilepath));
            
            String readline;
            int curLineNr = 1;
            int skipLines =1;
            while((readline = reader.readLine()) != null){
                if(curLineNr++ <= skipLines){
                    continue;
                }
                String datavalue[] = readline.split("\t");
                String conceptid1 = datavalue[1];
                long rel = Long.parseLong(datavalue[2]);
                String conceptid2 = datavalue[3];
                
                long sct2 = Long.parseLong(conceptid2);
                
                Node node1 = getNode(conceptid1);
                Node node2 = getNode(conceptid2);
                
                if(rel == 116680003){                  
                    if(sct2 != 363660007 && sct2 != 363662004 &&
                       sct2 != 363664003 && sct2 != 443559000 &&
                       sct2 != 370126003 && sct2 != 363663009 &&
                       sct2 != 363661006) 
                    {
                        node1.createRelationshipTo(node2, RelTypes.ISA);
                        isaNo = isaNo + 1;
                                    }
                }
                if(rel == 168666000){
                    node1.createRelationshipTo(node2, RelTypes.SameAs);
                    sameasNo = sameasNo+1;
                }
                if(rel == 384598002){
                    node1.createRelationshipTo(node2, RelTypes.MovedFrom);
                    movedfromNo = movedfromNo +1;
                }
                if(rel == 149016008){
                    node1.createRelationshipTo(node2, RelTypes.MayBeA);
                    maybeaNo = maybeaNo + 1;
                }
                if(rel == 370124000){
                    node1.createRelationshipTo(node2, RelTypes.ReplacedBy);
                    replacedbyNo = replacedbyNo +1;
                }
                if(rel == 159083000){
                    node1.createRelationshipTo(node2, RelTypes.WasA);
                    wasaNo = wasaNo+1;
                }
                counter = counter+1;
                storeData(counter,tx);

            }   
              
       
           
       }catch(IOException e){
         io.getErr().println(e.getStackTrace().toString());
         io.getErr().close();
       }
       tx.success();
//       handle.finish();
    }
      finally{
       tx.finish();        
       total = total + counter;
       io.getOut().println("Total number of relationships in release  : " + total );            
       io.getOut().println("    Imported ISA relationships: " + isaNo);
       io.getOut().println("    Imported SAME AS relationships: " + sameasNo);
       io.getOut().println("    Imported Moved From relationships: " + movedfromNo);
       io.getOut().println("    Imported May Be A relationships: " + maybeaNo);
       io.getOut().println("    Imported Replaced By relationships: " + replacedbyNo);
       io.getOut().println("    Imported Was A relationships: " + wasaNo);
       io.getOut().println("Import of relationship table commpleted.");
       io.getOut().close();
       }
       graphDB.shutdown();
       total = 0;
       counter = 0; 
        
    }

    @Override
    public void assignCurrentISAtoInactiveConcepts() {
       if(getGraphDB()!=null){
           getGraphDB().shutdown();
       }
       startGraphDB();
        
        int n = 0;
        String sctid = "138875005";
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        Traverser isaTraverser = getISATraverser(startNode);
        Transaction tx = graphDB.beginTx();
        io.getOut().println("Start assertion of iISA beween active and inactive concepts ...");
        io.getOut().close();
        try{

            for(Path isaPath : isaTraverser){             
                Node myStartNode = isaPath.endNode();
                Traverser subTraverser = getSameAsNonselfTraverser(myStartNode);
                                
                for(Path myPath : subTraverser){
                       Node inactiveNode = myPath.endNode();                  
                                              
                       // assign the active concept's supper concepts to inactive concept
                       Traverser directSuperTraverser = getDirectSuperTraverser(myStartNode);
                       for(Path superPath : directSuperTraverser){
                           inactiveNode.createRelationshipTo(superPath.endNode(), RelTypes.iISA);
 //                          System.out.println("InactiveNode-" + inactiveNode.getProperty(SCTID_KEY).toString()
 //                                  + " i-ISA " + superPath.endNode().getProperty(SCTID_KEY).toString()
 //                                  );                       
                        n = n+1;
                       }
                       
                       Traverser directSubTraverser = getDirectSubTraverser(myStartNode);
                       for(Path subPath : directSubTraverser){
                           subPath.endNode().createRelationshipTo(inactiveNode, RelTypes.iISA);
//                           System.out.println("ActiveNode-" + subPath.endNode().getProperty(SCTID_KEY).toString() 
//                                   + " i-ISA " + "InactiveNode-" + inactiveNode.getProperty(SCTID_KEY).toString());
                        n = n+ 1;
                       }                            
                    
                }
                counter=counter +1;
                storeData(counter, tx);
            }
         tx.success(); 
        }
        finally {
            tx.finish();                   
            total = total + counter;
            io.getOut().println(" =================== ");
            io.getOut().println("Total number of assertions of iISA between active and inactive concepts =" + total);
            io.getOut().println (" ================== ");
            io.getOut().close();
        }     
      graphDB.shutdown();
      total = 0;
      counter = 0;
    }

    @Override
    public void assigniISAtoInactiveConcepts() {
       if(getGraphDB()!=null){
           getGraphDB().shutdown();
       }
       startGraphDB();
        
        int n = 0;
        String sctid = "138875005";
        Node startNode = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        Traverser isaTraverser = getISATraverser(startNode);
        Transaction tx = graphDB.beginTx();
        io.getOut().println("Start assertion of iISA beween inactive concepts ...");
        io.getOut().close();
        try{
            for(Path isaPath : isaTraverser){             
                Node myStartNode = isaPath.endNode();
                Traverser subTraverser = getSameAsNonselfTraverser(myStartNode);
                for(Path myPath : subTraverser){
                       Node inactiveNode = myPath.endNode();                  
                                              
                       // assign the active concept's supper concepts to inactive concept
                       Traverser directSuperTraverser = getDirectiSuperTraverser(myStartNode);
                       for(Path superPath : directSuperTraverser){
                             inactiveNode.createRelationshipTo(superPath.endNode(), RelTypes.iISA);
//                           System.out.println("InactiveNode-" + inactiveNode.getProperty(SCTID_KEY).toString()
//                                   + " | " + inactiveNode.getProperty(FSN_KEY).toString() + "\n"
//                                   + " i-ISA " + superPath.endNode().getProperty(SCTID_KEY).toString()
//                                   + " | " + superPath.endNode().getProperty(FSN_KEY).toString() + "\n"
//                                   );                       
                        n = n+1;
                       }
                       
                       Traverser directSubTraverser = getDirectiSubTraverser(myStartNode);
                       for(Path subPath : directSubTraverser){
                           subPath.endNode().createRelationshipTo(inactiveNode, RelTypes.iISA);
//                           System.out.println("ActiveNode-" + subPath.endNode().getProperty(SCTID_KEY).toString()
//                                   + " | " + subPath.endNode().getProperty(FSN_KEY).toString() + "\n"
//                                   + " i-ISA " + "InactiveNode-" + inactiveNode.getProperty(SCTID_KEY).toString()
//                                   + " | " + inactiveNode.getProperty(FSN_KEY).toString() + "\n");
                        n = n+ 1;
                       }                            
                    
                }
                counter=counter +1;
                storeData(counter, tx);
            }
         tx.success(); 
        }
        finally {
            tx.finish();                   
            total = total + counter;
            io.getOut().println(" =================== ");
            io.getOut().println("Total number of assertions of iISA between inactive concepts =" + total);
            io.getOut().println (" ================== ");
            io.getOut().close();
        }     
      graphDB.shutdown();
      total = 0;
      counter = 0;
    }

    @Override
    public void startGraphDB() {
        if(graphDB != null){
            graphDB.shutdown();
            }
        graphDB = new EmbeddedGraphDatabase(DB_PATH);
        nodeIndex = graphDB.index().forNodes("nodes");
        registerShutdownHook(graphDB);
    }

    @Override
    public GraphDatabaseService getGraphDB() {
        return graphDB;
    }
    
    private Transaction storeData(long acounter, Transaction tx){
       if(acounter >= MAX){
         total = total + acounter;
         counter = 0;
         tx.success();
         tx.finish();
         io.getOut().println("Committed " + total + " ... ");
         io.getOut().close();
        }
       return tx = graphDB.beginTx();
   }
    
    
   
    private Node createNode(String sctid, String fsn, int conceptstatus){
      
       Node node = nodeIndex.get(SCTID_KEY, sctid).getSingle(); 
       if(node != null) {
//           System.out.println("Node exists: "+ sctid);
           return node;
       }
       else{
       node = graphDB.createNode();
//       System.out.println("A new node created.");
       node.setProperty(SCTID_KEY, sctid);
       node.setProperty(FSN_KEY, fsn);
       node.setProperty(CONSTATUS_KEY, conceptstatus);
       nodeIndex.putIfAbsent(node, SCTID_KEY, sctid);
//       nodeIndex.putIfAbsent(node, FSN_KEY, fsn);
//       nodeIndex.putIfAbsent(node, CONSTATUS_KEY, conceptstatus);
       return node;      
       
       }
     }
    
    private Node getNode(String sctid){
        Node node = nodeIndex.get(SCTID_KEY, sctid).getSingle();
        if(node != null){
        return node;
        }
        else {
        io.getOut().println("Nodes does not exist. Please import concept table first.");  
        io.getOut().close();
        return null;
        }
    }

    @Override
    public void releaseVersion(String release) {
       String[] releasev = release.split("_");
       relv = releasev[1];
       System.out.println(relv);
       
       setUKDate(releasev[2]);
       System.out.println("UK release date: " + ukreldate);
       System.out.println("UK release month: " +ukreldate.substring(4, 6));
       if((releasev[2].substring(4, 6)).equals("10"))
        {
        intreldate = releasev[2].substring(0,4)+"07"+"31";
        setINTDate(intreldate);
        System.out.println("INT release date: " + intreldate);
               }
            
       else{
           intreldate = releasev[2].substring(0,4)+"01"+"31";
           setINTDate(intreldate);
           System.out.println(intreldate);
       }          
    }
    
    public void setINTDate(String intdate){
        intreldate = intdate;
    }
    
    public String getINTDate(){
        return intreldate;
    }
    
    public void setUKDate(String ukdate){
        ukreldate = ukdate;
    }
    
    public String getUKDate(){
        return ukreldate;
    }

    @Override
    public String getConceptPathUK(){
//        conceptPathUK = File.separator + "SnomedCT_GB1000000_" + getUKDate() + File.separator 
//                + "Terminology"+ File.separator + "Content" +File.separator 
//                + "sct1_Concepts_National_GB1000000_"+ getUKDate() +".txt";        
        return conceptPathUK;
    }
    
    @Override
    public String getRelPathUK(){
//        relPathUK = File.separator + "SnomedCT_GB1000000_" + getUKDate() + File.separator 
//                + "Terminology" + File.separator + "Content" +File.separator 
//                + "sct1_Relationships_National_GB1000000_"+ getUKDate() +".txt";        
        return relPathUK;
    }
            
    @Override
    public String getConceptPathINT(){
//        conceptPathINT = File.separator +"SnomedCT_INT_"+ getINTDate() + File.separator 
//                +"Terminology" + File.separator + "Content" +File.separator 
//                + "sct1_Concepts_Core_INT_"+ getINTDate() + ".txt";       
        return conceptPathINT;
    }
            
    @Override
    public String getRelPathINT(){
//        relPathINT = File.separator +"SnomedCT_INT_"+ getINTDate() + File.separator 
//                +"Terminology" + File.separator+ "Content" +File.separator 
//                + "sct1_Relationships_Core_INT_"+ getINTDate() + ".txt";
        return relPathINT;
    }
    
}
