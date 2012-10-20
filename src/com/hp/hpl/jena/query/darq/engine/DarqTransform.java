/*
 * (c) Copyright 5505, 5506 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package com.hp.hpl.jena.query.darq.engine;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.core.ElementBasicGraphPattern;
import com.hp.hpl.jena.query.darq.config.Configuration;
import com.hp.hpl.jena.query.darq.config.ConfigurationException;
import com.hp.hpl.jena.query.darq.config.ServiceRegistry;
import com.hp.hpl.jena.query.darq.core.MultipleServiceGroup;
import com.hp.hpl.jena.query.darq.core.RemoteService;
import com.hp.hpl.jena.query.darq.core.ServiceGroup;
import com.hp.hpl.jena.query.darq.engine.compiler.FedPlanMultipleService;
import com.hp.hpl.jena.query.darq.engine.compiler.FedPlanService;
import com.hp.hpl.jena.query.darq.engine.compiler.PlanGroupDarq;
import com.hp.hpl.jena.query.darq.engine.optimizer.PlanUnfeasibleException;
import com.hp.hpl.jena.query.darq.engine.optimizer.planoperators.PlanOperatorBase;
import com.hp.hpl.jena.query.engine.Plan;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.plan.PlanBasicGraphPattern;
import com.hp.hpl.jena.query.engine1.plan.PlanBlockTriples;
import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
import com.hp.hpl.jena.query.engine1.plan.PlanGroup;
import com.hp.hpl.jena.query.engine1.plan.TransformCopy;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.query.lang.rdql.Node;
import com.hp.hpl.jena.query.util.Context;
//import com.sun.org.apache.xpath.internal.operations.String;

import de.hu_berlin.informatik.wbi.darq.cache.Caching;
import de.mpii.d5.synopsis.ArrayCollection;
import de.mpii.d5.synopsis.Collection;
import de.mpii.d5.synopsis.MIPsynopsis;

import com.hp.hpl.jena.query.darq.config.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
public class DarqTransform extends TransformCopy {

  
	Log log = LogFactory.getLog(DarqTransform.class);

	protected Plan plan = null;

	private Context context = null;

	private ServiceRegistry registry = null;

	private Configuration config = null;
	
	private Caching cache;
	private Boolean cacheEnabled;
	private int permutation = 100;

	// The return stack
	private Stack<PlanElement> retStack = new Stack<PlanElement>();

	HashMap<RemoteService, ServiceGroup> groupedTriples = new HashMap<RemoteService, ServiceGroup>();

	HashMap<Triple, MultipleServiceGroup> queryIndividuallyTriples = new HashMap<Triple, MultipleServiceGroup>();

	List<ServiceGroup> sgsPos = new LinkedList<ServiceGroup>();

	Set varsMentioned = new HashSet();

	boolean optimize = true;

	/**
	 * @return the optimize
	 */
	public boolean isOptimize() {
		return optimize;
	}

	/**
	 * @param optimize
	 *            the optimize to set
	 */
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}

	public DarqTransform(Context cntxt, Configuration conf, Caching cache, Boolean cacheEnabled) {
		super();
		context = cntxt;
		config = conf;
		registry = conf.getServiceRegistry();
		this.cache = cache;
		this.cacheEnabled = cacheEnabled;
	}

	
	public PlanElement transform(PlanBasicGraphPattern planElt, List newElts,PlanElement parent) {
		/*
		 * We do this only if we have a basic graph pattern with one triple:
		 * PlanGroups with one Triple will be converted to PlanBasicGraphPattern
		 * by ARQ :( We need to handle that!
		 * There are also some other cases where PlanBasicGraphPattern is not in a group... :((
		 */
		
		if (parent instanceof PlanGroup) return planElt;
	/*	 if (!( (newElts.size()==1) && (newElts.get(0) instanceof
		 PlanBlockTriples) &&
		 (((PlanBlockTriples)newElts.get(0)).getSubElements().size()==1) ) )
		 return planElt;*/
		 
		List<PlanElement> acc = new ArrayList<PlanElement>();
		acc.add(planElt);
		return make(acc, planElt.getContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hp.hpl.jena.query.engine1.plan.TransformBase#transform(com.hp.hpl.jena.query.engine1.plan.PlanBasicGraphPattern,
	 *      java.util.List)
	 */

	@Override
	public PlanElement transform(PlanGroup planElt, List newElts) {
		return make(newElts, planElt.getContext());
	}

	private PlanElement make(List newElts, Context context) {
		groupedTriples.clear(); // new for each PlanBasicGraphPattern !
		queryIndividuallyTriples.clear(); // "

		List<Triple> unmatchedTriples = new LinkedList<Triple>();

		List<PlanFilter> filters = new ArrayList<PlanFilter>();

		List<PlanElement> acc = new ArrayList<PlanElement>();
		
		System.out.println("                               ");
        System.out.println("=========================================Query Planning===========================================");
        
		// planning
		for (PlanElement elm : (List<PlanElement>) newElts) {

			if (elm instanceof PlanBasicGraphPattern) {

				for (PlanElement el : (List<PlanElement>) elm.getSubElements()) {

					if (el instanceof PlanBlockTriples) {
						for (Triple t : (List<Triple>) ((PlanBlockTriples) el)
								.getPattern()) {
							System.out.println("                               ");
							System.out.println("#  Query Triple : "+ t);

							List<RemoteService> services = selectServices(registry
									.getMatchingServices(t));
							if (services.size() == 1) {
								putIntoGroupedTriples(services.get(0), t);
								System.out.println("# Capable Service URL : "+ services.get(0).getUrl());
							} else if (services.size() > 1) {
								/*
								 * if there are more than one service, the
								 * triple has to be passed to the services
								 * individually. This is because ... TODO
								 */
								for (int j = 0; j < services.size(); j++) {
									System.out.println("# Capable Service URL : "+ services.get(j).getUrl());
									putIntoQueryIndividuallyTriples(t, services
											.get(j));
								}

							} else {

								unmatchedTriples.add(t);
								log.warn("No service found for statement: " + t
										+ " - it will be queried locally.");

							}

						}
					} else if (el instanceof PlanFilter) {
						filters.add((PlanFilter) el);
					} else {
						acc.add(0, el);

					}
				}

			} else if (elm instanceof PlanFilter) {
				filters.add((PlanFilter) elm);
			} else {
				acc.add(0, elm);

			}

		}
		 System.out.println("                      ");
		 System.out.println("=========================================Sub Query Execution===========================================");
	      
		// add filters to servcie groups and to plan (filters are also applied
		// locally because we don't trust the remote services)
		for (PlanFilter f : filters) {
			acc.add(f);
			if (optimize) { // do we optimize?
				for (ServiceGroup sg : groupedTriples.values()) {
					sg.addFilter(f.getExpr());
					//List<Triple> lsttrpl = sg.getTriples();
				     //for(int TrplNo=0; TrplNo<lsttrpl.size();TrplNo++)
					//	 System.out.println("Group Triple are " + lsttrpl.get(TrplNo));
				}
				for (ServiceGroup sg : queryIndividuallyTriples.values()) {
					sg.addFilter(f.getExpr());
				}
			}
		}
		//------------------------------------------------saleem extension-----------------------------
		String configFile = "src/demo/Demo1SD.n3";
		Model m=FileManager.get().loadModel(configFile);
					
		for (ServiceGroup sg1 : groupedTriples.values()) {
			System.out.println("        ");
			 System.out.println("Service URL: "+ sg1.getService().getUrl());
			 List<Triple> lsttrpl = sg1.getTriples();
			 System.out.println("{");
		     for(int TrplNo=0; TrplNo<lsttrpl.size();TrplNo++)
		     System.out.println("  " + lsttrpl.get(TrplNo));
		         List<Expr> expFltrLst = sg1.getFilters();   
		         for(int FltrNo=0; FltrNo<expFltrLst.size();FltrNo++)
				     System.out.println(" Filter(" + expFltrLst.get(FltrNo)+ ")");
		             System.out.println("}");
		}
		//--------------------------------------------------------------------------------
		for (MultipleServiceGroup msg1 : queryIndividuallyTriples.values()) 
		   {
			List<Triple> lsttrpl = msg1.getTriples();
			
		     for(int TrplNo=0; TrplNo<lsttrpl.size();TrplNo++)  //--for each individual user query triple
		     {
		    	 long MipsmaxRecord=0;   
		    	 String MipsmaxRecord_SrviceUrl="";
			     HashMap<String, MIPsynopsis> hshMipsV = new HashMap<String, MIPsynopsis>();
		    	 List<RemoteService> services = selectServices(registry.getMatchingServices(lsttrpl.get(TrplNo)));
		    	 for (int j = 0; j < services.size(); j++) //--For each service capable of answering the current query triple
		    	 {
		    		Triple trpl = lsttrpl.get(TrplNo);
		    		 System.out.println("        ");	
	 	    		 System.out.println("        ");	
	 	    		 System.out.println("*Service URL : "+ services.get(j).getUrl());
	 	    		 System.out.println(" {");              
	 	    		 System.out.println("  "+ lsttrpl.get(TrplNo));
	 	    		 List<Expr> expFltrLst = msg1.getFilters();   
	 	    		 for(int FltrNo=0; FltrNo<expFltrLst.size();FltrNo++)
	 	    			 System.out.println(" Filter(" + expFltrLst.get(FltrNo)+ ")");
	 	    		 		System.out.println(" }"); 
	 	    		 	//----------- for query result set calculation--------------------------------	
	 	    		  if( (trpl.getObject().isVariable()) && trpl.getSubject().isVariable())	//--we are checking for duplicates in query triples in which subject,objects are not bound
	 	    		  {
	 	    		 		String SdQryStr =  queryString(services.get(j).getGraph(),trpl.getPredicate().toString());
		 						Query query = QueryFactory.create(SdQryStr);
		 						QueryExecution qexec = QueryExecutionFactory.create(query, m);
		 						ResultSet rs = qexec.execSelect();
		 		                	if(rs.hasNext())
		 		            	    	{
		 									QuerySolution record = rs.nextSolution();
		 							   		String [] strMIPsV =record.get("MIPsV").toString().split(" "); //--get MIPs Vector for predicate p of triple trplNo and service no j
		 							        String strPredCount = record.get("predCount").toString();
			 						 	    if(strPredCount.contains("^^"))
			 							 	strPredCount = strPredCount.substring(0, strPredCount.indexOf("^^"));
			 							 	long predCount  = Long.parseLong(strPredCount);
		 						  	        MIPsynopsis synMipsVector = new MIPsynopsis(strMIPsV,242,predCount);	
		 					 	            //------Generate max records, hashmap MIPS vector for MIPS Synopsis
		 					 	            hshMipsV.put(services.get(j).getGraph(),synMipsVector);   
		 					 	            if(MipsmaxRecord<synMipsVector.getOriginalSize())
		 				            	    {
		 				            		  MipsmaxRecord=synMipsVector.getOriginalSize();
		 				            		  MipsmaxRecord_SrviceUrl= services.get(j).getGraph();
		 				            	    }
		 					              }      	
		 	              }	
		    	 }
		    	
		    	 	if( (lsttrpl.get(TrplNo).getObject().isVariable()) && lsttrpl.get(TrplNo).getSubject().isVariable()) 
		    	 	{
		    	 		System.out.print("=========("+lsttrpl.get(TrplNo)+")");
		    	 		MipsSynBased_showRanking(MipsmaxRecord_SrviceUrl,MipsmaxRecord,hshMipsV,lsttrpl.get(TrplNo));
		    	 		// DistinctRsBased_showRanking(maxRecord_SrviceUrl,maxRecord,CpyidsLst,lsttrpl.get(TrplNo));  //optimal solution
		    	 		//VectorSizeBased_showRanking(maxRecord_SrviceUrl,maxRecord,idsLst);
		    	 	}
		      }  
		  }
	
//---------------------------------------------------------------------------------------------
		// build new subplan
		if (groupedTriples.size() > 0 || queryIndividuallyTriples.size() > 0) {

			/*
			 * ArrayList<ServiceGroup> al = new ArrayList<ServiceGroup>(
			 * groupedTriples.values());
			 * al.addAll(queryIndividuallyTriples.values());
			 */

			ArrayList<ServiceGroup> al = new ArrayList<ServiceGroup>(sgsPos);
                   
			if (optimize) { // run optimizer
				PlanElement optimizedPlan = null;
				try {
					PlanOperatorBase planOperatorBase = config
							.getPlanOptimizer().getCheapestPlan(al,cache, cacheEnabled);
					FedQueryEngineFactory.logExplain(planOperatorBase);
					optimizedPlan = planOperatorBase.toARQPlanElement(context);

				} catch (PlanUnfeasibleException e) {
					throw new QueryBuildException("No feasible plan: " + e);
				}
				acc.add(0, optimizedPlan);
				// log.debug("selected: \n"
				// + optimizedPlan.toString());

			} else { // no optimization -> just add elements
				int pos = 0;
				for (ServiceGroup sg : al) {
                           
					if (sg instanceof MultipleServiceGroup) {
						acc.add(pos, FedPlanMultipleService.make(context,
								(MultipleServiceGroup) sg, null,cache,cacheEnabled));
					} else
						acc.add(pos, FedPlanService.make(context, sg, null, cache,cacheEnabled));
					pos++;

				}
			}
			
		}

		// unmatched patterns are executed locally
		if (unmatchedTriples.size() > 0) {
			ElementBasicGraphPattern elementBasicGraphPattern = new ElementBasicGraphPattern();
			for (Triple t : unmatchedTriples)
				elementBasicGraphPattern.addTriple(t);
			acc.add(0, PlanBasicGraphPattern.make(context,
					elementBasicGraphPattern));
		}

		PlanElement ex = PlanGroupDarq.make(context, (List) acc, false);

		// FedPlanFormatter.out(new IndentedWriter(System.out), ex);
		// System.out.println("-----");

		return ex;
	}

/*	private HashMap<String, ArrayList<String>>  sortObjMIPsV_BYsize(HashMap<String, ArrayList<String>> HshobjMIPsV) {
		HashMap<String, ArrayList<String>> objMIPsV_sorted = new HashMap<String, ArrayList<String>>();
		Iterator ServicesIterator = HshobjMIPsV.entrySet().iterator();
		ArrayList<String> curIdsVector = null;
		while (ServicesIterator.hasNext())  
        {
			HshobjMIPsV. 
			Map.Entry entry = (Map.Entry) ServicesIterator.next();
             String objId = (String) entry.getKey();
             curIdsVector = HshobjMIPsV.get(objId);
             Map.Entry nextEntry = (Map.Entry) ServicesIterator.next();
             String nextObjId = (String) entry.getKey();
             curIdsVector = HshobjMIPsV.get(objId);
        }
		return objMIPsV_sorted ;
	}*/

	private List<RemoteService> selectServices(List<RemoteService> services) {
		ArrayList<RemoteService> result = new ArrayList<RemoteService>();
		for (Iterator<RemoteService> it = services.iterator(); it.hasNext();) {
			RemoteService rs = it.next();

			// if there is a Definitive Service only use this one
			if (rs.isDefinitive()) {
				result.clear();
				result.add(rs);
				break;
			}

			result.add(rs);

		}
		return result;
	}

	private void putIntoGroupedTriples(RemoteService s, Triple t) {
		ServiceGroup tg = groupedTriples.get(s);
		if (tg == null) {
			tg = new ServiceGroup(s);
			groupedTriples.put(s, tg);
			sgsPos.add(tg);
		}

		tg.addB(t);
	}

	private void putIntoQueryIndividuallyTriples(Triple t, RemoteService s) {
		MultipleServiceGroup msg = queryIndividuallyTriples.get(t);
		if (msg == null) {
			msg = new MultipleServiceGroup();
			queryIndividuallyTriples.put(t, msg);
			sgsPos.add(msg);
			msg.addB(t);
		}
		msg.addService(s);
        
	}
	
	private String queryString(String srvUrl, String pred)  
	{
	
		String query="prefix rdfs: <http://www.w3.org/5500/01/rdf-schema#> "+
		    "prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
		    "prefix xsd:  <http://www.w3.org/5501/XMLSchema#> "+
		    "prefix sd:   <http://darq.sf.net/dose/0.1#> "+
		    "prefix darq: <http://darq.sf.net/darq/0.1#> "+
		    "prefix geo:  <http://www.w3.org/5503/01/geo/wgs84_pos#>"+
          "SELECT   ?predCount ?MIPsV " + // 
           "WHERE " +
               "{" +
               		"?service sd:graph '"+ srvUrl+"' ."+
               		"?service sd:capability ?cap . " +
               		"?cap sd:predicate <" + pred+ "> ."+
               		"?cap sd:MIPv ?MIPsV. " +
                 	"?cap sd:triples ?predCount. " +
               		"} " ;
	
				return query;
		}
	
	
	private void MipsSynBased_showRanking(String starting_SrviceUrl, Long rsCount, HashMap<String, MIPsynopsis> hshMipsV, Triple trpl )
	{
		System.out.println("----MIPS Synopsis based source selection----===================");
		System.out.println("1. Selected Service Url: " + starting_SrviceUrl+" Total Records: " + rsCount);
		MIPsynopsis curMIPsVector = hshMipsV.get(starting_SrviceUrl);
		 MIPsynopsis selectedMIPsVector = curMIPsVector; 
		 MIPsynopsis UnionVector = selectedMIPsVector;
		 //Collection selectedVector = UnionVector;
		 String selectedSrviceURL=starting_SrviceUrl;
		 hshMipsV.remove(starting_SrviceUrl);
		// System.out.println("size of hashmap:" + HshidsLst.size());
		//long [] CurIdsVector  = new long[curIdsVector.size()];
		//long minOverlap = curIdsVector.size();
		int count = 1;
		long MaxdistinctRecords=-1;
		String SrviceURL="";
	//	Collection unionVector = new ArrayCollection(curIdsVector);
	do
	{
		Iterator ServicesIterator = hshMipsV.entrySet().iterator();
		MaxdistinctRecords=-1;
		while (ServicesIterator.hasNext())  //while 1 started
        {
	         Map.Entry entry = (Map.Entry) ServicesIterator.next();
              SrviceURL = (String) entry.getKey();
             curMIPsVector = hshMipsV.get(SrviceURL);
            // Collection CurVector = new ArrayCollection(curIdsVector);
             long curOverlap= UnionVector.intersectionSize(curMIPsVector);
             long curDistinctRecords = curMIPsVector.getOriginalSize()-curOverlap;
             if(curDistinctRecords>MaxdistinctRecords)
             {
            	 selectedMIPsVector = curMIPsVector;
            	 selectedSrviceURL=SrviceURL;
            	 MaxdistinctRecords=curDistinctRecords;
             }
         }
		//selectedVector = new ArrayCollection(selectedIdsVector);
		if(MaxdistinctRecords>0)
		    UnionVector =  (MIPsynopsis) UnionVector.union(selectedMIPsVector);
		else
			selectedSrviceURL=SrviceURL;
		  count++;
		System.out.println(count+". Selected Service Url: "+selectedSrviceURL + " Total Records: "+ UnionVector.getOriginalSize() );
		hshMipsV.remove(selectedSrviceURL);
			
	}
	while(hshMipsV.size()>0);
	
	
	}
	
}

/*
  * (c) Copyright 5505, 5508 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */