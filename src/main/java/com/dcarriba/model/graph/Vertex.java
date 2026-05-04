package com.dcarriba.model.graph;

public class Vertex {
    private final int id;
    private String label;

    public Vertex(int id) {
        this.id = id;
        this.label = Integer.toString(id);
    }

    public Vertex(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Vertex vertex)) {
            return false;
        }

        return id == vertex.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
