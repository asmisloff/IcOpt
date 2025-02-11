package org.jgrapht.graph;

import graph.ICircuitNode;

public class TestVertex implements ICircuitNode {

    private int index;
    private final int lineIndex;

    public TestVertex(int index, int lineIndex) {
        this.index = index;
        this.lineIndex = lineIndex;
    }

    public TestVertex(int index) {
        this(index, 1);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getLineIndex() {
        return lineIndex;
    }
}
