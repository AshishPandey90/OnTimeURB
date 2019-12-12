package com.vimanlab.version2.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

//n VARIABLE TEST
public class ModelCreator {
	
	Map<String, ArrayList> resourcePool;
	ArrayList cspList;
	
	public ModelCreator(ArrayList cspList) {
		this.cspList = cspList;
	}

	public JsonObjectBuilder solveModel(String req_os, Integer req_vCPUs, Double req_ram, Double req_network, Double req_clock) {
		
		System.out.println("String req_os, Integer req_vCPUs, Double req_ram, Double req_network, Double req_clock"+  req_os+" "+ req_vCPUs +" "+ req_ram+" "+ req_network+" "+ req_clock);
		
		long a = System.currentTimeMillis();
		//JsonObjectBuilder tempObjBuilder = Json.createObjectBuilder();
		JsonArrayBuilder tempArrayBuilder = Json.createArrayBuilder();
		JsonArrayBuilder localCspArrayBuilder = Json.createArrayBuilder();
		JsonObjectBuilder finalJSONBuilder = Json.createObjectBuilder();

		int[][] compat = new int[5][];
		compat[0] = new int[]{1,0,0,0,0};
		compat[1] = new int[]{0,1,0,0,0};
		compat[2] = new int[]{0,0,1,0,0};
		compat[3] = new int[]{0,0,0,1,0};
		compat[4] = new int[]{0,0,0,0,1};
		
		/*int[][] compat = new int[5][];
		compat[0] = new int[]{1,0,0,0,0};
		compat[1] = new int[]{0,0,0,0,0};
		compat[2] = new int[]{0,0,0,0,0};
		compat[3] = new int[]{0,0,0,0,0};
		compat[4] = new int[]{0,0,0,0,0};
		*/
		//int[][] compat = new int[3][];
		//compat[0] = new int[]{1,1,1};
		//compat[1] = new int[]{1,1,1};
		//compat[2] = new int[]{1,1,1};
		//compat[3] = new int[]{0,0,0,1};

		
		try {
			
			//Modeling using CPLEX optimizer 
			IloCplex cplex = new IloCplex();
			
			int numOfInstances = 0;
			
			IloIntVar[] cspVarPool[] = new IloIntVar[cspList.size()][];
			
			Map<String, ArrayList> cspPool[] = new HashMap[cspList.size()];
			
			for(int i = 0 ; i < cspList.size() ; i++) {
				resourcePool = (Map<String, ArrayList>) cspList.get(i);	
				cspPool[i] = processData(resourcePool, req_network, req_clock);
				cspVarPool[i] = new IloIntVar[cspPool[i].get("name").size()];
				
				for (int j = 0; j < cspPool[i].get("name").size(); j++) {
					numOfInstances++;
					cspVarPool[i][j] = cplex.intVar(0, Integer.MAX_VALUE, "csp_" +i+"_"+j);
				}

			}
			
			
			//objective expression
			IloLinearNumExpr objective = cplex.linearNumExpr();
			IloLinearNumExpr summAllVar = cplex.linearNumExpr();
			for (int i = 0; i < cspPool.length; i++) {
				for (int j = 0; j < cspPool[i].get("name").size(); j++) {
					double inst_price = Double.parseDouble((String)cspPool[i].get("price").get(j));				
					objective.addTerm(inst_price, cspVarPool[i][j]); //agility?? smth with linear combination lambda
					summAllVar.addTerm(1,cspVarPool[i][j]);
				}
			}
			
			System.out.println("=TOTAL NUM OF VARS (not incl. delta)=" + numOfInstances);
			
			//cost objective minimization
			cplex.addMinimize(objective);
			
			//constraints
			List<IloRange> constraints = new ArrayList<IloRange>();
			
			IloNumExpr[] exprTotalvCPUs = new IloNumExpr[numOfInstances];
			IloNumExpr[] exprTotalRam = new IloNumExpr[numOfInstances];
			IloNumExpr[] exprTotalStorage = new IloNumExpr[numOfInstances];
			int k = 0;
			for (int i = 0; i < cspPool.length; i++) {
				for (int j = 0; j < cspPool[i].get("name").size(); j++) {
					exprTotalvCPUs[k] = cplex.prod(Integer.parseInt((String)cspPool[i].get("vCPU").get(j)), cspVarPool[i][j]); //linear combination between max and min demands
					exprTotalRam[k] = cplex.prod(Double.parseDouble((String)cspPool[i].get("ram").get(j)), cspVarPool[i][j]);			
					//constraints.add(cplex.addGe(cspVarPool[i][j] ,0));
					k++;
				}
			}
			constraints.add(cplex.addGe(cplex.sum(exprTotalvCPUs),req_vCPUs));
			constraints.add(cplex.addGe(cplex.sum(exprTotalRam),req_ram));
			
			// added for KNN solutions, constraints specifies only one instance is picked
			//constraints.add(cplex.addLe(summAllVar,1));
			
			
			IloNumExpr[] storeSummations = new IloNumExpr[cspPool.length];

			for (int i = 0; i < cspPool.length; i++) {
				IloNumExpr[] totalInstInCurrentCsp = new IloNumExpr[cspPool[i].get("name").size()];
				for (int j = 0; j < cspPool[i].get("name").size(); j++) {
						totalInstInCurrentCsp[j] = cplex.prod(1.0, cspVarPool[i][j]);
					}
				storeSummations[i] = cplex.sum(totalInstInCurrentCsp);	
			}


			for(int i = 0; i<cspPool.length ; i++) 
				for(int j = 0; j<cspPool.length ; j++)
					if(compat[i][j] == 0) {
						IloIntVar delta = cplex.boolVar();
						constraints.add((IloRange) cplex.addLe(cplex.sum(storeSummations[i], cplex.prod(-Integer.MAX_VALUE, delta)), 0));
						constraints.add((IloRange) cplex.addLe(cplex.sum(storeSummations[j],cplex.prod(Integer.MAX_VALUE, delta)), Integer.MAX_VALUE));
					}
				
			

			
			// to remove unnecessary display of parameters from cplex
			cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			

			
			if(cplex.solve()) {
				
				System.out.println("obj is  : " + cplex.getObjValue());
				JsonObjectBuilder totalCostObj = Json.createObjectBuilder();
				//totalCostObj.add("template_Cost", cplex.getObjValue()+"");
				
				finalJSONBuilder.add("template_Cost", cplex.getObjValue()+"");
				Set<String> template_CSPs = new HashSet<String>();
				
				//tempArrayBuilder.add(totalCostObj);
				System.out.println("Total instances considred : " + numOfInstances);
				
				for (int i = 0; i < cspPool.length; i++) {
					for (int j = 0; j < cspPool[i].get("name").size(); j++) {
					//System.out.println(cspPool[i].get("name").get(j) + "csp_" +i+"_"+j+" is  : " + cplex.getValue(cspVarPool[i][j]));
					
					
					if(cplex.getValue(cspVarPool[i][j]) > 0) {
						
						int inst_count = (int) Math.rint(cplex.getValue(cspVarPool[i][j]));
						
						JsonObjectBuilder objBuilder = Json.createObjectBuilder();
						JsonObjectBuilder objDetailsBuilder = Json.createObjectBuilder();
						objBuilder.add("instance_csp", (String)cspPool[i].get("csp").get(j));//(String)csp.get(i));
						objBuilder.add("instance_name", (String)cspPool[i].get("name").get(j));
						objBuilder.add("instance_count", inst_count+"");
						
						template_CSPs.add((String)cspPool[i].get("csp").get(j));
						
						objDetailsBuilder.add("OS", req_os);
						objDetailsBuilder.add("name", (String)cspPool[i].get("name").get(j));
						objDetailsBuilder.add("vCPU", (String)cspPool[i].get("vCPU").get(j));
						objDetailsBuilder.add("ram", (String)cspPool[i].get("ram").get(j));
						objDetailsBuilder.add("price", (String)cspPool[i].get("price").get(j));
						objDetailsBuilder.add("network", (String)cspPool[i].get("network").get(j));
						objDetailsBuilder.add("clock", (String)cspPool[i].get("clock").get(j));
						
						objBuilder.add("instance_details", objDetailsBuilder);
						
						tempArrayBuilder.add(objBuilder);
					
						}
					}
				}
				localCspArrayBuilder = setToArrayBuilder(template_CSPs);
				finalJSONBuilder.add("template_CSPs", localCspArrayBuilder);
				finalJSONBuilder.add("instances", tempArrayBuilder);
				
			} else {
				System.out.println("ERROR: something went wrong");
			}
						
			cplex.end();		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long b = System.currentTimeMillis();
		System.out.println("Time to solve the problem is : " + (b-a));
		
		return finalJSONBuilder;
		
 }

	private JsonArrayBuilder setToArrayBuilder(Set<String> template_CSPs) {
		
		JsonArrayBuilder localCspArrayBuilder = Json.createArrayBuilder();
		Iterator<String> it = template_CSPs.iterator();
	     while(it.hasNext()){
	    	 localCspArrayBuilder.add(it.next());
	     }
		return localCspArrayBuilder;
	}

	private Map<String, ArrayList> processData(Map<String, ArrayList> resourcePool2, Double req_network, Double req_clock) {
		
		Map<String, ArrayList> resourcePool_i = new HashMap<String, ArrayList>();
		
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
		 
		 resourcePool_i.put("csp", csp);
		 resourcePool_i.put("name", name);
		 resourcePool_i.put("vCPU", vCPU);
		 resourcePool_i.put("ram", ram);
		 resourcePool_i.put("price", price);
		 resourcePool_i.put("network", network);
		 resourcePool_i.put("clock", clock);
		 
		return resourcePool_i;
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