/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */
package org.neo4j.ogm.session.delegates;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Vince Bickers
 */
public class LoadByInstancesDelegate {

    private final Neo4jSession session;

    public LoadByInstancesDelegate(Neo4jSession session) {
        this.session = session;
    }

    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination, int depth) {

        if (objects == null || objects.isEmpty()) {
            return objects;
        }

        // this might lead to supertype having neither primaryIndexField or identityField
        ClassInfo classInfo = findCommonSupertype(objects);

        Set<Serializable> ids = new LinkedHashSet<>();
        for (Object o : objects) {
            FieldInfo idField;
            if (classInfo.hasPrimaryIndexField()) {
                idField = classInfo.primaryIndexField();
            } else {
                idField = classInfo.identityField();
            }
            ids.add((Serializable) idField.readProperty(o));
        }
        return session.loadAll((Class<T>) classInfo.getUnderlyingClass(), ids, sortOrder, pagination, depth);
    }

    private <T> ClassInfo findCommonSupertype(Collection<T> objects) {
        Set<ClassInfo> infos = objects.stream()
            .map(object -> object.getClass()).distinct()
            .map(type -> session.metaData().classInfo(type.getName()))
            .collect(toSet());

        if (infos.size() == 1) {
            return infos.iterator().next();
        } else {

            ClassInfo supertype = null;
            for (ClassInfo info : infos) {

                if (supertype == null) {
                    supertype = getSupertype(info);
                } else {
                    if (!supertype.equals(getSupertype(info))) {
                        throw new MappingException("Can't find single supertype for " + infos);
                    }
                }
            }
            // should we check that if supertype is abstract it has @NodeEntity annotation?
            return supertype;
        }
    }

    private ClassInfo getSupertype(ClassInfo info) {
        ClassInfo current = info;
        while (current.directSuperclass() != null) {
            current = current.directSuperclass();
        }
        return current;
    }

    public <T> Collection<T> loadAll(Collection<T> objects) {
        return loadAll(objects, new SortOrder(), null, 1);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, int depth) {
        return loadAll(objects, new SortOrder(), null, depth);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder) {
        return loadAll(objects, sortOrder, null, 1);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, int depth) {
        return loadAll(objects, sortOrder, null, depth);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination) {
        return loadAll(objects, new SortOrder(), pagination, 1);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, Pagination pagination, int depth) {
        return loadAll(objects, new SortOrder(), pagination, depth);
    }

    public <T> Collection<T> loadAll(Collection<T> objects, SortOrder sortOrder, Pagination pagination) {
        return loadAll(objects, sortOrder, pagination, 1);
    }
}
