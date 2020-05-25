package Examples;

import Base.CGP;
import Base.Function;
import Base.Individual;
import Base.Node;

public class OneBitAdder extends CGP {

	public OneBitAdder() {
		super(2, 4, 2);
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
		
		Function<Boolean> NAND = new Function<Boolean>("NAND") {
			public Boolean execute(Node a, Node b) {
				return !(((Boolean)a.value()) & ((Boolean)b.value()));
			}
		};
		
		addFunction(XOR);
		addFunction(OR);
		addFunction(AND);
		addFunction(NAND);
	}
	
	public static void main(String[] args) {
		OneBitAdder adder = new OneBitAdder();
		
		adder.setFitnessTestCases(
				
				// inputs
				new Object[][] {
					{false, false},
					{false, true},
					{true, false},
					{true, true}
				}, 				
				
				// expected outputs
				new Object[][] {
					{false, false},
					{false, true},
					{false, true},
					{true, false}
				});
		
		adder.testEvolve();	
		
	}
}
