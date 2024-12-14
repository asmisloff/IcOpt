import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolverDense;
import org.ejml.interfaces.linsol.LinearSolverSparse;
import org.ejml.sparse.FillReducing;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static ic.matrix.util.IcMatrixTestHelper.measureTimeMillis;
import static java.lang.Math.abs;

public class PerfTest {

    @Test
    void testLuDenseVsSparse() {
        int n = 25;
        int m = n * n / 4;
        int r = 500000;
        DMatrixRMaj dense = randomSymmetricPositivelyDefined(n, m);
        DMatrixSparseCSC sparse = toSparse(dense);
        LinearSolverDense<DMatrixRMaj> denseSolver = LinearSolverFactory_DDRM.lu(sparse.numRows);
        var denseCholSolver = LinearSolverFactory_DDRM.chol(n);
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> sparseSolver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> sparseCholSolver = LinearSolverFactory_DSCC.cholesky(FillReducing.NONE);
        System.out.printf("Dense Cholesky: %.4f\n", measureTimeMillis(() -> {
            if (!denseCholSolver.setA(dense)) {
                System.out.println("oops");
            }
        }, r));
        System.out.printf("dense: %.4f\n", measureTimeMillis(() -> {
            denseSolver.setA(dense);
        }, r));
        System.out.printf("sparse: %.4f\n", measureTimeMillis(() -> {
            sparseSolver.setA(sparse);
        }, r));
        System.out.printf("sparse Cholesky: %.4f\n", measureTimeMillis(() -> {
            sparseCholSolver.setA(sparse);
        }, r));
    }

    private DMatrixRMaj randomSymmetricPositivelyDefined(int size, int m) {
        DMatrixRMaj res = new DMatrixRMaj(size, size);
        Random r = ThreadLocalRandom.current();
        for (int k = 0; k < m / 2; k++) {
            int i = r.nextInt(0, size);
            int j = r.nextInt(0, size);
            double v = r.nextDouble(-1, 1);
            res.set(i, j, v);
            res.set(j, i, v);
        }
        for (int i = 0; i < size; i++) {
            double v = 0;
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    v += abs(res.get(i, j));
                }
            }
            res.set(i, i, v + 2);
        }
        return res;
    }

    private DMatrixSparseCSC toSparse(DMatrixRMaj M) {
        DMatrixSparseCSC res = new DMatrixSparseCSC(M.numRows, M.numCols);
        for (int i = 0; i < M.numRows; i++) {
            for (int j = 0; j < M.numCols; j++) {
                double v = M.get(i, j);
                if (v != 0) {
                    res.set(i, j, v);
                }
            }
        }
        return res;
    }

    private DMatrixRMaj fromSparse(DMatrixSparseCSC sparse) {
        DMatrixRMaj dense = new DMatrixRMaj(sparse.numRows, sparse.numCols);
        for (int i = 0; i < sparse.numRows; i++) {
            for (int j = 0; j < sparse.numCols; j++) {
                dense.set(i, j, sparse.get(i, j));
            }
        }
        return dense;
    }
}
