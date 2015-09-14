package org.mongodb.transaction.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Groups utility method to work with generic collections/enumeration/maps in manner of delegates
 */
public abstract class CollectionDelegate<T, V> {
    /**
     * Identity evaluator, the evaluated object is the given object.
     */
    public static final CollectionDelegate IDENTITY_EVALUATOR = new CollectionDelegate<Object, Object>(){
        @Override
        public Object evaluate(Object o) {
            return o;
        }
    };

    /**
     * Predicate that returns true for any entry
     */
    public static final CollectionDelegate TRUE = new CollectionDelegate<Object, Boolean>(){
        @Override
        public Boolean evaluate(Object o) {
            return Boolean.TRUE;
        }
    };
    
    /**
     * Predicate that returns false for any entry
     */
    public static final CollectionDelegate FALSE = new CollectionDelegate<Object, Boolean>(){
        @Override
        public Boolean evaluate(Object o) {
            return Boolean.FALSE;
        }
    };
    /**
     * Predicate returns true if value is not null
     */
    public static final CollectionDelegate NOT_NULL = new CollectionDelegate<Object, Boolean>(){
        @Override
        public Boolean evaluate(Object o) {
            return o != null;
        }
    };
    /**
     * Delegate method to extract value V from argument T
     *
     * @param t source element to extract V
     * @return converted value from t
     */
    public abstract V evaluate(T t);


    /**
     * find element from input collection by applying to each one predicate specified by parameter p
     *
     * @param source source collection of items
     * @param p      predicate to be applied to each element of source until it return true
     * @param <T>    predicate to apply, it must return true to stop iteration or false to continue
     * @return null if no elements found, otherwise succeeded element
     */
    public static <T> T find(Iterable<T> source, CollectionDelegate<T, Boolean> p) {
        for (T t : source) {
            if (p.evaluate(t))
                return t;
        }
        return null;
    }

    /**
     * Absolutely the same as #find, but instead of returning instance of T just returns it index
     * @param source source collection of items
     * @param p      predicate to be applied to each element of source until it return true
     * @param <T>    predicate to apply, it must return true to stop iteration or false to continue
     * @return index of found item or -1 if element has not been found
     */
    public static <T> int locate(Iterable<T> source, CollectionDelegate<T, Boolean> p)
    {
        Iterator<T> iter = source.iterator();
        for (int i = 0; iter.hasNext(); ++i)
        {
            if (p.evaluate(iter.next()))
                return i;
        }
        return -1;
    }
    /**
     * find element from input array by applying to each one predicate specified by parameter p
     *
     * @param source if argument is null, the null is returned
     * @param p      predicate to be applied to each element of source until it return true
     * @param <T>    predicate to apply, it must return true to stop iteration or false to continue
     * @return index of found result or (-1) if no such one
     */
    public static <T> int find(T[] source, CollectionDelegate<T, Boolean> p) {
        if (source == null)
            return -1;
        for (int i = 0;i < source.length; ++i) {
            if (p.evaluate(source[i]))
                return i;
        }
        return -1;
    }
    /**
     * Apply predicate for any item in source. Predicate can stop iteration by returning false
     *
     * @param source if argument is null, the null is returned
     * @param p      predicate to be applied to each element of source, you can return true or null to prolonging iteration.
     *               To stop iteration return false
     * @param <T>    predicate to apply
     * @return index number of iterated items
     */
    public static <T> int forEach(Iterable<T> source, CollectionDelegate<T, Boolean> p) {
        if (source == null)
            return 0;
        int count = 0;
        for (T t : source) {
            ++count;
            Boolean x = p.evaluate(t);
            if (x!=null && !x)
                break;
        }
        return count;
    }
    
    /**
     * Counts number of items in 'source' that matches to predicate 'p'
     *
     * @param source source collection of items
     * @param p      predicate to be applied to each element of source. If it returns true element is counted
     * @param <T>    type of item in source collection
     * @return number of items satisfied criteria
     */
    public static <T> int count(Iterable <T> source, CollectionDelegate<T, Boolean> p) {
        int retval = 0;
        for (T t : source) {
            if (p.evaluate(t)) {
                ++retval;
            }
        }
        return retval;
    }
    /**
     * find and remove single element from input collection by applying to each one predicate specified by parameter p
     *
     * @param source source collection of items
     * @param p      predicate to be applied to each element of source. If it returns true element is removed and method
     *               stops
     * @param <T>    predicate to apply, it must return true to stop iteration or false to continue
     * @return null if no elements found, otherwise removed element
     */
    public static <T> T remove(Iterable<T> source, CollectionDelegate<T, Boolean> p) {
        for (Iterator<T> li = source.iterator(); li.hasNext();) {
            T t = li.next();
            if (p.evaluate(t)) {
                li.remove();
                return t;
            }
        }
        return null;
    }
    /**
     * Removes all entries in list that satisfies predicate criteria
     *
     * @param source source list of items
     * @param p      predicate to be applied to each element of source. If it returns true element is removed
     * @param <T>    element of collection and source iteration
     * @return number of removed elements
     */
    public static <T> int removeThat(Iterable<T> source, CollectionDelegate<T, Boolean> p) {
        int result = 0;
        for (Iterator<T> li = source.iterator(); li.hasNext();) {
            T t = li.next();
            if (p.evaluate(t)) {
                li.remove();
                ++result;
            }
        }
        return result;
    }

    /**
     * Add elements from 'source' to 'dest' that satisfies to criteria specified by 'p'
     * @param source elements
     * @param dest destination collection to extend, (previously contained items ARE NOT ERASED)
     * @param p predicate to apply, it must return true to add element to target
     * @param <T> element of collection and source iteration
     */
    public static <T> void add(Iterable<? extends T> source, Collection<T> dest, CollectionDelegate<T, Boolean> p){
        for(T t : source){
            if(p.evaluate(t))
                dest.add(t);
        }
    }

    /**
     * Convert one map to another either by changing of type or by filtering pairs
     * @param source input map
     * @param dest result map
     * @param evalKey functor that converts source map entry to dest key, use {@link #IDENTITY_EVALUATOR} to copy as is
     * @param evalVal functor that converts source map entry to dest value, use {@link #IDENTITY_EVALUATOR} to copy as is
     * @param filter predicate to check if value should be placed to dest. May be null skip filtering
     * @param <K1> key-type in source map
     * @param <V1> value-type is source map
     * @param <K2> key-type in dest map
     * @param <V2> value-type in dest map
     * @return dest map
     */
    public static <K1, V1, K2, V2> Map<K2, V2> toMap(Map<K1, V1> source, Map<K2, V2> dest,
                                         CollectionDelegate<K1, K2> evalKey,
                                         CollectionDelegate<V1, V2> evalVal,
                                         CollectionDelegate<Map.Entry<K1, V1>, Boolean> filter){
        for(Map.Entry<K1, V1> kv: source.entrySet()){
            if(filter != null && filter.evaluate(kv))
                dest.put(evalKey.evaluate(kv.getKey()), evalVal.evaluate(kv.getValue()));
        }
        return dest;
    }
    /**
     * Converts source to map, using as a key value calculated by evaluator
     *
     * @param source input that is converted to map
     * @param dest   map populated with pair: eval(T), T
     * @param eval   delegate to calculate key from source
     * @param <T>    element of source
     * @param <V>    key-type extracted by argument eval (and key in dest map)
     * @param filter optional filter (may be null) if some records from source shouldn't be included to result, just
     *               implement and return true - to include or false to exclude
     * @return dest parameter
     */
    public static <T, V> Map<V, T> toMap(
            Iterable<T> source, Map<V, T> dest, CollectionDelegate<T, V> eval, CollectionDelegate<T, Boolean> filter) {
        for (T t : source) {
            if (filter == null || filter.evaluate(t))
                dest.put(eval.evaluate(t), t);
        }
        return dest;
    }

    /**
     * Converts source to map, using as a key value calculated by evaluator and as value calculated also
     *
     * @param source    input that is converted to map
     * @param dest      map populated with pair: evalKey(T), evalVal(T)
     * @param evalKey   evaluator of key. It is convert T to map's key type
     * @param evalValue evaluator of value. It is convert T to map's value type
     * @param filter    optional predicate that check if input T should be placed to result map
     * @param <T>       element of source
     * @param <K>       key type of result map
     * @param <V>       value type of result map
     * @return dest parameter
     */
    public static <T, K, V> Map<K, V> toMap(Iterable<T> source, Map<K, V> dest, CollectionDelegate<T, K> evalKey,
                                            CollectionDelegate<T, V> evalValue, CollectionDelegate<T, Boolean> filter) {
        for (T t : source) {
            if (filter == null || filter.evaluate(t))
                dest.put(evalKey.evaluate(t), evalValue.evaluate(t));
        }
        return dest;
    }

    /**
     * Converts source to set using conversion rule specified by eval
     *
     * @param source input source that is converted to dest set
     * @param dest   destination instance of set. It will be populated by converted value
     * @param eval   evaluator that converts input type T to dest type V
     * @param filter may be null. Used to reject some inputs from copying to dest
     * @param <T>    source type
     * @param <V>    destination type
     * @return the value of dest parameter
     */
    public static <T, V> Set<V> toSet(Iterable<T> source, Set<V> dest, CollectionDelegate<T, V> eval,
                                      CollectionDelegate<T, Boolean> filter) {
        for (T t : source) {
            if (filter == null || filter.evaluate(t))
                dest.add(eval.evaluate(t));
        }
        return dest;
    }

    /**
     * Converts source to set using conversion rule specified by eval
     *
     * @param source input source that is converted to dest set
     * @param dest   destination instance of set. It will be populated by converted value
     * @param eval   evaluator that converts input type T to dest type V
     * @param <T>    source type
     * @param <V>    destination type
     * @return the value of dest parameter
     */
    public static <T, V> Collection<V> toCollection(Iterable<T> source, Collection<V> dest, CollectionDelegate<T, V> eval) {
        return toCollection(source, dest, eval, null);
    }

    /**
     * Convert one input source to another by applying to each item converter, specified by eval param
     * @param source input source that is converted to dest
     * @param dest   destination instance of collection. It will be populated by converted value
     * @param eval evaluator that converts input type T to dest type V
     * @param filter may be null. Used to reject some inputs from copying to dest
     * @param <T>    source type
     * @param <V>    destination type
     * @return the value of dest parameter
     */
    public static <T, V> Collection<V> toCollection(
            Iterable<T> source,
            Collection<V> dest,
            CollectionDelegate<T, V> eval,
            CollectionDelegate<T, Boolean> filter) {
        for(T t : source){
            if( filter == null || filter.evaluate(t)){
                dest.add(eval.evaluate(t));
            }
        }
        return dest;
    }

    /**
     * Converts one iterable to another. Dramatic change from #toCollection that this method produces lazy iterable -
     * that wouldn't extract any data until user explicitly ask it
     * @param source input source that is converted to returning value
     * @param eval evaluator that converts input type T to dest type V
     * @param <T>    source type
     * @param <V>    destination type
     * @return lazy iterable
     */
    public static <T, V> Iterable<V> toIterable(Iterable<T> source, CollectionDelegate<T, V> eval){
        return new LazyIterable<T, V>(source, eval, null);
    }
    /**
     * Converts one iterable to another. Dramatic change from #toCollection that this method produces lazy iterable -
     * that wouldn't extract any data until user explicitly ask it
     * @param source input source that is converted to returning value
     * @param eval evaluator that converts input type T to dest type V
     * @param filter may be null. Used to reject some inputs from copying to dest
     * @param <T>    source type
     * @param <V>    destination type
     * @return lazy iterable
     */
    public static <T, V> Iterable<V> toIterable(Iterable<T> source, CollectionDelegate<T, V> eval,
                                                CollectionDelegate<T, Boolean> filter){
        return new LazyIterable<T, V>(source, eval, filter);
    }
    
    /**
     * * Converts source to map, using as a key value calculated by evaluator and as value calculated also
     *
     * @param source input source that is converted to dest
     * @param dest result map where values are grouped by key and placed as multi-value to container
     * @param evalKey   evaluator of key. It is convert T to map's key type
     * @param evalValue evaluator of value. It is convert T to map's value type
     * @param filter may be null. Used to reject some inputs from copying to dest
     * @param <T> type of entry in source container
     * @param <K> type of result map key
     * @param <V> type of resul multi-map value
     * @return 'dest' parameter
     */
    public static <T, K, V, Co extends Collection> Map<K, ? extends Co> groupBy(
            Iterable<T> source,
            Class<Co> container,
            Map<K, Co> dest,
            CollectionDelegate<T, K> evalKey,
            CollectionDelegate<T, V> evalValue,
            CollectionDelegate<T, Boolean> filter) {
        for (T t : source) {
            if (filter == null || filter.evaluate(t)) {
                K k = evalKey.evaluate(t);

                Co result = (Co) dest.get(k);
                if (result == null) {
                    try {
                        result = container.newInstance();
                    }
                    catch (Exception e) {
                        throw new IllegalArgumentException("Cannot create instance from:" + container, e);
                    }
                    dest.put(k, result);
                }
                result.add(evalValue.evaluate(t));
            }
        }
        return dest;
    }
}
