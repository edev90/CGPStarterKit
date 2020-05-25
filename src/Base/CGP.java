package Base;

import java.util.Vector;

public class CGP {

	private int levelsBack = 0;
	private int numRows = 0;
	private int numCols = 0;
	private int sizeOfEachGeneration = 0;
	private Vector<Function> functions = new Vector<Function>();
	
	private Object[][] testCaseInputs = null;
	private Object[][] testCaseOutputs = null;
	private boolean[] ignoredOutputs = null; // outputs that will be ignored during the fitness calculations
	private int totalTestCases = 0;
	
	private static final int DEFAULT_GENERATION_SIZE = 100;
	
	private Vector<Generation> history = new Vector<Generation>(); 
	
	public CGP(int rows, int cols, int levelsBack) {
		init(rows, cols, levelsBack, DEFAULT_GENERATION_SIZE);
	}
	
	public void init(int numRows, int numCols, int levelsBack, int sizeOfEachGeneration) {
		initFunctions();
		
		this.numRows = numRows;
		this.numCols = numCols;
		this.levelsBack = levelsBack;
		this.sizeOfEachGeneration = sizeOfEachGeneration;
		this.ignoredOutputs = new boolean[numRows];
	}
	
	public void initFunctions() {
		// override this in child classes and implement function initialization code
	}
	
	public void addFunction(Function f) {
		functions.add(f);
	}
	
	public Function selectRandomFunction() {
		return functions.get((int)(Math.random()*functions.size()));
	}
	
	/*** 
	 * Sets the test cases that will be used by the fitness test
	 * */
	public void setFitnessTestCases(Object[][] testCaseInputs, Object[][] testCaseOutputs) {
		this.testCaseInputs = testCaseInputs;
		this.testCaseOutputs = testCaseOutputs;
		this.totalTestCases = testCaseInputs.length;
	}
	
	public void testEvolve() {
		
		System.out.println("Randomly generating initial start individuals");
		
		Individual fittest = null;
		
		for(int i = 0; i < this.sizeOfEachGeneration; i++) {
			Individual individual = newIndividual();
			individual.setCGPSettings(this);
			individual.initWithCGPSettings();
			
			// evaluate the individual's performance
			individual.calculateFitness();
			
			postFitnessCalculation(individual, 0);
			
			if(fittest == null || individual.getCachedFitness() > fittest.getCachedFitness()) {
				fittest = individual;
			}
		}
		
		int generationCounter = 1;		
		int desiredFitness = totalTestCases;
		double mutationRate = 0.20; // start with this for now
		
		while(fittest.getCachedFitness() < desiredFitness) {
			System.out.println();
			System.out.println("Fittest individual was: " + fittest);
			System.out.println("Fitness level: " + fittest.getCachedFitness());
			
			for(int i = 0; i < this.sizeOfEachGeneration; i++) {
				Individual offspring = fittest.clone(); // fittest is current parent
				offspring.mutate(mutationRate);
				offspring.calculateFitness();
				
				postFitnessCalculation(offspring, 1);
				
				if(offspring.getCachedFitness() >= fittest.getCachedFitness()) {
					fittest = offspring;
				}
				// else leave fittest alone....
			}
			
//			try {
//				Thread.sleep(250);
//			} catch(InterruptedException ie) {}
			
			generationCounter++;
		}
		
		System.out.println("Took " + generationCounter + " generations.");
		System.out.println("Fittest individual was: " + fittest);
		System.out.println("Fitness level: " + fittest.getCachedFitness());
		
	}
	
	public int numberOfCols() {return this.numCols;}
	public int numberOfRows() {return this.numRows;}
	public int levelsBack() {return this.levelsBack;}
	
	public Object[][] getTestCaseInputs() {return testCaseInputs;}
	public Object[][] getTestCaseOutputs() {return testCaseOutputs;}
	
	/*** 
	 * Outputs that will be ignored during the fitness calculations for all individuals
	 * Useful for applications where we have many input rows but not all outputs are relevant,
	 * */
	public void setOutputsToIgnore(int... ignoreIndexes) {
		for(int outputIndex : ignoreIndexes) {
			this.ignoredOutputs[outputIndex] = true;
		}
	}
	
	public boolean[] getOutputsToIgnore() {return this.ignoredOutputs;}
	
	/*** Any processing that will be done after an individual's fitness is calculated
	 * Used by visualizer animation, for instance
	 *  */
	public void postFitnessCalculation(Individual individual, int step) {
	}
	
	public void postNodeEval(Node node, int step) {
		
	}
	
	// Primarily for testing
	
	/*** For testing: generate one individual */
	public Individual newIndividual() {
		Individual individual = new Individual();
		return individual;
	}
	
	class Generation {		
		private Vector<Individual> population = new Vector<Individual>();		
		public Generation(int size) {			
		}
	}

}
