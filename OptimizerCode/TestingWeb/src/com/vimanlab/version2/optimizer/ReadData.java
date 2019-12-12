package com.vimanlab.version2.optimizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class ReadData {


	public Map jointData(String os_req, String cspName, Boolean agility) throws FileNotFoundException, IOException {
		
		Map<String, ArrayList> baseData = new HashMap();
		
		//FileInputStream	fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Cloud_Computing_1\\Project_AiPaas\\"  +cspName +".json");
		//FileInputStream	fis = new FileInputStream("//tommy//apache-tomcat-9.0.22//webapps//"  +cspName +".json");
		//FileInputStream	fis = new FileInputStream("//users//apfd6//"  +cspName +".json");
		
		FileInputStream	fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Research All\\Project_AiPaas\\"  +cspName +".json");
		//C:\Users\Ashish_Pandey\Desktop\Research All\Project_AiPaas
		
		
		JsonReader reader = Json.createReader(fis);
        JsonArray a = reader.readArray();
		 
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
			    if(agility) {
			    	pri = getAgilityFactorNormalizedPrice(pri , cspName);
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

	private String getAgilityFactorNormalizedPrice(String pri, String cspName) {
		
		double max_price = 5.424;
		double min_price = 0;
	    
		double price = Double.parseDouble(pri);
		
		double normalized_price = (price - min_price)*100/(max_price - min_price);
		
		// CSPS: AWS, GENI, GCP, AZURE 
		
		double agilityfactor = 1.0;
		
		if(cspName.equals("AWS")) {
			agilityfactor = 10.0;
		} else if(cspName.equals("GENI")) {
			agilityfactor = 4.0;
		} else if(cspName.equals("GCP")) {
			agilityfactor = 9.6;
		} else {
			agilityfactor = 8;
		}
			
		System.out.print("normalized_price: " + normalized_price + " ");	
		double new_price = normalized_price/agilityfactor;	
		
		return new_price + "";
	}
}


/*
 * if(cspName.equals("AWS")) 
fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Cloud_Computing_1\\Project_AiPaas\\AWS_General_Pricing.json");
else if(cspName.equals("AZURE")) 
fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Cloud_Computing_1\\Project_AiPaas\\AZURE_Instances.json");
else if(cspName.equals("GENI")) 
fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Cloud_Computing_1\\Project_AiPaas\\GENI_Instances.json");
else if(cspName.equals("OSG")) 
fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Cloud_Computing_1\\Project_AiPaas\\OSG_Instances.json");
else 
*
*/
