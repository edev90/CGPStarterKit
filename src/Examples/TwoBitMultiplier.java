package Examples;

import Base.CGP;
import Base.Function;
import Base.Individual;
import Base.Node;
import UI.Visualizer;

public class TwoBitMultiplier extends CGP {
	
	public TwoBitMultiplier() {
		super(4, 4, 2);		
	}
	
	public void initFunctions() {
		Function<Boolean> XOR = new Function<Boolean>("XOR") {
			public Boolean execute(Node a, Node b) {
				return ((Boolean)a.value()) ^ ((Boolean)b.value());
			}
		};
		
		Function<Boolean> OR = new Function<Boolean>("OR") {
			public Boolean execute(Node a, Node b) {
				return ((Boolean)a.value()) | ((Boolean)b.value());
			}
		};
		
		Function<Boolean> AND = new Function<Boolean>("AND") {
			public Boolean execute(Node a, Node b) {
				return ((Boolean)a.value()) & ((Boolean)b.value());
			}
		};
		
		Function<Boolean> ANDi = new Function<Boolean>("ANDi") {
			public Boolean execute(Node a, Node b) {
				return !((Boolean)a.value()) & ((Boolean)b.value());
			}
		};
		
		addFunction(XOR);
		addFunction(OR);
		addFunction(AND);
		addFunction(ANDi);
	}
	
	public void postFitnessCalculation(Individual individual, int step) {
	}
	
	public void postNodeEval(Node node, int step) {
		
	}
	
	public static void main(String[] args) {
		TwoBitMultiplier multiplierCircuit = new TwoBitMultiplier();
		
		multiplierCircuit.setFitnessTestCases(
				
				// inputs
				new Object[][] {
					{false,false,	false,false},
					{false,false,	false,true},
					{false,false,	true,false},
					{false,false,	true,true},
					{false,true,	false,false},
					{false,true,	false,true},
					{false,true,	true,false},
					{false,true,	true,true},
					{true,false,	false,false},
					{true,false,	false,true},
					{true,false,	true,false},
					{true,false,	true,true},
					{true,true,	false,false},
					{true,true,	false,true},
					{true,true,	true,false},
					{true,true,	true,true}
				}, 				
				
				// expected outputs
				new Object[][] {
					{false,false,false,false},
					{false,false,false,false},
					{false,false,false,false},
					{false,false,false,false},
					{false,false,false,false},
					{false,false,false,true},
					{false,false,true,false},
					{false,false,true,true},
					{false,false,false,false},
					{false,false,true,false},
					{false,true,false,false},
					{false,true,true,false},
					{false,false,false,false},
					{false,false,true,true},
					{false,true,true,false},
					{true,false,false,true}
				});
		
		multiplierCircuit.testEvolve();
	}
}

