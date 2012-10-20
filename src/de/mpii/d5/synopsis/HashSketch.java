/*
 * Created on 24.05.2005 by Sebastian Michel
 * Max-Planck Institute for Computer Science
 * 
 * smichel@mpi-sb.mpg.de
 *
 */
package de.mpii.d5.synopsis;

import java.io.Serializable;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * Thanks to Nikos Ntarmos for providing his C++ implementation
 *
 * @author Sebastian Michel, MPII, smichel@mpi-sb.mpg.de
 *
 */
public class HashSketch implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2851792825582746724L;
	private long hsbits;
    private long vbits;
    
    private long numv;
    private long[] hs;
    
    private String hf;
    
    //private MessageDigest md;
    
    private final long Ace = 1;
    
    
    public HashSketch() throws NoSuchAlgorithmException {
        this(64, 6, "SHA-1" );
    }
    
    public HashSketch(int bitmaps) throws NoSuchAlgorithmException {
        this(64, bitmaps, "SHA-1" );
    }

    public HashSketch(long hsb, long vb, String h){
        hsbits = hsb;
        vbits = vb;
        numv = Ace << vbits;
        hs = new long[(int) numv];
        hf = h;
		
    }
    
    
    public long r(long in) {
        long i = 0; //mask und i = bitmap_t
      for (long mask = Ace; (i < hsbits) && (in & mask) == 0; mask <<= 1, i ++) {}
      return i;
    }

    public double fm85() 
    {
      double SR = 0;
      for (int b = 0; b < hs.length; b ++) {
        for (long i  = 0, mask = Ace; i < hsbits; mask <<= 1, i ++) {
          if ((hs[b] & mask)==0) {  //stimmt das?
            SR += i;
            i = hsbits;
          }
        }
      }
      SR /= (double)numv;
      return (int)(1.29281 * Math.pow(2.0, SR) * (double)numv);
    }
    
    
   public long md4h(byte[] in ) throws DigestException, NoSuchAlgorithmException   {
    
	   MessageDigest md = MessageDigest.getInstance(hf);
	   
	   
      byte[] ba = md.digest(in);

      long digest = 0;
      for (int index=0;index<hsbits;index+=8)
          digest|=((long)(ba[index/8]&0xFF))<<index;
      return digest;

    }
    
    
    public void insert(byte[] data) throws DigestException, NoSuchAlgorithmException 
    {
       long hi = md4h(data);
      // System.out.println(""+hi+" "+numv+" "+(hi%numv));
       hs[(int) (((hi % numv)+numv)%numv)] |= (Ace << r(hi >>> vbits /* / numv */));
      // System.out.println("array index " +(int) (((hi % numv)+numv)%numv));
    //   System.out.println("array content at index  = "+hs[(int) (((hi % numv)+numv)%numv)]);
  
    }
    
    public long hash(String data, int len) {

        return 0;
    }
    
   public long[] getBitmaps() {
       return hs; 
   }

    public long getNumOfBitmaps() {
        return numv; 
    }

    public long getNumOfHSBits() {
        return hsbits; 
    }

    public long getNumOfVBits() {
        return vbits; 
    }

    public String getHashFunctionName() { 
        return hf; 
    }
    
    public HashSketch union(HashSketch s) {
        HashSketch result = new HashSketch(s.hsbits, s.vbits, s.hf);

		for (int i=0; i< result.hs.length; i++) {

		    result.hs[i] = s.hs[i] | this.hs[i];

		}
		return result;
    }
    
    /**
     * Returns 0 if |A| + |B| - |A u B| is < 0  !
     * @param s
     * @return
     */
    public double resemblance(HashSketch s) {
        double u = (union(s).fm85());
        double i = fm85()+s.fm85() - u;
        if (i<0) return 0;
        return i / u;
    }
    
    public static byte[] long2bytes(long value) {
        byte[] result=new byte[8];
        for (int index=0;index<result.length;index++) {
            result[index]=(byte)(value&0xFF);
            value>>=8;
        }
        return result;
    }

    private static byte[] int2bytes(int value) {
        byte[] result=new byte[4];
        for (int index=0;index<result.length;index++) {
            result[index]=(byte)(value&0xFF);
            value>>=8;
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            HashSketch hs;
            
            int t1 = 500;
            int t2 = 1000;
            hs = new HashSketch(64, 6, "SHA-1" );
            for (int i=0; i<t2 /*24*/; i++) {
                hs.insert(int2bytes(i));
            }
            System.out.println("sizefm85 = "+hs.fm85());

            HashSketch hs1 = new HashSketch(64, 6, "SHA-1" );
            for (int i=0; i<t1 /*24*/; i++) {
                hs1.insert(int2bytes(i));
            }
            System.out.println("sizefm85 = "+hs1.fm85());

            HashSketch hs2 = new HashSketch(64, 6, "SHA-1" );
            for (int i=t1; i<t2 /*24*/; i++) {
                hs2.insert(int2bytes(i));
            }
            System.out.println("sizefm85 = "+hs2.fm85());
            
            HashSketch hs3=hs1.union(hs2);
            System.out.println("sizefm85 = "+hs3.fm85());
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
  
    }
}
