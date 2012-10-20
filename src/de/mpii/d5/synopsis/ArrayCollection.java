/*
 * Created on 15.06.2005 by Sebastian Michel
 * Max-Planck Institute for Computer Science
 * 
 * smichel@mpi-sb.mpg.de
 *
 */
package de.mpii.d5.synopsis;

import java.util.ArrayList;


/**
 * 
 * 
 *
 * @author Sebastian Michel, MPII, smichel@mpi-sb.mpg.de
 *
 */
public class ArrayCollection extends Collection {
    
    private ArrayList<Long> docs;
    
     public ArrayCollection(ArrayList<Long> curIdsVector) {
         this.docs = curIdsVector;
     }


    public long getDocByRank(int rank) {
        return docs.get(rank);
    }

    public int size() {
        return docs.size();
    }

}
