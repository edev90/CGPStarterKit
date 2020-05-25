package Base;

public class Function<T> {

	private String name = "";
	
	public Function(String name) {
		this.name = name;
	}
	
	public T execute(Node a, Node b) {
		return null;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.name;
	}
}
