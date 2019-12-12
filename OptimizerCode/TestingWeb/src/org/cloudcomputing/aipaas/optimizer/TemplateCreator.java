package org.cloudcomputing.aipaas.optimizer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.vimanlab.version2.optimizer.App;




@Path("/getTemplateCatalog")
public class TemplateCreator {
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getName() {
		return "Ashish";
	}
	//	@Produces(MediaType.TEXT_PLAIN)
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getTemplates(String stringdata) throws Exception {
		
		JsonReader jsonReader = Json.createReader(new StringReader(stringdata));
		JsonObject data = jsonReader.readObject();
		jsonReader.close();
		
		ArrayList<String> sd = new ArrayList<String>();
		sd.add("LINUX");
		sd.add("RHEL");
		sd.add("SLES");
		sd.add("WINDOWS");
		
		String req_os = "LINUX";
		String req_vCPU = "1";
		String req_ram = "2"; //GB
		String req_network = "5";
		String req_clock = "2"; //Ghz
		boolean req_gpu = false;
		String req_storage = "10";// GB
		boolean req_ssd = false;
		String req_threshold = "20";
		String req_preference = "";
		
		if(data.get("req_os")!=null && sd.contains(data.getString("req_os"))) {			
			req_os = data.getString("req_os");
		}
		if(data.get("req_vCPU")!=null) {			
			req_vCPU = data.getString("req_vCPU");
		}
		if(data.get("req_ram")!=null) {			
			req_ram = data.getString("req_ram");
		}
		if(data.get("req_network")!=null) {			
			req_network = data.getString("req_network");
		}
		if(data.get("req_clock")!=null) {			
			req_clock = data.getString("req_clock");
		}
		if(data.get("req_gpu")!=null) {			
			req_gpu = data.getBoolean("req_gpu");
		}
		if(data.get("req_storage")!=null) {		
			req_storage = data.getString("req_storage");
		}
		if(data.get("req_ssd")!=null) {			
			req_ssd = data.getBoolean("req_ssd");
		}
		if(data.get("req_threshold")!=null) {			
			req_threshold = data.getString("req_threshold");
		}
		if(data.get("req_preference")!=null) {
			req_preference = data.getString("req_preference");
		}
		
		ArrayList<String> cspBase = new ArrayList<String>();
		//cspBase.add("AWS");
		//cspBase.add("GENI");
		//cspBase.add("GCP");
		//cspBase.add("AZURE");
		cspBase.add("LEWIS");
		cspBase.add("PLSCI2");
		cspBase.add("ISI");
		
		App appModel = new App(req_os, req_vCPU, req_ram, req_network, req_clock, req_gpu, req_storage, req_ssd, req_threshold, req_preference);
		JsonObjectBuilder catalog = appModel.initialize(cspBase);
		
		System.out.println(data);
		return catalog.build().toString();
		
	}
}
