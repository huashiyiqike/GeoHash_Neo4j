package com.linkedin.gis.trie;
//import org.neo4j.cypher.javacompat.ExecutionResult;
import com.linkedin.gis.indexprovider.LayerNodeIndex;
import junit.framework.TestCase;
import org.junit.*;
import org.neo4j.graphdb.*;

import java.util.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//import org.neo4j.cypher.javacompat.ExecutionEngine;
//import org.neo4j.helpers.collection.IteratorUtil;

//import static org.neo4j.helpers.collection.MapUtil.map;

/**
 * Created by lvqi on 7/13/15.
 */
public class TestAll extends TestCase {
    static String dbPath = "/usr/local/Cellar/neo4j/2.2.1/libexec/data/graph.db";
    static GraphDatabaseService db;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        db = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
    }

//      For now just first run this to create data, then comment this and uncomment the next test;
    @Test
    public void test_addrealdata() {
        String querys = "match (n:User) return n";
        Map<String, Object> paramss = new HashMap<String, Object>();
        Transaction tx = db.beginTx();
        Result result = null;
        try {
            result = db.execute(querys, paramss);
            tx.success();
        }catch (Exception e) {
            System.out.println(e);
        } finally {
            tx.close();
        }
        int count = 0;
        tx = db.beginTx();
        try {
            while (result.hasNext()) {
                System.out.println(count++);


                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    Node tmp = (Node) column.getValue();
                    if (tmp.hasProperty(LayerNodeIndex.LON_PROPERTY_KEY)) {
                        System.out.println(tmp.getId() + "  " + tmp.getProperty("name") + "lon:"+tmp.getProperty("lon") + " lat:" + tmp.getProperty("lat")
                       +" geo:" + GeoHash.getHash(ConvertUtil.convertObjectToDouble(tmp.getProperty("lon")),
                                ConvertUtil.convertObjectToDouble(tmp.getProperty("lat"))) );
                         PrefixTree.addNode(db, PrefixTree.getOrCreateRoot("user", db), tmp);
                    }
                }

            }
            tx.success();
        }catch (Exception e) {
            System.out.println(e);
        } finally {
            tx.close();
        }


    }

//    @Test
//    public void testNear() {
//
//        Transaction tx = db.beginTx();
//        try {
//            gendata.insertNode(db,1000,false);
//            tx.success();
//        } catch (Exception e) {
//            System.out.println(e);
//        } finally {
//
//        }
//         Map<String, Object> params = new HashMap<String, Object>();
//
//        String query = "MATCH (n) WHERE HAS (n.geohash) RETURN n";
//        Result result = db.execute( query, params );
//
//        String rows = "";
//        while ( result.hasNext() )
//        {
//            Map<String,Object> row = result.next();
//            for ( Map.Entry<String,Object> column : row.entrySet() )
//            {
//                rows += column.getKey() + ": " +  column.getValue() ;
//                Node tmp = (Node)column.getValue();
//                rows += tmp.getProperty("geohash") + "; ";
//                PrefixTree.addNode(db,PrefixTree.getOrCreateRoot("user", db),tmp);
//            }
//            rows += "\n";
//        }
//
//        System.out.println("start search");
//        List<Node> newnodes = gendata.insertNode(db, 5, true);
//        for(Node node:newnodes){
//            System.out.println("node:\n lat:"+node.getProperty("lat")+" lon:"+node.getProperty("lon") +" geohash:"+node.getProperty("geohash"));
//            System.out.println("neighbors:");
//            Iterator<Node> neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user",db),node, 6).iterator();
//            for(;neighbors.hasNext();) {
//                Node tmp = neighbors.next();
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//            }
//            System.out.println("0.01\n");
//            neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user",db),node, 0.01).iterator();
//            for(;neighbors.hasNext();) {
//                Node tmp = neighbors.next();
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//            }
//            System.out.println("");
//        }
//
//        System.out.println("success");
//        tx.close();
//        db.shutdown();
//    }

}