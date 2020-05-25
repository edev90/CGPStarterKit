package Base;

import java.util.HashSet;
import java.util.Stack;

public class Individual {
	
	private Node[][] grid = null;
	private HashSet<Node> inactiveNodes = null; // cached inactive nodes
	//int[][] decoded = null;
	private CGP cgp = null;
	private int fitness = 0;
	
	public Individual() {
	}
	
	public Individual clone() {
		Individual clonedIndividual = new Individual();
		
		clonedIndividual.cgp = this.cgp;
		clonedIndividual.fitness = this.fitness;
		
		int numCols = cgp.numberOfCols();
		int numRows = cgp.numberOfRows(); 
		
		clonedIndividual.grid = new Node[numCols][numRows];
		
		// Clone nodes
		for(int col = 0; col < numCols; col++) {
			for(int row = 0; row < numRows; row++) {
				Node originalNode = grid[col][row];
				Node clonedNode = originalNode.primClone();			
				clonedIndividual.grid[col][row] = clonedNode;
			}
		}
		
		// Now finish the cloning and link the nodes in the same way as the original
		for(int col = 0; col < numCols; col++) {
			for(int row = 0; row < numRows; row++) {
				Node originalNode = grid[col][row];
				Node clonedNode = clonedIndividual.grid[col][row];			
				
				// Link inputs
				if(!clonedNode.isStartNode) {
					clonedNode.input0 = clonedIndividual.grid[originalNode.input0.col][originalNode.input0.row];
					clonedNode.input1 = clonedIndividual.grid[originalNode.input1.col][originalNode.input1.row];
				}
			}
		}
		
		return clonedIndividual;
	}
	
	public void mutate(double mutationRate) {
		int numCols = cgp.numberOfCols();
		int numRows = cgp.numberOfRows(); 
		
		for(int col = 1; col < numCols; col++) {
			for(int row = 0; row < numRows; row++) {
				Node n = grid[col][row];
				
				// Set the function to use
				if(Math.random() <= mutationRate) {
					n.func = cgp.selectRandomFunction();
				}
				
				// Wire nodes randomly to each other
				// Start at col 1 because col 0 consists of start nodes				
				if(Math.random() <= mutationRate) {
					// Wire up the inputs to other nodes
					n.input0 = selectRandomNodeForInput(col);
				}
				
				if(Math.random() <= mutationRate) {
					n.input1 = selectRandomNodeForInput(col);
				}
			}
		}
	}
	
	public void initWithCGPSettings() {
		Node.resetIDCounter();
		
		int numCols = cgp.numberOfCols();
		int numRows = cgp.numberOfRows();
		
		grid = new Node[cgp.numberOfCols()][numRows];
		//decoded = new int[numCols][numRows];
		
		for(int col = 0; col < numCols; col++) {
			for(int row = 0; row < numRows; row++) {
				Node node = new Node();
				node.col = col;
				node.row = row;
				grid[col][row] = node;
				if(col == 0) {
					node.isStartNode = true;
				}
			}
		}
		
		// Initial mutation to set everything
		mutate(1.00);
	}
	
	public void setCGPSettings(CGP cgp) {
		this.cgp = cgp;
	}
	
	public CGP getCGP() {return this.cgp;}
	
	/*** Resets all current node values */
	public void resetNodes() { // TODO: implement
		for(Node[] col : this.grid) {
			for(Node node : col) {
				node.setValue(null);
			}
		}
	}
	
	public Node[][] getGrid() {return this.grid;}
	
	// Run through the fitness cases and see how well this individual performs
	public int calculateFitness() {
		
		resetNodes();
		
		Object[][] testCaseInputs = cgp.getTestCaseInputs();
		Object[][] testCaseOutputs = cgp.getTestCaseOutputs();
		
		// The outputs we'll ignore during this fitness check
		boolean[] outputsToIgnore = cgp.getOutputsToIgnore();
		
		int fitness = 0;
		
		int numTestCases = testCaseInputs.length / cgp.numberOfRows();
		
		int tIndex = 0; // testCase index
		for(Object[] testCase : testCaseInputs) {
		
			// Set start input values in input nodes
			int row = 0;
			for(Object inputValue : testCase) {
				grid[0][row++].setValue(inputValue);
				//System.out.println("Setting " + inputValue + " in row " + row);
			}
			
			Stack<Node> nodes = getNodesToProcess();
			Node node = null;
			while(nodes.size() > 0) {
				node = nodes.pop();
				node.evaluate(true);
			}
			
			// Now get the output and evaluate it against this case's expected output
			Object[] expectedOutput = testCaseOutputs[tIndex];
			int lastCol = cgp.numberOfCols() - 1;
			boolean testPassed = true;
			for(row = 0; row < cgp.numberOfRows(); row++) {
				Object expected = expectedOutput[row]; 
				Object actual = grid[lastCol][row].value();
				if(!outputsToIgnore[row]) {
					if(!actual.equals(expected)) {
						testPassed = false;
					}
				}
			}
			
			fitness += testPassed ? 1 : 0;				
			
			tIndex++;
			// just perform 1 test case for now for testing purposes
			//break;
			
		}
		
		this.fitness = fitness;
		
		return this.fitness;
	}
	
	private Node selectRandomNodeForInput(int currentCol) {
		int levelsBack = cgp.levelsBack();
		int numRows = cgp.numberOfRows();
		
		if(levelsBack > 0) {
			int rndRow = (int) (Math.random() * numRows);
			// Roll dice, if random number greater than 0.5 then node will be in initial input column,
			// OR if the current column is the 2nd one then we don't have a choice anyway
			if(Math.random() > 0.5 || currentCol == 1) {
				return grid[0][rndRow];
			}
			int rndCol = Math.max(1, Math.min(currentCol-1,(int)(currentCol-Math.random()*levelsBack)));
			return grid[rndCol][rndRow];
		}
		return null;
	}
	
	// Returns nodes that should be processed
	public Stack<Node> getNodesToProcess() {		
		Stack<Node> nodesToProcess = new Stack<Node>();
		HashSet<Node> refedNodes = new HashSet<Node>();
		HashSet<Node> inactive = new HashSet<Node>();
		
		int numCols = cgp.numberOfCols();
		int numRows = cgp.numberOfRows();
		
		int outputCol = numCols - 1;
		for(int col = numCols-1; col >= 0; col--) {
			for(int row = 0; row < numRows; row++) {
				Node node = grid[col][row];
				if(col == outputCol || refedNodes.contains(node)) {
					//refedNodes.add(node);
					nodesToProcess.push(node);
					refedNodes.add(node.input0);
					refedNodes.add(node.input1);
				}
				else {
					//System.out.println("Node " + node.id + " is inactive and won't be processed.");
					inactive.add(node);
				}
			}
		}
		this.inactiveNodes = inactive;
		//System.out.println("The following nodes will be processed in the following order: " + nodesToProcess);
		return nodesToProcess;
	}
			
	public HashSet<Node> analyzePathsToStartInputs() {
		HashSet<Node> nodesToInputs = new HashSet<Node>();
		
		int lastCol = cgp.numberOfCols()-1;
		for(int row = 0; row < cgp.numberOfRows(); row++) {
			grid[lastCol][row].isConnectedToOrIsStartInput(nodesToInputs);
		}
//		for(Node n : nodesToInputs) {
//			//System.out.println("Node connected to start inputs: " + n);
//		}
		return nodesToInputs;
	}
	
	public HashSet<Node> getInactiveNodes() {
		return this.inactiveNodes;
	}
	
	public int getCachedFitness() {
		return this.fitness;
	}
	
	public String toString() {
		String info = "";
		for(int row = 0; row < cgp.numberOfRows(); row++) {
			String rowInfo = "";
			for(int col = 0; col < cgp.numberOfCols(); col++) {
				rowInfo += rowInfo.length()>0 ? " | " : "";
				rowInfo += grid[col][row].longInfoString();
			}
			info += info.length()>0 ? "\n" : "";
			info += rowInfo;
		}
		return info;
	}
}