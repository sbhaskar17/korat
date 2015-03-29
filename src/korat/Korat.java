package korat;

import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import korat.config.ConfigLoader;
import korat.config.ConfigManager;
import korat.testing.impl.CannotFindClassUnderTest;
import korat.testing.impl.CannotFindFinitizationException;
import korat.testing.impl.CannotFindPredicateException;
import korat.testing.impl.CannotInvokeFinitizationException;
import korat.testing.impl.CannotInvokePredicateException;
import korat.testing.impl.KoratTestException;
import korat.testing.impl.TestCradle;

/**
 * Korat Main Class
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class Korat extends JFrame {

	/**
	 * Loader of Korat Application
	 * 
	 * @param args -
	 *            program arguments are listed below. <p/>
	 * 
	 * Arguments: <table cellspacing="3" cellpadding="3">
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--args &lt;arg-list&gt;</code></td>
	 * <td>mandatory</td>
	 * <td>comma separated list of finitization parameters, ordered as in
	 * corresponding finitization method.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--class &lt;fullClassName&gt;</code></td>
	 * <td>mandatory</td>
	 * <td>name of test case class</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--config &lt;fileName&gt;</code></td>
	 * <td>optional</td>
	 * <td>name of the config file to be used</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvDelta</code></td>
	 * <td>optional</td>
	 * <td>use delta file format when storing candidate vectors to disk</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvEnd &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the end candidate vector to &lt;num&gt;-th vector from cvFile</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvExpected &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>expected number of total explored vectors</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvFile &lt;filename&gt;</code></td>
	 * <td>optional</td>
	 * <td>name of the candidate-vectors file</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvFullFormatRatio &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>the ratio of full format vectors (if delta file format is used)</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvStart &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the start candidate vector to &lt;num&gt;-th vector from cvFile</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvWrite</code></td>
	 * <td>optional</td>
	 * <td>write all explored candidate vectors to file</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--cvWriteNum &lt;num&gt;</code></td>
	 * <td>optional</td>
	 * <td>write only &lt;num&gt; equi-distant vectors to disk</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--excludePackages &lt;packages&gt;</code></td>
	 * <td>optional</td>
	 * <td>comma separated list of packages to be excluded from instrumentation</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--finitization &lt;finMethodName&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the name of finitization method. If ommited, default name
	 * fin&lt;ClassName&gt; is used.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--help</code></td>
	 * <td>optional</td>
	 * <td>print help message.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--listeners &lt;listenerClasses&gt;</code></td>
	 * <td>optional</td>
	 * <td>comma separated list of full class names that implement
	 * <code>ITestCaseListener</code> interface.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--maxStructs</code> &lt;num&gt;</td>
	 * <td>optional</td>
	 * <td>stop execution after finding &lt;num&gt; invariant-passing
	 * structures</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--predicate &lt;predMethodName&gt;</code></td>
	 * <td>optional</td>
	 * <td>set the name of predicate method. If ommited, default name "repOK"
	 * will be used</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--print</code></td>
	 * <td>optional</td>
	 * <td>print the generated structure to the console</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--printCandVects</code></td>
	 * <td>optional</td>
	 * <td>print candidate vector and accessed field list during the search.</td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--progress &lt;threshold&gt;</code></td>
	 * <td>optional</td>
	 * <td>print status of the search after exploration of &lt;threshold&gt;
	 * candidates </td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--serialize &lt;filename&gt;</code></td>
	 * <td>optional</td>
	 * <td>seralize the invariant-passing test cases to the specified file. If
	 * filename contains absolute path, use quotes. </td>
	 * </tr>
	 * 
	 * <tr>
	 * <td style="white-space:nowrap;"><code>--visualize</code> </td>
	 * <td>optional</td>
	 * <td>visualize the generated data structures</td>
	 * </tr>
	 * 
	 * </table>
	 * 
	 * <i>Example command line :: </i> <br/> java korat.Korat --class
	 * korat.examples.binarytree.BinaryTree --args 3,3,3
	 * 
	 */

	private static final long serialVersionUID = 1L;
	
    public static String DEFAULT_JSON_HOME = "viz_json/";
    public static String JSONFILEPATH=null;

	runPanel runpanel;
	graphPanel graphpanel;
	textPanel textpanel;
	consolePanel consolepanel;

	public Korat() {	
		this.setTitle("xKorat");
		this.setSize(800, 600);

		// Creates menu bar for JFrame
		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		this.setLayout(new FlowLayout());

		// Add drop down menu to the menu bar
		JMenu filemenu = new JMenu("File");
		JMenu runmenu = new JMenu("Run");
		JMenu outputmenu = new JMenu("Ouput");
		JMenu consolemenu = new JMenu("Console");
		menuBar.add(filemenu);
		menuBar.add(runmenu);
		menuBar.add(outputmenu);
		menuBar.add(consolemenu);

		// Add menu items to drop down menu
		JMenuItem loaditem = new JMenuItem("Load Location (JSON)");
		JMenuItem saveitem = new JMenuItem("Save Location (JSON)");
		JMenuItem exititem = new JMenuItem("Exit");
		filemenu.add(loaditem);
		filemenu.add(saveitem);
		filemenu.add(exititem);  

		JMenuItem graphitem = new JMenuItem("Graph");
		JMenuItem textitem = new JMenuItem("Text");
		outputmenu.add(graphitem);
		outputmenu.add(textitem);

		JMenuItem runitem = new JMenuItem("Run");	
		runmenu.add(runitem);

		JMenuItem consoleitem = new JMenuItem("Console");
		consolemenu.add(consoleitem);

		runpanel = new runPanel();
		graphpanel = new graphPanel();
		textpanel = new textPanel();
		consolepanel = new consolePanel();

		this.add(runpanel);
		this.add(graphpanel);
		this.add(textpanel);
		this.add(consolepanel);		

		runpanel.setVisible(false);
		graphpanel.setVisible(false);
		textpanel.setVisible(false);
		consolepanel.setVisible(false);
		this.setVisible(true);

		// Add listeners
		ActionListener loadactionlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {		
				int i;
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDialogTitle("Korat JSON file(s)");
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Korat JSON file(s)", "json");
			    fc.setFileFilter(filter);

			    if(fc.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
			    	String fpath = fc.getSelectedFile().getAbsolutePath().replace("\\", "/");
			    	i = fpath.lastIndexOf("_EDGE_");
			    	if (i == -1)
			    		i = fpath.lastIndexOf("_NODE_");
			        if (i == -1)
			        	System.err.println("No Korat JSON file(s) found in the folder.");
			        else {
			        	JSONFILEPATH = fpath.substring(0, i);
			        }			    				    	
			    }
 
			}
		}; 

		ActionListener saveactionlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int i;
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setDialogTitle("Korat JSON folder");
				fc.setAcceptAllFileFilterUsed(false);

			    if(fc.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
			    	Korat.DEFAULT_JSON_HOME = (fc.getSelectedFile().getAbsolutePath()+"/viz_json/").replace("\\", "/");
			    }
			}
		}; 

			
		ActionListener exitactionlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		}; 

		ActionListener runactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(true);
				graphpanel.setVisible(false);
				textpanel.setVisible(false);
				consolepanel.setVisible(false);
			}
		};

		ActionListener graphactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(true);
				textpanel.setVisible(false);
				consolepanel.setVisible(false);
			}
		};

		ActionListener textactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(false);
				textpanel.setVisible(true);
				consolepanel.setVisible(false);
			}
		};

		ActionListener consoleactionlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runpanel.setVisible(false);
				graphpanel.setVisible(false);
				textpanel.setVisible(false);
				consolepanel.setVisible(true);
			}
		};

		exititem.addActionListener(exitactionlistener);  
		loaditem.addActionListener(loadactionlistener);  
		saveitem.addActionListener(saveactionlistener);  
		runitem.addActionListener(runactionlistener);
		graphitem.addActionListener(graphactionlistener);
		textitem.addActionListener(textactionlistener);
		consoleitem.addActionListener(consoleactionlistener);    

	}

	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();

		for (String s: args) {
			sb.append(s);
			sb.append(" ");
		}

		if (sb.toString().contains("--gui")) {
			Korat js = new Korat();
			js.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		else {
			(new runKorat(args)).runnow();
		}
	}
}

class runPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JCheckBoxMenuItem cbprint, cbviz;
	JButton jbrun, jbreset;
	JCheckBox[] checkboxes;
	JTextField[] textfields;
	JLabel[] texfieldlabels;
	runPanel rp;

	GridBagConstraints c;

	String[] soutputs = {"--print", "--printCandVects", "--cvDelta", "--cvWrite", "--visualize"};
	String[] coutputs = {"--args", "--class", "--config", "--cvEnd", "--cvExpected",
			"--cvFile", "--cvFullFormatRatio", "--cvStart", "--cvWriteNum",
			"--excludePackages", "--finitization", "--listeners",
			"--maxStructs", "--predicate", "--progress", "--serialize"};
	String[] cparams = {"<arg-list>", "<fullClassName>", "<fileName>", "<num>", "<num>",
			"<filename>", "<num>", "<num>", "<num>",
			"<packages>", "<finMethodName>", "<listenerClasses>",
			"<num>", "<predMethodName>", "<threshold>", "<filename>"};

	//	String[] coutputs = {"--args <arg-list>", "--class <fullClassName>", "--config <fileName>", "--cvEnd <num>", "--cvExpected <num>",
	//			"--cvFile <filename>", "--cvFullFormatRatio <num>", "--cvStart <num>", "--cvWriteNum <num>",
	//			"--excludePackages <packages>", "--finitization <finMethodName>", "--listeners <listenerClasses>",
	//			"--maxStructs <num>", "--predicate <predMethodName>", "--progress <threshold>", "--serialize <filename>"};

	/*
	--args <arg-list>
	--class <fullClassName>
	--config <fileName>
	--cvDelta
	--cvEnd <num>
	--cvExpected <num>
	--cvFile <filename>
	--cvFullFormatRatio <num>
	--cvStart <num>
	--cvWrite
	--cvWriteNum <num>
	--excludePackages <packages>
	--finitization <finMethodName>
	--help
	--listeners <listenerClasses>
	--maxStructs <num>
	--predicate <predMethodName>
	--print
	--printCandVects
	--progress <threshold>
	--serialize <filename>
	--visualize
	 */

	public runPanel() {
		rp=this;

		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;

		checkboxes = new JCheckBox[soutputs.length];
		for(int i = 0; i < soutputs.length; i++) {
			checkboxes[i] = new JCheckBox(soutputs[i]);
			this.add(checkboxes[i], c);
			c.gridy ++;
		}

		c.gridx ++;
		c.gridy = 0;
		textfields = new JTextField[coutputs.length];
		texfieldlabels = new JLabel[coutputs.length];
		for(int i = 0; i < coutputs.length; i++) {
			textfields[i] = new JTextField(10);
			textfields[i].setText(cparams[i]);
			texfieldlabels[i] = new JLabel(coutputs[i]);
			texfieldlabels[i].setLabelFor(textfields[i]);
			this.add(texfieldlabels[i], c);		
			c.gridx++;
			this.add(textfields[i], c);
			c.gridx--;
			c.gridy++;

		}

		ActionListener jbrunlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String runcmd = rp.getSelection() + " --gui";
				//System.out.println (runcmd);			
				(new runKorat(runcmd.split(" "))).runnow();
				//				System.out.println (rp.getSelection());			
				//				(new runKorat(rp.getSelection().split(" "))).runnow();
			}
		};

		ActionListener jbresetlistener= new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetAll();
			}
		};

		c.gridx += 2;
		c.gridy = 0;
		c.insets = new Insets(0,20,0,0);  // padding
		jbrun = new JButton ("Run");
		this.add(jbrun, c);
		c.gridy += 1;
		jbrun.addActionListener(jbrunlistener);
		jbreset = new JButton ("Reset");
		this.add(jbreset, c);
		jbreset.addActionListener(jbresetlistener);
	}

	public void resetAll () {
		for(int i = 0; i < soutputs.length; i++) {
			checkboxes[i].setSelected(false);
		}   	
		for(int i = 0; i < coutputs.length; i++) {
			textfields[i].setText(cparams[i]);
		}  
	}


	public String getSelection() {
		StringBuilder sb = new StringBuilder(1);
		for(int i = 0; i < soutputs.length; i++) {
			if(checkboxes[i].isSelected())
				sb.append(" "+checkboxes[i].getText());
		}

		for(int i = 0; i < coutputs.length; i++) {
			if(!textfields[i].getText().contains("<")) {
				sb.append(" "+texfieldlabels[i].getText());
				sb.append(" "+textfields[i].getText());
			}
		}

		return sb.toString();
	}
}

class runKorat {
	String[] args;

	runKorat (String[] a) {
		this.args = a;
	}

	public void runnow () {
		TestCradle testCradle = TestCradle.getInstance();
		ConfigManager config = ConfigManager.getInstance();
		config.parseCmdLine(args);

		System.out.print("\nStart of Korat Execution for " + config.className
				+ " (" + config.predicate + ", ");
		System.out.println(Arrays.toString(config.args) + ")\n");

		try {

			long t1 = System.currentTimeMillis();
			testCradle.start(config.className, config.args);
			long t2 = System.currentTimeMillis();

			long dt1 = t2 - t1;
			System.out.println("\nEnd of Korat Execution");
			System.out.println("Overall time: " + dt1 / 1000.0 + " s.");

		} catch (CannotFindClassUnderTest e) {

			System.err.println("!!! Korat cannot find class under test:");
			System.err.println("        <class_name> = " + e.getFullClassName());
			System.err.println("    Use -"
					+ ConfigLoader.CLZ.getSwitches()
					+ " switch to provide full class name of the class under test.");

		} catch (CannotFindFinitizationException e) {

			System.err.println("!!! Korat cannot find finitization method for the class under test:");
			System.err.println("        <class> = " + e.getCls().getName());
			System.err.println("        <finitization> = " + e.getMethodName());
			System.err.println("    Use -"
					+ ConfigLoader.FINITIZATION.getSwitches()
					+ " switch to provide custom finitization method name.");

		} catch (CannotFindPredicateException e) {

			System.err.println("!!! Korat cannot find predicate method for the class under test:");
			System.err.println("        <class> = " + e.getCls().getName());
			System.err.println("        <predicate> = " + e.getMethodName());
			System.err.println("    Use -"
					+ ConfigLoader.PREDICATE.getSwitches()
					+ " switch to provide custom predicate method name.");

		} catch (CannotInvokeFinitizationException e) {

			System.err.println("!!! Korat cannot invoke finitization method:");
			System.err.println("        <class> = " + e.getCls().getName());
			System.err.println("        <finitization> = " + e.getMethodName());
			System.err.println();
			System.err.println("    Stack trace:");
			e.printStackTrace(System.err);

		} catch (CannotInvokePredicateException e) {

			System.err.println("!!! Korat cannot invoke predicate method:");
			System.err.println("      <class> = " + e.getCls().getName());
			System.err.println("      <predicate> = " + e.getMethodName());
			System.err.println();
			System.err.println("    Stack trace:");
			e.printStackTrace(System.err);

		} catch (KoratTestException e) {

			System.err.println("!!! A korat exception occured:");
			System.err.println();
			System.err.println("    Stack trace:");
			e.printStackTrace(System.err);

		}

	}

}


class graphPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String CUSTOM_VERTEX_STYLE = "CUSTOM_VERTEX_STYLE";
	public static final String CUSTOM_EDGE_STYLE = "CUSTOM_EDGE_STYLE";
	public static final String CUSTOM_ROOT_STYLE = "CUSTOM_ROOT_STYLE";
	public static final int MAX_CELLS=20;
	public static final String DEFAULT_JSON_HOME = "viz_json/";
	private int idx = 0;
	mxGraph mGraph = null;
	//Object parent;
	mxIGraphModel mgm;
	mxCell cell;
	mxGraphComponent graphComponent = null;
	GridBagConstraints c;
	Container container;
	JButton jbinfo;
	JButton jbprev, jbnext;

	KNodes kn;
	KEdges ke;

	class Node {
		String name;
	}

	class Edge {
		String label;
		Node source;
		Node dest;
	}

	class KNodes {
		int size;
		Node[] nodes;;

		KNodes () {
			size=0;
			nodes = new Node[MAX_CELLS];
		}

		public boolean addnode(String label) {
			if (size >= MAX_CELLS) return false;
			nodes[size] = new Node();
			nodes[size].name = label;
			size++;
			return true;
		}

		public int getnodeidx(String label) {
			for (int i=0; i<size; i++) {
				if ((nodes[i].name).equals(label))
					return i;
			}		
			return -1;
		}

	}

	class KEdges {
		int size;
		Edge[] edges;

		KEdges () {
			size=0;
			edges = new Edge[MAX_CELLS];
		}

		public boolean addedge(String elabel, String slabel, String dlabel) {
			if (size >= MAX_CELLS) return false;
			edges[size] = new Edge();
			edges[size].label=elabel;
			edges[size].source=new Node();
			edges[size].source.name = slabel;
			edges[size].dest=new Node();
			edges[size].dest.name = dlabel;
			size++;
			return true;
		}

	}


	public graphPanel() {
		JCheckBox jcbanimate;

		c = new GridBagConstraints();

		this.setLayout(new GridBagLayout());

		this.mGraph = new mxGraph();
		mGraph.setMinimumGraphSize(new mxRectangle(0, 0, 600, 500));
		setStyleSheet(mGraph);


		jcbanimate = new JCheckBox ("Animate");
		jbprev = new JButton ("Previous");
		jbnext = new JButton ("Next");
		jbinfo = new JButton ("Model# " + Integer.toString(1));

		c.gridx = 0;
		c.gridy = 0;
		this.add(jcbanimate, c);
		
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		this.add(jbprev, c);

		//c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		this.add(jbnext, c);

		//c.fill = GridBagConstraints.HORIZONTAL;
		//c.gridwidth = 2;
		c.gridx = 3;
		c.gridy = 0;
		this.add(jbinfo, c);

		jbprev.addActionListener(jbprevlistener);
		jbnext.addActionListener(jbnextlistener);

		jcbanimate.addItemListener(jcbanimatelistener);

		
		graphComponent = new mxGraphComponent(mGraph);
		JScrollPane jsp = new JScrollPane(graphComponent);
		//c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy = 1;
		this.add(jsp,c);

	}


	
	ItemListener jcbanimatelistener= new ItemListener() {		
		ActionListener timerlistener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (idx<0 || idx>=MAX_CELLS) 
					idx=0;
				else
					idx+=1;
				
				if (!getGraphFile(Korat.JSONFILEPATH, Integer.toString(idx))) { // we can't find graph files, reached the end
					idx=0;
				}
				jbinfo.setText("Model# " + Integer.toString(idx+1));           		
				timer.restart();
			}				
		};

		Timer timer = new Timer(1000*5, timerlistener);

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				jbprev.setEnabled(false);
				jbnext.setEnabled(false);
				timer.start();
			} else {
				timer.stop();
				jbprev.setEnabled(true);
				jbnext.setEnabled(true);
			}
		}	
	};

			
	ActionListener jbprevlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			idx = (idx<=0) ? 0 : idx-1;
			getGraphFile(Korat.JSONFILEPATH, Integer.toString(idx));
			jbinfo.setText("Model# " + Integer.toString(idx+1));
		}
	};


	ActionListener jbnextlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			idx = (idx<MAX_CELLS-1) ? idx+1 : MAX_CELLS-1;
			if (!getGraphFile(Korat.JSONFILEPATH, Integer.toString(idx))) {
				//roll back, we can't find graph files, idx reached the end
				if (idx > 0) idx -= 1; 
			}
			jbinfo.setText("Model# " + Integer.toString(idx+1));
		}
	};

	KNodes loadNodesJson (String file) {
		Gson gson = new Gson();
		BufferedReader in=null;
		String inline = null;

		try {
			in = new BufferedReader(new FileReader(file));
			inline=in.readLine();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		KNodes kn = gson.fromJson(inline, KNodes.class);
		return kn;

	}

	KEdges loadEdgesJson (String file) {
		Gson gson = new Gson();
		BufferedReader in=null;
		String inline = null;

		try {
			if (!(new File(file).isFile()))
				return null; // file does not exists

			in = new BufferedReader(new FileReader(file));
			inline=in.readLine();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		KEdges ke = gson.fromJson(inline, KEdges.class);
		return ke;

	}


	boolean getGraphFile (String filepath, String suffix) {
		KNodes kn;
		KEdges ke;

		kn=loadNodesJson (filepath+"_NODE_" + suffix + ".json");
		ke=loadEdgesJson (filepath+"_EDGE_" + suffix + ".json");

		if ((kn == null) || (ke == null))
			return false;

		if (this.mGraph != null) // clear graph
			this.mGraph.removeCells(this.mGraph.getChildCells(this.mGraph.getDefaultParent(), true, true));

		mgm = mGraph.getModel();
		mgm.beginUpdate();
		try{
			int cx=0, cy=0;	
			Object[] verts = new Object[MAX_CELLS];
			for (int i=0; i<kn.size; i++) {
				if (i==0) {
					// root
					cx = 50; cy = 50;
					verts[i] = mGraph.insertVertex(mGraph.getDefaultParent(), null, kn.nodes[i].name, cx, cy, 50, 50, CUSTOM_ROOT_STYLE);
				} else {
					if (i%3 == 1) {
						cx += 150;
						cy = 150;
					}
					else if (i%3 == 2) {
						cx += 100;
						cy = 250;
					}
					else if (i%3 == 0) {
						cx += 50;
						cy = 350;
					}

					verts[i] = mGraph.insertVertex(mGraph.getDefaultParent(), null, kn.nodes[i].name, cx, cy, 50, 50, CUSTOM_VERTEX_STYLE);
				}
			}

			int sidx, didx;
			String elabel;
			for (int i=0; i<ke.size; i++) {
				elabel = ke.edges[i].label;
				sidx = kn.getnodeidx(ke.edges[i].source.name);
				didx = kn.getnodeidx(ke.edges[i].dest.name);

				if ((sidx != -1) && (didx != -1)) {
					mGraph.insertEdge(mGraph.getDefaultParent(), null, elabel, verts[sidx], verts[didx], CUSTOM_EDGE_STYLE);
				}

			}
		}
		finally{
			mgm.endUpdate();
		}


		graphComponent.refresh();

		return true;


		//graph.refresh();
		//		Object[] cells = graph.getChildCells(parent, true, true);
		//		for( Object c: cells) {			
		//			cell = (mxCell) c;
		//			if (cell.isVertex()) {
		//				System.out.println("V:"+cell.getId()+" "+cell.getValue());
		//			} else { // edge
		//				System.out.println("E:"+cell.getSource().getId()+" "+cell.getValue()+":"+cell.getTarget().getId()+" "+cell.getValue());
		//			}
		//		}

	}


	private static void setStyleSheet(mxGraph graph) {

		Hashtable<String, Object> style;
		mxStylesheet stylesheet = graph.getStylesheet();

		// base style
		Hashtable<String, Object> baseStyle = new Hashtable<String, Object>();
		baseStyle.put(mxConstants.STYLE_FONTCOLOR, mxUtils.getHexColorString(Color.BLACK));
		//baseStyle.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);


		// custom vertex style
		style = new Hashtable<String, Object>(baseStyle);
		style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.BLUE));
		style.put(mxConstants.STYLE_STROKEWIDTH, 1);
		style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
		stylesheet.putCellStyle(CUSTOM_VERTEX_STYLE, style);


		// custom edge style
		style = new Hashtable<String, Object>(baseStyle);
		style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.RED));
		style.put(mxConstants.STYLE_STROKEWIDTH, 1);
		//style.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, mxUtils.getHexColorString(Color.WHITE));
		style.put(mxConstants.STYLE_ROUNDED, true);
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ELBOW);
		stylesheet.putCellStyle(CUSTOM_EDGE_STYLE, style);

		// custom root vertex style
		style = new Hashtable<String, Object>(baseStyle);
		style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.getHexColorString(Color.GREEN));
		style.put(mxConstants.STYLE_STROKEWIDTH, 2);
		style.put(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(Color.WHITE));
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_VERTICAL_ALIGN, mxConstants.ALIGN_MIDDLE);
		style.put(mxConstants.STYLE_WHITE_SPACE, "wrap");
		stylesheet.putCellStyle(CUSTOM_ROOT_STYLE, style);


	}
}

class consolePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JTextArea jta;
	JScrollPane jsp;
	JButton jbclear;
	GridBagConstraints c;

	public consolePanel() {
		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();

		jta = new JTextArea (30, 60);
		jta.setEditable(false);
		jta.setFont(new Font("Arial", Font.PLAIN, 12));
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jsp = new JScrollPane(jta);
		
		c.gridx = 0;
		c.gridy = 0;
		this.add(jsp, c);
		
		jbclear = new JButton("Clear");
		c.gridx = 0;
		c.gridy = 1;
		this.add(jbclear, c);
		jbclear.addActionListener(jbclearlistener);

		this.add(jsp);
		redirectSystemStreams();
	}
	
	ActionListener jbclearlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			jta.setText("");
		}
	};

	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream err = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setErr(new PrintStream(err, true));
	}

}

class textPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JTextArea jta;
	JScrollPane jsp;
	JButton jbclear;
	GridBagConstraints c;

	public textPanel() {
		this.setLayout(new GridBagLayout());
		c = new GridBagConstraints();

		jta = new JTextArea (30, 60);
		jta.setEditable(false);
		jta.setFont(new Font("Arial", Font.PLAIN, 12));
		jta.setLineWrap(true);
		jta.setWrapStyleWord(true);
		jsp = new JScrollPane(jta);

		c.gridx = 0;
		c.gridy = 0;
		this.add(jsp, c);
		
		jbclear = new JButton("Clear");
		c.gridx = 0;
		c.gridy = 1;
		this.add(jbclear, c);
		jbclear.addActionListener(jbclearlistener);

		redirectSystemStreams();
	}
	
	ActionListener jbclearlistener= new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			jta.setText("");
		}
	};


	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jta.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
	}

}


