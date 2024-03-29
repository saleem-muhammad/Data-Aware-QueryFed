/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package com.hp.hpl.jena.query.darq.mapping.rewriting;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;


public abstract class AbstractTripleRewriter implements TripleRewriter {

    private ArrayList<Triple> result = null; 
    private Iterator<Triple> it = null;
    
    private long limit=1;
    
    abstract public ArrayList<Triple> getRewritings(Triple triple);
    
    public boolean hasNext(long in) {
   if (in>=limit) return false;
        
        if (it==null) return false;
        
        return it.hasNext();
    }

    public Triple next(long in) {
        if (in>=limit || it==null) throw new IndexOutOfBoundsException();
        if (!it.hasNext()) throw new IndexOutOfBoundsException();
        return it.next();
    }


    public void setLimitResults(long l) {
        limit=l;

    }

    public void rewrite(Triple triple) {
        result = getRewritings(cloneTriple(triple));
        it=result.iterator();
    }
    
    public void reset(){
     //   it=
    }
    
    private Triple cloneTriple(Triple t) {
        return new Triple(t.getSubject(),t.getPredicate(),t.getObject());
    }
    
    

}
/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */