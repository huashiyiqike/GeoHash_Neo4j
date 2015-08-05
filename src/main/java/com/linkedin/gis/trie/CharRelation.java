package com.linkedin.gis.trie;

import com.linkedin.gis.indexprovider.LayerNodeIndex;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

//'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
//        'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

/**
 * Created by lvqi on 7/13/15.
 */
public enum CharRelation implements RelationshipType {
    IN_PREFIXTREE,
    TRIE_0,
    TRIE_1,
    TRIE_2,
    TRIE_3,
    TRIE_4,
    TRIE_5,
    TRIE_6,
    TRIE_7,
    TRIE_8,
    TRIE_9,
    TRIE_B,
    TRIE_C,
    TRIE_D,
    TRIE_E,
    TRIE_F,
    TRIE_G,
    TRIE_H,
    TRIE_J,
    TRIE_K,
    TRIE_M,
    TRIE_N,
    TRIE_P,
    TRIE_Q,
    TRIE_R,
    TRIE_S,
    TRIE_T,
    TRIE_U,
    TRIE_V,
    TRIE_W,
    TRIE_X,
    TRIE_Y,
    TRIE_Z,;

}
