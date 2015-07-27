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
//
////
//    @Test
//    public void test_addmany() {
//        //        // tmp
//
//        Transaction tx = db.beginTx();
//        double lonbias = 116.5, latbias = 40;
//        try {
//
//            Node tmp1 = gendata.insertNode(db, 116.5, 40);
//            int total  = 100000;
//            for(int i = 0; i < total; i++) {
//                PrefixTree.addNode(db, PrefixTree.getOrCreateRoot("user", db), gendata.insertNode(db,
//                        lonbias+(double) i / total, latbias+ (double) i / total));
//                System.out.println(CalDistance.calculateDistance(lonbias+(double) i/total, latbias+(double)i/total, tmp1));
//            }
//            tx.success();
//        } catch (Exception e) {
//            System.out.println(e);
//        } finally {
//
//        }
//
//        tx.close();
//        db.shutdown();
//    }
//
//
//    @Test
//    public void test_geo(){
//        double lon = 116.5, lat = 40.0;
//        System.out.println(GeoHash.getHash(lon, lat));
//        for(String i:GeoHash.getNeighboor(lon, lat, 5)) System.out.println(i);
//    }

//    @Test
//    public void test_time() {
//        String querys = "match n where n has lat return count(n)";
//
//
//        Transaction tx = db.beginTx();
//        try {
//            Node node = gendata.insertNode(db, 116.5, 40.0);
//            Iterator<Node> neighbors;
//
//            long start = System.nanoTime();
//
//            neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user", db), node, 0.1).iterator();
//            long end = System.nanoTime() ;
//            System.out.println(" 7 level Takes" + (end - start) /1e6+ " mili seconds");
//
//
//            start = System.nanoTime();
//
//            neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user", db), node, 0.9).iterator();
//            end = System.nanoTime() ;
//            System.out.println(" 6 level Takes" + (end - start) /1e6+ " mili seconds");
//
//            start = System.nanoTime();
//
//            neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user", db), node, 10).iterator();
//            end = System.nanoTime();
//            System.out.println(" 5 level Takes" + (end - start) /1e6 + " mili seconds");
//
//
//            start = System.nanoTime();
//
//            neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user", db), node, 1000).iterator();
//            end = System.nanoTime();
//            System.out.println(" 4 level Takes" + (end - start) /1e6 + " mili seconds");
//
//
//        }catch(Exception e){
//            System.out.println(e);
//        }finally{
//            tx.close();
//        }
//    }


    @Test
    public void test_realdis() {
        Transaction tx = db.beginTx();
        try {
            Node node = gendata.insertNode(db, 116.5, 40.0);
            Iterator<Node> neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user", db), node, 100).iterator();
            Node tmp = neighbors.next(); //"name:"+ tmp.getProperty("name")+
            System.out.println(" lat:" + tmp.getProperty("lat") + " lon:" + tmp.getProperty("lon") + " geohash:" + tmp.getProperty("geohash")
                    + " dis:" + CalDistance.calculateDistance(ConvertUtil.convertObjectToDouble(tmp.getProperty("lon"))
                    ,ConvertUtil.convertObjectToDouble( tmp.getProperty("lat")), node));
            for (; neighbors.hasNext(); ) {
                tmp = neighbors.next();// "name:"+ tmp.getProperty("name")+
                System.out.println(" lat:" + tmp.getProperty("lat") + " lon:" + tmp.getProperty("lon") + " geohash:" + tmp.getProperty("geohash")
                        + " dis:" + CalDistance.calculateDistance(ConvertUtil.convertObjectToDouble(tmp.getProperty("lon")),
                        ConvertUtil.convertObjectToDouble( tmp.getProperty("lat")), node));
            }
            tx.success();
        }catch(Exception e){
            System.out.println(e);
        }finally{
            tx.close();
        }

    }


//    @Test
//    public void test_1() {
//        String querys = "match (n:User) where n.name = \"白红妹\" or n.name = \"马征\" or n.name =  \"山巾\" or n.name = \"曹胜彬\" or n.name = \"陈帅\" or n.name = \"郭心炜\" return n";
//        Map<String, Object> paramss = new HashMap<String, Object>();
//        Transaction tx = db.beginTx();
//        Result result = null;
//        try {
//            result = db.execute(querys, paramss);
//            tx.success();
//        }catch (Exception e) {
//            System.out.println(e);
//        } finally {
////            tx.close();
//        }
//        int count = 0;
//        tx = db.beginTx();
//        try {
//            while (result.hasNext()) {
//                System.out.println(count++);
//
//
//                Map<String, Object> row = result.next();
//                for (Map.Entry<String, Object> column : row.entrySet()) {
//                    Node tmp = (Node) column.getValue();
//                    if (tmp.hasProperty(LayerNodeIndex.LON_PROPERTY_KEY)){
//                        System.out.println(tmp.getProperty("name") + "lon:"+tmp.getProperty("lon") + " lat:" + tmp.getProperty("lat")
//                        +" geo:" + GeoHash.getHash((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat")) );
//                    }
////                        PrefixTree.addNode(db, PrefixTree.getOrCreateRoot("user", db), tmp);
//                }
//
//            }
//            tx.success();
//        }catch (Exception e) {
//            System.out.println(e);
//        } finally {
//            tx.close();
//        }
//
//
//    }


//    @Test
//    public void test_addrealdata() {
//        String querys = "match (n:User) return n";
//        Map<String, Object> paramss = new HashMap<String, Object>();
//        Transaction tx = db.beginTx();
//        Result result = null;
//        try {
//            result = db.execute(querys, paramss);
//            tx.success();
//        }catch (Exception e) {
//            System.out.println(e);
//        } finally {
//            tx.close();
//        }
//        int count = 0;
//        tx = db.beginTx();
//        try {
//            while (result.hasNext()) {
//                System.out.println(count++);
//
//
//                Map<String, Object> row = result.next();
//                for (Map.Entry<String, Object> column : row.entrySet()) {
//                    Node tmp = (Node) column.getValue();
//                    if (tmp.hasProperty(LayerNodeIndex.LON_PROPERTY_KEY)) {
//                        System.out.println(tmp.getId() + "  " + tmp.getProperty("name") + "lon:"+tmp.getProperty("lon") + " lat:" + tmp.getProperty("lat")
//                       +" geo:" + GeoHash.getHash(ConvertUtil.convertObjectToDouble(tmp.getProperty("lon")),
//                                ConvertUtil.convertObjectToDouble(tmp.getProperty("lat"))) );
//                         PrefixTree.addNode(db, PrefixTree.getOrCreateRoot("user", db), tmp);
//                    }
//                }
//
//            }
//            tx.success();
//        }catch (Exception e) {
//            System.out.println(e);
//        } finally {
//            tx.close();
//        }
//
//
//    }



//    @Test
//    public void test_delete_update() {
//        //        // tmp
//        String querys = "match n-[r]-() delete n,r";
//        Map<String, Object> paramss = new HashMap<String, Object>();
//        Result results = db.execute( querys, paramss );
//
//        querys = "match n delete n";
//        results = db.execute(querys, paramss);
//
//
//        Transaction tx = db.beginTx();
//        double lonbias = 116.5, latbias = 40;
//        try {
//
//            Node tmp1 = gendata.insertNode(db, 116.5, 40);
//            int total  = 10000;
//            for(int i = 0; i < total; i++) {
//                PrefixTree.addNode(db, PrefixTree.getOrCreateRoot("user", db), gendata.insertNode(db,
//                        lonbias+(double) i / total, latbias+(double) i / total));
//                System.out.println(CalDistance.calculateDistance(lonbias+(double) i/total, latbias+(double)i/total, tmp1));
//            }
//            tx.success();
//        } catch (Exception e) {
//            System.out.println(e);
//        } finally {
//
//        }
//        Map<String, Object> params = new HashMap<String, Object>();
//
//
//        System.out.println("start search");
//        List<Node> newnodes = new LinkedList<Node>();
//        newnodes.add(gendata.insertNode(db, lonbias, latbias));
//        for(Node node:newnodes){
//            System.out.println("node:\n lat:"+node.getProperty("lat")+" lon:"+node.getProperty("lon") +" geohash:"+node.getProperty("geohash"));
//            System.out.println("neighbors:");
//            for(double dis = 4; dis < 5; dis += 1) { //15
//                System.out.println("dis" + dis);
//                Iterator<Node> neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user",db),node, dis).iterator();
//                Node tmp = neighbors.next();
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//                for(;neighbors.hasNext();) {
//                    tmp = neighbors.next();
//                }
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//
//            }
//
//        }
//        System.out.println("Now delete .....");
//        querys = "match n where n.lat = 40.0781 return n";
//        results = db.execute( querys, paramss );
//
//        Map<String,Object> row = results.next();
//        for ( Map.Entry<String,Object> column : row.entrySet() )
//        {
//            Node tmp = (Node)column.getValue();
//            PrefixTree.deleteNode(tmp);
//        }
//        for(Node node:newnodes){
//            System.out.println("node:\n lat:"+node.getProperty("lat")+" lon:"+node.getProperty("lon") +" geohash:"+node.getProperty("geohash"));
//            System.out.println("neighbors:");
//            for(double dis = 4; dis < 5; dis += 1) { //15
//                System.out.println("dis" + dis);
//                Iterator<Node> neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user",db),node, dis).iterator();
//                Node tmp = neighbors.next();
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//                for(;neighbors.hasNext();) {
//                    tmp = neighbors.next();
//                }
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//
//            }
//
//        }
//
//
//        System.out.println("Now update .....");
//        String delete = "match n where n.lat = 40.078 return n";
//        results = db.execute( delete, paramss );
//
//        row = results.next();
//        for ( Map.Entry<String,Object> column : row.entrySet() )
//        {
//            Node tmp = (Node)column.getValue();
//            PrefixTree.updateNode(db, PrefixTree.getOrCreateRoot("user", db), tmp,  116.578, 40.078);
//        }
//
//
//        for(Node node:newnodes){
//            System.out.println("node:\n lat:"+node.getProperty("lat")+" lon:"+node.getProperty("lon") +" geohash:"+node.getProperty("geohash"));
//            System.out.println("neighbors:");
//            for(double dis = 4; dis < 5; dis += 1) { //15
//                System.out.println("dis" + dis);
//                Iterator<Node> neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user",db),node, dis).iterator();
//                Node tmp = neighbors.next();
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//                for(;neighbors.hasNext();) {
//                    tmp = neighbors.next();
//                }
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//
//            }
//
//        }
//
//        System.out.println("success");
//        tx.close();
//        db.shutdown();
//    }
//
//    @Test
//    public void testPrecision() {
//    //        // tmp
//        String querys = "match n-[r]-() delete n,r";
//        Map<String, Object> paramss = new HashMap<String, Object>();
//        Result results = db.execute( querys, paramss );
//
//        querys = "match n delete n";
//        results = db.execute(querys, paramss);
//
//
//        Transaction tx = db.beginTx();
//        double lonbias = 116.5, latbias = 40;
//        try {
//
//            Node tmp1 = gendata.insertNode(db, 116.5, 40);
//            int total  = 10000;
//            for(int i = 0; i < total; i++) {
//                PrefixTree.addNode(db, PrefixTree.getOrCreateRoot("user", db), gendata.insertNode(db,
//                        lonbias+(double) i / total, latbias+(double) i / total));
//                System.out.println(CalDistance.calculateDistance(lonbias+(double) i/total, latbias+(double)i/total, tmp1));
//            }
//            tx.success();
//        } catch (Exception e) {
//            System.out.println(e);
//        } finally {
//
//        }
//        Map<String, Object> params = new HashMap<String, Object>();
//
//
//        System.out.println("start search");
//        List<Node> newnodes = new LinkedList<Node>();
//        newnodes.add(gendata.insertNode(db, lonbias, latbias));
//        for(Node node:newnodes){
//            System.out.println("node:\n lat:"+node.getProperty("lat")+" lon:"+node.getProperty("lon") +" geohash:"+node.getProperty("geohash"));
//            System.out.println("neighbors:");
//            for(double dis = 0; dis < 5; dis += 1) { //15
//                System.out.println("dis" + dis);
//                Iterator<Node> neighbors = PrefixTree.withinDistance(PrefixTree.getOrCreateRoot("user",db),node, dis).iterator();
//                Node tmp = neighbors.next();
//                System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                        +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//                for(;neighbors.hasNext();) {
//                    tmp = neighbors.next();
//                    System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
//                            +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
//                }
//
//            }
//
//        }
//
//        System.out.println("success");
//        tx.close();
//        db.shutdown();
//    }
//
//
//    @Test
//    public void testNear() {
////        // tmp
////        String querys = "match n-[r]-() delete n,r";
////        Map<String, Object> paramss = new HashMap<String, Object>();
////        Result results = db.execute( querys, paramss );
////
////        querys = "match n delete n";
////        results = db.execute( querys, paramss );
//
//        Transaction tx = db.beginTx();
//        try {
//            gendata.insertNode(db,10000,false);
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


//        String cleans = "MATCH (n) MATCH n-[r]-() DELETE n,r";
//        Result result = db.execute(cleans, params);

//        Map<String, Object> params = new HashMap<String, Object>();

}