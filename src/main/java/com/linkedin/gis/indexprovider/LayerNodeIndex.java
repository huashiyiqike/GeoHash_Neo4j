/**
 * Copyright (c) 2010-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 * <p>
 * This file is part of Neo4j.
 * <p>
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.linkedin.gis.indexprovider;

import com.linkedin.gis.trie.GeoNodeHit;
import com.linkedin.gis.trie.PrefixTree;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayerNodeIndex implements Index<Node> {
    public static final String LON_PROPERTY_KEY = "lon";    // Config parameter key: longitude property name for nodes in point layers
    public static final String LAT_PROPERTY_KEY = "lat";    // Config parameter key: latitude property name for nodes in point layers
    public static final String LBS_UPDATED_PROPERTY_KEY = "lbs_updated";

    public static final String WITHIN_DISTANCE_QUERY = "withinDistance";        //Query type

    public static final String DISTANCE_IN_KM_PARAMETER = "distanceInKm";        // Query parameter key: distance for withinDistance query
    //public static final String PEOPLE_LIMIT = "peopleLimit";		            // Query parameter key: number of people needed to query
    public static final String POINT_PARAMETER = "point";                        // Query parameter key: relative to this point for withinDistance query
    //public static final String SKIP = "point";						            // Query parameter key: relative to this point for withinDistance query

    private GraphDatabaseService db;
    private String indexName;
    private Map<String, Node> rootMap = new HashMap<>();

    public LayerNodeIndex(String indexName, GraphDatabaseService db) {
        this.db = db;
        this.indexName = indexName;
    }

    @Override
    public void add(Node geometry, String key, Object value) {
    }

    @Override
    public void remove(Node node, String s, Object o) {
    }

    @Override
    public void remove(Node node, String s) {
    }

    @Override
    public void remove(Node node) {
    }

    @Override
    public void delete() {
    }

    @Override
    public Node putIfAbsent(Node node, String s, Object o) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Class<Node> getEntityType() {
        return null;
    }

    @Override
    public IndexHits<Node> get(String s, Object o) {
        return null;
    }

    @Override
    public IndexHits<Node> query(String key, Object params) {

        if (key.equals(WITHIN_DISTANCE_QUERY)) {
            Double lon = null;
            Double lat = null;
            Double distance = null;

            // this one should enable distance searches using cypher query lang
            // by using: withinDistance:[7.0, 10.0, 100.0]  (long, lat, distance)
            if (params.getClass() == String.class) {
                try {
                    List<Number> coordsAndDistance = (List<Number>) new JSONParser().parse((String) params);
                    lon = coordsAndDistance.get(0).doubleValue();
                    lat = coordsAndDistance.get(1).doubleValue();
                    distance = coordsAndDistance.get(2).doubleValue();
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Map<?, ?> p = (Map<?, ?>) params;
                lon = (Double) p.get(LON_PROPERTY_KEY);
                lat = (Double) p.get(LAT_PROPERTY_KEY);
                distance = (Double) p.get(DISTANCE_IN_KM_PARAMETER);
            }

            Node root;
            if(rootMap.containsKey(indexName))
                root = rootMap.get(indexName);
            else {
                root = PrefixTree.getOrCreateRoot(indexName, db);
                rootMap.put(indexName ,root);
            }

            IndexHits<Node> nodes = new GeoNodeHit(PrefixTree.withinDistance(root, lon, lat, distance));
            return nodes;

        } else {
            throw new UnsupportedOperationException(String.format(
                    "only %s are implemented.",
                    WITHIN_DISTANCE_QUERY));
        }
    }

    @Override
    public IndexHits<Node> query(Object queryOrQueryObject) {
        String queryString = (String) queryOrQueryObject;
        IndexHits<Node> indexHits = query(queryString.substring(0, queryString.indexOf(":")),
                queryString.substring(queryString.indexOf(":") + 1));
        return indexHits;
    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public GraphDatabaseService getGraphDatabase() {
        return db;
    }

}
