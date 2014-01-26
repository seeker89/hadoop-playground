package hadoop_playground.make;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * @author mickey
 * 
 * Usage:
 * 		- create the Tree: Tree tree = new Tree(pathToMakeFile, ConfOfTheJob, NameOfTheGoalToExecute)
 * 		- while(tree.hasChildren()){
 * 			ArrayList<Tree> leaves = tree.getLeaves();
 * 			// store somewhere the commands needed
 *          // String cmd = node.toString();
 *          // run job
 *          // set setDeleted(true) sur les leaves d'avant;
 *      - run job avec juste tree.toString()
 *      - multo bene
 *
 */
public class Tree {

	private ArrayList<Tree> nodes = new ArrayList<Tree>();
	private String[] dependencies = null;
	private String goal = null;
	private String cmd = null;
	private boolean deleted = false;
	
	public Tree(String goal){
		this.setGoal(goal);
	}
	
	public boolean isLeaf(){
		return this.nodes.size() == 0;
	}
	
	public boolean hasChildren(){
		for (Tree node : getNodes()){
			if (!node.isDeleted()){
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Tree> getLeaves(){
		ArrayList<Tree> leaves = new ArrayList<Tree>();
		
		for (Tree node : getNodes()){
			if (!node.isDeleted()){
				if (node.isLeaf()){
					leaves.add(node);
				}else{
					leaves.addAll(node.getLeaves());
				}
			}
		}
		
		return leaves;
	}
	
	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
	public String getGoal() {
		return goal;
	}

	public void setGoal(String goal) {
		this.goal = goal;
	}
	
	public void addNode(Tree e){
		this.nodes.add(e);
	}

	public ArrayList<Tree> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Tree> nodes) {
		this.nodes = nodes;
	}
	
	public String[] getDependencies() {
		return dependencies;
	}

	public void setDependencies(String[] dependencies) {
		this.dependencies = dependencies;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String toString(){
		String res = getGoal() + ":";
		for (int i = 0; i < getDependencies().length; i++){
			String dep = getDependencies()[i];
			res = res + dep;
			if (i + 1 < getDependencies().length){
				res = res + " ";
			}
		}
		res = res + ":" + getCmd();
		return res;
	}

	
	// Create a Tree from a Makefile
	public Tree(Path pathToMakefile, Configuration conf, String rootGoal) throws IOException{
		
		ArrayList<Tree> allNodes = new ArrayList<Tree>();
		
		// get the HDFS
		FileSystem fs = FileSystem.get(conf);
		
		// Check if Makefile available
		if (!fs.isFile(pathToMakefile)) {
			System.err.println("Makefile should be a file in directory " + pathToMakefile);
			return;
		} 

		// Pass through the file to get all nodes 
		BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(pathToMakefile)));
		String line;
		
		while ((line = br.readLine()) != null)
		{
			if (line.contains(":") && !line.contains("#"))
			{
				// Parsing "GOAL"
				line = line.trim();
				Tree node = new Tree(line.substring(0, Math.min(line.indexOf(":"), line.indexOf(" "))));
				
				// Parsing dependencies
				line = line.substring(line.indexOf(":"));
				line = line.substring(1);
				line = line.trim();
				node.setDependencies(line.split("\\s+"));
				
				// Parsing "COMMAND"
				line = br.readLine();
				node.setCmd(line.trim());
				
				// add to the array & dependencies to build up the tree later on
				allNodes.add(node);
			}
		}
		br.close();
		
		// BUILD THE TREE
		int i, j;
		
		// connect the dots
		for (i = 0; i < allNodes.size(); i++){
			for (String depGoal : allNodes.get(i).getDependencies()){
				for (j = 0; j < allNodes.size(); j++){
					if (allNodes.get(j).getGoal().equals(depGoal)){
						allNodes.get(i).addNode(allNodes.get(j));
						break;
					}
				}
			}
		}
		
		// find the root = me
		Tree root = null;
		for (i = 0; i < allNodes.size(); i++){
			if (allNodes.get(i).getGoal().equals(rootGoal)){
				root = allNodes.get(i);
				break;
			}
		}
		
		if (root == null){
			System.err.println("Goal " + rootGoal + " not found in the Makefile " + pathToMakefile);
			return;
		}
		
		// set this node as the desired root
		setCmd(root.getCmd());
		setGoal(root.getGoal());
		setNodes(root.getNodes());
	}

}
