/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.cfh.querysct;

import java.util.Collection;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

/**
 *
 * @author yoga
 */
public interface QuerySCT {
    public void startGraphDB();
    
    public Index<Node> getIndex();
    
    
    public GraphDatabaseService getGraphDB();
    
    /*
     * @method Return String FSN by the input of string concept ID
     */
    public String getFSNbyID(String sctid);
    
    
    public void findDirectSuperNodes(String sctid);
    
    
    public void findSuperNodes(String sctid);
    
    
    public void findDirectSubNodes(String sctid);
    
    public Collection <Node> getDirectSubNodes(Node sctnode);
    
    /*
     * @Create a list of all descendent concepts
     */    
    public void findAllSubNodes(String sctid);
    

    
    
    /*
     *  @Crete a standard Transitive Closure table for ISA relationship 
     */    
    public void findISATCNodes(String sctid);
    
    
    
    public void findSameAsNodes(String sctid);
    
    
    
    public void findSameAsDirectSubNodes(String sctid);
    
    
    /*
     * @Create a list of all nodes linked by same as and moved from relationships. 
     * It is a substititution table from inactive to active concepts by
     * same as and moved from.
     */
    public void findSameAsForAllNodes (String sctid); 
    
    public void findMayBeForAllNodes(String sctid);
    
    public void findReplacedByForAllNodes(String sctid);
    
    public void findWasAForAllNodes(String sctid);
    
    public void findMayBeReplacedForAllNodes(String sctid);
    
    public void findAllHistoryForAllNodes(String sctid);
    
    
    /*
     * @Create a list of all subnodes and their same as nodes
     */
    public void findSameAsAllSubNodes(String sctid);
    
    
    /*
     * @Create a list of subnodes which includes active by ISA and inactive concepts by iISA relationships
     */
    public void findAllSubandInactiveNodes(String sctid);
    
    
    
    /*
     * @Create Daisy table by ISA and SAME AS relationships
     */
    public void findSameAsISATCNodes(String sctid);
       
    /*
     * @Create daisy table by iISA and ISA relationships (TC table with extension of SameAs and MovedFrom relationships)
     */
    public void findDaisyNodes(String sctid);
    
    
    
    /*
     * Creat a daisy table with MaybeA and WasA extension (TC table with extension of SameAs, MoveFrom, MayBeA, WasA, ReplacedBy relationships )
     */
    public void findMayBeDaisyNodes(String sctid);
    
}
