package com.dcarriba.model.graph.dot;

import com.dcarriba.model.graph.Vertex;

import java.io.IOException;
import java.nio.file.Path;

public abstract class DotSerializer<T extends DotSerializable> {

    public abstract String serialize(T dotSerializable);

    public abstract void writeToFile(T dotSerializable, Path outputPath) throws IOException;

    protected String toDotNodeId(Vertex vertex) {
        return Integer.toString(vertex.getId());
    }

    protected String escapeForDot(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
