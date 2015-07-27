package com.linkedin.gis.trie;

/**
 * Created by lvqi on 7/15/15.
 */

import com.linkedin.gis.indexprovider.LayerNodeIndex;
import org.neo4j.graphdb.Node;

/**
 * Created by lvqi on 7/15/15.
 */
public class CalDistance {
    private static final double earthRadiusInKm = 6371;
    public static double calculateDistance(Node src, Node point) {
        Double lon = ConvertUtil.convertObjectToDouble(src.getProperty(LayerNodeIndex.LON_PROPERTY_KEY));
        Double lat = ConvertUtil.convertObjectToDouble(src.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY));
        return calculateDistance(lon, lat, point);
    }
    public static double calculateDistance(Double lon, Double lat, Node point) {
        Double point_lon = ConvertUtil.convertObjectToDouble(point.getProperty(LayerNodeIndex.LON_PROPERTY_KEY));
        Double point_lat = ConvertUtil.convertObjectToDouble(point.getProperty(LayerNodeIndex.LAT_PROPERTY_KEY));
        return calculateDistance(lon, lat, point_lon, point_lat);
    }
    public static double calculateDistance(Double lon, Double lat, Double point_lon, Double point_lat) {
        // TODO use org.geotools.referencing.GeodeticCalculator?
        // d = acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1)) * R
        return Math.acos(Math.sin(Math.toRadians(lat)) * Math.sin(Math.toRadians(point_lat))
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(point_lat))
                * Math.cos(Math.toRadians(point_lon) - Math.toRadians(lon)))
                * earthRadiusInKm;

    }
}