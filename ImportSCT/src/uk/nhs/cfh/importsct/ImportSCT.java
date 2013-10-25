/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.nhs.cfh.importsct;

import org.neo4j.graphdb.GraphDatabaseService;
import org.openide.filesystems.FileObject;

/**
 *
 * @author yoga
 */
public interface ImportSCT {
    
    public void createGraphDB();
    public boolean deleteDir(FileObject folder);
    public void importConcepts(String conceptfilepath);
    public void importRelationships(String relfilepath);
    public void assignCurrentISAtoInactiveConcepts();
    public void assigniISAtoInactiveConcepts();

    public void startGraphDB();
    public GraphDatabaseService getGraphDB();
    
    public void releaseVersion(String release);
    public void releaseFiles(FileObject folder);
    public String getConceptPathUK();
    public String getRelPathUK();
    public String getConceptPathINT();
    public String getRelPathINT();
    public long getImportedConceptTotal();
    
    
}
