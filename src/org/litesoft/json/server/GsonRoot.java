package org.litesoft.json.server;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import org.litesoft.json.shared.*;
import com.google.gson.*;

import java.lang.reflect.*;
import java.util.*;

public abstract class GsonRoot extends Hierarchical {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); // Reformat "pretty"!

    public static <T> T fromJson( String json, Class<T> classOfT ) {
        return GSON.fromJson( json, classOfT );
    }

    public static String toJson( Object src, Type typeOfSrc ) {
        return GSON.toJson( src, typeOfSrc );
    }

    public static Map<String, String> fromJson( String json ) {
        return Cast.it( fromJson( json, GsonRoot.OrderedMap.class ) );
    }

    public static String toJson( Map<String, String> pMap ) {
        return toJson( pMap, GsonRoot.OrderedMap.class );
    }

    /**
     * This Map causes the entrySet to return the entries in three chunks (w/ each chunk alphabetized by key):
     * 1) Non Lists / Maps
     * 2) Lists
     * 3) Maps
     */
    @SuppressWarnings("NullableProblems")
    public static class OrderedMap extends LinkedHashMap<String, Object> {
        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            List<Map.Entry<String, Object>> zMaps = Lists.newArrayList();
            List<Map.Entry<String, Object>> zLists = Lists.newArrayList();
            List<Map.Entry<String, Object>> zRest = Lists.newArrayList();

            for ( Map.Entry<String, Object> zEntry : super.entrySet() ) {
                String zKey = zEntry.getKey();
                Object zValue = zEntry.getValue();
                if ( zValue instanceof Map ) {
                    addNonNull( zMaps, normalizeMap( zKey, zValue ) );
                } else if ( zValue instanceof List ) {
                    addNonNull( zLists, normalizeList( zKey, zValue ) );
                } else if ( zValue instanceof String ) {
                    addNonNull( zRest, createNonNull( zKey, ConstrainTo.significantOrNull( zValue.toString() ) ) );
                } else {
                    zRest.add( zEntry );
                }
            }
            Set<Map.Entry<String, Object>> zEntries = Sets.newLinkedHashSet( zMaps.size() + zLists.size() + zRest.size() );
            zEntries.addAll( sort( zRest ) );
            zEntries.addAll( sort( zLists ) );
            zEntries.addAll( sort( zMaps ) );
            return zEntries;
        }

        private void addNonNull( List<Map.Entry<String, Object>> pList, Map.Entry<String, Object> pEntry ) {
            if ( pEntry != null ) {
                pList.add( pEntry );
            }
        }

        private void addNonNull( Map<String, Object> pMap, String pKey, Object pValue ) {
            if ( pValue != null ) {
                pMap.put( pKey, pValue );
            }
        }

        private List<Map.Entry<String, Object>> sort( List<Map.Entry<String, Object>> pList ) {
            Collections.sort( pList, new Comparator<Map.Entry<String, Object>>() {
                @Override
                public int compare( Map.Entry<String, Object> o1, Map.Entry<String, Object> o2 ) {
                    return o1.getKey().compareTo( o2.getKey() );
                }
            } );
            return pList;
        }

        private Map.Entry<String, Object> normalizeMap( String pKey, Object pValue ) {
            return createNonNull( pKey, toOrderedMap( pValue ) );
        }

        private Map.Entry<String, Object> normalizeList( String pKey, Object pValue ) {
            return createNonNull( pKey, toOrderedList( pValue ) );
        }

        private Map.Entry<String, Object> createNonNull( String pKey, Object pValue ) {
            return (pValue != null) ? new SimpleEntry<String, Object>( pKey, pValue ) : null;
        }

        private Object toOrderedMap( Object pMap ) {
            Map<String, Object> zMap = Cast.it( pMap );
            OrderedMap zRV = new OrderedMap();
            for ( Map.Entry<String, Object> zEntry : zMap.entrySet() ) {
                String zKey = zEntry.getKey();
                Object zValue = zEntry.getValue();
                if ( zValue instanceof Map ) {
                    addNonNull( zRV, zKey, toOrderedMap( zValue ) );
                } else if ( zValue instanceof List ) {
                    addNonNull( zRV, zKey, toOrderedList( zValue ) );
                } else if ( zValue instanceof String ) {
                    addNonNull( zRV, zKey, ConstrainTo.significantOrNull( zValue.toString() ) );
                } else {
                    addNonNull( zRV, zKey, zValue );
                }
            }
            return zRV.isEmpty() ? null : zRV;
        }

        private Object toOrderedList( Object pValue ) {
            List<Object> zList = Cast.it( pValue );
            if ( zList.isEmpty() ) {
                return null;
            }
            List<Object> zRV = Lists.newArrayList( zList.size() );
            for ( Object zValue : zList ) {
                if ( zValue instanceof Map ) {
                    zValue = toOrderedMap( zValue );
                } else if ( zValue instanceof List ) {
                    zValue = toOrderedList( zValue );
                } else if ( zValue instanceof String ) {
                    zValue = ConstrainTo.significantOrNull( zValue.toString() );
                }
                if ( zValue != null ) {
                    zRV.add( zValue );
                }
            }
            return zRV;
        }
    }
}
