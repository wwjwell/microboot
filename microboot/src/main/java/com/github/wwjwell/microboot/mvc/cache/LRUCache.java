package com.github.wwjwell.microboot.mvc.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lru Cache, 网上贴一个例子，有较大缺陷，非线程安全
 * //FIXME 简单解决可以加锁主要是加锁，还是太重，可能用CAS来解决一下，待扩展
 *
 * Created by wwj on 2017/9/3.
 */
public class LRUCache<K,V>{
    class Node {
        Node pre;
        Node next;
        K key;
        V val;
        Node(K k, V v) {
            this.key = k;
            this.val = v;
        }
    }
    ConcurrentHashMap<K, Node> cacheMap = new ConcurrentHashMap<K, Node>();
    // The head (eldest) of the doubly linked list.
    Node head;
    // The tail (youngest) of the doubly linked list.
    Node tail;
    int cap;
    public LRUCache(int capacity) {
        cap = capacity;
        head = new Node(null, null);
        tail = new Node(null, null);
        head.next = tail;
        tail.pre = head;
    }
    public V get(K key) {
        Node n = cacheMap.get(key);
        if(n!=null) {
            n.pre.next = n.next;
            n.next.pre = n.pre;
            appendTail(n);
            return n.val;
        }
        return null;
    }
    public void set(K key, V value) {
        Node n = cacheMap.get(key);
        // existed
        if(n!=null) {
            n.val = value;
            cacheMap.put(key, n);
            n.pre.next = n.next;
            n.next.pre = n.pre;
            appendTail(n);
            return;
        }
        // else {
        if(cacheMap.size() == cap) {
            Node tmp = head.next;
            head.next = head.next.next;
            head.next.pre = head;
            cacheMap.remove(tmp.key);
        }
        n = new Node(key, value);
        // youngest node append taill
        appendTail(n);
        cacheMap.put(key, n);
    }
    private void appendTail(Node n) {
        n.next = tail;
        n.pre = tail.pre;
        tail.pre.next = n;
        tail.pre = n;
    }
}
