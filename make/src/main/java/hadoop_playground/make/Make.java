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
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Make extends Configured implements Tool {
	
	public static class MapExecutor extends Mapper<Object, Text, Text, IntWritable> {

		private static final IntWritable ONE = new IntWritable(1);
		private Text word = new Text();

		@Override
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			FileSystem fs = FileSystem.get(context.getConfiguration());
			
			System.out.println("INSIDE MAP JOB");
			
			// INFLATE VALUE
			String line = value.toString();
			// Value format: producedFileName:dep1 dep2 dep3:commandToExecute
			String producedFileName;
			String[] dependencies;
			String commandToExecute;
			
			// inflate filename
			producedFileName = line.substring(0, value.toString().indexOf(":"));
			
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
						System.out.println("TRYING TO COPY HDFS:" + dependencies[i] + " TO LOCAL");
						fs.copyToLocalFile(false, new Path(dependencies[i]), new Path("./" + dependencies));
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

			// MOVE RESULT TO HDFS
			System.out.println("TRYING TO COPY:" + producedFileName);
			fs.copyFromLocalFile(false, new Path(producedFileName), new Path("makefile/" + producedFileName));
			
			// save the created file info
			word.set(producedFileName);
			context.write(word, ONE);
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
		
		// Create the job we will modify and run several times
        job = new Job(getConf());
        
		job.setJarByClass(Make.class);
		job.setJobName("make");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
        job.setMapperClass(MapExecutor.class);
        
        
        // get the Makefile
		FileSystem fs = FileSystem.get(getConf());
		
		Tree tree = new Tree(new Path(wd + "/" + makefile), getConf(), goal);
        
		while(tree.hasChildren()){
			
			iterationDir = "/iteration" + i;
			
			System.out.println("Running iteration #" + i + " (" + iterationDir + ")");
			
			leaves = tree.getLeaves();
			
			// generate a text file with a list of commands which can be executed in parallel
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(wd + iterationDir + "/workload"), true)));
			for (Tree node : leaves){
				writer.append(node.toString() + System.getProperty("line.separator"));
			}
			writer.close();
			
			FileInputFormat.setInputPaths(job, new Path(wd + iterationDir));
			FileOutputFormat.setOutputPath(job, new Path(wd));
			
			// execute and check the status
			if (!job.waitForCompletion(true)){
				return 1;
			}
			
			// delete the leaves
			for (Tree leave : leaves){
				leave.setDeleted(true);
			}
		}
		
		// TODO execute the actual goal
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(wd + "/final" + "/workload"), true)));
		writer.append(tree.toString() + System.getProperty("line.separator"));
		writer.close();
		FileInputFormat.setInputPaths(job, new Path(wd + "/final"));
		FileOutputFormat.setOutputPath(job, new Path(wd));
		
		return job.waitForCompletion(true) ? 0 : 1; 
	}
	
	public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new Make(), args);
        System.exit(res);
	}

}