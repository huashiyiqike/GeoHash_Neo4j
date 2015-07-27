package com.linkedin.gis.trie;

import junit.framework.TestCase;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.unsafe.batchinsert.BatchInserters;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lvqi on 7/15/15.
 */
public class Trie_TestCase extends TestCase{
    public static final Map<String, String> NORMAL_CONFIG = new HashMap<String, String>();
    static {
        NORMAL_CONFIG.put( GraphDatabaseSettings.nodestore_mapped_memory_size.name(), "50M" );
        NORMAL_CONFIG.put( GraphDatabaseSettings.relationshipstore_mapped_memory_size.name(), "120M" );
        NORMAL_CONFIG.put( GraphDatabaseSettings.nodestore_propertystore_mapped_memory_size.name(), "150M" );
        NORMAL_CONFIG.put( GraphDatabaseSettings.strings_mapped_memory_size.name(), "200M" );
        NORMAL_CONFIG.put( GraphDatabaseSettings.arrays_mapped_memory_size.name(), "0M" );
        NORMAL_CONFIG.put( GraphDatabaseSettings.dump_configuration.name(), "false" );
    }
    protected static final Map<String, String> LARGE_CONFIG = new HashMap<String, String>();
    static {
        LARGE_CONFIG.put( GraphDatabaseSettings.nodestore_mapped_memory_size.name(), "100M" );
        LARGE_CONFIG.put( GraphDatabaseSettings.relationshipstore_mapped_memory_size.name(), "300M" );
        LARGE_CONFIG.put( GraphDatabaseSettings.nodestore_propertystore_mapped_memory_size.name(), "400M" );
        LARGE_CONFIG.put( GraphDatabaseSettings.strings_mapped_memory_size.name(), "800M" );
        LARGE_CONFIG.put( GraphDatabaseSettings.arrays_mapped_memory_size.name(), "10M" );
        LARGE_CONFIG.put( GraphDatabaseSettings.dump_configuration.name(), "true" );
    }

    private static File basePath = new File("target/var");
    private static File dbPath = new File(basePath, "neo4j-db");
    private GraphDatabaseService graphDb;
    private Transaction tx;
    private long storePrefix; // differ different store

    @Override
    @Before
    protected void setUp() throws Exception {
        updateStorePrefix();
        setUp(false, false, false);
    }

    protected void updateStorePrefix()
    {
        storePrefix++;
    }

    /**
     * Configurable options for text cases, with or without deleting the previous database, and with
     * or without using the BatchInserter for higher creation speeds. Note that tests that need to
     * delete nodes or use transactions should not use the BatchInserter.
     *
     * @param deleteDb
     * @param useBatchInserter
     * @throws Exception
     */
    protected void setUp(boolean deleteDb, boolean useBatchInserter, boolean autoTx) throws Exception {
        super.setUp();
        reActivateDatabase(deleteDb, useBatchInserter, autoTx);
    }

    /**
     * Some tests require switching between normal EmbeddedGraphDatabase and BatchInserter, so we
     * allow that with this method. We also allow deleting the previous database, if that is desired
     * (probably only the first time this is called).
     *
     * @param deleteDb
     * @param useBatchInserter
     * @throws Exception
     */
    protected void reActivateDatabase(boolean deleteDb, boolean useBatchInserter, boolean autoTx) throws Exception {
        shutdownDatabase(deleteDb);
        Map<String, String> config = NORMAL_CONFIG;
        String largeMode = System.getProperty("spatial.test.large");
        if (largeMode != null && largeMode.equalsIgnoreCase("true")) {
            config = LARGE_CONFIG;
        }
        //graphDb = new TestGraphDatabaseFactory().setFileSystem( fileSystem ).newImpermanentDatabase( getNeoPath().getAbsolutePath() );
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(getNeoPath().getAbsolutePath()).setConfig( config ).newGraphDatabase();

        if (autoTx) {
            // with the batch inserter the tx is a dummy that simply succeeds all the time
            tx = graphDb.beginTx();
        }
    }
    protected void beforeShutdown() {
    }
    /**
     * For test cases that want to control their own database access, we should
     * shutdown the current one.
     *
     * @param deleteDb
     */
    protected void shutdownDatabase(boolean deleteDb) {
        if (tx != null) {
            tx.success();
            tx.close();
            tx = null;
        }
        beforeShutdown();
        if (graphDb != null) {
            graphDb.shutdown();
            graphDb = null;
        }
        if (deleteDb) {
            deleteDatabase(true);
        }
    }

    protected void deleteDatabase(boolean synchronous) {
        if (synchronous)
        {
            try {
                FileUtils.deleteRecursively(getNeoPath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileUtils.deleteRecursively(getNeoPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    protected GraphDatabaseService graphDb() {
        return graphDb;
    }

    protected File getNeoPath() {
        return new File(dbPath.getAbsolutePath(), Long.toString(storePrefix));
    }

}
