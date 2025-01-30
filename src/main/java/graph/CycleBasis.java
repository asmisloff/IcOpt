package graph;

import ic.matrix.IMatrixCsr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

/**
 * Инкапсулирует логику для поиска фундаментального базиса циклов в графе.
 */
public interface CycleBasis<V extends ICircuitNode, E extends ICircuitEdge, G extends Graph<V, E>> {

    void setGraph(@NotNull G graph);

    /**
     * Найти базисные циклы и сохранить в форме матрицы независимых контуров.
     * @param dest       матрица независимых контуров. Если <code>dest == null</code>, она будет создана.
     * @param traversing способ обхода графа. В зависимости от значения этого аргумента будут получаться разные
     *                   циклы. Априорно следует ожидать, что <code>QUEUE_BASED</code> приведет к более коротким циклам.
     *                   См. "Narsingh Deo, G. Prabhu, and M. S. Krishnamoorthy. Algorithms for Generating Fundamental Cycles in a Graph."
     * @return <code>dest</code>.
     */
    @NotNull
    IMatrixCsr getCycles(@Nullable IMatrixCsr dest, Traversing traversing);

    /** Способ обхода графа. */
    enum Traversing {
        /** В ширину. */
        QUEUE_BASED,
        /** В глубину. */
        STACK_BASED
    }
}
