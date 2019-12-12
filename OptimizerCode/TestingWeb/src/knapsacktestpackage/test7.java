package knapsacktestpackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;


//n VARIABLE TEST
public class test7 {
	
	Map<String, ArrayList> resourcePool;
	
	public test7(Map<String, ArrayList> resourcePool) {
		this.resourcePool = resourcePool;
	}

	public void solveModel(Integer req_vCPUs, Integer req_ram, Integer req_storage) {
		try {
			 
			 ArrayList name = resourcePool.get("name");
			 ArrayList vCPU = resourcePool.get("vCPU");
			 ArrayList ram = resourcePool.get("ram");
			 ArrayList price = resourcePool.get("price");
			
			IloCplex cplex = new IloCplex();
			int numOfInstances = name.size();
			
			//IloNumVar[] allVariables = new IloNumVar[numOfInstances];
			IloIntVar[] allVariables = new IloIntVar[numOfInstances];
			for (int i = 0; i < numOfInstances; i++) {
				//allVariables[i] = cplex.intVar(0, Double.MAX_VALUE, "x"+i);
				allVariables[i] = cplex.intVar(0, Integer.MAX_VALUE, "x"+i);
				
			}
						
			//expression
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i = 0; i < numOfInstances; i++) {
				//objective.addTerm(0.12, x);
				double inst_price = Double.parseDouble((String) price.get(i));
				objective.addTerm(inst_price, allVariables[i]);
				//objective.addTerm(inst_price, allVariables_0[i]);
			}
			
			//objective
			cplex.addMinimize(objective);
			
			//constraints
			List<IloRange> constraints = new ArrayList<IloRange>();
			
			IloNumExpr[] exprTotalvCPUs = new IloNumExpr[numOfInstances];
			IloNumExpr[] exprTotalRam = new IloNumExpr[numOfInstances];
			IloNumExpr[] exprTotalStorage = new IloNumExpr[numOfInstances];
			for (int i = 0; i < numOfInstances; i++) {
				
				exprTotalvCPUs[i] = cplex.prod(Integer.parseInt((String)vCPU.get(i)), allVariables[i]);
				//exprTotalRam[i] = cplex.prod(Integer.parseInt((String)ram.get(i)), allVariables[i]);
				exprTotalRam[i] = cplex.prod(Double.parseDouble((String)ram.get(i)), allVariables[i]);
				//exprTotalStorage[i] = cplex.prod(Integer.parseInt((String)storage.get(i)), allVariables[i]);
				
				constraints.add(cplex.addGe(allVariables[i] ,0));
			}
			constraints.add(cplex.addGe(cplex.sum(exprTotalvCPUs),req_vCPUs));
			constraints.add(cplex.addGe(cplex.sum(exprTotalRam),req_ram));
			
			
			// to remove unnecessary display of parameters
			cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
					
			if(cplex.solve()) {
				System.out.println("obj is  : " + cplex.getObjValue());
				for (int i = 0; i < numOfInstances; i++) {
					System.out.println(name.get(i) + " x"+i +" is  : " + cplex.getValue(allVariables[i]));	
				}
				/*for (int i = 0; i < constraints.size(); i++) {
					System.out.println(" dual constraint " + (i+1) + " = " + cplex.getDual(constraints.get(i)));
					System.out.println(" slack constraint " + (i+1) + " = " + cplex.getSlack(constraints.get(i)));
				}*/
					
			}else {
				System.out.println("ERROR: something went wrong");
			}
						
			cplex.end();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
 }
}

/*constraints.add(cplex.addGe(cplex.sum(cplex.prod(60, x),cplex.prod(60, y),cplex.prod(60, z),cplex.prod(70, a),cplex.prod(65, b),cplex.prod(65, c)), 300));
constraints.add(cplex.addGe(x,0));
*/