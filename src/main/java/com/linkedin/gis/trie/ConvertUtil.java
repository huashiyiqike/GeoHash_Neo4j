package com.linkedin.gis.trie;

public class ConvertUtil {
    public static Double convertObjectToDouble(Object obj) {
        if (obj == null) {
            return 0.0;
        }

        if (obj instanceof Double) {
            return (Double) obj;
        }

        try {
            return Double.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}