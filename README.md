#neo4j_trie_plugin
The original [Neo4j Spatial plugin] for Neo4j Graph database uses RTree for geometry calculation,
which is not scalable 
for hanlding large number of Nodes for frequent time consuming splitting and joining operations.
 
Geohash on the other 
hand, suit the need of neighborhood calculation and is implemented in databases like mongoDB, but unfortunately not 
in Neo4j. 

This project implement Geohash algorithm based on Neo4j graph database, with a specially designed trie 
tree structure for fast and efficient retrieval. The Geohash implementation is borrowed from [geohash-java] and 
the project is licensed under LGPL license.


Install & Config
-------
First install and configure neo4j. 

For Mac users: 
Execute "brew install neo4j" in terminal.
Then find /usr/local/Cellar/neo4j/x.x.x/libexec/conf/neo4j-server.properties,

change dbms.security.auth_enabled from true to false to disable authentication.

Build
-------
Execute "mvn compile" to build.


Test
-------
For PrefixTree Test, run PrefixTreeTest for individual functionality test
and TestAll for high level experience.

For Plugin Test, run SpatialPluginFunctionTest.


[Neo4j Spatial plugin]: https://github.com/neo4j-contrib/spatial
[geohash-java]: https://github.com/kungfoo/geohash-java
