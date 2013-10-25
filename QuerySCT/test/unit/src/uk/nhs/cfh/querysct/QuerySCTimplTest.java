/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.cfh.querysct;

import org.junit.*;
import static org.junit.Assert.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 *
 * @author yoga
 */
public class QuerySCTimplTest {
    
    public QuerySCTimplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of startGraphDB method, of class QuerySCTimpl.
     */
    @Test
    public void testStartGraphDB() {
        System.out.println("startGraphDB");
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGraphDB method, of class QuerySCTimpl.
     */
    @Test
    public void testGetGraphDB() {
        System.out.println("getGraphDB");
        String DB_PATH = System.getProperty("user.home")+"/neo4j-sct-store"; 
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
//        GraphDatabaseService expResult = new EmbeddedGraphDatabase(DB_PATH);
        GraphDatabaseService result = instance.getGraphDB();
//        result.equals(this);
        
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        instance.getGraphDB().shutdown();
    }

    /**
     * Test of idToFSN method, of class QuerySCTimpl.
     */
    @Test
    public void testIdToFSN() {
        System.out.println("idToFSN");
        String sctid = "442203000";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.getFSNbyID(sctid);
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findDirectSuperNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindDirectSuperNodes() {
        System.out.println("findDirectSuperNodes");
        String sctid = "442203000";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findDirectSuperNodes(sctid);
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findSuperNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindSuperNodes() {
        System.out.println("findSuperNodes");
        String sctid = "";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.findSuperNodes(sctid);
        instance.startGraphDB();
        instance.findSuperNodes("442203000");
        instance.getGraphDB().shutdown();
        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findDirectSubNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindDirectSubNodes() {
        System.out.println("findDirectSubNodes");
        String sctid = "";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.findDirectSubNodes(sctid);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findAllSubNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindAllSubNodes() {
        System.out.println("findAllSubNodes");
        String sctid = "440466000";       
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findAllSubNodes(sctid);
        instance.findAllSubNodes("442203000");
        instance.findAllSubNodes("378111000000107");
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
    @Test
    public void testGetDirectSubNodes(){
        System.out.println("getDirectSubNodes");
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        String sctid =  "441624004";
        Node startNode = instance.getIndex().get("sct_id", sctid).getSingle();
        instance.getDirectSubNodes(startNode);
        instance.getGraphDB().shutdown();
    }

    /**
     * Test of findAllSubandInactiveNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindAllSubandInactiveNodes() {
        System.out.println("findAllSubandInactiveNodes");
        String sctid = "441624004";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findAllSubandInactiveNodes(sctid);
//        instance.findAllSubandInactiveNodes("442203000");
//        instance.findAllSubandInactiveNodes("378111000000107");
        instance.getGraphDB().shutdown();        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findSameAsNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindSameAsNodes() {
        System.out.println("findSameAsNodes");
        String sctid = "440466000";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findSameAsNodes(sctid);
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findSameAsForAllNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindSameAsForAllNodes() {
        System.out.println("findSameAsForAllNodes");
        String sctid = "440466000";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findSameAsForAllNodes(sctid);
        instance.findSameAsForAllNodes("508241000000109");
        instance.findSameAsForAllNodes("431350006");
        instance.findSameAsForAllNodes("323211000000108");
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of findSameAsAllSubNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindSameAsAllSubNodes() {
        System.out.println("findSameAsAllSubNodes");
        String sctid = "440466000";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findSameAsAllSubNodes(sctid);
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }    
    
    
    /**
     * Test of findSameAsDirectSubNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindSameAsDirectSubNodes() {
        System.out.println("findSameAsDirectSubNodes");
        String sctid = "";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.findSameAsDirectSubNodes(sctid);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of findISATCNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindISATCNodes() {
        System.out.println("findISATCNodes");
        String sctid = "441624004";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findISATCNodes(sctid);
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }



    /**
     * Test of findSameAsISATCNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindSameAsISATCNodes() {
        System.out.println("findSameAsISATCNodes");
        String sctid = "441624004";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findSameAsISATCNodes(sctid);
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
    
     /**
     * Test of findSameAsISATCNodes method, of class QuerySCTimpl.
     */
    @Test
    public void testFindDaisyNodes() {
        System.out.println("findDaisyNodes");
        String sctid = "431350006";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findDaisyNodes(sctid);
//        instance.findDaisyNodes("431350006");
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
         /**
     * Test of findMayBeDaisyNode method, of class QuerySCTimpl.
     */
    @Test
    public void testFindMayBeDaisyNodes() {
        System.out.println("findMayBeDaisyNodes");
        String sctid = "431350006";
        QuerySCTimpl instance = new QuerySCTimpl();
        instance.startGraphDB();
        instance.findMayBeDaisyNodes(sctid);
//        instance.findDaisyNodes("431350006");
        instance.getGraphDB().shutdown();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
