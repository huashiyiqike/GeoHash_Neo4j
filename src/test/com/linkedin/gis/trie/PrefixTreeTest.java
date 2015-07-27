package com.linkedin.gis.trie;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import junit.framework.TestCase;

public class PrefixTreeTest extends TestCase {

	private static final String GEOHASH = "geohash";
	private static final String LON = "lon";
	private static final String LAT = "lat";
	private static final String INDEXLABEL = "test";
	private Node root;
	private String dbPath = "/usr/local/Cellar/neo4j/2.2.2/libexec/data/graph.db";
	private GraphDatabaseService graphDb;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
	}

	@Test
	public void testAddNode() {
		List<Node> candidate = null;
		List<Node> child = null;
		Transaction tx = graphDb.beginTx();
		try {
			root = PrefixTree.getOrCreateRoot(INDEXLABEL, graphDb);
			candidate = gendata.insertNode(graphDb, 1, false);
			PrefixTree.addNode(graphDb, root, candidate.get(0));
			child = PrefixTree.getChild((String) candidate.get(0).getProperty(GEOHASH), root, 7);
			assertTrue(candidate.get(0).getProperty(GEOHASH) == child.get(0).getProperty(GEOHASH));
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			tx.close();
		}
		graphDb.shutdown();
	}
	
	@Test
	public void testDeleteNode() {
		Transaction tx = graphDb.beginTx();
		String query = "MATCH n-[r]-() DELETE n, r";
		graphDb.execute(query);
		query = "MATCH (n) WHERE HAS (n.geohash) DELETE n";
		graphDb.execute(query);
		root = PrefixTree.getOrCreateRoot(INDEXLABEL, graphDb);
		List<Node> candidate = gendata.insertNode(graphDb, 10, true);
		for (Node node : candidate) {
			PrefixTree.addNode(graphDb, root, node);
		}
		List<Node> child = PrefixTree.getChild((String) candidate.get(0).getProperty(GEOHASH), root, 7);
		assertTrue(candidate.get(0).getProperty(GEOHASH) == child.get(0).getProperty(GEOHASH));
		PrefixTree.deleteNode(candidate.get(0));
		child = PrefixTree.getChild((String) candidate.get(0).getProperty(GEOHASH), root, 7);
		assertEquals(child.size(), 0);
		tx.close();
		graphDb.shutdown();
	}
	
	@Test
	public void testUpdateNode() {
		double lon = -139.12;
		double lat = -45.24;
		Transaction tx = graphDb.beginTx();
		String query = "MATCH n-[r]-() DELETE n, r";
		graphDb.execute(query);
		query = "MATCH (n) WHERE HAS (n.geohash) DELETE n";
		graphDb.execute(query);
		root = PrefixTree.getOrCreateRoot(INDEXLABEL, graphDb);
		List<Node> candidate = gendata.insertNode(graphDb, 10, true);
		for (Node node : candidate) {
			PrefixTree.addNode(graphDb, root, node);
		}

		String geoHashPre = (String) candidate.get(0).getProperty(GEOHASH);
		String geoHashCur = GeoHash.getHash(lon, lat);
		List<Node> child = PrefixTree.getChild(geoHashPre, root, 7);
		assertEquals(child.size(), 1);
		PrefixTree.updateNode(graphDb, root, candidate.get(0), lon, lat);
		child = PrefixTree.getChild(geoHashPre, root, 7);
		assertEquals(child.size(), 0);
		child = PrefixTree.getChild(geoHashCur, root, 7);
		assertEquals(child.size(), 1);
		tx.close();
		graphDb.shutdown();
	}
	
	@Test
	public void testDistance() {
		double lon1 = 32.32;
		double lat1 = 32.32;
		Transaction tx = graphDb.beginTx();
		String query;
		System.out.println("delete all nodes");
		String deleteQuery = "MATCH n-[r]-() DELETE n, r";
		graphDb.execute(deleteQuery);
		
		root = PrefixTree.getOrCreateRoot(INDEXLABEL, graphDb);
		Node node = graphDb.createNode();
		node.setProperty(LON, lon1);
		node.setProperty(LAT, lat1);
		List<Node> candidate1 = gendata.insertNode(graphDb, 2, lon1, lat1, 0.1);
		List<Node> candidate2 = gendata.insertNode(graphDb, 2, lon1, lat1, 0.2);
		
		PrefixTree.addNode(graphDb, root, candidate1.get(0));
		query = "MATCH (n {PrefixTreeRoot:'root'})-[*8..8]-(x {geohash: 'stz030gzmygt'}) RETURN x";
		PrefixTree.addNode(graphDb, root, candidate1.get(1));
		query = "MATCH (n {PrefixTreeRoot:'root'})-[*8..8]-(x {geohash: 'stz00001z0d7'}) RETURN x";
		PrefixTree.addNode(graphDb, root, candidate2.get(0));
		query = "MATCH (n {PrefixTreeRoot:'root'})-[*8..8]-(x {geohash: 'stz0dy31m1hs'}) RETURN x";
		PrefixTree.addNode(graphDb, root, candidate2.get(1));
		query = "MATCH (n {PrefixTreeRoot:'root'})-[*8..8]-(x {geohash: 'stz00s3tzsd0'}) RETURN x";
//		for (Node can : candidate1) {
//			PrefixTree.addNode(graphDb, root, can);
//			query = "MATCH (n {PrefixTreeRoot:'root'})-[*8..8]-(n {geohash: ''}) RETURN x";
//			getAllNodes(query, "tag");
//			System.out.println("lat:" + can.getProperty("lat") + " lon:"
//					+ can.getProperty("lon") + " geohash:" + can.getProperty("geohash") + " dis:" + CalDistance
//							.calculateDistance((double) can.getProperty("lon"), (double) can.getProperty("lat"), node)
//					+ " value:" + can);
//		}
//		for (Node can : candidate2) {
//			PrefixTree.addNode(graphDb, root, can);
//			query = "MATCH (n {PrefixTreeRoot:'root'})-[*8..8]-(x) RETURN x";
//			getAllNodes(query, "tag");
//			System.out.println("lat:" + can.getProperty("lat") + " lon:"
//					+ can.getProperty("lon") + " geohash:" + can.getProperty("geohash") + " dis:" + CalDistance
//							.calculateDistance((double) can.getProperty("lon"), (double) can.getProperty("lat"), node)
//					+ " value:" + can);
//		}
		
		getWithDistance(20.0, root, node);
		getWithDistance(4.0, root, node);
		
        tx.close();
        graphDb.shutdown();
	}
	
	public void getWithDistance(double distance, Node root, Node node) {
		System.out.println("test distance");
		Iterator<Node> neighbors = PrefixTree.withinDistance(root, node, distance).iterator();
		print(neighbors, node);
	}
	
	public void getAllNodes(String query, String tag) {
		System.out.println(tag);
		System.out.println("-----------------------");
		Result result = graphDb.execute(query);
		while (result.hasNext()) {
			Map<String, Object> map = result.next();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				System.out.println("key:" + entry.getKey() + ", value" + entry.getValue());
				Node node1 = (Node)entry.getValue();
				Iterator<String> keys = node1.getPropertyKeys().iterator();
				while (keys.hasNext()) {
					System.out.println(keys.next());
				}
			}
		}
		System.out.println("-----------------------");
	}
	
	public void print(Iterator<Node> neighbors, Node node) {
		while (neighbors.hasNext()) {
            Node tmp = neighbors.next();
            System.out.println("lat:" + tmp.getProperty("lat")+" lon:"+tmp.getProperty("lon") +" geohash:"+tmp.getProperty("geohash")
                    +" dis:" + CalDistance.calculateDistance((double)tmp.getProperty("lon"), (double)tmp.getProperty("lat"), node));
		}
	}
}
