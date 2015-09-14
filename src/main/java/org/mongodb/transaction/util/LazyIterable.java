/*
 * $URL: http://velo2-kbp.globallogic.com/svn/projects/PTNG/trunk/PTNG/Impl/components/ptngUtils/src/java/com/globallogic/utils/LazyIterable.java $
 * Changed by:      $LastChangedBy: maksym.voroniy $
 * Revision:        $Revision: 3803 $ 
 * Last changed:    $Date: 2011-06-21 14:07:08 +0300 (Tue, 21 Jun 2011) $
 * $Log:$
 * Copyright (C) 2010 GlobalLogic, Inc. All Rights Reserved.
 */
package org.mongodb.transaction.util;

import java.util.*;

/**
 * Converts one iterable (hold type T) to another (hold type V)in lazy way, so
 * container wouldn't extract any data until user explicitly ask it
 */
public class LazyIterable<T, V>  implements Iterable<V>{
    Iterable<T> _other;
    CollectionDelegate<T, Boolean> _filter;
    /**
     * How convert T to V
     */
    CollectionDelegate<T, V>  _eval;

    /**
     *
     * @param other outer iterable to own by this
     * @param eval function to convert values from <b>other</b> to expected by this iterator value
     * @param filter may be null if source and target has the same size
     */
    public LazyIterable(Iterable<T> other, CollectionDelegate<T, V> eval, CollectionDelegate<T, Boolean> filter) {
        this._other = other;
        this._eval = eval;
        this._filter = filter;
    }

    @Override
    public Iterator<V> iterator() {
        return new LazyIterator(_other.iterator());
    }
    class LazyIterator implements Iterator<V>{
        Iterator<T> _other;
        T _fetch;
        LazyIterator(Iterator<T> other) {
            this._other = other;
            fetch();
        }
        void fetch(){
            if(LazyIterable.this._filter == null || _fetch != null)
                return;
            while(_other.hasNext()){
                _fetch = _other.next();
                if( LazyIterable.this._filter.evaluate(_fetch) ){
                    return; //success found item
                } else
                    _fetch = null; //reset to avoid fail on hasNext
            }
        }
        @Override
        public boolean hasNext() {
            return  LazyIterable.this._filter == null ?
                    _other.hasNext() //no filters, just wrap call to other
                    : _fetch != null;
        }

        @Override
        public V next() {
            if( LazyIterable.this._filter != null){
                if( _fetch == null )
                    throw new NoSuchElementException();
                T temp = _fetch;
                _fetch = null;
                fetch();
                return LazyIterable.this._eval.evaluate(temp);
            }
            return LazyIterable.this._eval.evaluate(_other.next());
        }

        @Override
        public void remove() {
            _other.remove();
        }
    }
}
