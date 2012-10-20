package ie.deri.service.description;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

import de.mpii.d5.synopsis.ArrayCollection;
import de.mpii.d5.synopsis.Collection;
import de.mpii.d5.synopsis.MIPsynopsis;

public class ServiceDescription {
	
	public static   BufferedWriter bw ;
	public static long trplCount ;
	
	/**
	 * @param args
	 * @author saleem
	 * Generate automatic Service Description for an RDF data
	 * @return 
	 * @throws IOException 
	 */
		
	public static void main(String[] args) throws IOException 
	{
		bw= new BufferedWriter(new FileWriter(new File("src/ie/deri/service/description/SD.n3")));
		writePrefixes();
		File folder = new File("src/ie/deri/service/description/rdfdata");
		File[] listOfFiles = folder.listFiles();
    	for (File listOfFile : listOfFiles)
	    {	
			 String rdfFile ="src/ie/deri/service/description/rdfdata/"+listOfFile.getName();
		     String endPointUrl = "http://localhost:8890/sparql";
			 String graph = "http://localhost:8890/"+listOfFile.getName().substring(0,listOfFile.getName().length()-3);
			 System.out.println("Generating Summaries: "+ listOfFile.getName());
			 buildServiceDescription(rdfFile,endPointUrl,graph);	
			 
		    }
    	bw.close();
    	System.out.print("Data Summaries are secessfully generated...");
	}
	
	//----- get data for which you need to build a service description--------------------------------
	
	public  String getDataQuery() {
		
		String dataQuery = "prefix rdfs: <http://www.w3.org/5500/01/rdf-schema#> "+
			    "prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
			    "prefix xsd:  <http://www.w3.org/5501/XMLSchema#> "+
			    "prefix rdfS:  <http://www.w3.org/2000/01/rdf-schema#> "+
			    "SELECT   * " + // 
	         "FROM <http://localhost:8890/DS11>" +
	          "WHERE " +
	               "{" +
	           
	               		"?s ?p ?o . " +
	                "} " ;
		return dataQuery;
	}
//----------build service descriptions----------------------------------------------------------------------
	public static  void buildServiceDescription(String rdfFile, String endPointUrl, String graph) throws IOException 
	{
		Model m=FileManager.get().loadModel(rdfFile);
		ArrayList<String> lstPred = getPredicates(m);
		String MIPsV ="";
		long totalTrpl=0;
		bw.append("#---------------------"+endPointUrl+" Descriptions--------------------------------");
		bw.newLine();
		bw.append("[] a sd:Service ;");
		bw.newLine();
		bw.append("     sd:url   <"+endPointUrl+"> ;");
		bw.newLine();
		bw.append("     sd:graph \""+graph+"\";");
		bw.newLine();
	      
		for(int i =0 ;i<lstPred.size();i++)
		{
			 bw.append("     sd:capability");
			 bw.newLine();
			 bw.append("         [");
			 bw.newLine();
			 bw.append("           sd:predicate  <"+lstPred.get(i)+"> ;");
			 bw.newLine();
			 MIPsV= getMIPsV(lstPred.get(i),m);
			 bw.append("           sd:MIPv       \""+MIPsV+"\" ;");
			 bw.newLine();
			 bw.append("           sd:triples    "+trplCount+"  ;");
		     bw.newLine();
			 bw.append("         ] ;");
			 bw.newLine();
			totalTrpl = totalTrpl+trplCount;
		 }
		     bw.append("     sd:totalTriples \""+totalTrpl+"\" ;");
		     bw.newLine();
		     bw.append("             .");
		     bw.newLine();
		
	}
		
//-----------------------Return Mean wise independent permutation vector (MIPsV) for a predicate-------------	
private static String getMIPsV(String pred,Model m) throws IOException
{
	ArrayList<Long> idsVector = new ArrayList<Long>() ;
	String MIPsV = "";
	String query = getMIPsVQury(pred);
	QueryExecution qexec = QueryExecutionFactory.create(query, m);
	ResultSet rs = qexec.execSelect();
	 while( rs.hasNext() ) 
	    {
	       	QuerySolution result = rs.nextSolution();
	       	String sbj_obj = result.get("s").toString().concat(result.get("o").toString());
	       	idsVector.add((long) (sbj_obj.hashCode()));
		 }
	  trplCount = idsVector.size();
	 Collection c = new ArrayCollection(idsVector);
	 MIPsynopsis  synMIPs= new MIPsynopsis(c, (int) trplCount/2, 242);
	 long[] minValues = synMIPs.minValues;
	  for(int i=0;i<minValues.length;i++)
		  MIPsV = MIPsV.concat(minValues[i]+" ");
	 
	 return MIPsV.trim();
}
//--------------------------------------------------------------------------
	private static String getMIPsVQury(String pred) 
	{
						String MIPsVQuery = "SELECT   ?s  ?o " + // 
			            "WHERE " +
			               "{" +
			           
			               		"?s <"+pred+"> ?o " +
			                "} " ;
				return MIPsVQuery;
	}
	


//------------Write prefixes used in service description---------------------------------------------
	private static void  writePrefixes() throws IOException 
	{
		bw.append("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.");
		bw.newLine();
		bw.append("@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.");
		bw.newLine();
		bw.append("@prefix xsd:  <http://www.w3.org/2001/XMLSchema#>.");
		bw.newLine();
		bw.append("@prefix sd:   <http://darq.sf.net/dose/0.1#>.");
		bw.newLine();
		bw.append("@prefix darq: <http://darq.sf.net/darq/0.1#>.");
		bw.newLine();
	    }

	//------------------get predicates lst--------------------------------------------------------------

private static ArrayList<String> getPredicates(Model m) 
{
	ArrayList<String>  predLst = new ArrayList<String>();
	String query = getPredQury();
	QueryExecution qexec = QueryExecutionFactory.create(query, m);
	ResultSet rs = qexec.execSelect();
	 while( rs.hasNext() ) 
	    {
	       	QuerySolution result = rs.nextSolution();
	       	predLst.add(result.get("p").toString());
	       	//System.out.println(result.get("p").toString());
	    }
	return predLst;
}
//--------------------------------------------------------------------------
	private static String getPredQury() {
		
				String dataQuery = "SELECT   distinct ?p " + // 
			            "WHERE " +
			               "{" +
			           
			               		"?s ?p ?o " +
			                "} " ;
				return dataQuery;
	}

}
