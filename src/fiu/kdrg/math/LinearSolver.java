package fiu.kdrg.math;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;


public class LinearSolver {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		LinearObjectiveFunction f = new LinearObjectiveFunction(new double[]{-2,1},-5);
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		
		constraints.add(new LinearConstraint(new double[]{1,2}, Relationship.LEQ, 6));
		constraints.add(new LinearConstraint(new double[]{3,2}, Relationship.LEQ, 12));
		constraints.add(new LinearConstraint(new double[]{0,1}, Relationship.GEQ, 0));
		
		
		//create and run the solver
		PointValuePair solution = new SimplexSolver().optimize(f,new LinearConstraintSet(constraints),GoalType.MINIMIZE);
		
		//get the solution
		double x = solution.getPoint()[0];
		double y = solution.getPoint()[1];
		double min = solution.getValue();
		
		System.out.println(x);
		System.out.println(y);
		System.out.println(min);
		
	}
	
}
