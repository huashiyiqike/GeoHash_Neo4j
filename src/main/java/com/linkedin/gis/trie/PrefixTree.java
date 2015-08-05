package com.linkedin.gis.trie;

import java.util.*;

import com.linkedin.gis.indexprovider.LayerNodeIndex;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.index.IndexHits;

/**
 * Created by lvqi on 7/13/15.(qiluy.pub@gmail.com)
 */
class PrefixTreeLabel implements Label{
    String name;
    PrefixTreeLabel(String type){
        name = type;
    }
    public String name(){
        return name;
    }
}

public class PrefixTree {
    private static final int max_level = 7;
    private static final Map<Character,CharRelation> char_relation_map= new HashMap<Character, CharRelation>() {
        {
            put('0', CharRelation.TRIE_0);
            put('1', CharRelation.TRIE_1);
            put('2', CharRelation.TRIE_2);
            put('3', CharRelation.TRIE_3);
            put('4', CharRelation.TRIE_4);
            put('5', CharRelation.TRIE_5);
            put('6', CharRelation.TRIE_6);
            put('7', CharRelation.TRIE_7);
            put('8', CharRelation.TRIE_8);
            put('9', CharRelation.TRIE_9);
            put('b', CharRelation.TRIE_B);
            put('c', CharRelation.TRIE_C);
            put('d', CharRelation.TRIE_D);
            put('e', CharRelation.TRIE_E);
            put('f', CharRelation.TRIE_F);
            put('g', CharRelation.TRIE_G);
            put('h', CharRelation.TRIE_H);
            put('j', CharRelation.TRIE_J);
            put('k', CharRelation.TRIE_K);
            put('m', CharRelation.TRIE_M);
            put('n', CharRelation.TRIE_N);
            put('p', CharRelation.TRIE_P);
            put('q', CharRelation.TRIE_Q);
            put('r', CharRelation.TRIE_R);
            put('s', CharRelation.TRIE_S);
            put('t', CharRelation.TRIE_T);
            put('u', CharRelation.TRIE_U);
            put('v', CharRelation.TRIE_V);
            put('w', CharRelation.TRIE_W);
            put('x', CharRelation.TRIE_X);
            put('y', CharRelation.TRIE_Y);
            put('z', CharRelation.TRIE_Z);
        }
    };
    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    public static Node getOrCreateRoot(String label, GraphDatabaseService db){
        String query = "MATCH (n:"+label+") WHERE (n.PrefixTreeRoot) = \"root\"  RETURN n";
        Result result = db.execute(query);
        Node root = null;
        if( result.hasNext() )
        {
            Map<String,Object> row = result.next();
            for ( Map.Entry<String,Object> column : row.entrySet() )
            {
                root = (Node)column.getValue();
            }
            if (result.hasNext())
                throw new RuntimeException("Have more than one root for index " + label);
            return root;
        }
        root = db.createNode(new PrefixTreeLabel(label));
        root.addLabel(new PrefixTreeLabel("PrefixTreeRoot"));
        root.setProperty("PrefixTreeRoot", "root");
        return root;
    }

    private static int distance_to_depth(double distance){
        int depth;
        if (distance < 0.2){
            depth = 7;
        } else if (distance <= 0.9){
            depth = 6;
        } else if (distance <= 10.0){
            depth = 5;
        } else if (distance <= 44){
        	depth = 4;
        }else{
            depth = 3;
        }
        return depth;
    }

    public static List<Node> withinDistance(Node root, final Double lon, final Double lat, double distance){
        int depth = distance_to_depth(distance);

        String[] arounds = GeoHash.getNeighboor(lon, lat, depth);
        List<Node> res = new ArrayList<>();
        for(String prefix:arounds){
            List<Node> tmpres = getChild(prefix, root,depth);
            if(tmpres != null && !tmpres.isEmpty())  res.addAll(tmpres);
        }

        class node_dis{
            Node node;
            double dis;
            node_dis(Node n, double d){
                this.node = n;
                this.dis = d;
            }
        }

        List<node_dis> rescompound = new ArrayList<>();
        for(int i = 0; i < res.size(); i++){
            double tmpdis = CalDistance.calculateDistance(lon, lat, res.get(i));
            if(tmpdis <= distance){
                rescompound.add(new node_dis(res.get(i), tmpdis));
            }
        }
        Collections.sort(rescompound, new Comparator<node_dis>() {
            @Override
            public int compare(node_dis o1, node_dis o2) {
                if (o1.dis < o2.dis) return -1;
                else if (o1.dis > o2.dis) return 1;
                else return 0;
            }
        });

        res.clear();
        for(int i = 0; i < rescompound.size(); i++){
            res.add(rescompound.get(i).node);
        }
        return res;
    }

    
    public static List<Node> getLessThanDistance(double lon, double lat, List<Node> nodes, double distance) {
    	int curPos = 0;
    	int start = 0, end = nodes.size() - 1;
    	while(start <= end) {
    		curPos = start + (end - start) / 2;
    		Node node = nodes.get(curPos);
    		double dis = CalDistance.calculateDistance(lon, lat, node);
    		if (dis < distance) {
    			start = curPos + 1;

    		} else if (dis > distance) {
    			end = curPos - 1;
    		} else {
    			break;
    		}
    	}
    	if (CalDistance.calculateDistance(lon, lat, nodes.get(curPos)) <= distance) {
    		curPos++;
    	}
    	return nodes.subList(0, curPos);
    }

    public static IndexHits<Node> withinDistance(Node root, Node node, double distance){
        double latt, lonn;
        if (node.getProperty(LayerNodeIndex.LON_PROPERTY_KEY) instanceof Integer ||
                node.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY) instanceof Integer) {
            lonn = (double) (int) node.getProperty(LayerNodeIndex.LON_PROPERTY_KEY);
            latt = (double) (int) node.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY);
        } else {
            lonn = (double) node.getProperty(LayerNodeIndex.LON_PROPERTY_KEY);
            latt = (double) node.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY);
        }
        return new GeoNodeHit(withinDistance(root, lonn, latt, distance));
    }

    public static List<Node> getChild(String prefix, Node parent, int depth){
        List<Node> res = new ArrayList<>();
        char[] hasharray = prefix.toCharArray();
        Node cur = parent;
        int level = 0;

        for(;level < Math.min(depth, max_level ); level++) {
            RelationshipType relation = char_relation_map.get(hasharray[level]);
            if (relation != null && cur.hasRelationship(relation)) {
                Relationship tmprelation = cur.getSingleRelationship(relation, Direction.OUTGOING);
                if(tmprelation != null) cur = tmprelation.getEndNode();
            } else {
                return null; // TODO check this
            }
        }


        Queue<Node> queue, next_queue = new LinkedList<>();
        next_queue.offer(cur);

        for(;level <= max_level; level++){
            queue = new LinkedList<>(next_queue);
            next_queue.clear();
            while(!queue.isEmpty()) {
                cur = queue.poll();
                if (level != max_level ) {
                    for (char i : base32) {

                        RelationshipType relation = char_relation_map.get(i);
                        if (cur.hasRelationship(relation, Direction.OUTGOING)) {
                            Node tmp = cur.getSingleRelationship(relation, Direction.OUTGOING).getEndNode();
                            next_queue.add(tmp);
                        }
                    }

                } else {

                    Iterable<Relationship> tmpres = cur.getRelationships(CharRelation.IN_PREFIXTREE, Direction.OUTGOING);
                    for (Iterator<Relationship> iter = tmpres.iterator(); iter.hasNext(); ) {
                        Node tmpnode = iter.next().getEndNode();
                        res.add(tmpnode);
                    }
                }
            }

        }

        return res;
    }
    
    // MOD : only one transaction control
    public static void addNode(GraphDatabaseService db, Node root, Node node) {
        Node cur = root;
//        Transaction tx = db.beginTx();
//        try {
            double latt, lonn;
            if (node.getProperty(LayerNodeIndex.LON_PROPERTY_KEY) instanceof Integer) {
                lonn = (double) (int) node.getProperty(LayerNodeIndex.LON_PROPERTY_KEY);
                latt = (double) (int) node.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY);
            } else {
                lonn = (double) node.getProperty(LayerNodeIndex.LON_PROPERTY_KEY);
                latt = (double) node.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY);
            }

            String geohash = GeoHash.getHash(lonn, latt);
            node.setProperty("geohash", geohash);
            char[] hasharray = geohash.toCharArray();

            for (int level = 0; level < max_level; level++) {
                RelationshipType relation = char_relation_map.get(hasharray[level]);
                if (cur.hasRelationship(relation, Direction.OUTGOING)) {
                    cur = cur.getSingleRelationship(relation, Direction.OUTGOING).getEndNode();
                } else {
                    Node newnode = db.createNode();
                    cur.createRelationshipTo(newnode, relation);
                    cur = newnode;
                }
            }
            cur.createRelationshipTo(node, CharRelation.IN_PREFIXTREE);
    }

    public static void deleteNode(Node node){
         Relationship delrelation = node.getSingleRelationship(CharRelation.IN_PREFIXTREE, Direction.BOTH);
         delrelation.delete();
    }
    public static void updateNode(GraphDatabaseService db, Node root, Node node, double lon, double lat){
         deleteNode(node);
         node.setProperty(LayerNodeIndex.LON_PROPERTY_KEY, lon);
         node.setProperty(LayerNodeIndex.LAT_PROPERTY_KEY, lat);
         addNode(db, root, node);
    }
}

