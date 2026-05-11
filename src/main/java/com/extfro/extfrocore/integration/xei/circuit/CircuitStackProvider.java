package com.extfro.extfrocore.integration.xei.circuit;

import java.util.List;

@FunctionalInterface
public interface CircuitStackProvider {

    List<CircuitStackEntry> getStacks();
}
