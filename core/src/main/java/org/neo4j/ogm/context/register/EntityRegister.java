/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.context.register;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class EntityRegister<T> {

    private final Logger LOGGER = LoggerFactory.getLogger(EntityRegister.class);

    private final Map<T, Object> register = new ConcurrentHashMap<>();

    public Object get(T id) {
        return register.get(id);
    }

    public boolean add(T id, Object entity) {
        final Object existing = register.get(id);

        if (existing != null) {
            LOGGER.debug("Object already in node registry: {}, {}", id, entity);
            return false;
        }

        register.put(id, entity);
        LOGGER.debug("Added object to node registry: {}, {}", id, entity);
        return true;
    }

    public boolean contains(T id) {
        return register.containsKey(id);
    }


    public void remove(T id) {
        LOGGER.debug("Removed object with id {}", id);
        register.remove(id);
    }

    public void clear() {
        LOGGER.debug("Register has been cleared");
        register.clear();
    }

    public Iterator<T> iterator() {
        return register.keySet().iterator();
    }
}
