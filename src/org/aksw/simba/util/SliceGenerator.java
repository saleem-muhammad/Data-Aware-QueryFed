/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.simba.util;

import ie.deri.service.description.ServiceDescription;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ngonga
 */
public class SliceGenerator {

    /**
     * Generates the size of the knowledge bases
     *
     * @param totalSize Total size of input knowledge base
     * @param discrepancy Maximal size difference between two slices
     * @param numberOfSlices Number of slices to be generated
     * @return Distribution of slice sizes
     */
    public static List<Integer> getSliceSizes(int totalSize, int discrepancy, int numberOfSlices) {
        List<Double> delta = new ArrayList<Double>();
        delta.add(0.0);
        for (int i = 1; i < numberOfSlices - 1; i++) {
            delta.add(Math.random());
        }
        delta.add(1.0);

        double m = totalSize;
        for (int i = 0; i < delta.size(); i++) {
            m = m - discrepancy * delta.get(i);
        }

        m = Math.ceil(m / (double) numberOfSlices);
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < numberOfSlices; i++) {
            result.add((int) Math.ceil(m + delta.get(i) * (double) discrepancy));
        }
        return result;
    }

    /** Reads slices from the input knowledge base according to the distribution in slices
     * 
     * @param sizes Size of the slices
     * @param inputFile Input knowledge base
     * @return Slices
     */
    public static List<List<String>> readSlices(List<Integer> sizes, String inputFile) {
        List<List<String>> result = new ArrayList<List<String>>();
        try {
            String s;
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            for (int i = 0; i < sizes.size(); i++) {
                List<String> slice = new ArrayList<String>();
                int size = sizes.get(i);
                for (int j = 0; j < size; j++) {
                    s = reader.readLine();
                    if (s != null) {
                        slice.add(s);
                    }
                }
                result.add(slice);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /** Adds duplicates to the slices
     * 
     * @param input Slices without duplicates
     * @param numberOfDuplicates Number of slices to be used for generating duplicates
     * @return Slices with duplicates
     */   
    public static List<List<String>> createDuplicates(List<List<String>> input, int numberOfDuplicates) {
        if(numberOfDuplicates == 0) return input;
        Set<Integer> duplicateIndex = new HashSet<Integer>();
        while (duplicateIndex.size() < numberOfDuplicates) {
            duplicateIndex.add((int) (input.size() * Math.random()));
        }
        System.out.println("Slices to be duplicated: "+duplicateIndex);
        List<List<String>> toBeAdded = new ArrayList<List<String>>();
        for (int i = 0; i < input.size(); i++) {
            toBeAdded.add(new ArrayList<String>());
        }
        for (int i : duplicateIndex) {
            //get data to be duplicated
            List<String> duplicates = input.get(i);
            //add data to other sources
            for (String data : duplicates) {
                int index;
                //pick a data source that is not one of the source to be duplicated
                do {
                    index = (int) (input.size() * Math.random());
                } while (duplicateIndex.contains(index));
                // add data 
                //System.out.println("Add data to "+index);
                toBeAdded.get(index).add(data);
            }
        }
        
        //merge
        int size = input.size();
       
        for(int i=0; i<size; i++)
        {
            for(int j=0; j<toBeAdded.get(i).size(); j++)
            {
                input.get(i).add(toBeAdded.get(i).get(j));
            }
        }
        
        return input;
    }

    /** Writes the slices to the hard drive
     * 
     * @param slices Slices as list of strings
     * @param outputFile Output file pattern
     */
    public static void writeSlices(List<List<String>> slices, String outputFile) {
        try {
            for (int i = 0; i < slices.size(); i++) {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile + (int )(i+1) +".n3")));
                for (int j = 0; j < slices.get(i).size(); j++) {
                    writer.println(slices.get(i).get(j));
                }
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Counts the number of lines in a file
     *
     * @param file Input file
     * @return Number of lines
     */
    public static int getFileSize(String file) {
        int count = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String s;
            s = reader.readLine();
            while (s != null) {
                s = reader.readLine();
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /** Computes slices and writes them to the harddrive
     * 
     * @param input Input knowledge base
     * @param numberOfSlices Number of slices
     * @param discrepancy Absolute value for the discrepancy
     * @param numberOfDuplicateKBs Number of slices to duplicate
     * @param output Output files
     */
    public static void computeSlices(String input, int numberOfSlices, int discrepancy, int numberOfDuplicateKBs, String output) {
        int kbSize = getFileSize(input);
        System.out.println("Found " + kbSize + " triples");
        List<Integer> sliceSizes = getSliceSizes(kbSize, discrepancy, numberOfSlices);
        System.out.println("KB sizes are " + sliceSizes);
        List<List<String>> slices = readSlices(sliceSizes, input);
        System.out.println("Read slices");
        slices = createDuplicates(slices, numberOfDuplicateKBs);
        System.out.println("Created duplicates");
        writeSlices(slices, output);
        System.out.println("Wrote slices to " + output);
    }

    /** Returns the parameters for the tool
     * 
     */
    public static void usage()
    {
        System.out.println("\nPlease give in the parameters in the following order:");
        System.out.println("arg1: Source file (only nt supported)");
        System.out.println("arg2: Number of slices (integer)");
        System.out.println("arg3: Discrepancy (absolute value, integer)");
        System.out.println("arg4: Number of slices to duplicate (integer)");
        System.out.println("arg5: Pattern for output file");        
    }
    
    public static void main(String args[]) throws IOException {
      //  if(args.length != 5)
        //    usage();
        //else
    	String dataSet="src/org/aksw/simba/util/diseasome_dump.nt";
    	int noOfSlices= 10;
    	int discrepancy=20;
    	int noDupSlices=3;
    	String outputFolder="src/ie/deri/service/description/rdfdata/DS";
    	
    	
            computeSlices(dataSet,noOfSlices,discrepancy,noDupSlices,outputFolder);
            ServiceDescription sd = new ServiceDescription();
            sd.main(null);
            
    }
}
