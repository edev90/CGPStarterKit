package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Base.CGP;
import Base.Individual;
import Base.Node;

import static Util.StringUtils.*;

/*** Used to visualize what's going on during the CGP evolution */

public class Visualizer extends JFrame {
	
	private static int DEFAULT_WIDTH = 500;
	private static int DEFAULT_HEIGHT = 500;
	
	private Canvas canvas = null;
	
	private String frameOutputDir = "./frameOutput/";
	
	private BufferedImage imageBuffer = null;
	
	private boolean debugOn = true;
	
	public static enum Modes {
		HIGHLIGHT_CURRENT_NODE_AND_ASSOCIATED_ENTITIES,
		HIGHLIGHT_ALL
	}
	
	private Modes mode = Modes.HIGHLIGHT_ALL;

	public Visualizer() {
		// For painting on screen
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		this.setLayout(new BorderLayout());
		this.add(canvas = new Canvas());
		this.setTitle("CGP Visualizer");
		this.setVisible(true);
		
		// For painting to an offscreen buffer that will be dumped to an image file
		imageBuffer = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) imageBuffer.getGraphics();
        graphics.setBackground(new Color(255, 255, 255, 0));
        graphics.clearRect(0,0, (int)imageBuffer.getWidth(), (int)imageBuffer.getHeight());
	}
	
	public void setMode(Modes mode) {
		this.mode = mode;
	}
	
	public class Canvas extends JPanel {
		
		final double NODE_WIDTH = 50;
		final double NODE_HEIGHT = 50;
		final double inputsPerNode = 2;
		final double paddingBetweenRows = 35; // should be fairly relative # of inputs nodes to accommodate non-messy connection lines 
		final double paddingBetweenCols = 75; // should be fairly relative # of inputs nodes to accommodate non-messy connection lines 
		
		final int NODE_NUM_LINES = 2; // number of text lines (rows) that each node supports
		
		final Color LINE_COLOR = Color.orange;
		final Color NUB_COLOR = Color.white;
		final Color NODE_BG_COLOR = Color.DARK_GRAY;
		final Color NODE_BORDER_COLOR = Color.yellow;
		final Color NODE_TEXT_COLOR = LINE_COLOR.brighter();
		final Font NODE_TEXT_FONT = new Font("Arial", Font.BOLD, 14);
		
		final Color OUTPUT_LINE_COLOR = Color.green;
		
		private Individual individual = null; // current individual
		private Node currentNode = null; // the node currently switched on (being evaluated) in the animation
		
		private int getNodeX(Node node) {
			return (int)((node.getCol() * NODE_WIDTH) + (node.getCol() * paddingBetweenCols));
		}
		
		private int getNodeY(Node node) {
			return (int)((node.getRow() * NODE_HEIGHT) + (node.getRow() * paddingBetweenRows));
		}
		
		private Color getColor(Color color, Node node, boolean isLine) {					
			int dimCount = 4;
			Color clr = color;
			
			// If mode is HIGHLIGHT_CURRENT_NODE_AND_ASSOCIATED_ENTITIES then we're only making the current node,
			// and associated entities (outputs, connection lines, text, etc) stand out, dim everything not related
			if(currentNode != null && mode.equals(Modes.HIGHLIGHT_CURRENT_NODE_AND_ASSOCIATED_ENTITIES)) {
				
				boolean doDimming = false;
				
				if(currentNode != node) {
					doDimming = true;
					for(int i = 0; i < inputsPerNode; i++) {
						Node n = currentNode.getInput(i);
						if(!isLine && n == node) {
							// this node's output is wired to one of the currentNode's input, and it's not a line
							doDimming = false;
						}
					}
				}
				
				if(doDimming) {
					while(0 < --dimCount) {
						clr = clr.darker();
					}
				}
			}
			
			return clr;
		}
		
		private Color getColor(Color color, Node node) {
			return getColor(color, node, false);
		}
		
		private void drawLineText(Graphics2D g2d, String text, Node node, Font font, int lineNum) {
			double lineHeight = NODE_HEIGHT / NODE_NUM_LINES;
			double textWidth = getStringWidth(g2d, text, font);
			int textHeight = font.getSize();
			
			double nodeX = getNodeX(node);
			double nodeY = getNodeY(node);
			
			g2d.drawString(text,	// the text to draw 
							(int)((nodeX + NODE_WIDTH/2) - textWidth/2), // xpos of text (centered horizontally)
							(int)(nodeY + lineHeight*lineNum + textHeight)); // ypos of text
		}
		
		// Draw the node's text, which is just it's node type name
		private void drawNodeText(Graphics2D g2d, Node node, Font font) {
			g2d.setColor(getColor(NODE_TEXT_COLOR, node));
			g2d.setFont(NODE_TEXT_FONT);	
			
			String text = node.getType();
			
			if(node.isStartNode()) {
				text = "INPUT";
			}
			
			// Show first line of text, which is either the function name or just "INPUT" which marks the first input node
			drawLineText(g2d, text, node, font, 0);
			
			// Show this node's current value
			drawLineText(g2d, ""+node.value(), node, font, 1);						
		}
		
		private void drawConnectionLine(Graphics2D g2d, Node nodeInput, Node outputNode, int inputIndex) {
			final double vSpaceBetweenInputs = NODE_HEIGHT / (inputsPerNode + 1);	
			
			double inputLevel = vSpaceBetweenInputs + (inputIndex * vSpaceBetweenInputs);
			double outputLevel = vSpaceBetweenInputs * (inputsPerNode - inputIndex);
			int nub = 6;
			
			int startX = getNodeX(nodeInput);
			int startY = (int) (getNodeY(nodeInput) + inputLevel);
			
			int outputPlugX = (int)(getNodeX(outputNode) + NODE_WIDTH);
			int outputPlugY = (int)(getNodeY(outputNode) + outputLevel);
			
			g2d.setColor(getColor(NUB_COLOR, nodeInput, true));
			
			// Draw the input connector nub		
			g2d.drawLine(startX, startY, startX - nub, startY);
			
			// Draw the output connector nub
			g2d.drawLine(outputPlugX, outputPlugY, outputPlugX + nub, outputPlugY);
			
			g2d.setColor(getColor(LINE_COLOR, nodeInput, true));
			
			// Draw the connection line spanning from the output node to the input connector
			g2d.drawLine(startX - nub, startY, outputPlugX + nub, outputPlugY);
		}
		
		// Draws the current state of the node in this animation
		private void drawNode(Graphics2D g2d, Node node, boolean doHighlight) {
			int nodeX = getNodeX(node);
			int nodeY = getNodeY(node);	
			
			// Paint background
			g2d.setColor(getColor(NODE_BG_COLOR, node));
			g2d.fillRect(nodeX, nodeY, (int)NODE_WIDTH, (int)NODE_HEIGHT);
			
			// Set node color
			g2d.setColor(getColor(NODE_BORDER_COLOR, node));		
			
			// Draw node border
			g2d.drawRect(nodeX, nodeY, (int)NODE_WIDTH, (int)NODE_HEIGHT);
			
			// Draw any text
			drawNodeText(g2d, node, NODE_TEXT_FONT);		
		}
		
		private void drawNodeConnectionLines(Graphics2D g2d, Node node, boolean doHighlight) {
			for(int i = 0; i < inputsPerNode; i++) {
				Node input = node.getInput(i);
				if(input != null) {
					drawConnectionLine(g2d, node, input, i);
				}
			}
		}
		
		public void paint(Graphics g) {
			if(individual == null) return;
			
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			
			// Draw the state of all the nodes
//			System.out.println("**1" + individual);					<<-- for debugging primarily
//			System.out.println("**2" + individual.getCGP());		<<-- for debugging primarily
			CGP cgp = individual.getCGP();
			
			int numRows = cgp.numberOfRows();
			int numCols = cgp.numberOfCols();
			
			int gridHeight = (int)(Math.max((numRows * (NODE_HEIGHT + paddingBetweenRows)) - paddingBetweenRows, 0));
			int gridWidth = (int)(Math.max((numCols * (NODE_WIDTH + paddingBetweenCols)) - paddingBetweenCols, 0));
			
			// Center the grid on the screen
			int startX = getWidth()/2 - gridWidth/2;
			int startY = getHeight()/2 - gridHeight/2;
			
			g2d.translate(startX, startY);
			
			Node[][] nodes = individual.getGrid();
			
			// Connection lines get precendence and should be drawn first and behind everything so things don't get messy
			for(int x = 0; x < numCols; x++) {						
				for(int y = 0; y < numRows; y++) {
					// draw the current node
					Node currentNode = nodes[y][x];
					drawNodeConnectionLines(g2d, currentNode, true);
				}
			}
			
			for(int x = 0; x < numCols; x++) {						
				for(int y = 0; y < numRows; y++) {
					// draw the current node
					Node currentNode = nodes[y][x];
					drawNode(g2d, currentNode, true);					
				}
			}
			
			g2d.translate(-startX, -startY);
		}
	}
	
	public void refreshState(Individual individual, Node currentNode) {
		canvas.individual = individual;		
		canvas.currentNode = currentNode;		
		canvas.paint(imageBuffer.getGraphics());		
		repaint(); // will happen in async
	}
	
	// mainly used for debugging purposes
	public void setVizTitle(String title) {
		setTitle(title);
	}
}
