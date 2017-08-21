package Cache;

import java.util.ArrayList;


import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

public class Cache<K, T> {
	
    // Class for storing object in cache
    // We use this so that we can store access times
    // and any further information
    protected class CacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;
 
        protected CacheObject(T value) {
            this.value = value;
        }
        
        public int hashcode() {
        	return this.value.hashCode();
        }
    }
    
    private long timeToLive;
    private LRUMap<K, CacheObject> CacheMap; //Cache datastructure
 
    public Cache(long TimeToLive, final long TimerInterval, int maxItems) {
        this.timeToLive = TimeToLive * 1000;
 
        CacheMap = new LRUMap<K, CacheObject>(maxItems);
 
        if (timeToLive > 0 && TimerInterval > 0) {
 
            // Periodically cleanup cache and remove expired objects
            Thread t = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(TimerInterval * 1000);
                        } catch (InterruptedException ex) {
                        	
                        }
                        cleanup();
                    }
                }
            });
 
            t.setDaemon(true);
            t.start();
        }
    }
 
    @SuppressWarnings("unchecked")
    public void put(K key, T value) {
        synchronized (CacheMap) {
            CacheMap.put(key, new CacheObject(value));
        }
    }
 
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (CacheMap) {
            CacheObject c = (CacheObject) CacheMap.get(key);
 
            if (c == null)
                return null;
            else {
                c.lastAccessed = System.currentTimeMillis();
                return c.value;
            }
        }
    }
 
    public void remove(K key) {
        synchronized (CacheMap) {
            CacheMap.remove(key);
        }
    }
 
    public int size() {
        synchronized (CacheMap) {
            return CacheMap.size();
        }
    }
    
    public boolean expired(K key) {
    	synchronized (CacheMap) {
    		CacheObject c = (CacheObject) CacheMap.get(key);
    		if ((c == null) || (System.currentTimeMillis() > (timeToLive + c.lastAccessed)))
                return true;
            else {
                return false;
            }
    	}  	
    }
 

    // Remove expired objects from cache
    @SuppressWarnings("unchecked")
    public void cleanup() {
 
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey = null;
 
        synchronized (CacheMap) {
            MapIterator<K, CacheObject> itr = CacheMap.mapIterator();
 
            deleteKey = new ArrayList<K>((CacheMap.size() / 2) + 1);
            K key = null;
            CacheObject c = null;
 
            while (itr.hasNext()) {
                key = (K) itr.next();
                c = (CacheObject) itr.getValue();
 
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
 
        for (K key : deleteKey) {
            synchronized (CacheMap) {
                CacheMap.remove(key);
            }
 
            Thread.yield();
        }
    }
}
