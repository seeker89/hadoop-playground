package hadoop_playground.make;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Make extends Configured implements Tool {
	
	public static class MapExecutor extends Mapper<Object, Text, Text, IntWritable> {

		private static final IntWritable ONE = new IntWritable(1);

		@Override
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			// do nothing - pass to Reduce so that they can be done on different nodes
			context.write(value, ONE);
		}
	}
	
	public class RoundRobinPartitioner extends Partitioner<Text, IntWritable>
	{
	    public int getPartition(Text key, IntWritable value, int numPartitions) {
	    	// round robin implementation
			int n = 0;
	    	String line = key.toString();
	    	n = Integer.parseInt(line.substring(0, line.indexOf(">") - 1));
	    	
	    	System.out.println("INSIDE Paritioner. n=" + n + ", numPartitions=" + numPartitions);
			return (n % numPartitions);
	    }
	}
	
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {

		private IntWritable count = new IntWritable();

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			
			// in reduce, we actually execute the code

			FileSystem fs = FileSystem.get(context.getConfiguration());
			
			System.out.println("INSIDE REDUCE JOB " + context.getWorkingDirectory());
			
			System.out.println("Input path: " + FileOutputFormat.getOutputPath(context));
			
			Path realPath = new Path(FileOutputFormat.getOutputPath(context) + "/../");
			
			System.out.println("Makefile real path: " + realPath);
			
			// INFLATE VALUE
			String line = key.toString();
			
			// round robin hack
			// skip the ID part
			line = line.substring(line.indexOf(">")+1);
			
			// Value format: producedFileName:dep1 dep2 dep3:commandToExecute
			String producedFileName;
			String[] dependencies;
			String commandToExecute;
			
			// inflate filename
			producedFileName = line.substring(0, line.indexOf(":"));
			
			// inflate dependency list and command
			String depList;
			
			depList = line.substring(line.indexOf(":") + 1);
			
			// inflate command
			int commandIndex = depList.indexOf(":") + 1;
			commandToExecute = depList.substring(commandIndex);
			
			depList = depList.substring(0, depList.indexOf(":"));
			
			if (depList.isEmpty()) {
				dependencies = null;
				System.out.println("DETECTED EMPTY LIST");
			} else {
				dependencies = depList.split(" ");
			}
			
			// COPY DEPENDENCIES FROM HDFS TO TMP
			int i = 0;
			try {
				if (dependencies != null) {
					for (i = 0; i < dependencies.length; i++) {
						System.out.println("TRYING TO COPY HDFS:" + new Path(realPath + "/" + dependencies[i])+ " TO LOCAL " + new Path("./" + dependencies[i]));
						fs.copyToLocalFile(false, new Path(realPath + "/" + dependencies[i]), new Path("./" + dependencies[i]));
					}
				}
			} catch(Exception e){
				System.err.println(" ** ERROR: Missing dependency " + dependencies[i]);
				return;
            }
			
			// RUN COMMAND
			System.out.println("RUNNING CMD:" + commandToExecute);
			Process p = Runtime.getRuntime().exec(new String[]{ "bash", "-c", commandToExecute.toString() });
			p.waitFor();

			// MOVE RESULT BACK TO HDFS
			System.out.println("TRYING TO COPY: " + new Path(producedFileName) + " -> " + new Path(realPath + "/" + producedFileName));
			fs.copyFromLocalFile(false, new Path(producedFileName), new Path(realPath + "/" + producedFileName));
			
			// STORE A TRACE OF WHAT HAPPENED
			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			count.set(sum);
			context.write(key, count);
		}
	}

	public int run(String[] args) throws Exception {
		
		if (args.length < 2){
			System.err.println(" ** Usage: .jar {working directory} {goal} {makefile name = Makefile} ");
			System.exit(0);
		}
		
		// init parameters
		String wd = args[0];
		String goal = args[1];
		String makefile;
		ArrayList<Tree> leaves;
		String iterationDir;
		Job job;
		int i = 0;
		
		if (args.length > 2){
			makefile = args[2];
		}else{
			makefile = "Makefile";
		}
		
		System.out.println("Running a distributed make: " + wd + " - '" + goal + "' from " + makefile);
        
        // get the Makefile
		FileSystem fs = FileSystem.get(getConf());
		
		Tree tree = new Tree(new Path(wd + "/" + makefile), getConf(), goal);
		
		// debug information
		tree.printAll();
		
		int nodesReduce;
        
		while(tree.hasChildren()) {
			
			leaves = tree.getLeaves();
			nodesReduce = leaves.size();
			
	        job = new Job(getConf());
	        
			job.setJarByClass(Make.class);
			job.setJobName("make");
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
			job.setMapperClass(MapExecutor.class);
			job.setReducerClass(Reduce.class);
			job.setNumReduceTasks(nodesReduce);
			job.setPartitionerClass(RoundRobinPartitioner.class);
			
			iterationDir = "/iteration" + i;
			
			System.out.println("Running iteration #" + i + " (" + iterationDir + ") with " + nodesReduce + " reduce tasks");
			
			
			
			// generate a text file with a list of commands which can be executed in parallel
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(wd + iterationDir + "/workload"), true)));
			int j = 0;
			for (Tree node : leaves){
				// round robin hack - store an id
				writer.append(j + ">" + node.toString() + System.getProperty("line.separator"));
				j++;
			}
			writer.close();
			
			FileInputFormat.setInputPaths(job, new Path(wd + iterationDir));
			FileOutputFormat.setOutputPath(job, new Path(wd + iterationDir + "_out"));
			
			// execute and check the status
			if (!job.waitForCompletion(true)){
				return 1;
			}
			
			// delete the leaves
			for (Tree leave : leaves){
				leave.setDeleted(true);
			}
			
			i++;
		}
		
		// TODO execute the actual goal
		iterationDir = "/final";
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(wd + iterationDir + "/workload"), true)));
		writer.append(tree.toString() + System.getProperty("line.separator"));
		writer.close();
		
        job = new Job(getConf());
        
		job.setJarByClass(Make.class);
		job.setJobName("make");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
        job.setMapperClass(MapExecutor.class);
        job.setReducerClass(Reduce.class);
        
		FileInputFormat.setInputPaths(job, new Path(wd + iterationDir));
		FileOutputFormat.setOutputPath(job, new Path(wd + iterationDir + "_out"));
		
		return job.waitForCompletion(true) ? 0 : 1; 
	}
	
	public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new Make(), args);
        System.exit(res);
	}

}