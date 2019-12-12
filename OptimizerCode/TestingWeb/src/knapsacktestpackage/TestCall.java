package knapsacktestpackage;

import javax.json.JsonObjectBuilder;

public class TestCall {

	public static void main(String[] args) {
		
		String req_os = "LINUX"; 
		String req_vCPU = "4";
		String req_ram = "10"; 
		String req_network ="4"; 
		String req_clock = "1"; 
		boolean req_gpu = false;
		boolean req_ssd = false;
		String req_storage ="";
		String req_threshold ="5";
		String req_preference ="";
		
		App appModel = new App(req_os, req_vCPU, req_ram, req_network, req_clock, req_gpu, req_storage, req_ssd, req_threshold, req_preference);
		JsonObjectBuilder catalog = appModel.initialize();
		
		System.out.println(catalog.build());

	}

}
