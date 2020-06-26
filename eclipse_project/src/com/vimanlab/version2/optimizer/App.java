package com.vimanlab.version2.optimizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

public class App {
		String req_os;
		String req_vCPU;
		String req_ram;
		String req_network;
		String req_clock;
		boolean req_gpu;
		String req_storage;
		boolean req_ssd;
		String req_threshold;
		String req_preference;

public App(String req_os, String req_vCPU, String req_ram, String req_network, String req_clock,
				boolean req_gpu, String req_storage, boolean req_ssd, String req_threshold, String req_preference) {
			this.req_os = req_os;
			this.req_vCPU = req_vCPU;
			this.req_ram = req_ram;
			this.req_network = req_network;
			this.req_clock = req_clock;
			this.req_gpu = req_gpu;
			this.req_storage = req_storage;
			this.req_ssd = req_ssd;
			this.req_threshold = req_threshold; 
			this.req_preference = req_preference; 
			
		}

public JsonObjectBuilder initialize(List<String> cspBase) {

	JsonObjectBuilder catalogObjectBuilder = Json.createObjectBuilder();
		try {
			ArrayList cspList = new ArrayList<HashMap>();
			ReadData data = new ReadData();
			for(int i = 0; i < cspBase.size(); i++) {
				String csp_name = cspBase.get(i);
				System.out.println("csp_name: " + csp_name);
				System.out.println("#########################");
				Map<String, ArrayList> baseData = data.jointData(req_os, csp_name);
				cspList.add(baseData);
			}
			
			
			ModelCreator mod = new ModelCreator(cspList);
			
			int int_req_vCPU = Integer.parseInt(req_vCPU);
			double dbl_req_ram = Double.parseDouble(req_ram);
			double dbl_req_network = Double.parseDouble(req_network);
			double dbl_req_clock = Double.parseDouble(req_clock);		
			
			int int_req_threshold = Integer.parseInt(req_threshold);
			
			JsonArrayBuilder redArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder greenArrayBuilder = Json.createArrayBuilder();
			JsonArrayBuilder goldArrayBuilder = Json.createArrayBuilder();
			
			//get the most cost optimal solution
			
			//RED SOLUTIONS		
			redArrayBuilder.add(mod.solveModel(req_os, int_req_vCPU, dbl_req_ram, dbl_req_network, dbl_req_clock));
			
			double zoom = (int_req_threshold + 100)/100.0;
			System.out.println("zoom is :" + zoom);
			
			//GREEN SOLUTIONS
			//get the most optimal solution with excess resources
			for(double i=1.4; i<=1.5; i = i+0.2 ) {
				greenArrayBuilder.add(mod.solveModel(req_os, (int)(int_req_vCPU*i), dbl_req_ram*i, dbl_req_network*i, dbl_req_clock));
			}
			
			//GOLD SOLUTIONS
			//get the most optimal solution with a bit extra resources resources
			for(double i=1.2; i<1.3; i = i+0.2 ) {
				goldArrayBuilder.add(mod.solveModel(req_os, (int)(int_req_vCPU*i), dbl_req_ram*i, dbl_req_network*i, dbl_req_clock));
			}
			
			// Initial Order of objects
			//catalogObjectBuilder.add("Gold", goldArrayBuilder);
			//catalogObjectBuilder.add("Green", greenArrayBuilder);
			//catalogObjectBuilder.add("Red", redArrayBuilder);
			
			//Temporary Change
			catalogObjectBuilder.add("Gold", greenArrayBuilder);
			catalogObjectBuilder.add("Silver", goldArrayBuilder);
			catalogObjectBuilder.add("Bronze", redArrayBuilder);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return catalogObjectBuilder;
	}
}
