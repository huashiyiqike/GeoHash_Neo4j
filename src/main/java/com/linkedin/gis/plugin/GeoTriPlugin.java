package com.linkedin.gis.plugin;

import com.linkedin.gis.indexprovider.LayerNodeIndex;
import com.linkedin.gis.trie.PrefixTree;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singleton;

// START SNIPPET: GeohashPlugin
@Description("a set of extensions that perform operations using the neo4j-spatial component")
public class GeoTriPlugin extends ServerPlugin {

//    private GraphDatabaseService db;
    private Map<String, Node> rootMap = new HashMap<>();

    @PluginTarget(GraphDatabaseService.class)
    @Description("add a new location index")
    public Iterable<Node> createGeoTriIndex(
            @Source GraphDatabaseService db,
            @Description("Location Index name") @Parameter(name = "index_name") String indexName) {
        Transaction tx = db.beginTx();

        try {
            Node node;
            if(rootMap.containsKey(indexName))
                node = rootMap.get(indexName);
            else {
                node = PrefixTree.getOrCreateRoot(indexName, db);
                rootMap.put(indexName ,node);
            }

            tx.success();
            return singleton(node);
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new RuntimeException("Error creating index" + e);
        } finally {
            tx.close();
        }
    }

    @PluginTarget(GraphDatabaseService.class)
    @Description("add a point to index")
    public Iterable<Node> addNodeToIndex(
            @Source GraphDatabaseService db,
            @Description("point to add") @Parameter(name = "node") Node node,
            @Description("Location Index name") @Parameter(name = "index_name") String indexName) {
        Transaction tx = db.beginTx();

        try {
            Node root;
            if(rootMap.containsKey(indexName))
                root = rootMap.get(indexName);
            else {
                root = PrefixTree.getOrCreateRoot(indexName, db);
                rootMap.put(indexName ,node);
            }

            PrefixTree.addNode(db, root, node);
            tx.success();
            return singleton(node);
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new RuntimeException("Error inserting point to index" + e);
        } finally {
            tx.close();
        }
    }

    @PluginTarget(GraphDatabaseService.class)
    @Description("add points to index")
    public Iterable<Node> addNodesToIndex(
            @Source GraphDatabaseService db,
            @Description("points to add") @Parameter(name = "nodes") List<Node> nodes,
            @Description("Location Index name") @Parameter(name = "index_name") String indexName) {
        Transaction tx = db.beginTx();
        try {
            Node root;
            if(rootMap.containsKey(indexName))
                root = rootMap.get(indexName);
            else{
                root = PrefixTree.getOrCreateRoot(indexName, db);
                rootMap.put(indexName ,root);
            }

            for (Node node : nodes) {
                PrefixTree.addNode(db, root, node);
            }
            tx.success();
            return nodes;
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new RuntimeException("Error inserting point to index" + e);
        } finally {
            tx.close();
        }
    }

    @PluginTarget(GraphDatabaseService.class)
    @Description("delete a point from index")
    public Iterable<Node> deleteNodeFromIndex(
            @Source GraphDatabaseService db,
            @Description("point to delete") @Parameter(name = "node") Node node,
            @Description("Location Index name") @Parameter(name = "index_name") String indexName) {
        Transaction tx = db.beginTx();
        try {
            PrefixTree.deleteNode(node);
            return singleton(node);
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new RuntimeException("Error inserting point to index" + e);
        } finally {
            tx.close();
        }
    }

    @PluginTarget(GraphDatabaseService.class)
    @Description("delete points from index")
    public Iterable<Node> deleteNodesFromIndex(
            @Source GraphDatabaseService db,
            @Description("points to add") @Parameter(name = "nodes") List<Node> nodes,
            @Description("Location Index name") @Parameter(name = "index_name") String indexName
    ) {

        Transaction tx = db.beginTx();
        try {
            for (Node node : nodes) {
                PrefixTree.deleteNode(node);
            }
            return nodes;
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new RuntimeException("Error inserting point to index" + e);
        } finally {
            tx.close();
        }

    }

    @PluginTarget(GraphDatabaseService.class)
    @Description("update an existing geometry specified in lon/lat. The layer must already contain the record.")
    public Iterable<Node> updateNodeFromPoint(@Source GraphDatabaseService db,
                                              @Description("The geometry lon to add to the layer") @Parameter(name = "lon") double lon,
                                              @Description("The geometry lat to add to the layer") @Parameter(name = "lat") double lat,
                                              @Description("The lbs_updated added to the layer") @Parameter(name = "lbs_updated") long lbs_updated,
                                              @Description("The geometry node id") @Parameter(name = "node_id") long nodeId,
                                              @Description("The layer to add the node to.") @Parameter(name = "index_name") String indexName) {
        try (Transaction tx = db.beginTx()) {
            Node node = db.getNodeById(nodeId);
            // update node
            node.setProperty(LayerNodeIndex.LON_PROPERTY_KEY, lon);
            node.setProperty(LayerNodeIndex.LAT_PROPERTY_KEY, lat);
            if (lbs_updated > 0) {
                node.setProperty(LayerNodeIndex.LBS_UPDATED_PROPERTY_KEY, lbs_updated);
            } else {
                node.setProperty(LayerNodeIndex.LBS_UPDATED_PROPERTY_KEY, System.currentTimeMillis());
            }

            // update tri-tree
            Node root;
            if(rootMap.containsKey(indexName))
                root = rootMap.get(indexName);
            else{
                root = PrefixTree.getOrCreateRoot(indexName, db);
                rootMap.put(indexName ,root);
            }

            PrefixTree.updateNode(db, root, node, lon, lat);

            tx.success();
            return singleton(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PluginTarget(GraphDatabaseService.class)
    @Description("query points from index")
    public Iterable<Node> queryNodesFromIndex(
            @Source GraphDatabaseService db,
            @Description("longitude of center") @Parameter(name = "lon") double lon,
            @Description("latitude of center") @Parameter(name = "lat") double lat,
            @Description("the distances") @Parameter(name = "distance") double distance,
            @Description("Location Index name") @Parameter(name = "index_name") String name
    ) {

        Transaction tx = db.beginTx();
        try {
            Node root;
            if(rootMap.containsKey(name))
                root = rootMap.get(name);
            else{
                root = PrefixTree.getOrCreateRoot(name, db);
                rootMap.put(name ,root);
            }

            List<Node> result = PrefixTree.withinDistance(root, lon, lat,distance);


            return result;
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new RuntimeException("Error inserting point to index" + e);
        } finally {
            tx.close();
        }

    }
}
// END SNIPPET: GeohashPlugin