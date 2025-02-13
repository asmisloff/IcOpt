package graph;

public interface ICircuitEdge {

    ICircuitNode getSourceNode();

    ICircuitNode getTargetNode();

    void setSourceNode(ICircuitNode n);

    void setTargetNode(ICircuitNode n);

    int getIndex();

    void setIndex(int index);
}
