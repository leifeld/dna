package dna.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import dna.Dna;

public class Exporter {

	public Exporter() {
		// TODO
	}
	
	/**
	 * Export network to a CSV matrix file.
	 * 
	 * @param network  The input Network object.
	 * @param outfile  The path and file name of the target CSV file.
	 */
	public void exportCSV (Network network, String outfile) {
		int nr = network.getMatrix().rownames.length;
		int nc = network.getMatrix().colnames.length;
		String[] rn = network.getMatrix().rownames;
		String[] cn = network.getMatrix().colnames;
		double[][] mat = network.getMatrix().getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("\"\"");
			for (int i = 0; i < nc; i++) {
				out.write(";\"" + cn[i] + "\"");
			}
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i] + "\"");
				for (int j = 0; j < nc; j++) {
					out.write(";" + String.format(new Locale("en"), "%.6f", mat[i][j]));
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving CSV matrix file.");
		}
	}

	/**
	 * Export network to a DL fullmatrix file for the software Ucinet.
	 * 
	 * @param network  The input Network object.
	 * @param outfile  The path and file name of the target .dl file.
	 */
	public void exportDL (Network network, String outfile) {
		int nr = network.getMatrix().rownames.length;
		int nc = network.getMatrix().colnames.length;
		String[] rn = network.getMatrix().rownames;
		String[] cn = network.getMatrix().colnames;
		double[][] mat = network.getMatrix().getMatrix();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "UTF8"));
			out.write("DL");
			out.newLine();
			if (network.getModes() == 1) {
				out.write("N=" + nr);
			} else if (network.getModes() == 2) {
				out.write("NR=" + nr + ", NC=" + nc);
			}
			out.newLine();
			out.write("FORMAT = FULLMATRIX DIAGONAL PRESENT");
			out.newLine();
			out.write("ROW LABELS:");
			for (int i = 0; i < nr; i++) {
				out.newLine();
				out.write("\"" + rn[i] + "\"");
			}
			out.newLine();
			out.write("ROW LABELS EMBEDDED");
			out.newLine();
			out.write("COLUMN LABELS:");
			for (int i = 0; i < nc; i++) {
				out.newLine();
				out.write("\"" + cn[i] + "\"");
			}
			out.newLine();
			out.write("COLUMN LABELS EMBEDDED");
			out.newLine();
			out.write("DATA:");
			for (int i = 0; i < nr; i++) {
				out.newLine();
				for (int j = 0; j < nc; j++) {
					out.write(" " + String.format(new Locale("en"), "%.6f", mat[i][j]));
				}
			}
			out.close();
		} catch (IOException e) {
			System.err.println("Error while saving DL fullmatrix file.");
		}
	}

	/**
	 * Export filter for graphML files.
	 * 
	 * @param network  The input Network object.
	 * @param outfile  The path and file name of the target .graphML file.
	 */
	private void exportGraphML(Network network, String outfile) {
		ArrayList<Edge> edges = network.getEdgelist().getEdgelist();
		int modes = network.getModes();
		String[] rn = network.getEdgelist().getSources();
		String[] cn = network.getEdgelist().getTargets();
		int nr = rn.length;
		int nc = cn.length;
		int numVertices = nr;
		if (modes == 1) {
			numVertices = nr;
		} else if (modes == 2) {
			numVertices = nr + nc;
		}
		int numEdges = edges.size();
		String[] labels = null;
		if (modes == 1) {
			labels = rn;
		} else if (modes == 2) {
			labels = Stream.concat(Arrays.stream(rn), Arrays.stream(cn)).toArray(String[]::new);
		}
		
		Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
		Element graphml = new Element("graphml", xmlns);
		Namespace visone = Namespace.getNamespace("visone", "http://visone.info/xmlns");
		graphml.addNamespaceDeclaration(visone);
		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		graphml.addNamespaceDeclaration(xsi);
		Namespace yNs = Namespace.getNamespace("y", "http://www.yworks.com/xml/graphml");
		graphml.addNamespaceDeclaration(yNs);
		Attribute attSchema = new Attribute("schemaLocation", "http://graphml.graphdrawing.org/xmlns/graphml http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd ", xsi);
		graphml.setAttribute(attSchema);
		Document document = new Document(graphml);
		
		Comment dataSchema = new Comment(" data schema ");
		graphml.addContent(dataSchema);
		
		Element keyVisoneNode = new Element("key", xmlns);
		keyVisoneNode.setAttribute(new Attribute("for", "node"));
		keyVisoneNode.setAttribute(new Attribute("id", "d0"));
		keyVisoneNode.setAttribute(new Attribute("yfiles.type", "nodegraphics"));
		graphml.addContent(keyVisoneNode);

		Element keyVisoneEdge = new Element("key", xmlns);
		keyVisoneEdge.setAttribute(new Attribute("for", "edge"));
		keyVisoneEdge.setAttribute(new Attribute("id", "e0"));
		keyVisoneEdge.setAttribute(new Attribute("yfiles.type", "edgegraphics"));
		graphml.addContent(keyVisoneEdge);

		Element keyVisoneGraph = new Element("key", xmlns);
		keyVisoneGraph.setAttribute(new Attribute("for", "graph"));
		keyVisoneGraph.setAttribute(new Attribute("id", "prop"));
		keyVisoneGraph.setAttribute(new Attribute("visone.type", "properties"));
		graphml.addContent(keyVisoneGraph);
		
		Element keyId = new Element("key", xmlns);
		keyId.setAttribute(new Attribute("id", "id"));
		keyId.setAttribute(new Attribute("for", "node"));
		keyId.setAttribute(new Attribute("attr.name", "id"));
		keyId.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyId);
		
		Element keyClass = new Element("key", xmlns);
		keyClass.setAttribute(new Attribute("id", "class"));
		keyClass.setAttribute(new Attribute("for", "node"));
		keyClass.setAttribute(new Attribute("attr.name", "class"));
		keyClass.setAttribute(new Attribute("attr.type", "string"));
		graphml.addContent(keyClass);
		
		Element keyFrequency = new Element("key", xmlns);
		keyFrequency.setAttribute(new Attribute("id", "statementFrequency"));
		keyFrequency.setAttribute(new Attribute("for", "node"));
		keyFrequency.setAttribute(new Attribute("attr.name", "statementFrequency"));
		keyFrequency.setAttribute(new Attribute("attr.type", "int"));
		graphml.addContent(keyFrequency);

		Element keyWeight = new Element("key", xmlns);
		keyWeight.setAttribute(new Attribute("id", "weight"));
		keyWeight.setAttribute(new Attribute("for", "edge"));
		keyWeight.setAttribute(new Attribute("attr.name", "weight"));
		keyWeight.setAttribute(new Attribute("attr.type", "double"));
		graphml.addContent(keyWeight);
		
		Element graphElement = new Element("graph", xmlns);
		graphElement.setAttribute(new Attribute("edgedefault", "undirected"));
		
		graphElement.setAttribute(new Attribute("id", "G"));
		graphElement.setAttribute(new Attribute("parse.edges", String.valueOf(numEdges)));
		graphElement.setAttribute(new Attribute("parse.nodes", String.valueOf(numVertices)));
		graphElement.setAttribute(new Attribute("parse.order", "free"));
		Element properties = new Element("data", xmlns);
		properties.setAttribute(new Attribute("key", "prop"));
		Element labelAttribute = new Element("labelAttribute", visone);
		labelAttribute.setAttribute("edgeLabel", "weight");
		labelAttribute.setAttribute("nodeLabel", "id");
		properties.addContent(labelAttribute);
		graphElement.addContent(properties);
		
		Comment nodes = new Comment(" nodes ");
		graphElement.addContent(nodes);
		
		for (int i = 0; i < labels.length; i++) {
			Element node = new Element("node", xmlns);
			node.setAttribute(new Attribute("id", labels[i]));
			
			Element id = new Element("data", xmlns);
			id.setAttribute(new Attribute("key", "id"));
			id.setText(labels[i]);
			node.addContent(id);
			
			String hex;
			String type;
			String shapeString;
			if (i > nr) {
				hex = "#00FF00";  // green
				type = "mode2";
				shapeString = "ellipse";
			} else {
				hex = "#3399FF";  // light blue
				type = "mode1";
				shapeString = "roundrectangle";
			}
			
			Element vClass = new Element("data", xmlns);
			vClass.setAttribute(new Attribute("key", "class"));
			vClass.setText(type);
			node.addContent(vClass);
			
			Element vis = new Element("data", xmlns);
			vis.setAttribute(new Attribute("key", "d0"));
			Element visoneShapeNode = new Element("shapeNode", visone);
			Element yShapeNode = new Element("ShapeNode", yNs);
			Element geometry = new Element("Geometry", yNs);
			geometry.setAttribute(new Attribute("height", "20.0"));
			geometry.setAttribute(new Attribute("width", "20.0"));
			geometry.setAttribute(new Attribute("x", String.valueOf(Math.random()*800)));
			geometry.setAttribute(new Attribute("y", String.valueOf(Math.random()*600)));
			yShapeNode.addContent(geometry);
			Element fill = new Element("Fill", yNs);
			fill.setAttribute(new Attribute("color", hex));
			fill.setAttribute(new Attribute("transparent", "false"));
			yShapeNode.addContent(fill);
			Element borderStyle = new Element("BorderStyle", yNs);
			borderStyle.setAttribute(new Attribute("color", "#000000"));
			borderStyle.setAttribute(new Attribute("type", "line"));
			borderStyle.setAttribute(new Attribute("width", "1.0"));
			yShapeNode.addContent(borderStyle);
			
			Element nodeLabel = new Element("NodeLabel", yNs);
			nodeLabel.setAttribute(new Attribute("alignment", "center"));
			nodeLabel.setAttribute(new Attribute("autoSizePolicy", "content"));
			nodeLabel.setAttribute(new Attribute("backgroundColor", "#FFFFFF"));
			nodeLabel.setAttribute(new Attribute("fontFamily", "Dialog"));
			nodeLabel.setAttribute(new Attribute("fontSize", "12"));
			nodeLabel.setAttribute(new Attribute("fontStyle", "plain"));
			nodeLabel.setAttribute(new Attribute("hasLineColor", "false"));
			nodeLabel.setAttribute(new Attribute("height", "19.0"));
			nodeLabel.setAttribute(new Attribute("modelName", "eight_pos"));
			nodeLabel.setAttribute(new Attribute("modelPosition", "n"));
			nodeLabel.setAttribute(new Attribute("textColor", "#000000"));
			nodeLabel.setAttribute(new Attribute("visible", "true"));
			nodeLabel.setText(labels[i]);
			yShapeNode.addContent(nodeLabel);
			
			Element shape = new Element("Shape", yNs);
			shape.setAttribute(new Attribute("type", shapeString));
			yShapeNode.addContent(shape);
			visoneShapeNode.addContent(yShapeNode);
			vis.addContent(visoneShapeNode);
			node.addContent(vis);
			
			graphElement.addContent(node);
		}
		
		Comment edgesComment = new Comment(" edges ");
		graphElement.addContent(edgesComment);
		
		for (int i = 0; i < edges.size(); i++) {
			Element edge = new Element("edge", xmlns);
			edge.setAttribute(new Attribute("source", edges.get(i).getSource()));
			edge.setAttribute(new Attribute("target", edges.get(i).getTarget()));
			Element weight = new Element("data", xmlns);
			weight.setAttribute(new Attribute("key", "weight"));
			weight.setText(String.valueOf(edges.get(i).getWeight()));
			edge.addContent(weight);
			
			Element visEdge = new Element("data", xmlns);
			visEdge.setAttribute("key", "e0");
			Element visPolyLineEdge = new Element("polyLineEdge", visone);
			Element yPolyLineEdge = new Element("PolyLineEdge", yNs);
			Element yLineStyle = new Element("LineStyle", yNs);
			String col;
			if (edges.get(i).getWeight() > 0) {
				col = "#00ff00";
			} else if (edges.get(i).getWeight() < 0) {
				col = "#ff0000";
			} else {
				col = "#000000";
			}
			yLineStyle.setAttribute("color", col);
			yLineStyle.setAttribute(new Attribute("type", "line"));
			yLineStyle.setAttribute(new Attribute("width", "2.0"));
			yPolyLineEdge.addContent(yLineStyle);
			visPolyLineEdge.addContent(yPolyLineEdge);
			visEdge.addContent(visPolyLineEdge);
			edge.addContent(visEdge);
			
			graphElement.addContent(edge);
		}
		
		graphml.addContent(graphElement);
		
		File dnaFile = new File (outfile);
		try {
			FileOutputStream outStream = new FileOutputStream(dnaFile);
			XMLOutputter outToFile = new XMLOutputter();
			Format format = Format.getPrettyFormat();
			format.setEncoding("utf-8");
			outToFile.setFormat(format);
			outToFile.output(document, outStream);
			outStream.flush();
			outStream.close();
		} catch (IOException e) {
			System.err.println("Cannot save \"" + dnaFile + "\":" + e.getMessage());
			JOptionPane.showMessageDialog(Dna.dna.gui, "Error while saving the file!\n" + e.getStackTrace());
		}
	}
}
