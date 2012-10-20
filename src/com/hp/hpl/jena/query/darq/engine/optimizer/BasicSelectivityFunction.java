/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package com.hp.hpl.jena.query.darq.engine.optimizer;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.darq.core.RemoteService;
import com.hp.hpl.jena.rdf.model.Model;


public class BasicSelectivityFunction implements
        SelectivityFunction {
  
    public double calcSelectivity(List<Triple> triples,Set<String> boundVariables, RemoteService service) {
        
     Set<String> bv = new HashSet<String>(boundVariables);
        
        
        double sel = 1;
        int count = 0;

        for (Triple t : triples) {

            
            boolean subjectBound = false; 
            boolean objectBound = false;
            boolean predicateBound = true; // TODO unbound predicate not supported at the moment
            
            if (t.getSubject().isConcrete()) subjectBound = true;
            else if (bv.contains(t.getSubject().getName())) subjectBound = true;  
                
            
            if (t.getObject().isConcrete()) objectBound = true;
            else if (bv.contains(t.getObject().getName())) objectBound = true;

                       
            if (subjectBound) sel *= MIN_SELECTIVITY;
            else  if (objectBound) {
                Double d = service.getObjectSelectivity(t.getPredicate().getURI());
                if (d != null) {
                    count++;
                    sel *= d;
                }
            }
            else {
                //sel*=1.1; // TODO HANDLE DIFFERENT !! XXX
            }
        }
    
        return sel;
    }

    public void init(Model m) {
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
