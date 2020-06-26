package knapsacktestpackage;

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
	
	/*{
		  "OS":"SLES",
		  "name":"t3.medium",
		  "vCPU":"2",
		  "ram":"4",
		  "price":"0.1046",
		  "network":"5",
		  "clock":"",
  		  "pricing_ssd":"0.10",
  		  "pricing_hdd":"0.045"
		}*/
	
	public Map jointData(String os_req) throws FileNotFoundException, IOException {
		
		Map<String, ArrayList> baseData = new HashMap();
		
        InputStream fis = new FileInputStream("C:\\Users\\Ashish_Pandey\\Desktop\\Cloud_Computing_1\\Project_AiPaas\\AWS_General_Pricing.json");
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
			    price.add((String)inst.getString("price"));
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
}


/*public final String OS_Linux = "Linux";
public final String OS_Rhel = "RHEL";
public final String OS_Sles = "SLES";
public final String OS_Windows = "WINDOWS";
public final String OS_WinSql = "WINDOWS_SQL";
*/