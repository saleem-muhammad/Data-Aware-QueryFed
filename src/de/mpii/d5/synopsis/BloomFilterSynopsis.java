/*
 * Created on 16.05.2005 by Sebastian Michel
 * Max-Planck Institute for Computer Science
 * 
 * smichel@mpi-sb.mpg.de
 *
 */
package de.mpii.d5.synopsis;

//import Synopsis;

import java.io.Serializable;



/**
 * 
 * 
 *
 * @author Sebastian Michel, MPII, smichel@mpi-sb.mpg.de
 *
 */
public class BloomFilterSynopsis extends Synopsis implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -1111904953606201370L;

	int original_size;
    
    int bits = 2048;
    
    final static int pfp_factor = 1;
	private final static double NATURAL_LOG_OF_2 = Math.log( 2 );
    BloomFilter filter;

    public BloomFilterSynopsis(BloomFilter bf, int original_size){
        this.filter = bf;
        this.original_size = original_size;
        
    }
    
    public BloomFilterSynopsis(Collection c) {
        this.original_size = c.size();
     //   int docs= (int) ((Math.floor(8*2048.0 / 1.44) / pfp_factor));
       //int docs= (int) ((Math.floor(16*4096.0 / 1.44) / pfp_factor));
        int docs= (int) ((Math.floor(bits * NATURAL_LOG_OF_2)));
      
        BloomFilter filter = new BloomFilter(docs , 1);
        for (int i=0; i<c.size(); i++) {
            filter.add(""+c.getDocByRank(i));
        }
     //   System.out.println("filter size = "+filter.getNumberOfBits());
   //     System.out.println("docs = "+docs);
        this.filter = filter;
    }

    /* (non-Javadoc)
     * @see overlap.Synopsis#overlapWithSynopsis(overlap.Synopsis)
     */
    public double overlapWithSynopsis(Synopsis s) {
        BloomFilterSynopsis s_ = (BloomFilterSynopsis) s;
        int count = 0;
        int count_b1 =0;
        int count_b2 =0;
       // System.out.println("this.filter.getNumberOfBits() ="+this.filter.getNumberOfBits());
        for (int i=0; i<this.filter.getNumberOfBits(); i++) {
            
            if (this.filter.get(i)==true) count_b1++;
            if (s_.filter.get(i)==true) count_b2++;
            
            if (this.filter.get(i)==true && s_.filter.get(i)==true) count++;
        }
        
       /* System.out.println("bits im filter 1 = "+count_b1);
        System.out.println("bits im filter 2 = "+count_b2);
        System.out.println("overlap with synopsis = "+count);*/
        return count;
    }

    public double unionCardinality(Synopsis s) {
        BloomFilterSynopsis s_ = (BloomFilterSynopsis) s;
    //    System.out.println("this.filter.bits = "+this.filter.getNumberOfBits());
     //   System.out.println("s_.filter.bits = "+s_.filter.getNumberOfBits());
        int count = 0;
        for (int i=0; i<this.filter.getNumberOfBits(); i++) {
            if (this.filter.get(i) || s_.filter.get(i)) count++;
        }
      // System.out.println("unionCardinality = "+count);
        return count;
    }

    /* (non-Javadoc)
     * @see overlap.Synopsis#noveltyWithSynopsis(overlap.Synopsis)
     */
    public double noveltyWithSynopsis(Synopsis s) {
        BloomFilterSynopsis s_ = (BloomFilterSynopsis) s;
        int count = 0;
        for (int i=0; i<this.filter.getNumberOfBits(); i++) {
            if ((this.filter.get(i)==false) && (s_.filter.get(i)==true)) count++;
        }
        return count;
    }

    /* (non-Javadoc)
     * @see overlap.Synopsis#getOriginalSize()
     */
    public long getOriginalSize() {
        return original_size;
    }

    /* (non-Javadoc)
     * @see overlap.Synopsis#union(overlap.Synopsis)
     */
    public Synopsis union(Synopsis s) {
            //int docs= (int) ((Math.floor(2048.0 / 1.44) / pfp_factor));
            int docs= (int) ((Math.floor(bits * NATURAL_LOG_OF_2) ));
            BloomFilterSynopsis s_ = (BloomFilterSynopsis) s;
            BloomFilter s_new = new BloomFilter(docs, 1);
            for (int i=0; i<this.filter.bits.length; i++) {
                s_new.bits[i] = this.filter.bits[i] | s_.filter.bits[i];
            }
            double resemblance = resemblance(s_);
            int newSize = (int) Math.ceil(resemblance *(s_.original_size+this.original_size) / (resemblance+1));

            BloomFilterSynopsis ret = new BloomFilterSynopsis(s_new, newSize);

            return ret;
        }

    /* (non-Javadoc)
     * @see overlap.Synopsis#intersect(overlap.Synopsis)
     */
    public Synopsis intersect(Synopsis s) {  
        //int docs= (int) ((Math.floor(2048.0 / 1.44) / pfp_factor));
        int docs= (int) ((Math.floor(bits * NATURAL_LOG_OF_2) ));
        BloomFilterSynopsis s_ = (BloomFilterSynopsis) s;
        BloomFilter s_new = new BloomFilter(docs, 1);
        for (int i=0; i<this.filter.bits.length; i++) {
            s_new.bits[i] = this.filter.bits[i] & s_.filter.bits[i];
        }
        double resemblance = resemblance(s_);
        int newSize = (int) Math.ceil(resemblance *(s_.original_size+this.original_size) / (resemblance+1));

        BloomFilterSynopsis ret = new BloomFilterSynopsis(s_new, newSize);

        return ret;
    }

    /* (non-Javadoc)
     * @see p2psearch.overlap.Synopsis#resemblance(p2psearch.overlap.Synopsis)
     */
    public double resemblance(Synopsis s) {
        return ((double) overlapWithSynopsis(s)/unionCardinality(s));
    }
    
    public static void main(String[] args) {
    
    }

	@Override
	public long getEstimatedSize() {
		// TODO Not implemented
		return 0;
	}

	@Override
	public int synopsisSizeInBytes() {
		// TODO Auto-generated method stub
		return bits/8;
	}

}
