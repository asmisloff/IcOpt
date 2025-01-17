package org.jgrapht.graph;

import graph.ICircuitEdge;
import graph.ICircuitNode;

public class TestEdge extends DefaultEdge implements ICircuitEdge {

    private int index;

    @Override
    public ICircuitNode getSourceNode() {
        return (ICircuitNode) source;
    }

    @Override
    public ICircuitNode getTargetNode() {
        return (ICircuitNode) target;
    }

    @Override
    public void setSourceNode(ICircuitNode n) {
        source = n;
    }

    @Override
    public void setTargetNode(ICircuitNode n) {
        target = n;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }
}
