package com.dcarriba.model.algorithm;

import com.dcarriba.model.graph.ResidualGraph;

@FunctionalInterface
public interface ResidualGraphObserver {

    ResidualGraphObserver NONE = (algorithmName, step, residualGraph) -> {
    };

    void onResidualGraphStep(String algorithmName, int step, ResidualGraph residualGraph);
}
