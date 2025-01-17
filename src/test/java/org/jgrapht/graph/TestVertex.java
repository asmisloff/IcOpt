package org.jgrapht.graph;

import graph.ICircuitNode;

public class TestVertex implements ICircuitNode {

    private int index;

    public TestVertex(int index) { this.index = index; }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }
}
