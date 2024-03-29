package de.hu_berlin.informatik.wbi.darq.mapping;


import static de.hu_berlin.informatik.wbi.darq.mapping.MapSearch.SWRL_MULTIPLY;
import static de.hu_berlin.informatik.wbi.darq.mapping.MapSearch.SWRL_STRINGCONCAT;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Rule{
	
	private ArrayList<RulePart> rulePartList;
	private String type = new String();
	private URI ruleURI;
	
	public Rule(URI uri){
		ruleURI = uri;
		rulePartList = new ArrayList<RulePart>();
	}

	public Rule(URI uri, RulePart part){
		ruleURI = uri;
		rulePartList = new ArrayList<RulePart>();
		rulePartList.add(part);
	}

	public Rule(URI uri, ArrayList<RulePart> rulePartList){
		ruleURI = uri;
		this.rulePartList = new ArrayList<RulePart>();
		this.rulePartList.addAll(rulePartList);
	}

	/**
	 * adds a RulePart to this rule
	 * @param rulePart
	 */
	public void addPart(RulePart rulePart){
		rulePartList.add(rulePart);
	}
	public  ArrayList<RulePart> getRulePartList() {
		return rulePartList;
	}

	public void setRulePartList(ArrayList<RulePart> rulePartList) {
		this.rulePartList = rulePartList;
	}
	
	/**
	 * 
	 * @return URI of this rule
	 */
	public URI getRuleURI() {
		return ruleURI;
	}

	public void setRuleURI(URI ruleURI) {
		this.ruleURI = ruleURI;
	}
	
	public void setMultiply(){
		type = SWRL_MULTIPLY;
	}
	public void setStringConcat(){
		type = SWRL_STRINGCONCAT;
	}
	
	/**
	 * 
	 * @return true, if it is a swrlb:multiply rule
	 */
	public boolean isMultiply(){
		if(type.equals(SWRL_MULTIPLY)) return true;
		else return false;
	}
	
	/**
	 * 
	 * @return true, if it is a swrlb:stringConcat rule
	 */

	public boolean isStrincConcat(){
		if(type.equals(SWRL_STRINGCONCAT)) return true;
		else return false;
	}
	
	/**
	 * 
	 * @param URI of a RulePart
	 * @return true, if this RulePart is contained in this rule
	 */
	public boolean containsPart(URI part){
		for(RulePart rulePart : rulePartList){
			if(rulePart.getUri().equals(part)) return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param URI of the part you want to get
	 * @return a RulePart, see javadoc
	 */
	public RulePart getPart(URI part){
		for(RulePart rulePart : rulePartList){
			if(rulePart.getUri().equals(part)) return rulePart;
		}
		return null;
	}
	
	/** 
	 * @return a Set of all parts of a head from this rule, not ordered.
	 * Use getRulePart for an ordered List*/

	public Set<RulePart> getHeadParts(){
		HashSet<RulePart> headParts = new HashSet<RulePart>();
		for(RulePart rulePart : rulePartList){
			if(rulePart.isHead()) headParts.add(rulePart);
		}
		return headParts;
	}
	
	/** 
	 * @return a Set of all parts of a body from this rule, not ordered.
	 * Use getRulePart for an ordered List*/
	public Set<RulePart> getBodyParts(){
		HashSet<RulePart> bodyParts = new HashSet<RulePart>();
		for(RulePart rulePart : rulePartList){
			if(rulePart.isBody()) bodyParts.add(rulePart);
		}
		return bodyParts;
	}
}
