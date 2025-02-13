package graph.data;

import graph.ICircuitEdge;
import graph.ICircuitNode;
import graph.SchemaGraph;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.graph.TestAcNode;
import org.jgrapht.graph.TestEdge;
import org.jgrapht.graph.TestVertex;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaGraphTestDataProvider {

    private static final Pattern dotEdgeRe = Pattern.compile(
            "^\\s*(\\d+)\\s*(->)?\\s*(\\d+)?\\s*(\\[\\s*label\\s*=\\s*\"(\\d+)\"\\s*])?\\s*$"
    );

    public SchemaGraph<TestVertex, TestEdge> g1() {
        return fromDot("""
                               digraph G {
                                 0 -> 1 [label = "0"]
                                 1 -> 2  [label = "1"]
                                 2 -> 3  [label = "2"]
                                 1 -> 4  [label = "3"]
                                 4 -> 5  [label = "4"]
                                 5 -> 6  [label = "5"]
                                 6 -> 3  [label = "6"]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g2() {
        return fromDot("""
                               digraph G {
                                 0 -> 1  [label = "0"]
                                 1 -> 2  [label = "1"]
                                 0 -> 3  [label = "2"]
                                 3 -> 4  [label = "3"]
                                 4 -> 5  [label = "4"]
                                 5 -> 2  [label = "5"]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g3() {
        return fromDot("""
                               digraph G {
                                 0 -> 1 [label = "0"]
                                 1 -> 2 [label = "1"]
                                 2 -> 3 [label = "2"]
                                 1 -> 4 [label = "3"]
                                 4 -> 5 [label = "4"]
                                 5 -> 6 [label = "5"]
                                 6 -> 3 [label = "6"]
                                 4 -> 6 [label = "7"]
                                 1 -> 7 [label = "8"]
                                 3 -> 7 [label = "9"]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g4() {
        return fromDot("""
                               digraph G {
                                 0
                                 1 -> 2 [label = "1"]
                                 2 -> 3 [label = "2"]
                                 3 -> 1 [label = "3"]
                                 3 -> 4 [label = "4"]
                                 4 -> 3 [label = "5"]
                                 3 -> 4 [label = "6"]
                                 4 -> 2 [label = "7"]
                                 5 -> 6 [label = "8"]
                                 6 -> 7 [label = "9"]
                                 7 -> 6 [label = "10"]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g5() {
        return fromDot("""
                               digraph G {
                                 1  -> 2   [ label="0" ]
                                 1  -> 2   [ label="1" ]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g6() {
        return fromDot("""
                               digraph G {
                                 1  -> 2   [ label="0" ]
                                 1  -> 2   [ label="1" ]
                                 0  -> 1   [ label="2" ]
                                 0  -> 1   [ label="3" ]
                                 2  -> 0   [ label="4"]
                                 2  -> 3   [ label="5"]
                                 2  -> 4   [ label="6"]
                                 2  -> 5   [ label="7"]
                                 2  -> 6   [ label="8"]
                                 7  -> 8   [ label="9"]
                                 7  -> 10  [ label="10"]
                                 7  -> 11  [ label="11"]
                                 7  -> 12  [ label="12"]
                                 7  -> 13  [ label="13"]
                                 8  -> 18  [ label="14"]
                                 8  -> 19  [ label="15"]
                                 8  -> 20  [ label="16"]
                                 8  -> 21  [ label="17"]
                                 7  -> 9   [ label="18"]
                                 8  -> 9   [ label="19"]
                                 22 -> 23 [ label="20"]
                                 26 -> 25 [ label="21"]
                                 28 -> 27 [ label="22"]
                                 30 -> 31 [ label="23"]
                                 30 -> 31 [ label="24"]
                                 29 -> 30 [ label="25"]
                                 29 -> 30 [ label="26"]
                                 31 -> 29 [ label="27"]
                                 31 -> 32 [ label="28"]
                                 31 -> 33 [ label="29"]
                                 31 -> 34 [ label="30"]
                                 31 -> 35 [ label="31"]
                                 0  ->  25  [ label="32"]
                                 25 -> 27 [ label="33"]
                                 27 -> 9  [ label="34"]
                                 9  -> 24  [ label="35"]
                                 24 -> 29 [ label="36"]
                                 3  -> 28  [ label="37"]
                                 28 -> 10 [ label="38"]
                                 18 -> 22 [ label="39"]
                                 22 -> 32 [ label="40"]
                                 4  -> 26  [ label="41"]
                                 26 -> 11 [ label="42"]
                                 19 -> 23 [ label="43"]
                                 23 -> 33 [ label="44"]
                                 5  -> 12  [ label="45"]
                                 20 -> 34 [ label="46"]
                                 6  -> 13  [ label="47"]
                                 21 -> 35 [ label="48"]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g7() {
        return fromDot("""
                               digraph G {
                                 3  -> 2  [ label="0" ]
                                 4  -> 1  [ label="1" ]
                                 2  -> 0  [ label="2" ]
                                 3  -> 4  [ label="3" ]
                                 3  -> 4  [ label="4" ]
                                 2  -> 3  [ label="5" ]
                                 1  -> 5  [ label="6" ]
                                 1  -> 6  [ label="7" ]
                                 4  -> 7  [ label="8" ]
                                 7  -> 8  [ label="9" ]
                                 7  -> 9  [ label="10" ]
                                 12 -> 10 [ label="11" ]
                                 13 -> 11 [ label="12" ]
                                 14 -> 15 [ label="13" ]
                                 14 -> 18 [ label="14" ]
                                 14 -> 19 [ label="15" ]
                                 15 -> 20 [ label="16" ]
                                 15 -> 21 [ label="17" ]
                                 22 -> 23 [ label="18" ]
                                 22 -> 26 [ label="19" ]
                                 22 -> 27 [ label="20" ]
                                 23 -> 28 [ label="21" ]
                                 23 -> 29 [ label="22" ]
                                 32 -> 30 [ label="23" ]
                                 33 -> 31 [ label="24" ]
                                 36 -> 34 [ label="25" ]
                                 37 -> 35 [ label="26" ]
                                 40 -> 38 [ label="27" ]
                                 41 -> 39 [ label="28" ]
                                 42 ->  0 [ label="29" ]
                                 43 ->  0 [ label="30" ]
                                 44 ->  0 [ label="31" ]
                                 47 -> 46 [ label="32" ]
                                 48 -> 45 [ label="33" ]
                                 46 ->  0 [ label="34" ]
                                 48 -> 47 [ label="35" ]
                                 48 -> 47 [ label="36" ]
                                 47 -> 46 [ label="37" ]
                                 45 -> 49 [ label="38" ]
                                 45 -> 50 [ label="39" ]
                                 48 -> 51 [ label="40" ]
                                 51 -> 52 [ label="41" ]
                                 51 -> 53 [ label="42" ]
                                 5  -> 43 [ label="43" ]
                                 43 -> 10 [ label="44" ]
                                 10 -> 18 [ label="45" ]
                                 20 -> 30 [ label="46" ]
                                 30 -> 34 [ label="47" ]
                                 34 -> 38 [ label="48" ]
                                 38 -> 49 [ label="49" ]
                                 6  -> 42 [ label="50" ]
                                 42 -> 11 [ label="51" ]
                                 11 -> 19 [ label="52" ]
                                 21 -> 31 [ label="53" ]
                                 31 -> 44 [ label="54" ]
                                 44 -> 35 [ label="55" ]
                                 35 -> 39 [ label="56" ]
                                 39 -> 50 [ label="57" ]
                                 8  -> 12 [ label="58" ]
                                 12 -> 26 [ label="59" ]
                                 28 -> 32 [ label="60" ]
                                 32 -> 36 [ label="61" ]
                                 36 -> 40 [ label="62" ]
                                 40 -> 52 [ label="63" ]
                                 9  -> 13 [ label="64" ]
                                 13 -> 27 [ label="65" ]
                                 29 -> 33 [ label="66" ]
                                 33 -> 37 [ label="67" ]
                                 37 -> 41 [ label="68" ]
                                 41 -> 53 [ label="69" ]
                               }
                               """);
    }

    public SchemaGraph<TestVertex, TestEdge> g8() {
        return fromDot("""
                               digraph G {
                                 1 -> 2 [ label="0" ]
                                 2 -> 3 [ label="1" ]
                                 1 -> 0 [ label="2" ]
                                 4 -> 0 [ label="3" ]
                                 4 -> 3 [ label="4" ]
                               }
                               """, true);
    }

    @NotNull
    private static <V extends ICircuitNode, E extends ICircuitEdge>
    SchemaGraph<V, E> fromDot(@NotNull String dot) {
        return fromDot(dot, false);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    private static <V extends ICircuitNode, E extends ICircuitEdge>
    SchemaGraph<V, E> fromDot(@NotNull String dot, boolean ac) {
        SchemaGraph<V, E> g = ac
                ? SchemaGraph.ac(10, (V) new TestAcNode(-1, 0))
                : SchemaGraph.dc(10);
        String[] lines = dot.split("\n");
        Map<Integer, V> vertices = new TreeMap<>();
        Map<V, Integer> degrees = new HashMap<>();
        Queue<E> edges = new PriorityQueue<>(Comparator.comparingInt(ICircuitEdge::getIndex));
        IntFunction<V> vertexSupplier = (int index) -> ac
                ? (V) new TestAcNode(index, index)
                : (V) new TestVertex(index, index);
        Supplier<E> edgeSupplier = () -> (E) new TestEdge();
        for (int i = 1; i < lines.length - 1; i++) {
            Matcher m = dotEdgeRe.matcher(lines[i]);
            if (m.find()) {
                V src = m.group(1) != null
                        ? vertices.computeIfAbsent(Integer.parseInt(m.group(1)), vertexSupplier::apply)
                        : null;
                V tgt = m.group(2) != null && m.group(3) != null
                        ? vertices.computeIfAbsent(Integer.parseInt(m.group(3)), vertexSupplier::apply)
                        : null;
                if (src != null && tgt != null && m.group(4) != null && m.group(5) != null) {
                    E e = edgeSupplier.get();
                    e.setSourceNode(src);
                    e.setTargetNode(tgt);
                    e.setIndex(Integer.parseInt(m.group(5)));
                    edges.add(e);
                    degrees.put(src, degrees.getOrDefault(src, 0) + 1);
                    degrees.put(tgt, degrees.getOrDefault(tgt, 0) + 1);
                }
            }
        }
        vertices.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getIndex()))
                .forEach(entry -> {
                    V v = entry.getValue();
                    g.addVertex(v, degrees.getOrDefault(v, 0));
                });
        edges.forEach(e -> g.addEdge((V) e.getSourceNode(), (V) e.getTargetNode(), e));
        return g;
    }
}