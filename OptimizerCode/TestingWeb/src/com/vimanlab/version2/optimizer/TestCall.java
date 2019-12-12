package com.vimanlab.version2.optimizer;

import java.util.ArrayList;

import javax.json.JsonObjectBuilder;

public class TestCall {

	public static void main(String[] args) {
		
		String req_os = "LINUX";
		String req_vCPU = "10";
		String req_ram = "50";
		String req_network ="5";
		String req_clock = "1.1";
		boolean req_gpu = false;
		boolean req_ssd = false;
		String req_storage ="";
		String req_threshold ="20";
		String req_preference ="";
		
		ArrayList<String> cspBase = new ArrayList<String>();
		cspBase.add("AWS");
		//cspBase.add("AWS");
		cspBase.add("GENI");
		cspBase.add("GCP");
		cspBase.add("AZURE");
		//cspBase.add("ISI");
		
		//cspBase.add("OSG");
		App appModel = new App(req_os, req_vCPU, req_ram, req_network, req_clock, req_gpu, req_storage, req_ssd, req_threshold, req_preference);
		JsonObjectBuilder catalog = appModel.initialize(cspBase);
		
		System.out.println(catalog.build());

	}

}
