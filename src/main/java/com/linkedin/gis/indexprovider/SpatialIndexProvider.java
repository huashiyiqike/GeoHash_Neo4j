/**
 * Copyright (c) 2010-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.linkedin.gis.indexprovider;

import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.helpers.collection.MapUtil;

import java.util.Collections;
import java.util.Map;

public class SpatialIndexProvider
{

    public static final String SERVICE_NAME = "geo_tri";
    //public static final String GEOMETRY_TYPE = "geometry_type";

    public static final Map<String, String> SIMPLE_POINT_CONFIG =
            Collections.unmodifiableMap( MapUtil.stringMap(
                    IndexManager.PROVIDER, SERVICE_NAME, LayerNodeIndex.LAT_PROPERTY_KEY, "lat", LayerNodeIndex.LON_PROPERTY_KEY, "lon") );
}
