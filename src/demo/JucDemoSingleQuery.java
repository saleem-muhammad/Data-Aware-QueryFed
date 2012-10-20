package demo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.darq.core.DarqDataset;
import com.hp.hpl.jena.query.darq.engine.FedQueryEngineFactory;
import com.hp.hpl.jena.query.resultset.ResultSetMem;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class JucDemoSingleQuery {

    public static final String configFile = "src/demo/Demo1SD.n3";

    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
    /*    Logger.getLogger(DarqTransform.class).setLevel(Level.DEBUG);
        Logger.getLogger(FedQueryEngine.class).setLevel(Level.DEBUG);
        //Logger.getLogger(BasicPlanOptimizer.class).setLevel(Level.DEBUG);
        Logger.getLogger(CostBasedBasicOptimizer.class).setLevel(Level.DEBUG); */
        
    	/*String [] q = new String[]{
    			 "PREFIX spatial: <java:org.geospatialweb.arqext.>",
    		    "PREFIX lgdo: <http://linkedgeodata.org/ontology/>",
    		    "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>",
    		     "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
    		     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
    			"SELECT ?s ?name ?lat ?long WHERE {" ,
    			//"<http://linkedgeodata.org/triplify/node450250100> rdf:type ?s." ,
    			//"{?uri rdfs:type ?s." ,
    			
    			//"?uri rdfs:label ?name.", 
    			
    			"?uri geo:lat ?lat. ?uri geo:long ?long.",
    			
    			"}"
    		//	"SELECT  ?o WHERE {<http://linkedgeodata.org/triplify/node450250100> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o}"
    				    	
    	};*/
       String [] q = new String[]{
        "PREFIX spatial: <java:org.geospatialweb.arqext.>",
        "PREFIX lgdo: <http://linkedgeodata.org/ontology/>",
        "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>",
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
        "SELECT ?uri ?name ?lat ?long   WHERE {",
       
      "?uri <http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/bio2rdfSymbol> ?name.",
        //   "?uri rdfs:label ?name.",
         // "?uri rdf:type lgdo:pub.",
       // "	Filter(?name=lgdo:cafe).",
        //"Filter(?lat=53.56).",
        // "?uri rdfs:label 'Arabica Cafe'  .",
      //  "?uri geo:lat ?lat." +
       //  "?uri geo:long ?long.",
       // "FILTER (REGEX(?lat, 53.2733106) )",
        //"FILTER (REGEX(?lat, 53.2733106) )",
         "        }	"
        };
       /*String [] q = new String[]{ 
                "PREFIX dc: <http://purl.org/dc/elements/1.1/#>",
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/#>",
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
                "SELECT ?n ?mbox ?date {",
              // "  ?x dc:title 'Jena: Implementing the Semantic Web Recommendations' .", 
              "  ?x dc:creator ?n .", 
                "  ?y foaf:name ?n .",
             // "  ?y foaf:mbox ?mbox .",
               // "  ?y rdf:type foaf:Person ." ,
            //   "FILTER (REGEX(?n, 'abc') )", 
             //  "  OPTIONAL {?x dc:date ?date .}",
               
                "}"
        }; */
 
        
        
        
        String querystring = getQueryString(q);

        // output query and wait for key
        System.out.println("Query: \n------ \n" + querystring);
        waitForKey();

        long startTime = System.currentTimeMillis();
        // register new FedQueryEngineFactory and load configuration from file
       FedQueryEngineFactory.register(configFile,null,0,null,false);

       // create query
       Query query = QueryFactory.create(querystring);
      
            //  query =query.cloneQuery();
          // ArrayList<Element> elemelst=   query.getQueryElement();
          //  Object queryptrn= query.getQueryPattern();
   
     
       // create model - it is used for the local parts of the query -
       Dataset ds = new DarqDataset();
       
       // get FedQueryEngineFactory
       QueryExecution qe = QueryExecutionFactory.create(query, ds);
         int count=0;
       // execute
       ResultSet rs = qe.execSelect();
       long endTime = System.currentTimeMillis();
       List Lstvar = rs.getResultVars();
      // System.out.println("=========================================Results===========================================");
      // for (int V=0;V<Lstvar.size();V++)
		//{
   	   // System.out.println("   " +(String) Lstvar.get(V) + "   ");
	//		}
      /* while (rs.hasNext())  //start of while 2 
		{
			QuerySolution result = rs.nextSolution();
			System.out.print("   "+count+ "   ");
			 
			for (int Var=0;Var<Lstvar.size();Var++)
			{
				
				RDFNode s  = result.get((String) Lstvar.get(Var));
		       if(s!=null)
		       {
				String sub= s.toString();
		         if(sub.contains("^^"))
		           sub = sub.substring(0, sub.indexOf("^^"));
		           System.out.print("   " +sub + "  ");	 
		       }
		       else
		    	System.out.print("   ");
		       
		       }    //end of for 
		           count=count+1;
				 System.out.println();
		  }   //end of while 2 */
     //    ResultSetFormatter.out(System.out, rs, query) ;
       ResultSetMem rsm = new ResultSetMem(rs);
       
       //System.out.println("=========================================Results===========================================");
       
       // output results
     
       ResultSetFormatter.out(System.out, rsm, query);
       System.out.println("Results: "+rsm.size()); 
System.out.println("Execution time is : " + (endTime-startTime));
    }
    
    protected static String getQueryString(String [] in) {
        String s ="";
            for (String sub:in) {
                s+=sub+"\n";
            }
        return s;
    }

    
    public  static void waitForKey() {
        InputStreamReader reader = new InputStreamReader (System.in);

            System.out.println("- press ENTER to continue -");
                try    {
                    reader.read();
                }
                catch (IOException e)    {}

    }

}


// --- 1
/*   String qs = "PREFIX dc: <http://purl.org/dc/elements/1.1/#>"
           + "PREFIX foaf: <http://xmlns.com/foaf/0.1/#>"
           + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
           + "PREFIX hp: <http://jena.hpl.hp.com/schemas/hpcorp#>"
           + "SELECT ?n ?mbox  ?managername{ \n" +
           // "OPTIONAL {?a foaf:homepage ?c . }"+
           "?x dc:title \"Jena: Implementing the Semantic Web Recommendations\" ."+ 
           "?x dc:creator ?n ." + 
           "?y foaf:name ?n ." +
           "?y foaf:mbox ?mbox ." +
           "?y hp:manager ?manager ." +
           "?manager foaf:name ?managername ." +
           "?y rdf:type foaf:Person" + 
           "}";*/
    
/*   String qs = "PREFIX dc: <http://purl.org/dc/elements/1.1/#>"
       + "PREFIX foaf: <http://xmlns.com/foaf/0.1/#>"
       + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
       + "PREFIX hp: <http://jena.hpl.hp.com/schemas/hpcorp#>"
       + "SELECT ?name ?mbox ?title { \n" +
       // "OPTIONAL {?a foaf:homepage ?c . }"+
       "?b foaf:name \"Bastian Ruben Quilitz\" ." + 
       "?b hp:manager ?manager ." +
       "?y hp:manager ?manager ." +
       "?y foaf:name ?name . "+
       "?y foaf:mbox ?mbox ." +
       "?p dc:creator ?name ." +
       "?p dc:title ?title ." +
       "?y rdf:type foaf:Person" + 
       "}";  */
       
   
   
   
   
/*    String qs = "PREFIX dc: <http://purl.org/dc/elements/1.1/#> \n"
       + "PREFIX foaf: <http://xmlns.com/foaf/0.1/#> \n"
       + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
       + "SELECT ?title ?n ?mbox { \n" +
       "?x dc:title ?title . \n"+ 
       "?x dc:creator ?n . \n" + 
       "?y foaf:name ?n . \n" +
       "?y foaf:mbox ?mbox . \n" +
       "?y rdf:type foaf:Person \n" + 
       "}";*/
   
   