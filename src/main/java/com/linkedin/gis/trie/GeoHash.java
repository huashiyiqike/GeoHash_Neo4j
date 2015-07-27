package com.linkedin.gis.trie;

/**
 * Created by lvqi on 7/13/15.
 */
/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */


import java.lang.System;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("javadoc")
public class GeoHash{
    private static final int MAX_BIT_PRECISION = 64;
    private static final int MAX_CHARACTER_PRECISION = 12;

    private static final int[] BITS = { 16, 8, 4, 2, 1 };
    private static final int BASE32_BITS = 5;
    public static final long FIRST_BIT_FLAGGED = 0x8000000000000000l;
    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private final static Map<Character, Integer> decodeMap = new HashMap<>();

    static {
        int sz = base32.length;
        for (int i = 0; i < sz; i++) {
            decodeMap.put(base32[i], i);
        }
    }

    protected long bits = 0;

    protected byte significantBits = 0;

    protected GeoHash() {
    }

    /**
     * This method uses the given number of characters as the desired precision
     * value. The hash can only be 64bits long, thus a maximum precision of 12
     * characters can be achieved.
     */
    public static GeoHash withCharacterPrecision(double latitude, double longitude, int numberOfCharacters) {
        if (numberOfCharacters > MAX_CHARACTER_PRECISION) {
            throw new IllegalArgumentException("A geohash can only be " + MAX_CHARACTER_PRECISION + " character long.");
        }
        int desiredPrecision = (numberOfCharacters * 5 <= 60) ? numberOfCharacters * 5 : 60;
        return new GeoHash(latitude, longitude, desiredPrecision);
    }

    /**
     * create a new {@link GeoHash} with the given number of bits accuracy. This
     * at the same time defines this hash's bounding box.
     */
    public static GeoHash withBitPrecision(double latitude, double longitude, int numberOfBits) {
        if (numberOfBits > MAX_BIT_PRECISION) {
            throw new IllegalArgumentException("A Geohash can only be " + MAX_BIT_PRECISION + " bits long!");
        }
        if (Math.abs(latitude) > 90.0 || Math.abs(longitude) > 180.0) {
            throw new IllegalArgumentException("Can't have lat/lon values out of (-90,90)/(-180/180)");
        }
        return new GeoHash(latitude, longitude, numberOfBits);
    }

    /**
     * build a new {@link GeoHash} from a base32-encoded {@link String}.<br>
     * This will also set up the hashes bounding box and other values, so it can
     * also be used with functions like within().
     */
    public static GeoHash fromGeohashString(String geohash) {
        double[] latitudeRange = { -90.0, 90.0 };
        double[] longitudeRange = { -180.0, 180.0 };

        boolean isEvenBit = true;
        GeoHash hash = new GeoHash();

        for (int i = 0; i < geohash.length(); i++) {
            int cd = decodeMap.get(geohash.charAt(i));
            for (int j = 0; j < BASE32_BITS; j++) {
                int mask = BITS[j];
                if (isEvenBit) {
                    divideRangeDecode(hash, longitudeRange, (cd & mask) != 0);
                } else {
                    divideRangeDecode(hash, latitudeRange, (cd & mask) != 0);
                }
                isEvenBit = !isEvenBit;
            }
        }

        double latitude = (latitudeRange[0] + latitudeRange[1]) / 2;
        double longitude = (longitudeRange[0] + longitudeRange[1]) / 2;

        hash.bits <<= (MAX_BIT_PRECISION - hash.significantBits);
        return hash;
    }

    public static GeoHash fromLongValue(long hashVal, int significantBits) {
        double[] latitudeRange = { -90.0, 90.0 };
        double[] longitudeRange = { -180.0, 180.0 };

        boolean isEvenBit = true;
        GeoHash hash = new GeoHash();

        String binaryString = Long.toBinaryString(hashVal);
        while (binaryString.length() < MAX_BIT_PRECISION) {
            binaryString = "0" + binaryString;
        }
        for (int j = 0; j < significantBits; j++) {
            if (isEvenBit) {
                divideRangeDecode(hash, longitudeRange, binaryString.charAt(j) != '0');
            } else {
                divideRangeDecode(hash, latitudeRange, binaryString.charAt(j) != '0');
            }
            isEvenBit = !isEvenBit;
        }

        double latitude = (latitudeRange[0] + latitudeRange[1]) / 2;
        double longitude = (longitudeRange[0] + longitudeRange[1]) / 2;

        hash.bits <<= (MAX_BIT_PRECISION - hash.significantBits);
        return hash;
    }

    /**
     * This method uses the given number of characters as the desired precision
     * value. The hash can only be 64bits long, thus a maximum precision of 12
     * characters can be achieved.
     */
    public static String geoHashStringWithCharacterPrecision(double longitude, double latitude, int numberOfCharacters) {
        GeoHash hash = withCharacterPrecision(latitude, longitude, numberOfCharacters);
        return hash.toBase32();
    }

    private GeoHash(double longitude, double latitude, int desiredPrecision) {
        desiredPrecision = Math.min(desiredPrecision, MAX_BIT_PRECISION);

        boolean isEvenBit = true;
        double[] latitudeRange = { -90, 90 };
        double[] longitudeRange = { -180, 180 };

        while (significantBits < desiredPrecision) {
            if (isEvenBit) {
                divideRangeEncode(longitude, longitudeRange);
            } else {
                divideRangeEncode(latitude, latitudeRange);
            }
            isEvenBit = !isEvenBit;
        }

        bits <<= (MAX_BIT_PRECISION - desiredPrecision);
    }

    public long ord() {
        int insignificantBits = MAX_BIT_PRECISION - significantBits;
        return bits >>> insignificantBits;
    }

    /**
     * Returns the number of characters that represent this hash.
     *
     * @throws IllegalStateException
     *             when the hash cannot be encoded in base32, i.e. when the
     *             precision is not a multiple of 5.
     */
    public int getCharacterPrecision() {
        if (significantBits % 5 != 0) {
            throw new IllegalStateException(
                    "precision of GeoHash is not divisble by 5: " + this);
        }
        return significantBits / 5;
    }

    public static GeoHash fromOrd(long ord, int significantBits) {
        int insignificantBits = MAX_BIT_PRECISION - significantBits;
        return fromLongValue(ord << insignificantBits, significantBits);
    }

    private void divideRangeEncode(double value, double[] range) {
        double mid = (range[0] + range[1]) / 2;
        if (value >= mid) {
            addOnBitToEnd();
            range[0] = mid;
        } else {
            addOffBitToEnd();
            range[1] = mid;
        }
    }

    private static void divideRangeDecode(GeoHash hash, double[] range, boolean b) {
        double mid = (range[0] + range[1]) / 2;
        if (b) {
            hash.addOnBitToEnd();
            range[0] = mid;
        } else {
            hash.addOffBitToEnd();
            range[1] = mid;
        }
    }

    /**
     * returns the 8 adjacent hashes for this one. They are in the following
     * order:<br>
     * N, NE, E, SE, S, SW, W, NW
     */
    public GeoHash[] getAdjacent() {
        GeoHash northern = getNorthernNeighbour();
        GeoHash eastern = getEasternNeighbour();
        GeoHash southern = getSouthernNeighbour();
        GeoHash western = getWesternNeighbour();
        return new GeoHash[] { northern, northern.getEasternNeighbour(), eastern, southern.getEasternNeighbour(),
                southern,
                southern.getWesternNeighbour(), western, northern.getWesternNeighbour() };
    }

    /**
     * how many significant bits are there in this {@link GeoHash}?
     */
    public int significantBits() {
        return significantBits;
    }

    public long longValue() {
        return bits;
    }

    /**
     * get the base32 string for this {@link GeoHash}.<br>
     * this method only makes sense, if this hash has a multiple of 5
     * significant bits.
     *
     * @throws IllegalStateException
     *             when the number of significant bits is not a multiple of 5.
     */
    public String toBase32() {
        if (significantBits % 5 != 0) {
            throw new IllegalStateException("Cannot convert a geohash to base32 if the precision is not a multiple of 5.");
        }
        StringBuilder buf = new StringBuilder();

        long firstFiveBitsMask = 0xf800000000000000l;
        long bitsCopy = bits;
        int partialChunks = (int) Math.ceil(((double) significantBits / 5));

        for (int i = 0; i < partialChunks; i++) {
            int pointer = (int) ((bitsCopy & firstFiveBitsMask) >>> 59);
            buf.append(base32[pointer]);
            bitsCopy <<= 5;
        }
        return buf.toString();
    }

    protected GeoHash recombineLatLonBitsToHash(long[] latBits, long[] lonBits) {
        GeoHash hash = new GeoHash();
        boolean isEvenBit = false;
        latBits[0] <<= (MAX_BIT_PRECISION - latBits[1]);
        lonBits[0] <<= (MAX_BIT_PRECISION - lonBits[1]);
        double[] latitudeRange = { -90.0, 90.0 };
        double[] longitudeRange = { -180.0, 180.0 };

        for (int i = 0; i < latBits[1] + lonBits[1]; i++) {
            if (isEvenBit) {
                divideRangeDecode(hash, latitudeRange, (latBits[0] & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED);
                latBits[0] <<= 1;
            } else {
                divideRangeDecode(hash, longitudeRange, (lonBits[0] & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED);
                lonBits[0] <<= 1;
            }
            isEvenBit = !isEvenBit;
        }
        hash.bits <<= (MAX_BIT_PRECISION - hash.significantBits);
        return hash;
    }

    public GeoHash getNorthernNeighbour() {
        long[] latitudeBits = getRightAlignedLatitudeBits();
        long[] longitudeBits = getRightAlignedLongitudeBits();
        latitudeBits[0] += 1;
        latitudeBits[0] = maskLastNBits(latitudeBits[0], latitudeBits[1]);
        return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
    }

    public GeoHash getSouthernNeighbour() {
        long[] latitudeBits = getRightAlignedLatitudeBits();
        long[] longitudeBits = getRightAlignedLongitudeBits();
        latitudeBits[0] -= 1;
        latitudeBits[0] = maskLastNBits(latitudeBits[0], latitudeBits[1]);
        return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
    }

    public GeoHash getEasternNeighbour() {
        long[] latitudeBits = getRightAlignedLatitudeBits();
        long[] longitudeBits = getRightAlignedLongitudeBits();
        longitudeBits[0] += 1;
        longitudeBits[0] = maskLastNBits(longitudeBits[0], longitudeBits[1]);
        return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
    }

    public GeoHash getWesternNeighbour() {
        long[] latitudeBits = getRightAlignedLatitudeBits();
        long[] longitudeBits = getRightAlignedLongitudeBits();
        longitudeBits[0] -= 1;
        longitudeBits[0] = maskLastNBits(longitudeBits[0], longitudeBits[1]);
        return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
    }

    protected long[] getRightAlignedLatitudeBits() {
        long copyOfBits = bits << 1;
        long value = extractEverySecondBit(copyOfBits, getNumberOfLatLonBits()[0]);
        return new long[] { value, getNumberOfLatLonBits()[0] };
    }

    protected long[] getRightAlignedLongitudeBits() {
        long copyOfBits = bits;
        long value = extractEverySecondBit(copyOfBits, getNumberOfLatLonBits()[1]);
        return new long[] { value, getNumberOfLatLonBits()[1] };
    }

    private long extractEverySecondBit(long copyOfBits, int numberOfBits) {
        long value = 0;
        for (int i = 0; i < numberOfBits; i++) {
            if ((copyOfBits & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
                value |= 0x1;
            }
            value <<= 1;
            copyOfBits <<= 2;
        }
        value >>>= 1;
        return value;
    }

    protected int[] getNumberOfLatLonBits() {
        if (significantBits % 2 == 0) {
            return new int[] { significantBits / 2, significantBits / 2 };
        } else {
            return new int[] { significantBits / 2, significantBits / 2 + 1 };
        }
    }

    protected final void addOnBitToEnd() {
        significantBits++;
        bits <<= 1;
        bits = bits | 0x1;
    }

    protected final void addOffBitToEnd() {
        significantBits++;
        bits <<= 1;
    }

    @Override
    public String toString() {
        if (significantBits % 5 == 0) {
            return String.format("%s -> %s", Long.toBinaryString(bits), toBase32());
        } else {
            return String.format("%s, bits: %d", Long.toBinaryString(bits), significantBits);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof GeoHash) {
            GeoHash other = (GeoHash) obj;
            if (other.significantBits == significantBits && other.bits == bits) {
                return true;
            }
        }
        return false;
    }

    private long maskLastNBits(long value, long n) {
        long mask = 0xffffffffffffffffl;
        mask >>>= (MAX_BIT_PRECISION - n);
        return value & mask;
    }

    public GeoHash(long bits, int significantBits){
        this.bits = bits;
        this.significantBits = (byte)significantBits;
    }


    private static int precision = 60;

    public static String getHash(double lon, double lat){
        return new GeoHash(lon, lat,precision).toBase32();
    }

    public static String[] getNeighboor(double lon, double lat, int bit){
//        bit = Math.min(bit,12);
//        GeoHash geo = new GeoHash(lon, lat, bit*5);
//        GeoHash[] nei = geo.getAdjacent();
//
//        int length = nei.length;
//
//        String[] result = new String[length + 1];
//        int i = 0;
//        for(;i < length; i++){
//            result[i] = nei[i].toBase32();
//        }
//        result[i] = geo.toBase32();
//
//        return result;

        bit = Math.min(bit,12);
        GeoHash geo = new GeoHash(lon, lat, bit*5);
        GeoHash[] neighbor = geo.getAdjacent();

        String[] result = new String[neighbor.length+1];
        int i = 0;
        for(; i < neighbor.length; i++){
            result[i] = neighbor[i].toBase32();
        }
        result[i] = geo.toBase32();

        return result;

    }

    public static void main(String[] args){
        GeoHash geo = new GeoHash(10, 10, 50);

        String[] ss = GeoHash.getNeighboor(10, 10,6);

        for(String s : ss) {
            System.out.println(s);
        }
    }

}

