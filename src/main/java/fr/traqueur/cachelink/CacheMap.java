package fr.traqueur.cachelink;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface CacheMap<K,V> {

    void put(K key, V value);

    V get(K key);

    V getOrDefault(K key, V defaultValue);

    void remove(K key);

    boolean containsKey(K key);

    boolean containsValue(V value);

    int size();

    void clear();

    boolean isEmpty();

    Collection<K> keys();

    Collection<V> values();

    void forEach(BiConsumer<? super K, ? super V> action);

    void putAll(CacheMap<K,V> map);



}
