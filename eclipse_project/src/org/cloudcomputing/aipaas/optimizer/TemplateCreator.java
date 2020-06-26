package org.cloudcomputing.aipaas.optimizer;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
		return "Get request works for this service";
	}
	//	@Produces(MediaType.TEXT_PLAIN)
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getTemplates(String stringdata) throws Exception {
		
		JsonReader jsonReader = Json.createReader(new StringReader(stringdata));
		JsonObject data = jsonReader.readObject();
		jsonReader.close();
		
		//reading the properties file for initialization
		Properties prop=new Properties();
		String propFileName = "/config/config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		prop.load(inputStream);
		
		System.out.println(prop.getProperty("os"));
		
		//Used to check format data of incoming instances. Single OS check done. To test if the format of os request is in correct format
		String osPropString = prop.getProperty("os");
		String[] osPropList = osPropString.split(",");
		List<String> allOsOptions = Arrays.asList(osPropList);
		
		//default values to be used if there is no explicit request from the user.
		// Can we read it from a default properties file?? which can be configured by the user of application??
		String req_os = prop.getProperty("req_os");
		String req_vCPU = prop.getProperty("req_vCPU");
		String req_ram = prop.getProperty("req_ram");
		String req_network = prop.getProperty("req_network");
		String req_clock = prop.getProperty("req_clock");
		boolean req_gpu = Boolean.parseBoolean(prop.getProperty("req_gpu"));
		String req_storage = prop.getProperty("req_storage");
		boolean req_ssd = Boolean.parseBoolean(prop.getProperty("res_ssd"));
		String req_threshold = prop.getProperty("req_threshold");
		String req_preference = prop.getProperty("req_preference");
		
		
		if(data.get("req_os")!=null && allOsOptions.contains(data.getString("req_os"))) {			
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
		
		// CSPs base to be considered for knowledge base for OnTimeURB brokering.
		String cspPropString = prop.getProperty("csp_files");
		String[] cspPropList = cspPropString.split(",");
		List<String> cspBase = Arrays.asList(cspPropList);
		
		
		// initialize the broker object
		App appModel = new App(req_os, req_vCPU, req_ram, req_network, req_clock, req_gpu, req_storage, req_ssd, req_threshold, req_preference);
		
		// execute the broker object
		JsonObjectBuilder catalog = appModel.initialize(cspBase);
		
		System.out.println(data);
		return catalog.build().toString();
		
	}
}
