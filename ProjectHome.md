A deep ehcache and memcached integration with looking into performance and heavy load.

I hope in future releases it will be part of ehcache trunk.

## Why ehcache? ##

## Why memcached? ##

## Why 'JSE 5 only'? ##
We are looking into the future and always trying to take maximum from platforms and technologies.
If you still on version 1.4 - when you could change your WebSphere to something better :)

## What about performance? ##
http://gregluck.com/blog/archives/2007/05/comparing_memca.html

This is good comparison but it based on wrong idea - all caches must be equal.

For example, we have two servers with eight-core processors each for handling web requests and only 8 Gb of memory. But we could buy some cheap servers with huge amount of memory for shared memory cache. And in this case 'standard' ehcache with replicated memory will be useless.

Much more close to ideal solution for utilizing resources is Coherence. But it characterized with resource wasting (-Xmx512M for JVM is not 512M for cache store) and permanent network activity for discovering nodes and in-process communications.

Memcached is tested, fast and solid solution for storing objects in memory and it could be great extension for ehcache.
One part of this project is 'performance framework' for testing caching efficiency and timings.

Also [PerformanceReport](PerformanceReport.md)