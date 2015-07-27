#neo4j_trie_plugin
The original [Neo4j Spatial plugin] for Neo4j Graph database uses RTree for geometry calculation, which is not scalable for hanlding large number of Nodes for frequent time consuming splitting and joining operations. Geohash on the other hand, suit the need of neighborhood calculation and is implemented in databases like mongoDB, but unfortunately not in Neo4j.

This project implement Geohash algorithm based on Neo4j graph database, with a specially designed trie tree structure for fast and efficient retrieval. The Geohash implementation is borrowed from [geohash-java] and the code is partially open sourced. This is my summer intern project in Linkedin, Beijing, and the code is partially published with reserved right.



[Neo4j Spatial plugin]: https://github.com/neo4j-contrib/spatial
[geohash-java]: https://github.com/kungfoo/geohash-java
