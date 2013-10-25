/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.cfh.importsct;

import org.junit.*;
import static org.junit.Assert.*;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 *
 * @author yoga
 */
public class ImportSCTimplTest {
    
    public ImportSCTimplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }
    
            /**
     * Test of startGraphDB method, of class ImportSCTimpl.
    */ 
    
    @Test
    public void testStartGraphDB() {
        System.out.println("startGraphDB");
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.startGraphDB();
        if(instance.getGraphDB()!=null){
            System.out.println("The testing graph database started");
            instance.getGraphDB().shutdown();
            System.out.println("The testing graphDB is shutting donwn ...");
        }
        else {
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }
    }
    
    
    /**
     * Test of getGraphDB method, of class ImportSCTimpl.
     */
    
    @Test
    public void testGetGraphDB() {
        System.out.println("getGraphDB");
        ImportSCTimpl instance = new ImportSCTimpl();
        GraphDatabaseService result = instance.getGraphDB();
        assertNotNull(result);
        result.shutdown();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    
    /**
     * Test of createGraphDB method, of class ImportSCTimpl.
   */  
    
    @Test
    public void testCreateGraphDB() {
        System.out.println("createGraphDB");
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.createGraphDB();
        assertNotNull(instance.getGraphDB());
        if(instance.getGraphDB()!=null){
            System.out.println("The testing graph database was created");
            instance.getGraphDB().shutdown();
            System.out.println("The testing graphDB is shutting donwn ...");
        }
        else {
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
        
    /**
     * Test of importConcepts method, of class ImportSCTimpl.
     * 
     */
    
    
    @Test
    public void testImportConcepts() {
        System.out.println("importConcepts");
        String conceptfilepath = "D:/NationalTerminologyReleases/SNOMEDCT_12.0.0_20111003/SnomedCT_INT_20110731/Terminology/Content/sct1_Concepts_Core_INT_20110731.txt";
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.importConcepts(conceptfilepath);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
    
    
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    


        
    
    

 
    
    /**
     * Test of importRelationships method, of class ImportSCTimpl.
     *
    */
    @Test
    public void testImportRelationships() {
        System.out.println("importRelationships");
        String relfilepath = "D:/NationalTerminologyReleases/SNOMEDCT_12.0.0_20111003/SnomedCT_INT_20110731/Terminology/Content/sct1_Relationships_Core_INT_20110731.txt";
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.importRelationships(relfilepath);
              
//            Total number of relationships in release  : 1440000
//            Imported ISA relationships: 440468
//            Imported SAME AS relationships: 43948
//            Imported Moved From relationships: 0
//            Imported May Be A relationships: 29972
//            Imported Replaced By relationships: 4761
//            Imported Was A relationships: 21151
             
            
        //fail("The test case is a prototype.");
        }

    
    /**
     * Test of releaseVersion method, of class ImportSCTimpl.
     */
    @Test
    public void testReleaseVersion() {
        System.out.println("releaseVersion");
        String release = "SNOMEDCT_12.0.0_20111003";
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.releaseVersion(release);        
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getConceptPathUK method, of class ImportSCTimpl.
     */
    @Test
    public void testGetConceptPathUK() {
        System.out.println("getConceptPathUK");
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.releaseVersion("SNOMEDCT_12.0.0_20111003");
        String expResult = "D:\\NationalTerminologyReleases\\SNOMEDCT_12.0.0_20111003\\SnomedCT_GB1000000_20111003\\Terminology\\Content\\sct1_Concepts_National_GB1000000_20111003.txt";
        String result = instance.getConceptPathUK();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getRelPathUK method, of class ImportSCTimpl.
     */
    @Test
    public void testGetRelPathUK() {
        System.out.println("getRelPathUK");
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.releaseVersion("SNOMEDCT_12.0.0_20111003");
        String expResult = "D:\\NationalTerminologyReleases\\SNOMEDCT_12.0.0_20111003\\SnomedCT_GB1000000_20111003\\Terminology\\Content\\sct1_Relationships_National_GB1000000_20111003.txt";
        String result = instance.getRelPathUK();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getConceptPathINT method, of class ImportSCTimpl.
     */
    @Test
    public void testGetConceptPathINT() {
        System.out.println("getConceptPathINT");
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.releaseVersion("SNOMEDCT_12.0.0_20111003");
        String expResult = "D:\\NationalTerminologyReleases\\SNOMEDCT_12.0.0_20111003\\SnomedCT_INT_20110731\\Terminology\\Content\\sct1_Concepts_Core_INT_20110731.txt";
        String result = instance.getConceptPathINT();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getRelPathINT method, of class ImportSCTimpl.
     */
    @Test
    public void testGetRelPathINT() {
        System.out.println("getRelPathINT");
        ImportSCTimpl instance = new ImportSCTimpl();
        instance.releaseVersion("SNOMEDCT_12.0.0_20111003");
        String expResult = "D:\\NationalTerminologyReleases\\SNOMEDCT_12.0.0_20111003\\SnomedCT_INT_20110731\\Terminology\\Content\\sct1_Relationships_Core_INT_20110731.txt";
        String result = instance.getRelPathINT();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}