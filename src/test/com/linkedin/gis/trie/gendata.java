package com.linkedin.gis.trie;
import org.neo4j.graphdb.*;

import java.util.*;

/**
 * Created by lvqi on 7/13/15.
 */
public class gendata {
    public static Node insertNode(GraphDatabaseService db, double lon, double lat){
        assert(lon < 180.0 && lon > - 180.0 && lat < 90.0 && lat > -90.0);
        Node tmp = db.createNode();
        tmp.setProperty("lat", lat);
        tmp.setProperty("lon", lon);
        tmp.setProperty("geohash", GeoHash.getHash(lon, lat));
        //System.out.println("geohash" + GeoHash.getHash(lon, lat));
        return tmp;
    }
    public static List<Node> insertNode(GraphDatabaseService db, int num, Boolean rand){
        Random random = new Random(0);
        List<Node> res = new LinkedList<>();
        for(int i = 0; i < num; i++){
            Node tmp = db.createNode();
            res.add(tmp);
            double lat, lon;
            if(rand){
                double tmps = random.nextDouble();
                lat = ( 2 * tmps - 1);
                lon = ( 2 * tmps - 1);//90 * ( 2 * random.nextDouble() - 1), lon = 180 * ( 2 * random.nextDouble() - 1);
            }
            else{
                lat = (2.0*i/num - 1.0);
                lon = (2.0*i/num - 1.0);
            }
            tmp.setProperty("lat", lat);
            tmp.setProperty("lon", lon);
            tmp.setProperty("geohash", GeoHash.getHash(lon, lat));
        }
        return res;
    }
    public static List<Node> insertNode(GraphDatabaseService db, int num, double lon, double lat, double range) {
    	Random random = new Random(0);
        List<Node> res = new LinkedList<>();
        for(int i = 0; i < num; i++){
            Node node = db.createNode();
            res.add(node);
            double dou = random.nextDouble();
            node.setProperty("lat", dou * range + lat);
            node.setProperty("lon", dou * range + lon);
            node.setProperty("geohash", GeoHash.getHash(lon, lat));
        }
        return res;
    }
}
