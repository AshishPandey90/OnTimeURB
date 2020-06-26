package com.vimanlab.version2.optimizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ReadData {

	public Map jointData(String os_req, String cspName) throws FileNotFoundException, IOException {
		
		//reading the properties file for initialization
		Properties prop=new Properties();
		String propFileName = "/config/config.properties";
		InputStream inputStreamProperty = getClass().getClassLoader().getResourceAsStream(propFileName);
		prop.load(inputStreamProperty);
		
		boolean ml_enabled = Boolean.parseBoolean(prop.getProperty("ml_enabled"));
		boolean agility_enabled = Boolean.parseBoolean(prop.getProperty("agility_enabled"));
		
		
		Map<String, ArrayList> baseData = new HashMap();
		/*
			// Windows file system to read knowledge base.
			FileInputStream	fis = new FileInputStream("D:\\Data\\Old_Laptop\\Project_AiPaas\\"  +cspName +".json");
			// Linux file system to read knowledge base.
			//FileInputStream	fis = new FileInputStream("//users//apfd6//"  +cspName +".json");
		*/
		// reading resources from json files which are part of project itself.
		String cspFileName = "/resources/data/"+cspName+".json";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(cspFileName);
		JsonReader reader = Json.createReader(inputStream);
		
		//JsonReader reader = Json.createReader(fis);
        JsonArray a = reader.readArray();
		 
        // Data from all instances is distributed in order into multiple dimensions.
        // These dimensions act as variables used in integer linear programming ILP later.
         ArrayList csp = new ArrayList();
		 ArrayList name = new ArrayList();
		 ArrayList vCPU = new ArrayList();
		 ArrayList ram = new ArrayList();
		 ArrayList price = new ArrayList();
		 ArrayList network = new ArrayList();
		 ArrayList clock = new ArrayList();
		 
		  for (Object o : a)
		  {
		    JsonObject inst = (JsonObject) o;
		    String os = (String) inst.getString("OS");
		    
		    if(os.equals(os_req)) {
		    	csp.add((String) inst.getString("csp"));
		    	name.add((String) inst.getString("name"));
			    vCPU.add((String) inst.getString("vCPU"));
			    ram.add((String)inst.getString("ram"));
			    
			    String pri = (String)inst.getString("price");
			    
			    // If agility of CSPs are considered then the prices will have to be normalized. 
			    if(agility_enabled) {
			    	pri = getAgilityFactorNormalizedPrice(pri , cspName, prop);
				  }
			    // If machine learning is enabled
			    if(ml_enabled) {
			    	pri = getProbabilityFactorNormalizedPrice(pri , cspName, prop);
				  }
			    price.add(pri);
			    
			    network.add((String)inst.getString("network"));
			    clock.add((String)inst.getString("clock"));
		    }
		    
		  }
		  
		  baseData.put("csp", csp);
		  baseData.put("name", name);
		  baseData.put("vCPU", vCPU);
		  baseData.put("ram", ram);
		  baseData.put("price", price);
		  baseData.put("network", network);
		  baseData.put("clock", clock);
		  
		  return baseData;
	}

	private String getProbabilityFactorNormalizedPrice(String pri, String cspName, Properties prop) {
   
			double price = Double.parseDouble(pri);
			boolean ml_default = Boolean.parseBoolean(prop.getProperty("ml_default"));

			//default probability distribution factor
			double probFactor = 1.0;
				
			//agility factors benchmarks to be configured by users from a configuration file.
			if(ml_default) {
				if(cspName.equals("AWS")) {
					probFactor = Double.parseDouble(prop.getProperty("prob_factor_aws"));
				} else if(cspName.equals("GENI")) {
					probFactor = Double.parseDouble(prop.getProperty("prob_factor_geni"));
				} else if(cspName.equals("GCP")) {
					probFactor = Double.parseDouble(prop.getProperty("prob_factor_gcp"));
				} else if(cspName.equals("AZURE")) {
					probFactor = Double.parseDouble(prop.getProperty("prob_factor_azure"));
				} else {
					probFactor = 1.0;
				}
			}else {
				//write code to get these values from ml classifier
			}
			
			double new_price = price*probFactor;	
			return new_price + "";
}

	private String getAgilityFactorNormalizedPrice(String pri, String cspName, Properties prop) {
		
		//double max_price = 5.424; 
		// maximum price of any single instance from any cloud platform.
		double max_price = Double.parseDouble(prop.getProperty("max_instance_price")); 
		double min_price = 0;
	    
		double price = Double.parseDouble(pri);
		
		double normalized_price = (price - min_price)*100/(max_price - min_price);
		
		//default agility factor
		double agilityfactor = 1.0;
		
		//agility factors benchmarks to be configured by users from a configuration file.
		if(cspName.equals("AWS")) {
			agilityfactor = Double.parseDouble(prop.getProperty("ag_factor_aws"));
		} else if(cspName.equals("GENI")) {
			agilityfactor = Double.parseDouble(prop.getProperty("ag_factor_geni"));
		} else if(cspName.equals("GCP")) {
			agilityfactor = Double.parseDouble(prop.getProperty("ag_factor_gcp"));
		} else if(cspName.equals("AZURE")) {
			agilityfactor = Double.parseDouble(prop.getProperty("ag_factor_azure"));
		} else {
			agilityfactor = 1.0;
		}
			
		System.out.print("normalized_price: " + normalized_price + " ");	
		double new_price = normalized_price/agilityfactor;	
		
		return new_price + "";
	}
}
