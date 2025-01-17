package graph;

import ic.matrix.IMatrixCsr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.vniizht.asuterkortes.counter.latticemodel.DynamicIntArray;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Инкапсулирует логику для поиска базиса циклов в графе.
 */
public class CycleBasis {

    private SchemaGraph<?, ?> graph;
    private ICircuitEdge[] spanningTree;
    private final ArrayDeque<ICircuitNode> deque;
    private final DynamicIntArray tmpv;
    private final DynamicIntArray tmpe;
    private final ArrayList<ICircuitEdge> left = new ArrayList<>(16);
    private final ArrayList<ICircuitEdge> right = new ArrayList<>(16);
    private final Traversing traversing;
    private int mark;

    /**
     * @param graph      граф.
     * @param traversing способ обхода графа.
     */
    public CycleBasis(SchemaGraph<?, ?> graph, Traversing traversing) {
        this.graph = graph;
        spanningTree = new ICircuitEdge[graph.getVertices().size()];
        deque = new ArrayDeque<>();
        tmpv = new DynamicIntArray(graph.getVertices().size());
        tmpe = new DynamicIntArray(graph.getEdges().size());
        tmpv.setSize(tmpv.getCapacity());
        tmpe.setSize(tmpe.getCapacity());
        mark = 0;
        this.traversing = traversing;
    }

    public void setGraph(SchemaGraph<?, ?> graph) {
        this.graph = graph;
    }

    /** Найти циклы и сохранить в матрицу независимых контуров <code>dest</code>. */
    @NotNull
    public IMatrixCsr getCycles(@Nullable IMatrixCsr dest) {
        reset();
        if (dest == null) {
            dest = new IMatrixCsr(tmpe.getSize());
        } else {
            dest.reset(tmpe.getSize());
        }
        for (int i = 0; i < graph.getVertices().size(); ++i) {
            if (spanningTree[i] == null) {
                spanningTree[i] = NONE;
                push(graph.getVertices().get(i));
            }
            while (!deque.isEmpty()) {
                ICircuitNode v = pop();
                int vi = v.getIndex();
                ICircuitEdge ve = spanningTree[vi];
                int begin = graph.getLoi().begin(vi);
                int end = begin + graph.getLoi().degree(vi);
                for (int j = begin; j < end; j++) {
                    ICircuitEdge e = graph.getLoi().get(j);
                    if (e != ve) {
                        ICircuitNode ov = oppositeVertex(v, e);
                        int ovi = ov.getIndex();
                        if (spanningTree[ovi] == null) {
                            spanningTree[ovi] = e;
                            deque.addLast(ov);
                        } else if (tmpe.get(e.getIndex()) == 0) {
                            tmpe.set(e.getIndex(), 1);
                            ICircuitEdge el = spanningTree[ovi];
                            makePath(ovi, el, e, dest);
                        }
                    }
                }
            }
        }
        return dest;
    }

    private void push(ICircuitNode v) {
        deque.addLast(v);
    }

    private ICircuitNode pop() {
        return (traversing == Traversing.QUEUE_BASED) ? deque.pollFirst() : deque.pollLast();
    }

    private void reset() {
        int vCnt = graph.getVertices().size();
        int eCnt = graph.getEdges().size();
        if (spanningTree.length < vCnt) {
            spanningTree = new ICircuitEdge[vCnt];
        } else {
            Arrays.fill(spanningTree, 0, vCnt, null);
        }
        tmpv.setSize(vCnt);
        tmpe.setSize(eCnt);
        Arrays.fill(tmpv.getData(), 0, vCnt, 0);
        Arrays.fill(tmpe.getData(), 0, eCnt, 0);
        deque.clear();
        mark = 0;
    }

    private void makePath(int rootIdx, ICircuitEdge el, ICircuitEdge er, IMatrixCsr dest) {
        ICircuitNode root = graph.getVertices().get(rootIdx);
        ICircuitNode vl = root;
        ICircuitNode vr = root;
        tmpv.set(rootIdx, ++mark);
        left.clear();
        right.clear();
        while (el != NONE || er != NONE) {
            if (el != NONE) {
                left.add(el);
                vl = oppositeVertex(vl, el);
                int vli = vl.getIndex();
                if (tmpv.get(vli) < mark) {
                    tmpv.set(vli, mark);
                } else {
                    trimToVertex(right, vl);
                    break;
                }
                el = spanningTree[vl.getIndex()];
            }
            if (er != NONE) {
                right.add(er);
                vr = oppositeVertex(vr, er);
                int vri = vr.getIndex();
                if (tmpv.get(vri) < mark) {
                    tmpv.set(vri, mark);
                } else {
                    trimToVertex(left, vr);
                    break;
                }
                er = spanningTree[vr.getIndex()];
            }
        }
        dest.addRow();
        for (ICircuitEdge e : left) {
            root = appendEdge(dest, e, root);
        }
        for (int i = right.size() - 1; i >= 0; i--) {
            ICircuitEdge e = right.get(i);
            root = appendEdge(dest, e, root);
        }
    }

    private static ICircuitNode appendEdge(IMatrixCsr dest, ICircuitEdge e, ICircuitNode root) {
        ICircuitNode src = e.getSourceNode();
        if (src == root) {
            dest.append(e.getIndex(), 1);
            root = e.getTargetNode();
        } else {
            dest.append(e.getIndex(), -1);
            root = src;
        }
        return root;
    }

    private void trimToVertex(List<ICircuitEdge> path, ICircuitNode v) {
        int lastIdx = path.size() - 1;
        while (lastIdx >= 0 && !isIncident(path.get(lastIdx), v)) {
            path.remove(lastIdx--);
        }
        while (lastIdx > 0 && isIncident(path.get(lastIdx - 1), v)) {
            path.remove(lastIdx--);
        }
    }

    private boolean isIncident(ICircuitEdge e, ICircuitNode v) {
        return e.getSourceNode() == v || e.getTargetNode() == v;
    }

    private ICircuitNode oppositeVertex(ICircuitNode v, ICircuitEdge e) {
        ICircuitNode src = e.getSourceNode();
        return (v == src) ? e.getTargetNode() : src;
    }

    private static final ICircuitEdge NONE = new ICircuitEdge() {
        @Override
        public ICircuitNode getSourceNode() {
            return null;
        }

        @Override
        public ICircuitNode getTargetNode() {
            return null;
        }

        @Override
        public void setSourceNode(ICircuitNode n) {

        }

        @Override
        public void setTargetNode(ICircuitNode n) {

        }

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public void setIndex(int index) {

        }
    };

    /** Способ обхода графа. */
    public enum Traversing {
        /** В ширину. */
        QUEUE_BASED,
        /** В глубину. */
        STACK_BASED
    }
}
