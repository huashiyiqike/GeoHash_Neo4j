/**
 * Copyright (c) 2010-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.linkedin.gis.test;

import junit.framework.Assert;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.annotations.Documented;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.neo4j.server.rest.AbstractRestFunctionalTestBase;
import org.neo4j.test.ImpermanentGraphDatabase;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpatialPluginFunctionalTest extends AbstractRestFunctionalTestBase
{
    private static final int PORT = 7575;
    private static final String ENDPOINT = "http://localhost:"+PORT+"/db/data/ext/GeoTriPlugin";
    JSONParser jsonParser = new JSONParser();
    

    /** The server variable in SharedServerTestBase is disabled, and we use an alternative */
    private static NeoServer altServer;
	
    /**
     * Disable normal server creation, so we can override port number. 
     * @throws IOException
     */
    @BeforeClass
    public static void allocateServer() throws IOException {
        altServer = CommunityServerBuilder.server().onPort( PORT ).build();
        altServer.start();
    }

	/** Since we created a different server, we need to release that explicitly */
	@AfterClass
	public static final void releaseAltServer() {
		altServer.stop();
		altServer = null;
	}

	/** Use the alternative server with different port number */
	@Override
	public GraphDatabaseService graphdb() {
		return altServer.getDatabase().getGraph();
	}

    /**
     * The Neo4j Spatial Server plugin, if
     * installed, will be announced in the root representation
     * for the Neo4j Server REST API.
     */
    @Test
    @Documented
    public void finding_the_plugin() throws UnsupportedEncodingException
    {
        gen.get().expectedStatus( Status.OK.getStatusCode() );
        String response = gen.get().get( ENDPOINT ).entity();
        assertTrue( response.contains( "graphdb" ) );
    }

    @Test
    public void create_prefixtree(){
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));
    }

    @Test
    public void create_index(){
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri2\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri2\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));
    }

    @Test
    public void add_node() throws ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri3\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri3\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED,"{\"lat\":60.1, \"lon\":15.2}", "http://localhost:"+PORT+"/db/data/node");
        int nodeId = getNodeId(response);

        response = post(Status.OK, "{\"index_name\":\"geo_tri3\", \"node\":\"http://localhost:"+PORT+"/db/data/node/"+nodeId+"\"}", ENDPOINT + "/graphdb/addNodeToIndex");
        assertTrue(response.contains("geohash"));
    }

    @Test
    public void add_nodes() throws ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri4\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri4\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        int id1 = getNodeId(post(Status.CREATED,"{\"lat\":60.1, \"lon\":15.2}", "http://localhost:"+PORT+"/db/data/node"));
        int id2 = getNodeId(post(Status.CREATED,"{\"lat\":60.2, \"lon\":15.1}", "http://localhost:"+PORT+"/db/data/node"));

        response = post(Status.OK, "{\"index_name\":\"geo_tri4\", \"nodes\": [\"http://localhost:"+PORT+"/db/data/node/" + id1 + "\",\"http://localhost:"+PORT+"/db/data/node/" + id2 + "\"]}", ENDPOINT + "/graphdb/addNodesToIndex");
        assertTrue(response.contains("geohash"));

    }

    @Test
    public void del_node() throws ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri5\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri5\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED,"{\"lat\":60.1, \"lon\":15.2}", "http://localhost:"+PORT+"/db/data/node");
        int nodeId = getNodeId(response);

        post(Status.OK, "{\"index_name\":\"geo_tri5\", \"node\":\"http://localhost:"+PORT+"/db/data/node/"+nodeId+"\"}", ENDPOINT + "/graphdb/addNodeToIndex");

        response = post(Status.OK, "{\"index_name\":\"geo_tri5\", \"node\":\"http://localhost:"+PORT+"/db/data/node/"+nodeId+"\"}", ENDPOINT + "/graphdb/deleteNodeFromIndex");
        assertTrue(response.contains("geohash"));
    }

    @Test
    public void del_nodes() throws ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri6\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri6\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        int id1 = getNodeId(post(Status.CREATED,"{\"lat\":60.1, \"lon\":15.2}", "http://localhost:"+PORT+"/db/data/node"));
        int id2 = getNodeId(post(Status.CREATED,"{\"lat\":60.2, \"lon\":15.1}", "http://localhost:"+PORT+"/db/data/node"));

        response = post(Status.OK, "{\"index_name\":\"geo_tri6\", \"nodes\": [\"http://localhost:"+PORT+"/db/data/node/" + id1 + "\",\"http://localhost:"+PORT+"/db/data/node/" + id2 + "\"]}", ENDPOINT + "/graphdb/addNodesToIndex");

        response = post(Status.OK, "{\"index_name\":\"geo_tri6\", \"nodes\": [\"http://localhost:"+PORT+"/db/data/node/" + id1 + "\",\"http://localhost:"+PORT+"/db/data/node/" + id2 + "\"]}", ENDPOINT + "/graphdb/deleteNodesFromIndex");

        assertTrue(response.contains("geohash"));
    }

    @Test
    public void update_node() throws ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri7\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri7\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED,"{\"lat\":60.1, \"lon\":15.2}", "http://localhost:"+PORT+"/db/data/node");
        int nodeId = getNodeId(response);

        post(Status.OK, "{\"index_name\":\"geo_tri7\", \"node\":\"http://localhost:"+PORT+"/db/data/node/"+nodeId+"\"}", ENDPOINT + "/graphdb/addNodeToIndex");

        response = post(Status.OK, "{\"index_name\":\"geo_tri7\", \"node_id\":\""+nodeId+"\", \"lat\":59, \"lon\":17, \"lbs_updated\":1111111}", ENDPOINT + "/graphdb/updateNodeFromPoint");

        assertTrue(response.contains("59"));
        assertTrue(response.contains("17"));
        assertTrue(response.contains("1111111"));

    }

    @Test
    public void queryByCypher() throws IOException, ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri8\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri8\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"lat\":60.1, \"lon\":15.2}", "http://localhost:" + PORT + "/db/data/node");
        int nodeId = getNodeId(response);
        post(Status.OK, "{\"index_name\":\"geo_tri8\", \"node\":\"http://localhost:"+PORT+"/db/data/node/"+nodeId+"\"}", ENDPOINT + "/graphdb/addNodeToIndex");


        response = post(Status.OK,"{\"query\":\"start node = node:geo_tri8(\'withinDistance:[15.2, 60.1, 10.0]\') return node\"}", "http://localhost:"+PORT+"/db/data/cypher");
        ObjectMapper mapper = new ObjectMapper();

        assert mapper.readTree(response).get("data").size() != 0;

        JsonNode node = mapper.readTree(response).get("data").get(0).get(0).get("data");

        assertEquals(15.2, node.get("lon").getDoubleValue());
        assertEquals(60.1, node.get("lat").getDoubleValue());
    }

    @Test
    public void queryByREST() throws ParseException {
        data.get();

        String response = post(Status.OK, "{\"index_name\":\"geo_tri9\"}", ENDPOINT + "/graphdb/createGeoTriIndex");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"name\":\"geo_tri9\", \"config\":{\"provider\":\"geo_tri\",\"lat\":\"lat\",\"lon\":\"lon\"}}", "http://localhost:"+PORT+"/db/data/index/node/");
        assertTrue(response.contains("geo_tri"));

        response = post(Status.CREATED, "{\"lat\":60.1, \"lon\":15.2}", "http://localhost:" + PORT + "/db/data/node");
        int nodeId = getNodeId(response);
        post(Status.OK, "{\"index_name\":\"geo_tri9\", \"node\":\"http://localhost:"+PORT+"/db/data/node/"+nodeId+"\"}", ENDPOINT + "/graphdb/addNodeToIndex");

        response = post(Status.OK, "{\"index_name\":\"geo_tri9\", \"lat\":60.1, \"lon\":15.2, \"distance\":1}", ENDPOINT + "/graphdb/queryNodesFromIndex");
        JSONArray array = (JSONArray) jsonParser.parse(response);
        assertTrue(response.contains("15.2"));
        assertTrue(response.contains("60.1"));

    }

    private int getNodeId(String response) throws ParseException {
        JSONObject o = (JSONObject) jsonParser.parse(response);
//        JSONArray array = (JSONArray) o;
        String self = (String)  o.get("self");
        String res = self.substring(self.lastIndexOf("/")+1);
        return Integer.parseInt(res);
    }


    private void dumpDB() {
        ExecutionResult cypher = new ExecutionEngine(graphdb()).execute("MATCH (n)-[r]->() return n,type(r),r");
        System.out.println(cypher.dumpToString());
    }
    
    private String post(Status status, String payload, String endpoint) {
        return gen().expectedStatus( status.getStatusCode() ).payload( payload ).post( endpoint).entity();
    }
    private String put(Status status, String payload, String endpoint) {
        return gen().expectedStatus( status.getStatusCode() ).payload( payload ).put(endpoint).entity();
    }
    
    @Before
    public void cleanContent()
    {
        ImpermanentGraphDatabase graphdb = (ImpermanentGraphDatabase) graphdb();
        graphdb.cleanContent();
        //clean
        gen.get().setGraph( graphdb() );
        
    }
    
}
