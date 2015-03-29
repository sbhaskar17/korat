package korat.gui.viz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;

import korat.*;
import korat.gui.viz.metamodel.AlloyAtom;
import korat.gui.viz.metamodel.AlloyField;
import korat.gui.viz.metamodel.AlloySig;
import korat.gui.viz.metamodel.AtomFactory;
import korat.gui.viz.metamodel.FieldFactory;
import korat.gui.viz.metamodel.SigFactory;
import korat.instrumentation.KoratArrayManager;
import korat.instrumentation.IKoratArray;
import korat.utils.ReflectionUtils;

/**
 * Converts arbitrary object into Alloy4Viz instance (.xml) file and theme
 * (.thm) file. Those two files should be given to <code>VizGUI</code>
 * instance in order to visualize its object structure.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ToXMLInstanceConverter {

    protected Map<AlloyAtom, Object> visited = new IdentityHashMap<AlloyAtom, Object>();

    protected Stack<AlloyAtom> workList = new Stack<AlloyAtom>();

    public static final String DEFAULT_MODELS_HOME = "viz_instances/";

    public static final String DEFAULT_THEMES_HOME = "viz_themes/";
    
    private String generatedThemeFilename;

    private String generatedModelFilename;

    private String generatedNodeFilename;

    private String generatedEdgeFilename;

    private boolean skipThemeGeneration;
    
    private boolean newGUI;

    public ToXMLInstanceConverter(boolean generateTheme) {
        skipThemeGeneration = !generateTheme;
        newGUI = false;
    }
    
    public ToXMLInstanceConverter() {
        //this(true);
    	
    	// new GUI   	
    	newGUI = true;  
    }

    /*
     * 
     */
    
	public static final int MAX_CELLS=20;
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
			
			// add only if not already present
			for (int i=0; i<size;i++) {
				if ((nodes[i].name).equals(label))
					return false;
			}
			nodes[size] = new Node();
			nodes[size].name = label;
			size++;
			return true;
		}
		
		public boolean nodeexists(String label) {		
			for (int i=0; i<size;i++) {
				if ((nodes[i].name).equals(label))
					return true;
			}
			return false;
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
			
			// add only if nodes are already present
			if (!(kn.nodeexists(slabel)) || !(kn.nodeexists(dlabel)))
				return false;
			
			// edge already exists
			for (int i=0; i<size;i++) {
				if ((edges[i].label).equals(elabel))
					return false;
			}


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

    /*
     * 
     */
    
    public String getGeneratedModelFilename() {
        return generatedModelFilename;
    }

    public String getGeneratedThemeFilename() {
        return generatedThemeFilename;
    }

    
    
    /*
     * new GUI JSon
     * 
     */
    
    public void convertJson(Object obj, int idx)   {
        try {
        	makeFolders(obj, Integer.toString(idx));
        } catch (IOException e) {
            e.printStackTrace();

        } catch (UnsupportedTypeException e) {
        	e.printStackTrace();
        }
    }

    public void makeFolders(Object obj, String suffix)
            throws UnsupportedTypeException, IOException {

        String name = obj.getClass().getName();
        name = name.replace(".", "/");

        int i = name.lastIndexOf('/');
        String tpath = Korat.DEFAULT_JSON_HOME;
        if (i != -1) {
            String dir = name.substring(0, i + 1);
            tpath = Korat.DEFAULT_JSON_HOME + dir;
        }

        File nodeFile = new File(tpath);
        File edgeFile = new File(tpath);
        if (!nodeFile.exists())
        	nodeFile.mkdirs();

        String simpleName = name.substring(i + 1);

        generatedNodeFilename = tpath + simpleName + "_NODE_" + suffix + ".json";
        nodeFile = new File(generatedNodeFilename);

        generatedEdgeFilename = tpath + simpleName + "_EDGE_" + suffix + ".json";
        edgeFile = new File(generatedEdgeFilename);

		BufferedWriter outNode = null;
		BufferedWriter outEdge = null;
		
        try {

        	outNode = new BufferedWriter(new FileWriter(nodeFile));
        	outEdge = new BufferedWriter(new FileWriter(edgeFile));  
        	
        	Korat.JSONFILEPATH =tpath + simpleName;
        	
            SigFactory.getInstance().clear();
            AtomFactory.getInstance().clear();
            FieldFactory.getInstance().clear();

            workList.clear();
            visited.clear();

        	buildModel(obj);
        	
            if ( (outNode != null) && (outEdge != null)) {
            	dumpJson(outNode, outEdge);

            }
        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (outNode != null)
            	outNode.close();
            if (outEdge != null)
            	outEdge.close();

        }

    }

    
    /*
     * 
     * 
     * 
     */
    
    private void dumpJson(BufferedWriter outNode, BufferedWriter outEdge) throws IOException {
		kn = new KNodes();
		ke = new KEdges();
		
		Gson gson = new Gson();



    	// Gather nodes and edges
		
        for (AlloySig sig : SigFactory.getInstance().getAllSigs()) {

            for (AlloyAtom atom : sig.getAtoms()) {
            	kn.addnode(atom.getName());
            }

        }

        for (AlloyField field : FieldFactory.getInstance().getAllFields()) {
        	Set <String> atoms = new HashSet <String> ();
        	String elabel, slabel, dlabel;
        	
        	elabel = field.getName();
        	atoms.clear();
            for (AlloyAtom atom : field.getValues()) {
            	atoms.add(atom.getName());
            }
            if (atoms.size() >=2) {
            	String[] ss = atoms.toArray(new String[atoms.size()]);
            	slabel = ss[0];
            	dlabel = ss[1];
            	ke.addedge(elabel, slabel, dlabel);
            }

        }
        
        // jSon dump
        outNode.write(gson.toJson(kn));
        outEdge.write(gson.toJson(ke));
     

    }

    
    /**
     * Performs conversion of the given object. The resultant files will have
     * default names and will be stored to default location. Relative file names
     * of those files can be retrieved by calling
     * <code>getGeneratedModelFilename</code> and
     * <code>getGeneratedThemeFilename</code> methods.
     * 
     * @param obj
     *            object to convert to alloy instance & theme file
     * @throws UnsupportedTypeException
     *             currently, the only primitive type that is supported is int.
     *             If any other primitive type is found, the exception will be
     *             thrown.
     */
    public void convert(Object obj) throws UnsupportedTypeException {
        convert(obj, "");
    }

    /**
     * Performs conversion of the given object. The resultant files will have
     * default names (with the given index at the end) and will be stored to
     * default location. Relative file names of those files can be retrieved by
     * calling <code>getGeneratedModelFilename</code> and
     * <code>getGeneratedThemeFilename</code> methods.
     * 
     * @param obj
     *            object to convert to alloy instance & theme file
     * @param idx
     *            index to append at the end of the instance file name
     * @throws UnsupportedTypeException
     *             currently, the only primitive type that is supported is int.
     *             If any other primitive type is found, the exception will be
     *             thrown.
     */
    public void convert(Object obj, int idx) throws UnsupportedTypeException {
        convert(obj, Integer.toString(idx));
    }

    /**
     * Performs conversion of the given object. The resultant files will have
     * default names (with the given suffix at the end) and will be stored to
     * default location. Relative file names of those files can be retrieved by
     * calling <code>getGeneratedModelFilename</code> and
     * <code>getGeneratedThemeFilename</code> methods.
     * 
     * @param obj
     *            object to convert to alloy instance & theme file
     * @param suffix
     *            suffix to append at the end of the instance file name
     * @throws UnsupportedTypeException
     *             currently, the only primitive type that is supported is int.
     *             If any other primitive type is found, the exception will be
     *             thrown.
     */
    public void convert(Object obj, String suffix)
            throws UnsupportedTypeException {

        String name = obj.getClass().getName();
        name = name.replace(".", "/");

        int i = name.lastIndexOf('/');
        String tpath1 = DEFAULT_MODELS_HOME;
        String tpath2 = DEFAULT_THEMES_HOME;
        if (i != -1) {
            String dir = name.substring(0, i + 1);
            tpath1 = DEFAULT_MODELS_HOME + dir;
            tpath2 = DEFAULT_THEMES_HOME + dir;
        }

        File modelFile = new File(tpath1);
        if (!modelFile.exists())
            modelFile.mkdirs();

        String simpleName = name.substring(i + 1);

        generatedModelFilename = tpath1 + simpleName + suffix + ".xml";
        modelFile = new File(generatedModelFilename);

        File themeFile = new File(tpath2);
        if (!themeFile.exists())
            themeFile.mkdirs();

        generatedThemeFilename = tpath2 + simpleName + ".thm";
        themeFile = new File(generatedThemeFilename);

        PrintStream modelPs = null;
        PrintStream themePs = null;

        try {

            if (!modelFile.exists())
                modelFile.createNewFile();


            //COMPAT1.4
            //modelPs = new PrintStream(modelFile);
            modelPs = new PrintStream(new FileOutputStream(modelFile));

            
            if (!skipThemeGeneration) {
                
                if (!themeFile.exists()) {
                    themeFile.createNewFile();
                }

                //COMPAT1.4
                //themePs = new PrintStream(themeFile);
                themePs = new PrintStream(new FileOutputStream(themeFile));
            }

            convert(obj, modelPs, themePs);

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            if (modelPs != null)
                modelPs.close();
            if (themePs != null)
                themePs.close();

        }

    }

    /**
     * Performs conversion of the given object. The content of the generated
     * instance and theme files will be sent to the given print streams 
     * (<code>instanceXMLStream</code> and <code>themeXMLStream</code> 
     * respectively).
     * 
     * @param obj
     *            object to convert to alloy instance & theme
     * @param instanceXMLStream
     *            stream to write the instance file to
     * @param themeXMLStream
     *            stream to write the theme file to
     * @throws UnsupportedTypeException
     *             currently, the only primitive type that is supported is int.
     *             If any other primitive type is found, the exception will be
     *             thrown.
     */
    public void convert(Object obj, PrintStream instanceXMLStream,
            PrintStream themeXMLStream) throws UnsupportedTypeException {

        SigFactory.getInstance().clear();
        AtomFactory.getInstance().clear();
        FieldFactory.getInstance().clear();

        workList.clear();
        visited.clear();

        buildModel(obj);

        if (instanceXMLStream != null)
            writeInstanceXML(instanceXMLStream, "",
                    obj.getClass().getSimpleName());
        if (themeXMLStream != null)
            writeThemeXML(themeXMLStream);

    }

    private static final String[] colors = new String[] { "Yellow", "Green",
            "Red", "Blue", "Gray", "Black", "White" };

    private static final String[] shapes = new String[] { "Box", "Ellipse",
            "Circle", "Hexagon", "Octagon", "Dbl Octagon", "Tpl Octagon" };

    // private final String[] edges = new String[] {"Solid", "Dashed", "Dotted",
    // "Bold"};

    private void writeThemeXML(PrintStream ps) {

        ps.println("<?xml version=\"1.0\"?>");
        ps.println();
        ps.println("<alloy>");
        ps.println("<view useOriginalAtomNames=\"yes\">");
        ps.println();
        ps.println("<defaultnode/>");
        ps.println("<defaultedge/>");
        ps.println();

        int i = 0;
        for (AlloySig sig : SigFactory.getInstance().getAllSigs()) {

            if (sig.isPrimitive())
                ps.println("<node visible=\"no\">");
            else
                ps.println("<node shape=\"" + shapes[i % shapes.length]
                        + "\" color=\"" + colors[i % colors.length] + "\">");

            ps.println("  <type name=\"" + sig.getName() + "\"/>");
            ps.println("</node>");
            i++;
        }

        ps.println();

        for (AlloyField f : FieldFactory.getInstance().getAllFields()) {

            boolean attribute = false;
            for (AlloySig sig : f.getTypes()) {
                if (sig.isPrimitive()) {
                    attribute = true;
                    break;
                }
            }

            if (attribute) {

                ps.println("<edge visible=\"no\" merge=\"no\" attribute=\"yes\">");
                ps.println("  <relation name=\"" + f.getName() + "\">");
                for (AlloySig sig : f.getTypes()) {
                    ps.println("      <type name=\"" + sig.getName() + "\"/>");
                }
                ps.println("  </relation>");
                ps.println("</edge>");

            }

        }

        ps.println();

        ps.println("</view>");
        ps.println("</alloy>");

    }

    private void writeInstanceXML(PrintStream ps, String filename,
            String command) {

        ps.println("<?xml version=\"1.0\"?>");
        ps.println("<alloy>");
        ps.println("<instance filename=\"" + filename + "\" command=\""
                + command + "\">");

        ps.println();

        for (AlloySig sig : SigFactory.getInstance().getAllSigs()) {

            String s = "  <sig name=\"{0}\" extends=\"{1}\">";
            String extendsSigName = "univ";
            if (sig.getExtendsSig() != null)
                extendsSigName = sig.getExtendsSig().getName();
            s = MessageFormat.format(s, sig.getName(), extendsSigName);
            ps.println(s);
            for (AlloyAtom atom : sig.getAtoms()) {
                ps.println("    <atom name=\"" + atom.getName() + "\"/>");
            }
            ps.println("  </sig>");

        }

        ps.println();

        for (AlloyField field : FieldFactory.getInstance().getAllFields()) {

            ps.println("  <field name=\"" + field.getName() + "\">");
            ps.println("    <type>");
            for (AlloySig sig : field.getTypes()) {
                ps.println("      <sig name=\"" + sig.getName() + "\"/>");
            }
            ps.println("    </type>");
            ps.println("    <tuple>");
            for (AlloyAtom atom : field.getValues()) {
                ps.println("      <atom name=\"" + atom.getName() + "\"/>");
            }
            ps.println("    </tuple>");
            ps.println("  </field>");

        }

        ps.println();

        ps.println("</instance>");
        ps.println("</alloy>");

    }

    private void buildModel(Object obj) throws UnsupportedTypeException {

        AlloySig sig = SigFactory.getInstance().getAlloySig(
                obj.getClass().getSimpleName());
        AlloyAtom atom = AtomFactory.getInstance().getAlloyAtom(sig, obj);

        workList.add(atom);
        visited.put(atom, null);

        while (!workList.isEmpty()) {

            atom = workList.pop();
            handleClass(atom.getMyObj().getClass(), atom);
            atom.getMySig().addAtom(atom);

        }

        AtomFactory.getInstance().initAtomNames();

    }

    private void handleClass(Class clz, AlloyAtom atom)
            throws UnsupportedTypeException {

        if (clz == null)
            return;

        Class superClz = clz.getSuperclass();
        if (superClz != null) {
            handleClass(superClz, atom);
            AlloySig superSig = SigFactory.getInstance().getAlloySig(
                    superClz.getSimpleName());
            AlloySig sig = SigFactory.getInstance().getAlloySig(
                    clz.getSimpleName());
            sig.setExtendsSig(superSig);
        }

        handleFields(clz, atom);

    }

    private void handleFields(Class clz, AlloyAtom atom)
            throws UnsupportedTypeException {

        if (atom == null)
            return;

        Object obj = atom.getMyObj();

        if (obj == null)
            return;

        AlloySig clzSig = SigFactory.getInstance().getAlloySig(
                clz.getSimpleName());

        Field[] fields = ReflectionUtils.getDeclaredNonStaticFields(clz);

        try {

            for (Field f : fields) {

                if (Modifier.isTransient(f.getModifiers()))
                    continue;

                Class fieldType = f.getType();

                if (fieldType.isArray()) {
                    
                    handleArrayField(obj, f, clzSig, atom);
                    
                } else if (Collection.class.isAssignableFrom(fieldType)) {
                    
                    handleCollectionField(obj, f, clzSig, atom);
                    
                } else if (IKoratArray.class.isAssignableFrom(fieldType)) {
                    
                    continue;
                    
                } else if (fieldType.isPrimitive()) {

                    handlePrimitives(obj, f, clzSig, atom);

                } else {

                    Object fieldValue = f.get(obj);
                    if (fieldValue != null) {
                        AlloySig fieldSig = SigFactory.getInstance().getAlloySig(
                                fieldValue.getClass().getSimpleName());
                        AlloyAtom fieldValueAtom = AtomFactory.getInstance().getAlloyAtom(
                                fieldSig, fieldValue);

                        AlloyField af = FieldFactory.getInstance().createAlloyField(
                                f.getName());
                        af.getTypes().add(clzSig);
                        af.getTypes().add(fieldSig);
                        af.getValues().add(atom);
                        af.getValues().add(fieldValueAtom);

                        if (!visited.containsKey(fieldValueAtom)) {
                            visited.put(fieldValueAtom, null);
                            workList.add(fieldValueAtom);
                        }
                    }

                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception occured!", e);
        }

    }

    private void handleArrayField(Object obj, Field f, AlloySig clzSig, AlloyAtom atom) throws IllegalArgumentException, IllegalAccessException {
               
        int idx = -1;
        for (Object fieldValue : KoratArrayManager.getArrayValues(obj, f.getName())) {
            idx++;
            if (fieldValue != null) {
                Class elemType = fieldValue.getClass();                
                AlloySig fieldSig = SigFactory.getInstance().getAlloySig(
                        elemType.getSimpleName());
                AlloyAtom fieldValueAtom = AtomFactory.getInstance().getAlloyAtom(
                        fieldSig, fieldValue);
    
                AlloyField af = FieldFactory.getInstance().createAlloyField(
                        f.getName() + "_" + idx);
                af.getTypes().add(clzSig);
                af.getTypes().add(fieldSig);
                af.getValues().add(atom);
                af.getValues().add(fieldValueAtom);
    
                if (!visited.containsKey(fieldValueAtom)) {
                    visited.put(fieldValueAtom, null);
                    workList.add(fieldValueAtom);
                }
            }
        }
        
    }

    private void handleCollectionField(Object obj, Field f, AlloySig clzSig, AlloyAtom atom) throws IllegalArgumentException, IllegalAccessException {
        
        Collection col = (Collection)f.get(obj);
        int idx = -1;
        for (Object fieldValue : col) {
            idx++;
            if (fieldValue != null) {
                AlloySig fieldSig = SigFactory.getInstance().getAlloySig(
                        fieldValue.getClass().getSimpleName());
                AlloyAtom fieldValueAtom = AtomFactory.getInstance().getAlloyAtom(
                        fieldSig, fieldValue);
    
                AlloyField af = FieldFactory.getInstance().createAlloyField(
                        f.getName() + "_" + idx);
                af.getTypes().add(clzSig);
                af.getTypes().add(fieldSig);
                af.getValues().add(atom);
                af.getValues().add(fieldValueAtom);
    
                if (!visited.containsKey(fieldValueAtom)) {
                    visited.put(fieldValueAtom, null);
                    workList.add(fieldValueAtom);
                }
            }
        }
        
    }


    private void handlePrimitives(Object obj, Field f, AlloySig objSig,
            AlloyAtom objAtom) throws UnsupportedTypeException,
            IllegalAccessException {

        if (f.getType() == int.class) {

            int fieldValue = f.getInt(obj);
            AlloySig sig = SigFactory.getInstance().getAlloySig("Int");
            AlloyAtom atom = AtomFactory.getInstance().getIntAlloyAtom(sig,
                    fieldValue);
            sig.addAtom(atom);

            AlloyField af = FieldFactory.getInstance().createAlloyField(
                    f.getName());
            af.getTypes().add(objSig);
            af.getTypes().add(sig);
            af.getValues().add(objAtom);
            af.getValues().add(atom);

        } else {
            throw new UnsupportedTypeException(
                    "The only primitive type that is supported is int.");
        }

    }

}