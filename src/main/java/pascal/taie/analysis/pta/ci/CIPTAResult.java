/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2020-- Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020-- Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * Tai-e is only for educational and academic purposes,
 * and any form of commercial use is disallowed.
 * Distribution of Tai-e is disallowed without the approval.
 */

package pascal.taie.analysis.pta.ci;

import pascal.taie.analysis.graph.callgraph.CallGraph;
import pascal.taie.analysis.pta.PointerAnalysisResult;
import pascal.taie.analysis.pta.core.cs.element.ArrayIndex;
import pascal.taie.analysis.pta.core.cs.element.CSCallSite;
import pascal.taie.analysis.pta.core.cs.element.CSMethod;
import pascal.taie.analysis.pta.core.cs.element.CSObj;
import pascal.taie.analysis.pta.core.cs.element.CSVar;
import pascal.taie.analysis.pta.core.cs.element.InstanceField;
import pascal.taie.analysis.pta.core.cs.element.StaticField;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.analysis.pta.plugin.exception.PTAThrowResult;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import pascal.taie.util.collection.MapUtils;
import pascal.taie.util.collection.Pair;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CIPTAResult implements PointerAnalysisResult {

    private final PointerFlowGraph pointerFlowGraph;

    private final CallGraph<Invoke, JMethod> callGraph;

    /**
     * Points-to sets of variables.
     */
    private final Map<Var, Set<Obj>> varPointsTo = MapUtils.newMap();

    /**
     * Points-to sets of field expressions, e.g., v.f.
     */
    private final Map<Pair<Var, JField>, Set<Obj>> fieldPointsTo = MapUtils.newMap();

    private Set<Obj> objects;

    CIPTAResult(PointerFlowGraph pointerFlowGraph,
                CallGraph<Invoke, JMethod> callGraph) {
        this.pointerFlowGraph = pointerFlowGraph;
        this.callGraph = callGraph;
    }

    @Override
    public Stream<Var> vars() {
        return pointerFlowGraph.pointers()
                .filter(p -> p instanceof VarPtr)
                .map(p -> ((VarPtr) p).getVar());
    }

    @Override
    public Stream<Obj> objects() {
        if (objects == null) {
            pointerFlowGraph.pointers()
                    .map(Pointer::getPointsToSet)
                    .flatMap(PointsToSet::objects)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return objects.stream();
    }

    @Override
    public Set<Obj> getPointsToSet(Var var) {

        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Obj> getPointsToSet(Var base, JField field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Obj> getPointsToSet(JField field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallGraph<Invoke, JMethod> getCallGraph() {
        return callGraph;
    }

    // ------------------------------------------
    // Below methods are only for context-sensitive pointer analysis,
    // thus not supported in context-insensitive analysis.
    // ------------------------------------------

    @Override
    public Stream<CSVar> csVars() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<InstanceField> instanceFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<ArrayIndex> arrayIndexes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<StaticField> staticFields() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<CSObj> csObjects() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<CSObj> getPointsToSet(CSVar var) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallGraph<CSCallSite, CSMethod> getCSCallGraph() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PTAThrowResult getThrowResult() {
        throw new UnsupportedOperationException();
    }
}