/**
 * MemCached Java client, connection pool for Socket IO
 * Copyright (c) 2007 Greg Whalin
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the BSD license
 *
 * This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 *
 * You should have received a copy of the BSD License along with this
 * library.
 *
 * @author greg whalin <greg@meetup.com>
 * @version 2.0
 */
package com.danga.jmemcached;

import static com.danga.jmemcached.PoolConfigurationWrapper.INIT_CONNECTIONS;
import static com.danga.jmemcached.PoolConfigurationWrapper.MAINT_SLEEP;
import static com.danga.jmemcached.PoolConfigurationWrapper.MAX_CONNECTIONS;
import static com.danga.jmemcached.PoolConfigurationWrapper.MAX_IDLE;
import static com.danga.jmemcached.PoolConfigurationWrapper.MIN_CONNECTIONS;
import static com.danga.jmemcached.PoolConfigurationWrapper.NAGLE;
import static com.danga.jmemcached.PoolConfigurationWrapper.SOCKET_CONNECT_TIMEOUT;
import static com.danga.jmemcached.PoolConfigurationWrapper.SOCKET_TIMEOUT;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.CRC32;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class is a connection pool for maintaning a pool of persistent connections<br/>
 * to memcached servers.
 *
 * The pool must be initialized prior to use. This should typically be early on<br/>
 * in the lifecycle of the JVM instance.<br/>
 * <br/>
 * <h3>An example of initializing using defaults:</h3>
 * <pre>
 *
 *	static {
 *		String[] serverlist = { "cache0.server.com:12345", "cache1.server.com:12345" };
 *
 *		SockIOPool pool = SockIOPool.getInstance();
 *		pool.setServers(serverlist);
 *		pool.initialize();
 *	}
 * </pre>
 * <h3>An example of initializing using defaults and providing weights for servers:</h3>
 *  <pre>
 *	static {
 *		String[] serverlist = { "cache0.server.com:12345", "cache1.server.com:12345" };
 *		Integer[] weights   = { new Integer(5), new Integer(2) };
 *
 *		SockIOPool pool = SockIOPool.getInstance();
 *		pool.setServers(serverlist);
 *		pool.setWeights(weights);
 *		pool.initialize();
 *	}
 *  </pre>
 * <h3>An example of initializing overriding defaults:</h3>
 *  <pre>
 *	static {
 *		String[] serverlist     = { "cache0.server.com:12345", "cache1.server.com:12345" };
 *		Integer[] weights       = { new Integer(5), new Integer(2) };
 *		int initialConnections  = 10;
 *		int minSpareConnections = 5;
 *		int maxSpareConnections = 50;
 *		long maxIdleTime        = 1000 * 60 * 30;	// 30 minutes
 *		long maxBusyTime        = 1000 * 60 * 5;	// 5 minutes
 *		long maintThreadSleep   = 1000 * 5;			// 5 seconds
 *		int	socketTimeOut       = 1000 * 3;			// 3 seconds to block on reads
 *		int	socketConnectTO     = 1000 * 3;			// 3 seconds to block on initial connections.  If 0, then will use blocking connect (default)
 *		boolean failover        = false;			// turn off auto-failover in event of server down
 *		boolean nagleAlg        = false;			// turn off Nagle's algorithm on all sockets in pool
 *		boolean aliveCheck      = false;			// disable health check of socket on checkout
 *
 *		SockIOPool pool = SockIOPool.getInstance();
 *		pool.setServers( serverlist );
 *		pool.setWeights( weights );
 *		pool.setInitConn( initialConnections );
 *		pool.setMinConn( minSpareConnections );
 *		pool.setMaxConn( maxSpareConnections );
 *		pool.setMaxIdle( maxIdleTime );
 *		pool.setMaxBusyTime( maxBusyTime );
 *		pool.setMaintSleep( maintThreadSleep );
 *		pool.setSocketTO( socketTimeOut );
 *		pool.setNagle( nagleAlg );
 *		pool.setHashingAlg( SockIOPool.NEW_COMPAT_HASH );
 *		pool.setAliveCheck( true );
 *		pool.initialize();
 *	}
 *  </pre>
 * The easiest manner in which to initialize the pool is to set the servers and rely on defaults as in the first example.<br/>
 * After pool is initialized, a client will request a SockIO object by calling getSock with the cache key<br/>
 * The client must always close the SockIO object when finished, which will return the connection back to the pool.<br/>
 * <h3>An example of retrieving a SockIO object:</h3>
 * <pre>
 *		SockIOPool.SockIO sock = SockIOPool.getInstance().getSock( key );
 *		try {
 *			sock.write( "version\r\n" );
 *			sock.flush();
 *			System.out.println( "Version: " + sock.readLine() );
 *		}
 *		catch (IOException ioe) { System.out.println( "io exception thrown" ) };
 *
 *		sock.close();
 * </pre>
 *
 * @author greg whalin <greg@whalin.com>
 * @version 1.5
 */
public class SockIOPool {
	// logger
	private static Log log = LogFactory.getLog(SockIOPool.class);

	// store instances of pools
	private static Map<String,SockIOPool> pools = new HashMap<String,SockIOPool>();

	// avoid recurring construction
	private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance( "MD5" );
			}
			catch ( NoSuchAlgorithmException e ) {
				log.error("No md5 algorithm found");
				throw new IllegalStateException("No md5 algorythm found");
			}
		}
	};

	// Constants
	public static final int NATIVE_HASH     = 0;				// native String.hashCode();
	public static final int OLD_COMPAT_HASH = 1;				// original compatibility hashing algorithm (works with other clients)
	public static final int NEW_COMPAT_HASH = 2;				// new CRC32 based compatibility hashing algorithm (works with other clients)
	public static final int CONSISTENT_HASH = 3;				// MD5 Based -- Stops thrashing when a server added or removed

	public static final long MAX_RETRY_DELAY = 10 * 60 * 1000;  // max of 10 minute delay for fall off


	// Pool data
	private MaintThread maintThread;
	private boolean initialized        = false;
	private int maxCreate              = 1;					// this will be initialized by pool when the pool is initialized
	private Map<String,Integer> createShift;

	// initial, min and max pool sizes
	private int poolMultiplier        = 3;
	private int initConn              = 1;
	private int minConn               = 1;
	private int maxConn               = 10;
	private long maxIdle              = 1000 * 60 * 5;		// max idle time for avail sockets
	private long maxBusyTime          = 1000 * 30;			// max idle time for avail sockets
	private long maintSleep           = 1000 * 30;			// maintenance thread sleep time
	private int socketTO              = 1000 * 30;			// default timeout of socket reads
	private int socketConnectTO       = 1000 * 3;	        // default timeout of socket connections
	private boolean aliveCheck        = false;				// default to not check each connection for being alive
	private boolean failover          = true;				// default to failover in event of cache server dead
	private boolean failback          = true;				// only used if failover is also set ... controls putting a dead server back into rotation
	private boolean nagle             = true;				// enable/disable Nagle's algorithm
	private int hashingAlg 		      = NATIVE_HASH;		// default to using the native hash as it is the fastest

	// locks
	private final ReentrantLock hostDeadLock = new ReentrantLock();
	
	private String name;
	
	// list of all servers
	private String[] servers;
	private Integer[] weights;
	private Integer totalWeight = 0;

	private List<String> buckets;
	private TreeMap<Long,String> consistentBuckets;

	// dead server map
	private Map<String,Date> hostDead;
	private Map<String,Long> hostDeadDur;

	// map to hold all available sockets
	// map to hold busy sockets
	// set to hold sockets to close
	private Map<String, Queue<SockIO>> availableSockets;
    private Map<String, Queue<SockIO>> busySockets;
	private Queue<SockIO> deadSockets;

	private AtomicInteger referenceCounter = new AtomicInteger();
	
	// Configuration
    private static final String DEFAULT_CONFIG = "/memcached.xml";
    private static boolean configLoaded = false;
    private static Document config;
    
	// empty constructor
	protected SockIOPool() { }

	/**
	 * Factory to create/retrieve new pools given a unique poolName.
	 *
	 * @param poolName unique name of the pool
	 * @return instance of SockIOPool
	 */
	public static synchronized SockIOPool getInstance(String poolName) {
		if (pools.containsKey(poolName)) {
			return pools.get(poolName);
		}

		SockIOPool pool = new SockIOPool();
		Document doc = loadConfiguration();
		
		pool.name = poolName;
		
		if (doc != null) {
		    initPool(pool, new PoolConfigurationWrapper(doc, poolName));
		}
		
		pools.put(poolName, pool);

		return pool;
	}

	/**
	 * Single argument version of factory used for back compat.
	 * Simply creates a pool named "default".
	 *
	 * @return instance of SockIOPool
	 */
	public static SockIOPool getInstance() {
		return getInstance( "default" );
	}

	/**
	 * Sets the list of all cache servers.
	 *
	 * @param servers String array of servers [host:port]
	 */
	public void setServers( String[] servers ) { this.servers = servers; }

	/**
	 * Returns the current list of all cache servers.
	 *
	 * @return String array of servers [host:port]
	 */
	public String[] getServers() { return this.servers; }

	/**
	 * Sets the list of weights to apply to the server list.
	 *
	 * This is an int array with each element corresponding to an element<br/>
	 * in the same position in the server String array.
	 *
	 * @param weights Integer array of weights
	 */
	public void setWeights( Integer[] weights ) { this.weights = weights; }

	/**
	 * Returns the current list of weights.
	 *
	 * @return int array of weights
	 */
	public Integer[] getWeights() { return this.weights; }

	/**
	 * Sets the initial number of connections per server in the available pool.
	 *
	 * @param initConn int number of connections
	 */
	public void setInitConn( int initConn ) { this.initConn = initConn; }

	/**
	 * Returns the current setting for the initial number of connections per server in
	 * the available pool.
	 *
	 * @return number of connections
	 */
	public int getInitConn() { return this.initConn; }

	/**
	 * Sets the minimum number of spare connections to maintain in our available pool.
	 *
	 * @param minConn number of connections
	 */
	public void setMinConn( int minConn ) { this.minConn = minConn; }

	/**
	 * Returns the minimum number of spare connections in available pool.
	 *
	 * @return number of connections
	 */
	public int getMinConn() { return this.minConn; }

	/**
	 * Sets the maximum number of spare connections allowed in our available pool.
	 *
	 * @param maxConn number of connections
	 */
	public void setMaxConn( int maxConn ) { this.maxConn = maxConn; }

	/**
	 * Returns the maximum number of spare connections allowed in available pool.
	 *
	 * @return number of connections
	 */
	public int getMaxConn() { return this.maxConn; }

	/**
	 * Sets the max idle time for threads in the available pool.
	 *
	 * @param maxIdle idle time in ms
	 */
	public void setMaxIdle( long maxIdle ) { this.maxIdle = maxIdle; }

	/**
	 * Returns the current max idle setting.
	 *
	 * @return max idle setting in ms
	 */
	public long getMaxIdle() { return this.maxIdle; }

	/**
	 * Sets the max busy time for threads in the busy pool.
	 *
	 * @param maxBusyTime idle time in ms
	 */
	public void setMaxBusyTime( long maxBusyTime ) { this.maxBusyTime = maxBusyTime; }

	/**
	 * Returns the current max busy setting.
	 *
	 * @return max busy setting in ms
	 */
	public long getMaxBusy() { return this.maxBusyTime; }

	/**
	 * Set the sleep time between runs of the pool maintenance thread.
	 * If set to 0, then the maint thread will not be started.
	 *
	 * @param maintSleep sleep time in ms
	 */
	public void setMaintSleep( long maintSleep ) { this.maintSleep = maintSleep; }

	/**
	 * Returns the current maint thread sleep time.
	 *
	 * @return sleep time in ms
	 */
	public long getMaintSleep() { return this.maintSleep; }

	/**
	 * Sets the socket timeout for reads.
	 *
	 * @param socketTO timeout in ms
	 */
	public void setSocketTO( int socketTO ) { this.socketTO = socketTO; }

	/**
	 * Returns the socket timeout for reads.
	 *
	 * @return timeout in ms
	 */
	public int getSocketTO() { return this.socketTO; }

	/**
	 * Sets the socket timeout for connect.
	 *
	 * @param socketConnectTO timeout in ms
	 */
	public void setSocketConnectTO( int socketConnectTO ) { this.socketConnectTO = socketConnectTO; }

	/**
	 * Returns the socket timeout for connect.
	 *
	 * @return timeout in ms
	 */
	public int getSocketConnectTO() { return this.socketConnectTO; }

	/**
	 * Sets the failover flag for the pool.
	 *
	 * If this flag is set to true, and a socket fails to connect,<br/>
	 * the pool will attempt to return a socket from another server<br/>
	 * if one exists.  If set to false, then getting a socket<br/>
	 * will return null if it fails to connect to the requested server.
	 *
	 * @param failover true/false
	 */
	public void setFailover( boolean failover ) { this.failover = failover; }

	/**
	 * Returns current state of failover flag.
	 *
	 * @return true/false
	 */
	public boolean getFailover() { return this.failover; }

	/**
	 * Sets the failback flag for the pool.
	 *
	 * If this is true and we have marked a host as dead,
	 * will try to bring it back.  If it is false, we will never
	 * try to resurrect a dead host.
	 *
	 * @param failback true/false
	 */
	public void setFailback( boolean failback ) { this.failback = failback; }

	/**
	 * Returns current state of failover flag.
	 *
	 * @return true/false
	 */
	public boolean getFailback() { return this.failback; }

	/**
	 * Sets the aliveCheck flag for the pool.
	 *
	 * When true, this will attempt to talk to the server on
	 * every connection checkout to make sure the connection is
	 * still valid.  This adds extra network chatter and thus is
	 * defaulted off.  May be useful if you want to ensure you do
	 * not have any problems talking to the server on a dead connection.
	 *
	 * @param aliveCheck true/false
	 */
	public void setAliveCheck( boolean aliveCheck ) { this.aliveCheck = aliveCheck; }


	/**
	 * Returns the current status of the aliveCheck flag.
	 *
	 * @return true / false
	 */
	public boolean getAliveCheck() { return this.aliveCheck; }

	/**
	 * Sets the Nagle alg flag for the pool.
	 *
	 * If false, will turn off Nagle's algorithm on all sockets created.
	 *
	 * @param nagle true/false
	 */
	public void setNagle( boolean nagle ) { this.nagle = nagle; }

	/**
	 * Returns current status of nagle flag
	 *
	 * @return true/false
	 */
	public boolean getNagle() { return this.nagle; }

	/**
	 * Sets the hashing algorithm we will use.
	 *
	 * The types are as follows.
	 *
	 * SockIOPool.NATIVE_HASH (0)     - native String.hashCode() - fast (cached) but not compatible with other clients
	 * SockIOPool.OLD_COMPAT_HASH (1) - original compatibility hashing alg (works with other clients)
	 * SockIOPool.NEW_COMPAT_HASH (2) - new CRC32 based compatibility hashing algorithm (fast and works with other clients)
	 *
	 * @param alg int value representing hashing algorithm
	 */
	public void setHashingAlg( int alg ) { this.hashingAlg = alg; }

	/**
	 * Returns current status of customHash flag
	 *
	 * @return true/false
	 */
	public int getHashingAlg() { return this.hashingAlg; }

	/**
	 * Internal private hashing method.
	 *
	 * This is the original hashing algorithm from other clients.
	 * Found to be slow and have poor distribution.
	 *
	 * @param key String to hash
	 * @return hashCode for this string using our own hashing algorithm
	 */
	private static long origCompatHashingAlg( String key ) {
		long hash   = 0;
		char[] cArr = key.toCharArray();

		for ( int i = 0; i < cArr.length; ++i ) {
			hash = (hash * 33) + cArr[i];
		}

		return hash;
	}

	/**
	 * Internal private hashing method.
	 *
	 * This is the new hashing algorithm from other clients.
	 * Found to be fast and have very good distribution.
	 *
	 * UPDATE: This is dog slow under java
	 *
	 * @param key
	 * @return
	 */
	private static long newCompatHashingAlg( String key ) {
		CRC32 checksum = new CRC32();
		checksum.update( key.getBytes() );
		long crc = checksum.getValue();
		return (crc >> 16) & 0x7fff;
	}

	/**
	 * Internal private hashing method.
	 *
	 * MD5 based hash algorithm for use in the consistent
	 * hashing approach.
	 *
	 * @param key
	 * @return
	 */
	private static long md5HashingAlg( String key ) {
		MessageDigest md5 = MD5.get();
		md5.reset();
		md5.update( key.getBytes() );
		byte[] bKey = md5.digest();
		long res = ((long)(bKey[3]&0xFF) << 24) | ((long)(bKey[2]&0xFF) << 16) | ((long)(bKey[1]&0xFF) << 8) | (long)(bKey[0]&0xFF);
		return res;
	}

	/**
	 * Returns a bucket to check for a given key.
	 *
	 * @param key String key cache is stored under
	 * @return int bucket
	 */
	private long getHash( String key, Integer hashCode ) {

		if ( hashCode != null ) {
			if ( hashingAlg == CONSISTENT_HASH )
				return hashCode.longValue() & 0xffffffffL;
			else
				return hashCode.longValue();
		}
		else {
			switch ( hashingAlg ) {
				case NATIVE_HASH:
					return (long)key.hashCode();
				case OLD_COMPAT_HASH:
					return origCompatHashingAlg( key );
				case NEW_COMPAT_HASH:
					return newCompatHashingAlg( key );
				case CONSISTENT_HASH:
					return md5HashingAlg( key );
				default:
					// use the native hash as a default
					hashingAlg = NATIVE_HASH;
					return (long)key.hashCode();
			}
		}
	}

	private long getBucket( String key, Integer hashCode ) {
		long hc = getHash( key, hashCode );

		if ( this.hashingAlg == CONSISTENT_HASH ) {
			return findPointFor( hc );
		}
		else {
			long bucket = hc % buckets.size();
			if ( bucket < 0 ) bucket *= -1;
			return bucket;
		}
	}

	/**
	 * Gets the first available key equal or above the given one, if none found,
	 * returns the first k in the bucket
	 * @param k key
	 * @return
	 */
	private Long findPointFor( Long hv ) {
		// this works in java 6, but still want to release support for java5
		//Long k = this.consistentBuckets.ceilingKey( hv );
		//return ( k == null ) ? this.consistentBuckets.firstKey() : k;

		SortedMap<Long,String> tmap =
			this.consistentBuckets.tailMap( hv );

		return ( tmap.isEmpty() ) ? this.consistentBuckets.firstKey() : tmap.firstKey();
	}

	/**
	 * Initializes the pool.
	 */
	public void initialize() {
		synchronized( this ) {
			// check to see if already initialized
			if ( initialized
					&& ( buckets != null || consistentBuckets != null )
					&& ( availableSockets != null )
					&& ( busySockets != null ) ) {
				log.error("Trying to initialize an already initialized pool");
				
				return;
			}

            // if servers is not set, or it empty, then
            // throw a runtime exception
            if ( servers == null || servers.length <= 0 ) {
                log.error("Trying to initialize with no servers");

                throw new IllegalStateException("Trying to initialize with no servers");
            }

			// pools
			availableSockets = new HashMap<String, Queue<SockIO>>();
			busySockets = new HashMap<String, Queue<SockIO>>();
			deadSockets = new ConcurrentLinkedQueue<SockIO>();

			hostDeadDur = new ConcurrentHashMap<String,Long>();
			hostDead    = new ConcurrentHashMap<String,Date>();
			createShift = new ConcurrentHashMap<String,Integer>();
			maxCreate   = (poolMultiplier > minConn) ? minConn : minConn / poolMultiplier;		// only create up to maxCreate connections at once

			log.debug("Initializing pool with following settings:" );
			log.debug("Initial size: " + initConn );
			log.debug("Min spare   : " + minConn );
			log.debug("Max spare   : " + maxConn );

			// initalize our internal hashing structures
			if (this.hashingAlg == CONSISTENT_HASH) {
				populateConsistentBuckets();
			} else {
				populateBuckets();
			}
			
			// mark pool as initialized
			this.initialized = true;

			// start maint thread
			if (this.maintSleep > 0) {
				this.startMaintThread();
			}
		}
	}

	private void populateBuckets() {
		log.debug("Initializing internal buckets for simple hashing");

		// store buckets in tree map
		this.buckets = new ArrayList<String>();

		for ( int i = 0; i < servers.length; i++ ) {
			if ( this.weights != null && this.weights.length > i ) {
				for ( int k = 0; k < this.weights[i].intValue(); k++ ) {
					this.buckets.add(servers[i]);
					
					log.debug("Added " + servers[i] + " to server bucket");
				}
			} else {
				this.buckets.add(servers[i]);
				
				log.debug("Added " + servers[i] + " to server bucket");
			}

			// create initial connections
			log.debug("Creating initial connections (" + initConn + ") for host: " + servers[i]);

			for ( int j = 0; j < initConn; j++ ) {
				SockIO socket = createSocket(servers[i]);
				
				if (socket == null) {
					log.error("Failed to create connection to: " + servers[i] + " -- only " + j + " created.");
					
					break;
				}

				addSocketToPool(availableSockets, servers[i], socket);
				
				log.debug("Created and added socket: " + socket.toString() + " for host " + servers[i]);
			}
		}
	}

	private void populateConsistentBuckets() {
		log.debug("Initializing internal hashing structure for consistent hashing");

		// store buckets in tree map
		this.consistentBuckets = new TreeMap<Long,String>();

		MessageDigest md5 = MD5.get();
		if ( this.totalWeight <= 0 && this.weights !=  null ) {
			for ( int i = 0; i < this.weights.length; i++ )
				this.totalWeight += ( this.weights[i] == null ) ? 1 : this.weights[i];
		}
		else if ( this.weights == null ) {
			this.totalWeight = this.servers.length;
		}

		for ( int i = 0; i < servers.length; i++ ) {
			int thisWeight = 1;
			if ( this.weights != null && this.weights[i] != null )
				thisWeight = this.weights[i];

			double factor = Math.floor( ((double)(40 * this.servers.length * thisWeight)) / (double)this.totalWeight );

			for ( long j = 0; j < factor; j++ ) {
				byte[] d = md5.digest( ( servers[i] + "-" + j ).getBytes() );
				for ( int h = 0 ; h < 4; h++ ) {
					Long k =
						  ((long)(d[3+h*4]&0xFF) << 24)
						| ((long)(d[2+h*4]&0xFF) << 16)
						| ((long)(d[1+h*4]&0xFF) << 8)
						| ((long)(d[0+h*4]&0xFF));

					consistentBuckets.put( k, servers[i] );
					log.debug( "++++ added " + servers[i] + " to server bucket" );
				}
			}

			// create initial connections
			log.debug( "+++ creating initial connections (" + initConn + ") for host: " + servers[i] );

			for ( int j = 0; j < initConn; j++ ) {
				SockIO socket = createSocket( servers[i] );
				if ( socket == null ) {
					log.error( "++++ failed to create connection to: " + servers[i] + " -- only " + j + " created." );
					break;
				}

				addSocketToPool(availableSockets, servers[i], socket );
				log.debug( "++++ created and added socket: " + socket.toString() + " for host " + servers[i] );
			}
		}
	}

	/**
	 * Returns state of pool.
	 *
	 * @return <CODE>true</CODE> if initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Creates a new SockIO obj for the given server.
	 *
	 * If server fails to connect, then return null and do not try<br/>
	 * again until a duration has passed.  This duration will grow<br/>
	 * by doubling after each failed attempt to connect.
	 *
	 * @param host host:port to connect to
	 * @return SockIO obj or null if failed to create
	 */
	protected SockIO createSocket( String host ) {

		SockIO socket = null;

		// if host is dead, then we don't need to try again
		// until the dead status has expired
		// we do not try to put back in if failback is off
		hostDeadLock.lock();
		try {
			if ( failback && hostDead.containsKey( host ) && hostDeadDur.containsKey( host ) ) {

				Date store  = hostDead.get( host );
				long expire = hostDeadDur.get( host ).longValue();

				if ( (store.getTime() + expire) > System.currentTimeMillis() )
					return null;
			}
		}
		finally {
			hostDeadLock.unlock();
		}

		try {
			socket = new SockIO( this, host, this.socketTO, this.socketConnectTO, this.nagle );

			if ( !socket.isConnected() ) {
				log.error("Failed to get SockIO obj for: " + host + " -- new socket is not connected");
				
				deadSockets.add(socket);
				
				socket = null;
			}
		} catch (Exception ex) {
			log.error("Failed to get SockIO obj for: " + host, ex);

			socket = null;
		}

		// if we failed to get socket, then mark
		// host dead for a duration which falls off
		hostDeadLock.lock();
		
		try {
			if ( socket == null ) {
				Date now = new Date();
				hostDead.put( host, now );

				long expire = ( hostDeadDur.containsKey( host ) ) ? (((Long)hostDeadDur.get( host )).longValue() * 2) : 1000;

				if ( expire > MAX_RETRY_DELAY )
					expire = MAX_RETRY_DELAY;

				hostDeadDur.put(host, new Long(expire));

				log.info("Ignoring dead host: " + host + " for " + expire + " ms");

				// also clear all entries for this host from availPool
				clearHostFromPool(availableSockets, host);
			} else {
				log.debug("Created socket (" + socket.toString() + ") for host: " + host);
				
				if (hostDead.containsKey(host) || hostDeadDur.containsKey(host) ) {
					hostDead.remove(host);
					hostDeadDur.remove(host);
				}
			}
		}
		finally {
			hostDeadLock.unlock();
		}

		return socket;
	}

 	/**
	 * @param key
	 * @return
	 */
	public String getHost( String key ) {
		return getHost( key, null );
	}

	/**
	 * Gets the host that a particular key / hashcode resides on.
	 *
	 * @param key
	 * @param hashcode
	 * @return
	 */
	public String getHost( String key, Integer hashcode ) {
		SockIO socket = getSock( key, hashcode );
		String host = socket.getHost();
		socket.close();
		return host;
	}

	/**
	 * Returns appropriate SockIO object given
	 * string cache key.
	 *
	 * @param key hashcode for cache key
	 * @return SockIO obj connected to server
	 */
	public SockIO getSock( String key ) {
		return getSock( key, null );
	}

	/**
	 * Returns appropriate SockIO object given
	 * string cache key and optional hashcode.
	 *
	 * Trys to get SockIO from pool.  Fails over
	 * to additional pools in event of server failure.
	 *
	 * @param key hashcode for cache key
	 * @param hashCode if not null, then the int hashcode to use
	 * @return SockIO obj connected to server
	 */
	public SockIO getSock( String key, Integer hashCode ) {
		if (log.isDebugEnabled()) {
		    log.debug("Cache socket pick " + key + " " + hashCode );
		}

		if ( !this.initialized ) {
			log.error( "attempting to get SockIO from uninitialized pool!" );
			return null;
		}

		// if no servers return null
		if ( ( this.hashingAlg == CONSISTENT_HASH && consistentBuckets.size() == 0 )
				|| ( buckets != null && buckets.size() == 0 ) )
			return null;

		// if only one server, return it
		if ( ( this.hashingAlg == CONSISTENT_HASH && consistentBuckets.size() == 0 )
				|| ( buckets != null && buckets.size() == 1 ) ) {

			SockIO sock = ( this.hashingAlg == CONSISTENT_HASH )
				? getConnection( consistentBuckets.get( consistentBuckets.firstKey() ) )
				: getConnection( buckets.get(0) );

			if ( sock != null && sock.isConnected() ) {
				if ( aliveCheck ) {
					if ( !sock.isAlive() ) {
						sock.close();
						try {
						    sock.trueClose(); 
						} catch (IOException ioe) {
						    log.error("failed to close dead socket"); 
						}
						
						sock = null;
					}
				}
			} else {
				if (sock != null) {
					deadSockets.add(sock);
					
					sock = null;
				}
			}

			return sock;
		}

		// from here on, we are working w/ multiple servers
		// keep trying different servers until we find one
		// making sure we only try each server one time
		Set<String> tryServers = new HashSet<String>( Arrays.asList( servers ) );

		// get initial bucket
		long bucket = getBucket( key, hashCode );
		String server = ( this.hashingAlg == CONSISTENT_HASH )
			? consistentBuckets.get( bucket )
			: buckets.get( (int)bucket );

		while ( !tryServers.isEmpty() ) {

			// try to get socket from bucket
			SockIO sock = getConnection( server );
			
			if (log.isDebugEnabled()) {
			    log.debug("Cache choose " + server + " for " + key);
			}

			if ( sock != null && sock.isConnected() ) {
				if ( aliveCheck ) {
					if ( sock.isAlive() ) {
						return sock;
					}
					else {
						sock.close();
						try { sock.trueClose(); } catch ( IOException ioe ) { log.error( "failed to close dead socket" ); }
						sock = null;
					}
				}
				else {
					return sock;
				}
			} else {
				if ( sock != null ) {
					deadSockets.add(sock);
					
					sock = null;
				}
			}

			// if we do not want to failover, then bail here
			if ( !failover )
				return null;

			// log that we tried
			tryServers.remove( server );

			if ( tryServers.isEmpty() )
				break;

			// if we failed to get a socket from this server
			// then we try again by adding an incrementer to the
			// current key and then rehashing
			int rehashTries = 0;
			while ( !tryServers.contains( server ) ) {

				String newKey = String.format( "%s%s", rehashTries, key );
				
				if (log.isDebugEnabled()) {
				    log.debug("rehashing with: " + newKey);
				}
				
				bucket = getBucket( newKey, null );
				server = ( this.hashingAlg == CONSISTENT_HASH )
					? consistentBuckets.get( bucket )
					: buckets.get( (int)bucket );

				rehashTries++;
			}
		}

		return null;
	}

	/**
	 * Returns a SockIO object from the pool for the passed in host.
	 *
	 * Meant to be called from a more intelligent method<br/>
	 * which handles choosing appropriate server<br/>
	 * and failover.
	 *
	 * @param host host from which to retrieve object
	 * @return SockIO object or null if fail to retrieve one
	 */
	public SockIO getConnection( String host ) {
		if (!this.initialized) {
			log.error("Attempting to get SockIO from uninitialized pool");
			
			return null;
		}

		if (host == null) {
			return null;
		}

		// if we have items in the pool
		// then we can return it
		if (availableSockets != null) {
		    Queue<SockIO> queue = availableSockets.get(host);
		    
		    if (queue != null) {
                SockIO socket = queue.poll();
                
                if (socket != null) {
                    if (socket.isConnected()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Moving socket for host (" + host + ") to busy pool ... socket: " + socket);
                        }
                        
                        // add to busy pool
                        addSocketToPool(busySockets, host, socket);

                        // return socket
                        return socket;
                    } else {
                        // add to deadpool for later reaping
                        deadSockets.add(socket);
                    }
                }		    
		    }
		}

		// create one socket -- let the maint thread take care of creating more
		SockIO socket = createSocket(host);
		
		if (socket != null) {
		    addSocketToPool(busySockets, host, socket);
		}

		return socket;
	}

	/**
	 * Adds a socket to a given pool for the given host.
	 * THIS METHOD IS NOT THREADSAFE, SO BE CAREFUL WHEN USING!
	 *
	 * Internal utility method.
	 *
	 * @param pool pool to add to
	 * @param host host this socket is connected to
	 * @param socket socket to add
	 */
	protected void addSocketToPool(Map<String, Queue<SockIO>> pool, String host, SockIO socket ) {
	    socket.touch();
	    
	    if (pool.containsKey(host)) {
			Queue<SockIO> sockets = pool.get(host);

			if (sockets != null) {
				sockets.add(socket);
				
				return;
			}
		}

        Queue<SockIO> sockets = new ConcurrentLinkedQueue<SockIO>();

		sockets.add(socket);
		
		pool.put(host, sockets);
	}

	/**
	 * Removes a socket from specified pool for host.
	 * THIS METHOD IS NOT THREADSAFE, SO BE CAREFUL WHEN USING!
	 *
	 * Internal utility method.
	 *
	 * @param pool pool to remove from
	 * @param host host pool
	 * @param socket socket to remove
	 */
	protected void removeSocketFromPool(Map<String, Queue<SockIO>> pool, String host, SockIO socket ) {
		if (pool.containsKey(host)) {
			Queue<SockIO> sockets = pool.get(host);
			
			if (sockets != null) {
				sockets.remove(socket);
			}
		}
	}

	/**
	 * Closes and removes all sockets from specified pool for host.
	 * THIS METHOD IS NOT THREADSAFE, SO BE CAREFUL WHEN USING!
	 *
	 * Internal utility method.
	 *
	 * @param pool pool to clear
	 * @param host host to clear
	 */
	protected void clearHostFromPool(Map<String, Queue<SockIO>> pool, String host ) {
		if (pool.containsKey(host)) {
			Queue<SockIO> sockets = pool.get(host);

			if (sockets != null) {
			    SockIO socket;
                
			    while ((socket = sockets.poll()) != null) {
                    try {
                        socket.trueClose();
                    } catch ( IOException ioe ) {
                        log.error("Failed to close socket for host " + host, ioe);
                    }
			    }
			}
		}
	}

	/**
	 * Checks a SockIO object in with the pool.
	 *
	 * This will remove SocketIO from busy pool, and optionally<br/>
	 * add to avail pool.
	 *
	 * @param socket socket to return
	 * @param addToAvail add to avail pool if true
	 */
	private void checkIn(SockIO socket, boolean addToAvail) {
		String host = socket.getHost();
		
		if (log.isDebugEnabled()) {
		    log.debug("Calling check-in on socket: " + socket + " for host: " + host);

		    // remove from the busy pool
		    log.debug("Removing socket (" + socket + ") from busy pool for host: " + host);
		}
		
		removeSocketFromPool(busySockets, host, socket);

		if ( socket.isConnected() && addToAvail ) {
		    // add to avail pool
		    if (log.isDebugEnabled()) {
		        log.debug("Returning socket " + socket + " to avail pool for host: " + host);
		    }
		    
		    addSocketToPool(availableSockets, host, socket);
		} else {
		    deadSockets.add(socket);
		    
		    socket = null;
		}
	}

	/**
	 * Returns a socket to the avail pool.
	 *
	 * This is called from SockIO.close().  Calling this method<br/>
	 * directly without closing the SockIO object first<br/>
	 * will cause an IOException to be thrown.
	 *
	 * @param socket socket to return
	 */
	private void checkIn( SockIO socket ) {
		checkIn( socket, true );
	}

	/**
	 * Closes all sockets in the passed in pool.
	 *
	 * Internal utility method.
	 *
	 * @param pool pool to close
	 */
	protected void closePool(Map<String, Queue<SockIO>> pool) {
		for (Entry<String, Queue<SockIO>> entry : pool.entrySet()) {
		    final String host = entry.getKey();
		    final Queue<SockIO> queue = entry.getValue();
		    
            SockIO socket;
            
            while ((socket = queue.poll()) != null) {
                try {
                    socket.trueClose();
                } catch (IOException ioe) {
                    log.error("Failed to trueClose socket: " + socket.toString() + " for host: " + host);
                }
            }
		}
	}

	/**
	 * Shuts down the pool.
	 *
	 * Cleanly closes all sockets.<br/>
	 * Stops the maint thread.<br/>
	 * Nulls out all internal maps<br/>
	 */
	public void shutDown() {
		synchronized( this ) {
			log.debug("SockIOPool shutting down...");

			if ( maintThread != null && maintThread.isRunning() ) {
				// stop the main thread
				stopMaintThread();

				// wait for the thread to finish
				while (maintThread.isRunning()) {
					log.debug("Waiting for main thread to finish run");
					
					try {
					    Thread.sleep(500); 
					} catch (InterruptedException ex) {
					    break;
					}
				}
			}

			log.debug("Closing all internal pools.");
			
			closePool(availableSockets);
			closePool(busySockets);
			availableSockets = null;
			busySockets = null;
			buckets           = null;
			consistentBuckets = null;
			hostDeadDur       = null;
			hostDead          = null;
			maintThread       = null;
			initialized       = false;
			
			log.debug("SockIOPool finished shutting down.");
		}
	}

	/**
	 * Starts the maintenance thread.
	 *
	 * This thread will manage the size of the active pool<br/>
	 * as well as move any closed, but not checked in sockets<br/>
	 * back to the available pool.
	 */
	protected void startMaintThread() {
		if ( maintThread != null ) {

			if ( maintThread.isRunning() ) {
				log.error( "main thread already running" );
			} else {
				maintThread.start();
			}
		} else {
			maintThread = new MaintThread( this );
			maintThread.setInterval( this.maintSleep );
			maintThread.start();
		}
	}

	/**
	 * Stops the maintenance thread.
	 */
	protected void stopMaintThread() {
		if ( maintThread != null && maintThread.isRunning() )
			maintThread.stopThread();
	}

	/**
	 * Runs self maintenance on all internal pools.
	 *
	 * This is typically called by the maintenance thread to manage pool size.
	 */
	protected void selfMaint() {
		if (log.isTraceEnabled()) {
		    log.trace("Starting self maintenance....");
		}

		// go through avail sockets and create sockets
		// as needed to maintain pool settings
		Map<String, Set<SockIO>> newSockets = new HashMap<String, Set<SockIO>>();
		
		for (Entry<String, Queue<SockIO>> entry : availableSockets.entrySet()) {
			final String host = entry.getKey();
			final Queue<SockIO> sockets = entry.getValue();
			
			if (log.isTraceEnabled()) {
			    log.trace("Size of avail pool for host (" + host + ") = " + sockets.size());
			}

			// if pool is too small (n < minSpare)
			if (sockets.size() < minConn) {
				// need to create new sockets
				int need = minConn - sockets.size();
				
				if (log.isDebugEnabled()) {
				    log.debug("Need to create " + need + " new sockets for pool for host: " + host);
				}
				
				Set<SockIO> newSock = new HashSet<SockIO>(need);
				
				for ( int j = 0; j < need; j++ ) {
					SockIO socket = createSocket(host);

					if (socket == null) { 
						break;
					}

					newSock.add(socket);
				}

				newSockets.put(host, newSock);
			}

			// reset the shift value for creating new SockIO objects
			createShift.put(host, new Integer(0));
		}

		// synchronize to add and remove to/from avail pool
		// as well as clean up the busy pool (no point in releasing
		// lock here as should be quick to pool adjust and no
		// blocking ops here)
		for ( String host : newSockets.keySet() ) {
		    Set<SockIO> sockets = newSockets.get(host);

		    for (SockIO socket : sockets) {
		        addSocketToPool(availableSockets, host, socket );
		    }
		}

		for (Entry<String, Queue<SockIO>> entry : availableSockets.entrySet()) {
		    final String host = entry.getKey();
		    final Queue<SockIO> sockets = entry.getValue();
		    
		    if (log.isTraceEnabled()) {
		        log.trace("Size of avail pool for host (" + host + ") = " + sockets.size());
		    }

		    if (sockets.size() > maxConn) {
		        // need to close down some sockets
		        int diff        = sockets.size() - maxConn;
		        int needToClose = (diff <= poolMultiplier)
		            ? diff
		            : (diff) / poolMultiplier;
		        
		        if (log.isDebugEnabled()) {    
		            log.debug("Need to remove " + needToClose + " spare sockets for host: " + host);
		        }
		        
		        Iterator<SockIO> iter = sockets.iterator();
		        
		        while (iter.hasNext()) {
		            if (needToClose <= 0) {
		                break;
		            }
		            
		            // remove stale entries
		            SockIO socket = iter.next();
		            long lastAccess = socket.getLastAccess();

		            // if past idle time
		            // then close socket
		            // and remove from pool
		            if ((lastAccess + maxIdle) < System.currentTimeMillis() ) {
		                if (log.isTraceEnabled()) {
		                    log.trace("Removing stale entry from pool as it is past its idle timeout and pool is over max spare");
		                }

		                // remove from the availPool
                        iter.remove();
                        // add to deadPool for later reaping
                        deadSockets.add(socket);

                        needToClose--;
		            }
		        }
		    }
		}

		// go through busy sockets and destroy sockets
		// as needed to maintain pool settings
        for (Entry<String, Queue<SockIO>> entry : busySockets.entrySet()) {
            final String host = entry.getKey();
            final Queue<SockIO> sockets = entry.getValue();
            
            if (log.isTraceEnabled()) {
                log.trace("Size of busy pool for host (" + host + ") = " + sockets.size());
            }
            
            Iterator<SockIO> iter = sockets.iterator();
            
            while (iter.hasNext()) {
                // remove stale entries
                SockIO socket = iter.next();
                long lastAccess = socket.getLastAccess();
            
                // if past max busy time
                // then close socket
                // and remove from pool
                if ((lastAccess + maxBusyTime) < System.currentTimeMillis() ) {
                    log.warn(
                            "Removing potentially hung connection from busy pool (" + host + 
                            ") socket in pool for " + (System.currentTimeMillis() - lastAccess) + " ms" );

                    // add to deadPool for later reaping
                    iter.remove();
                    deadSockets.add(socket);
                }
            }
        }

		// finally clean out the deadPool
		SockIO toClose;
		
		while ((toClose = deadSockets.poll()) != null) {
            try {
                toClose.trueClose();
            } catch (Exception ex) {
                log.error("Failed to close SockIO obj from deadPool", ex);
            }

            toClose = null;
		}
		
		if (log.isTraceEnabled()) {
		    log.trace("Ending self maintenance.");
		}
	}

	protected static Document loadConfiguration() {
	    if ((config == null) && (!configLoaded)) {
	        configLoaded = true;
	        InputStream in = SockIOPool.class.getResourceAsStream(DEFAULT_CONFIG);

	        if (in != null) {
	            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

	            docFactory.setNamespaceAware(true);

	            try {
                    config = docFactory.newDocumentBuilder().parse(in);
                } catch (SAXException e) {
                    log.error("Unable to parse configuration file ["+ DEFAULT_CONFIG + "]", e);
                } catch (IOException e) {
                    log.error("Unable to parse configuration file ["+ DEFAULT_CONFIG + "]", e);
                } catch (ParserConfigurationException e) {
                    log.error("Unable to parse configuration file ["+ DEFAULT_CONFIG + "]", e);
                }

                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("Unable to close configuration file ["+ DEFAULT_CONFIG + "]", e);
                }
	        } else {
	            log.warn("Unable to load default configuration from ["+ DEFAULT_CONFIG + "]");
	        }
	    }

	    return config;
    }

	protected static void initPool(SockIOPool pool, PoolConfigurationWrapper wrapper) {
	    final String prefix = "SockIOPool [" + pool.getName() + "]: ";
	    
	    final String[] s = wrapper.getServers();
        final Integer[] w = wrapper.getWeights();
	    
	    log.info(prefix + "Set server list " + Arrays.toString(s) + " with weights " + Arrays.toString(w));

	    pool.setServers(s);
        pool.setWeights(w);
	    
	    if (wrapper.hasParameter(INIT_CONNECTIONS)) {
	        final int p = wrapper.getParameter(INIT_CONNECTIONS, Integer.class);
	        
	        log.info(prefix + "Set init connections: " + p);
	        
            pool.setInitConn(p);
	    }

	    if (wrapper.hasParameter(MAX_CONNECTIONS)) {
            final Integer p = wrapper.getParameter(MAX_CONNECTIONS, Integer.class);

            log.info(prefix + "Set max connections: " + p);

            pool.setMaxConn(p);
	    }

	    if (wrapper.hasParameter(MIN_CONNECTIONS)) {
            final Integer p = wrapper.getParameter(MIN_CONNECTIONS, Integer.class);

            log.info(prefix + "Set min connections: " + p);

            pool.setMinConn(p);
	    }

	    if (wrapper.hasParameter(MAX_IDLE)) {
	        final Long p = wrapper.getParameter(MAX_IDLE, Long.class);

	        log.info(prefix + "Set max idle time: " + p + " ms");
	        
            pool.setMaxIdle(p);
	    }

	    if (wrapper.hasParameter(MAINT_SLEEP)) {
	        final Long p = wrapper.getParameter(MAINT_SLEEP, Long.class);
            
	        log.info(prefix + "Set maint thread sleep time: " + p + " ms");

            pool.setMaintSleep(p);
	    }

	    if (wrapper.hasParameter(NAGLE)) {
	        final Boolean p = wrapper.getParameter(NAGLE, Boolean.class);

	        log.info(prefix + "Is nagle: " + p);
	        
            pool.setNagle(p);
	    }
	    
	    if (wrapper.hasParameter(SOCKET_TIMEOUT)) {
	        final Integer p = wrapper.getParameter(SOCKET_TIMEOUT, Integer.class);

	        log.info(prefix + "Socket timeout: " + p + " ms");

            pool.setSocketTO(p);
	    }

	    if (wrapper.hasParameter(SOCKET_CONNECT_TIMEOUT)) {
	        final Integer p = wrapper.getParameter(SOCKET_CONNECT_TIMEOUT, Integer.class);

	        log.info(prefix + "Socket connect timeout: " + p + " ms");

            pool.setSocketConnectTO(p);
	    }
	}
	
	/**
	 * Increment reference counter for pool.
	 */
	public void incReferenceCounter() {
	    referenceCounter.incrementAndGet();
	}
	
	/**
	 * Decrement reference counter for pool.
	 */
	public void decReferenceCounter() {
	    referenceCounter.decrementAndGet();
	}
	
	/**
	 * Get reference counter for pool.
	 * @return
	 */
	public int getReferenceCounter() {
	    return referenceCounter.get();
	}

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
	
    public int getAvailableSockets(String server) {
        return getQueueSize(availableSockets, server);
    }

    public int getBusySockets(String server) {
        return getQueueSize(busySockets, server);
    }

    public int getDeadSockets(String server) {
        return deadSockets.size();
    }

    private int getQueueSize(Map<String, Queue<SockIO>> pool, String server) {
        if (pool.containsKey(server)) {
            final Queue<SockIO> queue = pool.get(server);
            
            if (queue != null) {
                return queue.size();
            }
        }
        
        return (-1);
    }

	/**
	 * Class which extends thread and handles maintenance of the pool.
	 *
	 * @author greg whalin <greg@meetup.com>
	 * @version 1.5
	 */
	protected static class MaintThread extends Thread {
		private SockIOPool pool;
		private long interval      = 1000 * 3; // every 3 seconds
		private boolean stopThread = false;
		private boolean running;

		protected MaintThread( SockIOPool pool ) {
			this.pool = pool;
			this.setDaemon( true );
		}

		public void setInterval( long interval ) { this.interval = interval; }

		public boolean isRunning() {
			return this.running;
		}

		/**
		 * sets stop variable
		 * and interupts any wait
		 */
		public void stopThread() {
			this.stopThread = true;
			this.interrupt();
		}

		/**
		 * Start the thread.
		 */
		public void run() {
			this.running = true;

			while ( !this.stopThread ) {
				try {
					Thread.sleep( interval );

					// if pool is initialized, then
					// run the maintenance method on itself
					if ( pool.isInitialized() )
						pool.selfMaint();

				}
				catch ( Exception e ) {
					break;
				}
			}

			this.running = false;
		}
	}

	
	/**
	 * MemCached Java client, utility class for Socket IO.
	 *
	 * This class is a wrapper around a Socket and its streams.
	 *
	 * @author greg whalin <greg@meetup.com>
	 * @author Richard 'toast' Russo <russor@msoe.edu>
	 * @version 1.5
	 */
	public static class SockIO implements LineInputStream {
		// logger
        private static Log log = LogFactory.getLog(SockIO.class);

		// pool
		private SockIOPool pool;

		// data
		private String host;
		private Socket sock;

		private DataInputStream in;
		private BufferedOutputStream out;
		
		private long lastAccess;
		
		/**
		 * creates a new SockIO object wrapping a socket
		 * connection to host:port, and its input and output streams
		 *
		 * @param pool Pool this object is tied to
		 * @param host host to connect to
		 * @param port port to connect to
		 * @param timeout int ms to block on data for read
		 * @param connectTimeout timeout (in ms) for initial connection
		 * @param noDelay TCP NODELAY option?
		 * @throws IOException if an io error occurrs when creating socket
		 * @throws UnknownHostException if hostname is invalid
		 */
		public SockIO( SockIOPool pool, String host, int port, int timeout, int connectTimeout, boolean noDelay ) throws IOException, UnknownHostException {

			this.pool = pool;

			// get a socket channel
			sock = getSocket( host, port, connectTimeout );

			if ( timeout >= 0 )
				sock.setSoTimeout( timeout );

			// testing only
			sock.setTcpNoDelay( noDelay );

			// wrap streams
			in  = new DataInputStream( sock.getInputStream() );
			out = new BufferedOutputStream( sock.getOutputStream() );

			this.host = host + ":" + port;
		}

		/**
		 * creates a new SockIO object wrapping a socket
		 * connection to host:port, and its input and output streams
		 *
		 * @param host hostname:port
		 * @param timeout read timeout value for connected socket
		 * @param connectTimeout timeout for initial connections
		 * @param noDelay TCP NODELAY option?
		 * @throws IOException if an io error occurrs when creating socket
		 * @throws UnknownHostException if hostname is invalid
		 */
		public SockIO( SockIOPool pool, String host, int timeout, int connectTimeout, boolean noDelay ) throws IOException, UnknownHostException {

			this.pool = pool;

			String[] ip = host.split(":");

			// get socket: default is to use non-blocking connect
			sock = getSocket( ip[ 0 ], Integer.parseInt( ip[ 1 ] ), connectTimeout );

			if ( timeout >= 0 )
				this.sock.setSoTimeout( timeout );

			// testing only
			sock.setTcpNoDelay( noDelay );

			// wrap streams
			in   = new DataInputStream( sock.getInputStream() );
			out  = new BufferedOutputStream( sock.getOutputStream() );

			this.host = host;
		}

		/**
		 * Method which gets a connection from SocketChannel.
		 *
		 * @param host host to establish connection to
		 * @param port port on that host
		 * @param timeout connection timeout in ms
		 *
		 * @return connected socket
		 * @throws IOException if errors connecting or if connection times out
		 */
		protected static Socket getSocket( String host, int port, int timeout ) throws IOException {
			SocketChannel sock = SocketChannel.open();
			sock.socket().connect( new InetSocketAddress( host, port ), timeout );
			return sock.socket();
		}

		/**
		 * Lets caller get access to underlying channel.
		 *
		 * @return the backing SocketChannel
		 */
		public SocketChannel getChannel() { return sock.getChannel(); }

		/**
		 * returns the host this socket is connected to
		 *
		 * @return String representation of host (hostname:port)
		 */
		public String getHost() { return this.host; }

		/**
		 * closes socket and all streams connected to it
		 *
		 * @throws IOException if fails to close streams or socket
		 */
		public void trueClose() throws IOException {
			if (log.isDebugEnabled()) {
			    log.debug("Closing socket for real: " + this);
			}

			boolean err = false;
			StringBuilder errMsg = new StringBuilder();

			if ( in == null || out == null || sock == null ) {
				err = true;
				errMsg.append( "++++ socket or its streams already null in trueClose call" );
			}

			if ( in != null ) {
				try {
					in.close();
				} catch( IOException ioe ) {
					log.error("Error closing input stream for socket: " + toString() + " for host: " + getHost());
					log.error( ioe.getMessage(), ioe );
					errMsg.append( "++++ error closing input stream for socket: " + toString() + " for host: " + getHost() + "\n" );
					errMsg.append( ioe.getMessage() );
					err = true;
				}
			}

			if ( out != null ) {
				try {
					out.close();
				}
				catch ( IOException ioe ) {
					log.error( "++++ error closing output stream for socket: " + toString() + " for host: " + getHost() );
					log.error( ioe.getMessage(), ioe );
					errMsg.append( "++++ error closing output stream for socket: " + toString() + " for host: " + getHost() + "\n" );
					errMsg.append( ioe.getMessage() );
					err = true;
				}
			}

			if ( sock != null ) {
				try {
					sock.close();
				}
				catch ( IOException ioe ) {
					log.error( "++++ error closing socket: " + toString() + " for host: " + getHost() );
					log.error( ioe.getMessage(), ioe );
					errMsg.append( "++++ error closing socket: " + toString() + " for host: " + getHost() + "\n" );
					errMsg.append( ioe.getMessage() );
					err = true;
				}
			}

			// check in to pool
			if ( sock != null )
				pool.checkIn( this, false );

			in = null;
			out = null;
			sock = null;

			if ( err )
				throw new IOException( errMsg.toString() );
		}

		/**
		 * sets closed flag and checks in to connection pool
		 * but does not close connections
		 */
		void close() {
			// check in to pool
		    if (log.isDebugEnabled()) {
		        log.debug("Marking socket (" + this.toString() + ") as closed and available to return to avail pool");
		    }
		    
			pool.checkIn(this);
		}

		/**
		 * checks if the connection is open
		 *
		 * @return true if connected
		 */
		boolean isConnected() {
			return ( sock != null && sock.isConnected() );
		}

		/*
		 * checks to see that the connection is still working
		 *
		 * @return true if still alive
		 */
		boolean isAlive() {

			if ( !isConnected() )
				return false;

			// try to talk to the server w/ a dumb query to ask its version
			try {
				this.write( "version\r\n".getBytes() );
				this.flush();
				String response = this.readLine();
			}
			catch ( IOException ex ) {
				return false;
			}

			return true;
		}

		/**
		 * reads a line
		 * intentionally not using the deprecated readLine method from DataInputStream
		 *
		 * @return String that was read in
		 * @throws IOException if io problems during read
		 */
		public String readLine() throws IOException {
			if ( sock == null || !sock.isConnected() ) {
				log.error( "++++ attempting to read from closed socket" );
				throw new IOException( "++++ attempting to read from closed socket" );
			}

			byte[] b = new byte[1];
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			boolean eol = false;

			while ( in.read( b, 0, 1 ) != -1 ) {

				if ( b[0] == 13 ) {
					eol = true;
				}
				else {
					if ( eol ) {
						if ( b[0] == 10 )
							break;

						eol = false;
					}
				}

				// cast byte into char array
				bos.write( b, 0, 1 );
			}

			if ( bos == null || bos.size() <= 0 ) {
				throw new IOException( "++++ Stream appears to be dead, so closing it down" );
			}

			// else return the string
			return bos.toString().trim();
		}

		/**
		 * reads up to end of line and returns nothing
		 *
		 * @throws IOException if io problems during read
		 */
		public void clearEOL() throws IOException {
			if ( sock == null || !sock.isConnected() ) {
				log.error( "++++ attempting to read from closed socket" );
				throw new IOException( "++++ attempting to read from closed socket" );
			}

			byte[] b = new byte[1];
			boolean eol = false;
			while ( in.read( b, 0, 1 ) != -1 ) {

				// only stop when we see
				// \r (13) followed by \n (10)
				if ( b[0] == 13 ) {
					eol = true;
					continue;
				}

				if ( eol ) {
					if ( b[0] == 10 )
						break;

					eol = false;
				}
			}
		}

		/**
		 * reads length bytes into the passed in byte array from dtream
		 *
		 * @param b byte array
		 * @throws IOException if io problems during read
		 */
		public int read( byte[] b ) throws IOException {
			if ( sock == null || !sock.isConnected() ) {
				log.error( "++++ attempting to read from closed socket" );
				throw new IOException( "++++ attempting to read from closed socket" );
			}

			int count = 0;
			while ( count < b.length ) {
				int cnt = in.read( b, count, (b.length - count) );
				count += cnt;
			}

			return count;
		}

		/**
		 * flushes output stream
		 *
		 * @throws IOException if io problems during read
		 */
		void flush() throws IOException {
			if ( sock == null || !sock.isConnected() ) {
				log.error( "++++ attempting to write to closed socket" );
				throw new IOException( "++++ attempting to write to closed socket" );
			}
			out.flush();
		}

		/**
		 * writes a byte array to the output stream
		 *
		 * @param b byte array to write
		 * @throws IOException if an io error happens
		 */
		void write( byte[] b ) throws IOException {
			if ( sock == null || !sock.isConnected() ) {
				log.error( "++++ attempting to write to closed socket" );
				throw new IOException( "++++ attempting to write to closed socket" );
			}
			out.write( b );
		}
		
		/**
         * @return the lastAccess
         */
        public long getLastAccess() {
            return lastAccess;
        }

        /**
         * Touching socket 
         * @param lastAccess the lastAccess to set
         */
        public void touch() {
            this.lastAccess = System.currentTimeMillis();
        }

        /**
		 * use the sockets hashcode for this object
		 * so we can key off of SockIOs
		 *
		 * @return int hashcode
		 */
		public int hashCode() {
			return ( sock == null ) ? 0 : sock.hashCode();
		}

		/**
		 * returns the string representation of this socket
		 *
		 * @return string
		 */
        public String toString() {
			return ( sock == null ) ? "" : sock.toString();
		}

		/**
		 * Hack to reap any leaking children.
		 */
        protected void finalize() throws Throwable {
			try {
				if ( sock != null ) {
					log.error("Closing potentially leaked socket in finalize");
					sock.close();
					sock = null;
				}
			} catch (Throwable t) {
				log.error(t);
			} finally {
				super.finalize();
			}
		}
	}
}
