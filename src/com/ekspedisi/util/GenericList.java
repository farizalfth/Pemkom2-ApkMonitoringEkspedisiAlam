// FILE: com/ekspedisi/util/GenericList.java
package com.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas list generik untuk menyimpan data.
 * [IMPLEMENTASI] Generic & Serializable
 */
public class GenericList<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> list;

    public GenericList() {
        this.list = new ArrayList<>();
    }
    public void add(T item) { list.add(item); }
    public T get(int index) { return list.get(index); }
    public List<T> getList() { return list; }
    public int size() { return list.size(); }
    public void clear() { list.clear(); }
}