### Version 0.1 (Feb, 15 2007) ###

ehcache from this version is fully compatible with ehcache 1.4 - don't need to change API usage or recompile existed code.
One major difference is new configuration parameter:
```
    <diskStore path="java.io.tmpdir"
	       store="com.danga.jmemcached.ehcache.MemcachedStore"/>
```

> With parameter _store_ you could specify custom store implementation that could be used instead of default `DiskStore`. Default `DiskStore` implementation will be used if custom store is not specified.

Changes in memcached client are much more solid.

  1. Main package for client was changed to `com.danga.jmemcached`
  1. Added maven project model.
  1. Added simple stub implementation for ehcache `Store`.
  1. Removed internal logger and attached `commons-logging`.
  1. Added xml configuration for memcached pool - [look at example](http://ehmem.googlecode.com/svn/java_memcached/trunk/src/main/config/memcached.xml)