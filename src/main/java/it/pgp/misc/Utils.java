package it.pgp.misc;

import java.security.SecureRandom;
import java.util.*;

/**
 * Created by pgp on 06/04/17
 */
public class Utils {

    public static Random r;

    public static void resetRNG() {
        r = new SecureRandom();
    }

    static {
        resetRNG();
    }

    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static <T> T[] concatSkipNulls(T... items) {
        List<T> l = new ArrayList<>();
        for (T item : items) {
            if (item != null) {
                l.add(item);
            }
        }
        return (T[]) l.toArray();
    }

    public static <T> Collection<List<T>> listPartitions(List<T> l, int partitionSize) {
        if (partitionSize < 1) {
            throw new IllegalArgumentException("Invalid partition size: " + partitionSize);
        }
        Collection<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < l.size(); i += partitionSize) {
            partitions.add(l.subList(i,
                    Math.min(i + partitionSize, l.size())));
        }
        return partitions;
    }

    public static <K, V> Collection<Map<K, V>> mapPartitions(Map<K, V> m, int partitionSize) {
        if (partitionSize < 1) {
            throw new IllegalArgumentException("Invalid partition size: " + partitionSize);
        }
        Collection<Map<K, V>> partitions = new ArrayList<>();
        Map<K, V> currentMap = new HashMap<>();
        for (Map.Entry<K, V> entry : m.entrySet()) {
            currentMap.put(entry.getKey(), entry.getValue());
            if (currentMap.size() == partitionSize) {
                partitions.add(currentMap);
                currentMap = new HashMap<>();
            }
        }
        if (!currentMap.isEmpty()) {
            partitions.add(currentMap);
        }
        return partitions;
    }

    public static <T> T randomChoice(T[] keys) {
        return keys[r.nextInt(keys.length)];
    }

    public static <T> T randomChoice(Collection<T> keys) {
        if (keys instanceof List) {
            return ((List<T>) keys).get(r.nextInt(keys.size()));
        }
        int cnt = 0;
        int target = r.nextInt(keys.size());
        for (T key : keys) {
            if (cnt == target) {
                return key;
            }
            cnt++;
        }
        throw new RuntimeException("Should not arrive here");
    }

    static final String alphabet = "qwertyuiopasdfghjklzxcvbnm";

    public static String randString(int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            s.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return s.toString();
    }

    // s1 - s2 != s2 - s1
    // set difference: elements of s1 which are not in s2
    public static <T> Set<T> setDifference(Set<T> s1, Set<T> s2) {
        Set<T> diff = new HashSet<>();
        for (T t : s1) {
            if (!s2.contains(t)) {
                diff.add(t);
            }
        }
        return diff;
    }

    public static <T> Set<T> setUnion(Set<T> s1, Set<T> s2) {
        Set<T> union = new HashSet<>();
        union.addAll(s1);
        union.addAll(s2);
        return union;
    }

    public static <T> Set<T> setIntersection(Set<T> s1, Set<T> s2) {
        Set<T> intersection = new HashSet<>();
        for (T t : s1) {
            if (s2.contains(t)) {
                intersection.add(t);
            }
        }
        return intersection;
    }

    public static <K, V> Map.Entry<K, V> getLast(Map<K, V> map) {
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        Map.Entry<K, V> result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
        }
        return result;
    }

    public static <K, V> Map.Entry<K, V> getFirst(Map<K, V> map) {
        Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        Map.Entry<K, V> result = null;
        result = iterator.next();
        return result;
    }

    public enum NumberOutType {
        INT,
        LONG,
        DOUBLE;
    }

    public static Number parseNumber(Object number, NumberOutType... targetType) {
        if (number == null) {
            return null; // explicit type conversion to interface for null inputs
        }
        if (number instanceof String) {
            if (targetType.length > 0) {
                try {
                    switch (targetType[0]) {
                        case INT:
                            return Integer.parseInt((String) number);
                        case LONG:
                            return Long.parseLong((String) number);
                        case DOUBLE:
                            return Double.parseDouble((String) number);
                    }
                }
                catch (Exception e) {
                    return null;
                }
            }
            else {
                try {
                    return Integer.valueOf((String) number);
                }
                catch (Exception ignored) {
                }
                try {
                    return Double.valueOf((String) number);
                }
                catch (Exception e) {
                    return null;
                }
            }
        }
        else if (number instanceof Number) {
            if (targetType.length > 0) {
                switch (targetType[0]) {
                    case INT:
                        return ((Number) number).intValue();
                    case LONG:
                        return ((Number) number).longValue();
                    case DOUBLE:
                        return ((Number) number).doubleValue();
                }
            }
            else {
                return (Number) number;
            }
        }
        return null;
    }

    public static Map mapOf(Object... keyValueItems) {
        if (keyValueItems.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even (key1, value1, ..., key_n, value_n)");
        }
        return new HashMap<Object, Object>() {
            {
                for (int i = 0; i < keyValueItems.length; i += 2) {
                    put(keyValueItems[i], keyValueItems[i + 1]);
                }
            }
        };
    }

    public static Map nonnullMapOf(boolean nonNullKeys, boolean nonNullValues, Object... keyValueItems) {
        if (keyValueItems.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even (key1, value1, ..., key_n, value_n)");
        }

        return new HashMap<Object, Object>() {
            {
                for (int i = 0; i < keyValueItems.length; i += 2) {
                    if (nonNullKeys && keyValueItems[i] == null) {
                        continue;
                    }
                    if (nonNullValues && keyValueItems[i + 1] == null) {
                        continue;
                    }
                    put(keyValueItems[i], keyValueItems[i + 1]);
                }
            }
        };
    }

    // only for functional syntactic sugar facilities
    public static <K, V> Map.Entry<K, V> Pair(K k, V v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    public static <K, V> Map<K, V> typedMapOf(Object... keyValueItems) {
        if (keyValueItems.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even (key1, value1, ..., key_n, value_n)");
        }
        return new HashMap<K, V>() {
            {
                for (int i = 0; i < keyValueItems.length; i += 2) {
                    put((K) (keyValueItems[i]), (V) (keyValueItems[i + 1]));
                }
            }
        };
    }

    // for one-liners with lambda expressions
    public static <K, V> Map<K, V> typedMapOf(Map.Entry<K, V>... pairs) {
        return new HashMap<K, V>() {
            {
                for (Map.Entry<K, V> pair : pairs) {
                    put(pair.getKey(), pair.getValue());
                }
            }
        };
    }

    // signature ambiguity with Map(bool,bool), use Collections.singletonMap instead
    public static <K, V> Map<K, V> nonNullTypedMapOf(boolean nonNullKeys, boolean nonNullValues, Object... keyValueItems) {
        if (keyValueItems.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even (key1, value1, ..., key_n, value_n)");
        }

        return new HashMap<K, V>() {
            {
                for (int i = 0; i < keyValueItems.length; i += 2) {
                    if (nonNullKeys && keyValueItems[i] == null) {
                        continue;
                    }
                    if (nonNullValues && keyValueItems[i + 1] == null) {
                        continue;
                    }
                    put((K) keyValueItems[i], (V) keyValueItems[i + 1]);
                }
            }
        };
    }

    public static <K, V> Map<K, V> nonNullTypedMapOf(boolean nonNullKeys, boolean nonNullValues, Map.Entry<K, V>... pairs) {
        return new HashMap<K, V>() {
            {
                for (Map.Entry<K, V> pair : pairs) {
                    if (nonNullKeys && pair.getKey() == null) {
                        continue;
                    }
                    if (nonNullValues && pair.getValue() == null) {
                        continue;
                    }
                    put(pair.getKey(), pair.getValue());
                }
            }
        };
    }

    public static Object callConstructorUnchecked(Class type) {
        try {
            return type.newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    @SafeVarargs
    public static <T> Set<T> mergeAllIntoSet(Collection<T>... s) {
        Set<T> set = new HashSet<>();
        for (Collection<T> x : s) {
            set.addAll(x);
        }
        return set;
    }

    @SafeVarargs
    public static <T> List<T> mergeAllIntoList(Collection<T>... l) {
        List<T> list = new ArrayList<>();
        for (Collection<T> x : l) {
            list.addAll(x);
        }
        return list;
    }

    public static Boolean stringCompare(String a, String b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static boolean isAllNulls(Iterable<?> array) {
        for (Object element : array) {
            if (element != null) {
                return false;
            }
        }
        return true;
    }

    public static <T> T[] truncateOnFirstNull(T[] input) {
        for (int i = 0; i < input.length; i++) {
            if (input[i] == null) {
                return Arrays.copyOf(input, i);
            }
        }
        return input;
    }

    public static void sleepNoThrow(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ignored) {
        }
    }

    public static void sleepNoThrowSetInterrupt(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
