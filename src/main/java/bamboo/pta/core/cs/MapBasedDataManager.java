/*
 * Bamboo - A Program Analysis Framework for Java
 *
 * Copyright (C) 2020 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2020 Yue Li <yueli@nju.edu.cn>
 * All rights reserved.
 *
 * This software is designed for the "Static Program Analysis" course at
 * Nanjing University, and it supports a subset of Java features.
 * Bamboo is only for educational and academic purposes, and any form of
 * commercial use is disallowed.
 */

package bamboo.pta.core.cs;

import bamboo.pta.core.context.Context;
import bamboo.pta.element.CallSite;
import bamboo.pta.element.Field;
import bamboo.pta.element.Method;
import bamboo.pta.element.Obj;
import bamboo.pta.element.Variable;
import bamboo.pta.set.PointsToSetFactory;
import bamboo.util.CollectionUtils;
import bamboo.util.HybridArrayHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Managing data by maintaining the data and their context-sensitive
 * counterparts by maps.
 */
public class MapBasedDataManager implements DataManager {

    private final Map<Variable, Map<Context, CSVariable>> vars = new HashMap<>();
    private final Map<CSObj, Map<Field, InstanceField>> instanceFields = new HashMap<>();
    private final Map<CSObj, ArrayIndex> arrayIndexes = new HashMap<>();
    private final Map<Field, StaticField> staticFields = new HashMap<>();
    private final Map<Obj, Map<Context, CSObj>> objs = new HashMap<>();
    private final Map<CallSite, Map<Context, CSCallSite>> callSites = new HashMap<>();
    private final Map<Method, Map<Context, CSMethod>> methods = new HashMap<>();
    private PointsToSetFactory setFactory;

    public MapBasedDataManager(PointsToSetFactory setFactory) {
        setPointsToSetFactory(setFactory);
    }

    private static <R, Key1, Key2> R getOrCreateCSElement(
            Map<Key1, Map<Key2, R>> map, Key1 key1, Key2 key2, BiFunction<Key1, Key2, R> creator) {
        return map.computeIfAbsent(key1, k -> new HybridArrayHashMap<>())
                .computeIfAbsent(key2, (k) -> creator.apply(key1, key2));
    }

    @Override
    public void setPointsToSetFactory(PointsToSetFactory setFactory) {
        this.setFactory = setFactory;
    }

    @Override
    public CSVariable getCSVariable(Context context, Variable var) {
        return getOrCreateCSElement(vars, Objects.requireNonNull(var), context,
                (v, c) -> initializePointsToSet(new CSVariable(v, c)));
    }

    @Override
    public InstanceField getInstanceField(CSObj base, Field field) {
        return getOrCreateCSElement(instanceFields, base, field,
                (b, f) -> initializePointsToSet(new InstanceField(b, f)));
    }

    @Override
    public ArrayIndex getArrayIndex(CSObj array) {
        return arrayIndexes.computeIfAbsent(array,
                (a) -> initializePointsToSet(new ArrayIndex(a)));
    }

    @Override
    public StaticField getStaticField(Field field) {
        return staticFields.computeIfAbsent(field,
                (f) -> initializePointsToSet(new StaticField(f)));
    }

    @Override
    public CSObj getCSObj(Context heapContext, Obj obj) {
        return getOrCreateCSElement(objs, obj, heapContext, CSObj::new);
    }

    @Override
    public CSCallSite getCSCallSite(Context context, CallSite callSite) {
        return getOrCreateCSElement(callSites, callSite, context, CSCallSite::new);
    }

    @Override
    public CSMethod getCSMethod(Context context, Method method) {
        return getOrCreateCSElement(methods, method, context, CSMethod::new);
    }

    @Override
    public Stream<CSVariable> getCSVariables() {
        return CollectionUtils.getAllValues(vars);
    }

    @Override
    public Stream<InstanceField> getInstanceFields() {
        return CollectionUtils.getAllValues(instanceFields);
    }

    @Override
    public Stream<ArrayIndex> getArrayIndexes() {
        return arrayIndexes.values().stream();
    }

    @Override
    public Stream<StaticField> getStaticFields() {
        return staticFields.values().stream();
    }

    private <P extends Pointer> P initializePointsToSet(P pointer) {
        pointer.setPointsToSet(setFactory.makePointsToSet());
        return pointer;
    }
}