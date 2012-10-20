
/*
 * Created on Oct 24, 2004 by Sebastian Michel
 * smichel@mpi-sb.mpg.de
 *
 */

package de.mpii.d5.synopsis;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import cern.colt.bitvector.QuickBitVector;
import cern.jet.random.engine.MersenneTwister;


/** 
 * <p>Peer-to-Peer Project</p>
 *  
 * <h2>Description:</h2>
 * <p>class represents a BloomFilter</p> 
 *  
 * <p>Max-Planck Institut für Informatik, Saarbrücken</p>
 * <p>24.10.2004
 * 
 * @author Sebastian Michel, smichel@mpi-sb.mpg.de
 */
public class BloomFilter implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5424417436946603454L;
	/** The number of weights used to create hash functions. */
	final public static int NUMBER_OF_WEIGHTS = 16;
	/** The number of bits in this filter. */
	 public int m;
	/** The number of hash functions used by this filter. */
	 public int d;
	/** The underlying bit vector. */
	 public long[] bits;
	/** The random integers used to generate the hash functions. */
	 public int[][] weight;

	/** The number of elements currently in the filter. It may be
	 * smaller than the actual number of additions of distinct character
	 * sequences because of false positives.
	 */
	private int size;

	/** The natural logarithm of 2, used in the computation of the number of bits. */
	private final static double NATURAL_LOG_OF_2 = Math.log( 2 );

	private final static boolean DEBUG = false;

	public BloomFilter() {
	    
	}
	
	/** Creates a new Bloom filter with given number of hash functions and expected number of elements.
	 * 
	 * @param n the expected number of elements.
	 * @param d the number of hash functions; if the filter add not more than <code>n</code> elements,
	 * false positives will happen with probability 2<sup>-<var>d</var></sup>.
	 */
	public BloomFilter( final int n, final int d ) {
		this.d = d;
		bits = new long[ (int)Math.ceil( ( n * d / NATURAL_LOG_OF_2 ) / 64 ) ];
		if ( bits.length > Integer.MAX_VALUE / 64 ) throw new IllegalArgumentException( "This filter would require " + bits.length * 64L + " bits" );
		m = bits.length * 64;

		if ( DEBUG ) System.err.println( "Number of bits: " + m );
		
		final MersenneTwister mersenneTwister = new MersenneTwister();
		weight = new int[ d ][];
		for( int i = 0; i < d; i++ ) {
			weight[ i ] = new int[ NUMBER_OF_WEIGHTS ];
			for( int j = 0; j < NUMBER_OF_WEIGHTS; j++ )
				 weight[ i ][ j ] = mersenneTwister.nextInt();
		}
	}
	
	/**
	 * Returns the size of this bloomfilter in bits.
	 * Note, that it is not the value that is returned by the size() method!!!
	 * @return
	 */
	public int getNumberOfBits() {
	    return m;
	}

	/** The number of character sequences in the filter.
	 * 
	 * @return the number of character sequences in the filter (but see {@link #contains(CharSequence)}).
	 */

	public int size() {
		return size;
	}

	/** Hashes the given sequence with the given hash function.
	 * 
	 * @param s a character sequence.
	 * @param l the length of <code>s</code>.
	 * @param k a hash function index (smaller than {@link #d}).
	 * @return the position in the filter corresponding to <code>s</code> for the hash function <code>k</code>.
	 */

	private int hash( final CharSequence s, final int l, final int k ) {
		final int[] w = weight[ k ];
		int h = 0, i = l;
		while( i-- != 0 ) h ^= s.charAt( i ) * w[ i % NUMBER_OF_WEIGHTS ];
		return ( h & 0x7FFFFFFF ) % m; 
	}

	/** Checks whether the given character sequence is in this filter. 
	 * 
	 * <P>Note that this method may return true on a character sequence that is has
	 * not been added to the filter. This will happen with probability 2<sub>-<var>d</var></sub>,
	 * where <var>d</var> is the number of hash functions specified at creation time, if
	 * the number of the elements in the filter is less than <var>n</var>, the number
	 * of expected elements specified at creation time.
	 * 
	 * @param s a character sequence.
	 * @return true if the sequence is in the filter (or if a sequence with the
	 * same hash sequence is in the filter).
	 */

	public boolean contains( final CharSequence s ) {
		int i = d, l = s.length();
		while( i-- != 0 ) if ( ! QuickBitVector.get( bits, hash( s, l, i ) ) ) return false;
		return true;
	}

	/** Adds a character sequence to the filter.
	 * 
	 * @param s a character sequence.
	 * @return true if the character sequence was not in the filter (but see {@link #contains(CharSequence)}).
	 */

	public boolean add( final CharSequence s ) {
		boolean result = false;
		int i = d, l = s.length(), h;
		while( i-- != 0 ) {
			h = hash( s, l, i );
			if ( ! QuickBitVector.get( bits, h ) ) result = true;
			QuickBitVector.set( bits, h );
		}
		if ( result ) size++;
		return result;
	}

	/**
	 * Returns an int-array that contains the positions of the bits in this bloomfilter
	 * that would be set when inserting the given charsequence.
	 * @param s
	 * @return int-array containing the positions of the bits 
	 */
	public int[] hash(CharSequence s) {
		int[] ret = new int[d];
		int i = d, l = s.length();
		while( i-- != 0 ) {
			ret[i] = hash( s, l, i );
		}
		return ret;
	}

	public static void main(String[] args) {

	    BloomFilter bf = new BloomFilter(23433, 8);
	    bf.add("2345354325");
	    System.out.println(bf.contains("2345354324"));
	    int i = bf.hash("2345354325")[0];
	    System.out.println(bf.get(i));
	    
	    BloomFilter bf2 = new BloomFilter(23433, 8);
	    bf2.add("2345354325");
	    System.out.println(bf2.get(i));
	}
	
	public boolean get(int i) {
	    return QuickBitVector.get(bits, i);
	}
	
	public void set(int i) {
	    QuickBitVector.set(bits, i);
	}
	
	public void read(InputStream in) throws Exception {
	    DataInputStream dataIn = new DataInputStream(in);
	    m = dataIn.readInt();
	    d = dataIn.readInt();

	    int bits_length = dataIn.readInt();

	    bits = new long[bits_length];
	    for (int i=0; i<bits_length; i++) {
	        bits[i] = dataIn.readLong();
	    }

	    weight = new int[d][NUMBER_OF_WEIGHTS];
		for( int i = 0; i < d; i++ ) {
			weight[ i ] = new int[ NUMBER_OF_WEIGHTS ];
			for( int j = 0; j < NUMBER_OF_WEIGHTS; j++ )
				 weight[ i ][ j ] = dataIn.readInt();
		}
	}
	
	public void write(OutputStream out)  throws Exception {
	    DataOutputStream dataOut = new DataOutputStream(out);
	    dataOut.writeInt(m);
	    dataOut.writeInt(d);
	    dataOut.writeInt(bits.length);
	    for (int i=0; i<bits.length; i++) {
	        dataOut.writeLong(bits[i]);
	    }
		for( int i = 0; i < d; i++ ) {
			for( int j = 0; j < NUMBER_OF_WEIGHTS; j++ )
				 dataOut.writeInt(weight[ i ][ j ]);
		}
	}
	
}
