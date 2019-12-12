package knapsacktestpackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

//import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

//n VARIABLE TEST
public class ModelCreator {
	
	Map<String, ArrayList> resourcePool;
	
	public ModelCreator(Map<String, ArrayList> resourcePool) {
		this.resourcePool = resourcePool;
	}

	public JsonObjectBuilder solveModel(String req_os, Integer req_vCPUs, Double req_ram, Double req_network, Double req_clock) {
		//JsonObjectBuilder tempObjBuilder = Json.createObjectBuilder();
		JsonArrayBuilder tempArrayBuilder = Json.createArrayBuilder();
		JsonObjectBuilder finalJSONBuilder = Json.createObjectBuilder();

		try {
			 
			 ArrayList csp = resourcePool.get("csp");
			 ArrayList name = resourcePool.get("name");
			 ArrayList vCPU = resourcePool.get("vCPU");
			 ArrayList ram = resourcePool.get("ram");
			 ArrayList price = resourcePool.get("price");
			 ArrayList network = resourcePool.get("network");
			 ArrayList clock = resourcePool.get("clock");
			 
			 //removing instances from the list which does not satisfy
			 //network or clock speed
			 for (int i = 0; i < clock.size(); i++) {
				double inst_clock = Double.parseDouble((String)clock.get(i));
				double inst_network = Double.parseDouble((String)network.get(i));
				if(inst_clock < req_clock || inst_network < req_network) {
					clock.remove(i);
					name.remove(i);
					vCPU.remove(i);
					ram.remove(i);
					price.remove(i);
					network.remove(i);
					csp.remove(i);
					i--;
				}
			 }
			 
			
			 
			//Modeling using CPLEX optimizer 
			IloCplex cplex = new IloCplex();
			int numOfInstances = name.size();
			
			IloIntVar[] allVariables = new IloIntVar[numOfInstances];
			for (int i = 0; i < numOfInstances; i++) {
				allVariables[i] = cplex.intVar(0, Integer.MAX_VALUE, "x"+i);
			}
						
			//objective expression
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i = 0; i < numOfInstances; i++) {
				double inst_price = Double.parseDouble((String) price.get(i));
				objective.addTerm(inst_price, allVariables[i]);
			}
			
			//cost objective minimization
			cplex.addMinimize(objective);
			
			//constraints
			List<IloRange> constraints = new ArrayList<IloRange>();
			
			IloNumExpr[] exprTotalvCPUs = new IloNumExpr[numOfInstances];
			IloNumExpr[] exprTotalRam = new IloNumExpr[numOfInstances];
			IloNumExpr[] exprTotalStorage = new IloNumExpr[numOfInstances];
			for (int i = 0; i < numOfInstances; i++) {
				
				exprTotalvCPUs[i] = cplex.prod(Integer.parseInt((String)vCPU.get(i)), allVariables[i]);
				exprTotalRam[i] = cplex.prod(Double.parseDouble((String)ram.get(i)), allVariables[i]);
				
				constraints.add(cplex.addGe(allVariables[i] ,0));
			}
			constraints.add(cplex.addGe(cplex.sum(exprTotalvCPUs),req_vCPUs));
			constraints.add(cplex.addGe(cplex.sum(exprTotalRam),req_ram));
			
			
			
			// to remove unnecessary display of parameters from cplex
			cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			

			
			if(cplex.solve()) {
				System.out.println("obj is  : " + cplex.getObjValue());
				JsonObjectBuilder totalCostObj = Json.createObjectBuilder();
				//totalCostObj.add("template_Cost", cplex.getObjValue()+"");
				
				finalJSONBuilder.add("template_Cost", cplex.getObjValue()+"");
				
				//tempArrayBuilder.add(totalCostObj);
				
				
				for (int i = 0; i < numOfInstances; i++) {
					System.out.println(name.get(i) + " x"+i +" is  : " + cplex.getValue(allVariables[i]));
					
					
					if(cplex.getValue(allVariables[i]) > 0) {
						
						int inst_count = (int) Math.rint(cplex.getValue(allVariables[i]));
						
						JsonObjectBuilder objBuilder = Json.createObjectBuilder();
						JsonObjectBuilder objDetailsBuilder = Json.createObjectBuilder();
						objBuilder.add("instance_csp", (String)csp.get(i));
						objBuilder.add("instance_name", (String)name.get(i));
						objBuilder.add("instance_count", inst_count+"");
						
						objDetailsBuilder.add("OS", req_os);
						objDetailsBuilder.add("name", (String)name.get(i));
						objDetailsBuilder.add("vCPU", (String)vCPU.get(i));
						objDetailsBuilder.add("ram", (String)ram.get(i));
						objDetailsBuilder.add("price", (String)price.get(i));
						objDetailsBuilder.add("network", (String)network.get(i));
						objDetailsBuilder.add("clock", (String)clock.get(i));
						
						objBuilder.add("instance_details", objDetailsBuilder);
						
						tempArrayBuilder.add(objBuilder);
					
					}
				}		
				
				finalJSONBuilder.add("instances", tempArrayBuilder);
				//finalArrayBuilder.add(redJSONObject);
			} else {
				System.out.println("ERROR: something went wrong");
			}
						
			cplex.end();		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return finalJSONBuilder;
		
 }
}




/*{
	{
	 "instance_csp":"AWS",
	 "instance_name":"t3.large",
	 "instance_count":"3",
	 "instance_details":{
					  "OS":"LINUX",
					  "name":"t3.large",
					  "vCPU":"2",
					  "ram":"8",
					  "price":"0.0832",
					  "network":"5",
					  "clock":""
					  }
   },
   {
	"instance_csp":"AWS",
	"instance_name":"t3.medium",
	"instance_count":"3",
	"instance_details":{
					  "OS":"SLES",
					  "name":"t3.medium",
					  "vCPU":"2",
					  "ram":"4",
					  "price":"0.1046",
					  "network":"5",
					  "clock":""
					  }
   }
}*/


/*constraints.add(cplex.addGe(cplex.sum(cplex.prod(60, x),cplex.prod(60, y),cplex.prod(60, z),cplex.prod(70, a),cplex.prod(65, b),cplex.prod(65, c)), 300));
constraints.add(cplex.addGe(x,0));
*/
/*for (int i = 0; i < constraints.size(); i++) {
System.out.println(" dual constraint " + (i+1) + " = " + cplex.getDual(constraints.get(i)));
System.out.println(" slack constraint " + (i+1) + " = " + cplex.getSlack(constraints.get(i)));
}*/