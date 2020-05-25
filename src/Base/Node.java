package Base;

import java.util.HashSet;

public class Node {
	
	private static int ID_COUNTER = 0;
	public int id = 0;
	boolean isStartNode = false;
	Node input0;
	Node input1;
	private Object value;
	Function func = null; // index of function to use
	
	// These vars used mainly when debugging and cloning
	protected int col = 0;
	protected int row = 0;
	
	public Node() {
		this.id = ID_COUNTER++;
	}
	
	public Node(int id) {
		this.id = id;
	}
	
	/*** Primitive clone of this node -- has entire structure (id, value, func ptr, etc) the same 
	 * EXCEPT nodes. Nodes are not preserved and must be handle seperately. */
	public Node primClone() {
		Node clone = new Node(id);		
		clone.isStartNode = this.isStartNode;
		clone.value = this.value;
		clone.func = this.func;
		clone.col = this.col;
		clone.row = this.row;		
		return clone;
	}
	
	public Object value() {
		return this.value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public void evaluate(boolean doTrackTime) {
		if(isStartNode) return;
		this.value = func.execute(input0, input1);
	}
	
	public void evaluate() {
		evaluate(false);
	}
	
	public static void resetIDCounter() {
		ID_COUNTER = 0;
	}
	
	/*** Returns true if this node is connected to any start input nodes -- and also adds the nodes along the way to a set */
	public boolean isConnectedToOrIsStartInput(HashSet<Node> pathNodes) {
		if(isStartNode) {
			pathNodes.add(this);
			return true;
		}
		if(input0.isConnectedToOrIsStartInput(pathNodes) || input1.isConnectedToOrIsStartInput(pathNodes)) {
			pathNodes.add(this);
			return true;
		}
		return false;
	}
	
	public String shortInfoString() {
		return "NODE: " + id;
	}
	
	public Node getInput(int index) {	// TODO: implement exception for non-existent/out of bounds input
		if(index == 0) {
			return input0;
		}
		return input1;
	}
	
	public String longInfoString() {
		StringBuilder nInfo = new StringBuilder();
		nInfo.append("NODE: " + id);
		if(!isStartNode) {
			nInfo.append(" input0->:" + (input0==null?"Nothing":input0.id));
			nInfo.append(" input1->:" + (input1==null?"Nothing":input1.id));
			nInfo.append(" " + (func != null ? func.getName() : "NoFunc"));
			nInfo.append(" val:" + value());
		}
		return nInfo.toString();
	}
	
	public String getType() {
		if(func != null) {
			return func.getName();
		}
		return "function: N/A";
	}
	
	public int getRow() {return this.row;}
	
	public int getCol() {return this.col;}
	
	public boolean isStartNode() {return isStartNode;}
	
	public String toString() {return this.shortInfoString();}
}
