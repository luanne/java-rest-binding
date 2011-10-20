/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.rest.graphdb.traversal;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.converter.TypeInformation;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Hunger
 * @since 03.02.11
 */
public class RestPathParser {


     public static Path parse(Map path, final RestAPI restApi) {
         TypeInformation typeInfo = new TypeInformation(path.get("nodes"));
         if ( Map.class.isAssignableFrom(typeInfo.getGenericArguments()[0])){
            return parseFullPath(path, restApi);
         }
         if (typeInfo.getGenericArguments()[0].equals(String.class)){
             return parsePath(path, restApi);
         }

         throw new IllegalArgumentException("params map contained illegal type "+typeInfo.getGenericArguments()[0]);
    }



    private static Path parseFullPath(Map path, final RestAPI restApi) {
        final Collection<Map<?, ?>> nodesData = (Collection<Map<?, ?>>) path.get("nodes");
        final Collection<Map<?, ?>> relationshipsData = (Collection<Map<?, ?>>) path.get("relationships");
        final Map<?, ?> lastRelationshipData = lastElement(relationshipsData);
        final Map<?, ?> startData = (Map<?, ?>) path.get("start");
        final Map<?, ?> endData = (Map<?, ?>) path.get("end");
        final Integer length = (Integer) path.get("length");

        return new SimplePath(
                new RestNode(startData,restApi),
                new RestNode(endData,restApi),
                new RestRelationship(lastRelationshipData,restApi),
                length,
                new IterableWrapper<Node, Map<?,?>>(nodesData) {
                    @Override
                    protected Node underlyingObjectToObject(Map<?, ?> data) {
                        return new RestNode(data,restApi);
                    }
                },
                new IterableWrapper<Relationship, Map<?,?>>(relationshipsData) {
                    @Override
                    protected Relationship underlyingObjectToObject(Map<?, ?> data) {
                        return new RestRelationship(data,restApi);
                    }
                });
    }

    private static Path parsePath(Map path, final RestAPI restApi){
        final Collection<String> nodesData = (Collection<String>) path.get("nodes");
        final Collection<String> relationshipsData = (Collection<String>) path.get("relationships");
        final String lastRelationshipData = lastElement(relationshipsData);
        final String startData = (String) path.get("start");
        final String endData = (String) path.get("end");
        final Integer length = (Integer) path.get("length");
        return new SimplePath(
                new RestNode(startData,restApi),
                new RestNode(endData,restApi),
                new RestRelationship(lastRelationshipData,restApi),
                length,
                new IterableWrapper<Node, String>(nodesData) {
                    @Override
                    protected Node underlyingObjectToObject(String data) {
                        return new RestNode(data,restApi);
                    }
                },
                new IterableWrapper<Relationship, String>(relationshipsData) {
                    @Override
                    protected Relationship underlyingObjectToObject(String data) {
                        return new RestRelationship(data,restApi);
                    }
                });
    }

    private static String lastElement(Collection<String> collection){
       if (collection.isEmpty()) return null;
       if (collection instanceof List) {
            List<String> list = (List<String>) collection;
            return list.get(list.size()-1);
       }

       return null;
    }

    private static Map<?, ?> lastElement(Collection<Map<?, ?>> collection) {
        if (collection.isEmpty()) return null;
        if (collection instanceof List) {
            List<Map<?,?>> list = (List<Map<?,?>>) collection;
            return list.get(list.size()-1);
        }
        Map<?, ?> result = null;
        for (Map<?, ?> value : collection) {
            result=value;
        }
        return result;
    }
}
