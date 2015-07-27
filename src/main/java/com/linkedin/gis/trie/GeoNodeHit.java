package com.linkedin.gis.trie;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yihe on 7/11/15.
 */
public class GeoNodeHit implements IndexHits<Node> {
    private Iterator<Node> nodes;
    private int size;
    private Node current;

    public GeoNodeHit(List<Node> nodes){
        this.nodes = nodes.iterator();
        this.size = nodes.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void close() {

    }

    @Override
    public Node getSingle() {
        return null;
    }

    @Override
    public float currentScore() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return nodes.hasNext();
    }

    @Override
    public Node next() {
        current = nodes.next();
        return current;
    }

    @Override
    public ResourceIterator<Node> iterator() {
        return this;
    }
}
