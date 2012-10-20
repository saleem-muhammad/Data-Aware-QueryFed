/*
 * Created on 24.05.2005 by Sebastian Michel
 * Max-Planck Institute for Computer Science
 * 
 * smichel@mpi-sb.mpg.de
 *
 */
package de.mpii.d5.synopsis;

import java.io.Serializable;

import de.mpii.d5.synopsis.Collection;



/**
 * 
 * 
 *
 * @author Sebastian Michel, MPII, smichel@mpi-sb.mpg.de
 *
 */
public class HashSketchSynopsis extends Synopsis implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -2938094361965043363L;
	public HashSketch hs;
    private int originalSize;
    @SuppressWarnings("unused")
	private int bitmaps;

    public HashSketchSynopsis(HashSketch hs) {
        this.hs = hs;
        this.originalSize = -1;
    }

    public HashSketchSynopsis(Collection c, int bitmaps) {
        try {
        this.bitmaps = bitmaps;
        this.hs = new HashSketch(bitmaps);
        
        this.originalSize = c.size();
        for (int i=0; i<c.size(); i++) { 
            hs.insert(HashSketch.long2bytes(c.getDocByRank(i)));
        }
        }
        catch (Exception e) {
            throw new RuntimeException("HashSketchSynopsis() "+e);
        }
    }

    public double resemblance(Synopsis s) {
        HashSketchSynopsis s_ = (HashSketchSynopsis) s;
        return s_.hs.resemblance(this.hs);
        
        
    }


    public double noveltyWithSynopsis(Synopsis s) {
       throw new UnsupportedOperationException();
    }


    public long getOriginalSize() {
        return originalSize;
    }


    public Synopsis union(Synopsis s) {
        HashSketchSynopsis s_ = (HashSketchSynopsis) s;
        HashSketch hs_union = hs.union(s_.hs);
        return new HashSketchSynopsis(hs_union);
    }

    /* (non-Javadoc)
     * @see overlap.Synopsis#intersect(overlap.Synopsis)
     */
    public Synopsis intersect(Synopsis s) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getEstimatedSize() {
    	
  		return (long) Math.ceil(this.hs.fm85());
	}

	@Override
	public int synopsisSizeInBytes() {
		
		return bitmaps/8;
	}
    
}
