package org.jgrapht.graph;

import graph.ICircuitEdge;
import graph.ICircuitNode;

public class BasicEdge extends DefaultEdge implements ICircuitEdge {

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
}
