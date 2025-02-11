package org.jgrapht.graph;

public class TestAcNode extends TestVertex {

    public TestAcNode(int index, int lineIndex) {
        super(index, lineIndex);
    }

    @Override
    public int hashCode() {
        return getLineIndex() == 0 ? 0 : super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        TestVertex that = (TestVertex) obj;
        return this.getLineIndex() == 0 && that.getLineIndex() == 0 || this == obj;
    }
}
