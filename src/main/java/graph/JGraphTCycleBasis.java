package graph;

import ic.matrix.IMatrixCsr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.AbstractFundamentalCycleBasis;
import org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis;
import org.jgrapht.alg.cycle.StackBFSFundamentalCycleBasis;

import java.util.List;
import java.util.Set;

public class JGraphTCycleBasis<V extends ICircuitNode, E extends ICircuitEdge>
        implements CycleBasis<V, E, Graph<V, E>> {

    private Graph<V, E> graph;

    public JGraphTCycleBasis(Graph<V, E> graph) { this.graph = graph; }

    @Override
    public void setGraph(@NotNull Graph<V, E> graph) {
        this.graph = graph;
    }

    @NotNull
    @Override
    public IMatrixCsr getCycles(@Nullable IMatrixCsr dest, Traversing traversing) {
        dest = resetOrCreate(dest);
        AbstractFundamentalCycleBasis<V, E> basis = traversing == SchemaGraphCycleBasis.Traversing.QUEUE_BASED
                ? new QueueBFSFundamentalCycleBasis<>(graph)
                : new StackBFSFundamentalCycleBasis<>(graph);
        Set<List<E>> cycles = basis.getCycleBasis().getCycles();
        for (List<E> c : cycles) {
            dest.addRow();
            var idx = dest.last(dest.rows);
            ICircuitEdge e1 = c.get(0);
            ICircuitEdge e2 = c.get(1);
            ICircuitNode src1 = e1.getSourceNode();
            ICircuitNode src2 = e2.getSourceNode();
            ICircuitNode tgt1 = e1.getTargetNode();
            ICircuitNode tgt2 = e2.getTargetNode();
            ICircuitNode begin = (tgt1 == src2 || tgt1 == tgt2) ? src1 : tgt1;
            dest.cols.setSize(dest.cols.getSize() + c.size());
            dest.data.setSize(dest.data.getSize() + c.size());
            for (ICircuitEdge e : c) {
                ICircuitNode src = e.getSourceNode();
                ICircuitNode tgt = e.getTargetNode();
                int value;
                if (begin == src) {
                    begin = tgt;
                    value = 1;
                } else {
                    begin = src;
                    value = -1;
                }
                dest.cols.set(idx, e.getIndex());
                dest.data.set(idx++, value);
            }
            dest.incLast(dest.rows, c.size());
        }
        return dest;
    }

    @NotNull
    private IMatrixCsr resetOrCreate(@Nullable IMatrixCsr dest) {
        if (dest == null) {
            dest = new IMatrixCsr(graph.edgeSet().size());
        } else {
            dest.reset(graph.edgeSet().size());
        }
        return dest;
    }
}