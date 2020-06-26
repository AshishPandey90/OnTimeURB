package knapsacktestpackage;


import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;


//SIX VARIABLE TEST
public class test5 {
	
	public static void solveModel() {
		try {
			IloCplex cplex = new IloCplex();
			
			//variables				
			IloNumVar x = cplex.numVar(0, Double.MAX_VALUE, "x");
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE, "y");
			IloNumVar z = cplex.numVar(0, Double.MAX_VALUE, "z");
			IloNumVar a = cplex.numVar(0, Double.MAX_VALUE, "a");
			IloNumVar b = cplex.numVar(0, Double.MAX_VALUE, "b");
			IloNumVar c = cplex.numVar(0, Double.MAX_VALUE, "c");
			
			
			//expression
			IloLinearNumExpr objective = cplex.linearNumExpr();
			objective.addTerm(0.12, x);
			objective.addTerm(0.15, y);
			objective.addTerm(0.13, z);
			objective.addTerm(0.15, a);
			objective.addTerm(0.14, b);
			objective.addTerm(0.11, c);
			
			//objective
			cplex.addMinimize(objective);
			
			List<IloRange> constraints = new ArrayList<IloRange>();
			//constraint 
			constraints.add(cplex.addGe(cplex.sum(cplex.prod(60, x),cplex.prod(60, y),cplex.prod(60, z),cplex.prod(70, a),cplex.prod(65, b),cplex.prod(65, c)), 300));
			constraints.add(cplex.addGe(cplex.sum(cplex.prod(12, x),cplex.prod(6, y),cplex.prod(10, z),cplex.prod(9, a),cplex.prod(10, b),cplex.prod(10, c)), 36));
			constraints.add(cplex.addGe(cplex.sum(cplex.prod(10, x),cplex.prod(30, y),cplex.prod(35, z),cplex.prod(35, a),cplex.prod(40, b),cplex.prod(40, b)), 90));
			constraints.add(cplex.addGe(x,0));
			constraints.add(cplex.addGe(y,0));
			constraints.add(cplex.addGe(z,0));
			constraints.add(cplex.addGe(a,0));
			constraints.add(cplex.addGe(b,0));
			constraints.add(cplex.addGe(c,0));
			
			//new constraints
			/*IloLinearNumExpr num_expr = cplex.linearNumExpr();
			num_expr.addTerm(2, x);
			num_expr.addTerm(-1, y);
			constraints.add(cplex.addEq(num_expr, 0));
			num_expr.addTerm(1, y);
			num_expr.addTerm(-1, x);
			constraints.add(cplex.addLe(num_expr, 8));
			*/
			// to remove unnecessary display of parameters
			cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			
			if(cplex.solve()) {
				System.out.println("obj is  : " + cplex.getObjValue());
				System.out.println("x is  : " + cplex.getValue(x));
				System.out.println("y is  : " + cplex.getValue(y));
				System.out.println("z is  : " + cplex.getValue(z));
				System.out.println("a is  : " + cplex.getValue(a));
				System.out.println("b is  : " + cplex.getValue(b));
				System.out.println("c is  : " + cplex.getValue(c));
				
				for (int i = 0; i < constraints.size(); i++) {
					System.out.println(" dual constraint " + (i+1) + " = " + cplex.getDual(constraints.get(i)));
					System.out.println(" slack constraint " + (i+1) + " = " + cplex.getSlack(constraints.get(i)));
				}
				
			}else {
				System.out.println("ERROR: something went wrong");
			}
			
			cplex.end();
			
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
