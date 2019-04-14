package dreamwalker.com.mypictureclient.utils

class Cache<K, V> {
    private val mKVHashMap: HashMap<K, V> = HashMap()

    @Synchronized
    operator fun get(key: K): V? {
        return mKVHashMap[key]
    }

    @Synchronized
    fun put(key: K, value: V) {
        mKVHashMap[key] = value
    }
}