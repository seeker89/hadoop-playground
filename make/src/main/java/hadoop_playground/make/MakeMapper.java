package hadoop_playground.make;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

public class MakeMapper extends Configured implements Tool {
	
	public static class MapClass extends Mapper<Object, Text, Text, IntWritable> {

		private static final IntWritable ONE = new IntWritable(1);
		private Text word = new Text();

		@Override
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {

			StringTokenizer tokenizer = new StringTokenizer(value.toString());
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				word.set(token);
				context.write(word, ONE);
			}
		}
	}
		
	public static class Reduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		private IntWritable count = new IntWritable();

		@Override
		protected void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			int sum = 0;
			for (IntWritable value : values) {
				sum += value.get();
			}
			count.set(sum);
			context.write(key, count);
		}
	}

	public int run(String[] args) throws Exception {
		/*
		 *  PARSE MAKEFILE
		 */
		
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path pathToMakefile = new Path(args[0]+"/Makefile");
		Path pathToParsed = new Path(args[0]+"/Makefile_parsed");
		
		// Check if Makefile available
		if (!fs.exists(pathToMakefile)) {
			System.err.println("Makefile not found in directory " + args[0]);
			return 1;
		} else if (!fs.isFile(pathToMakefile)) {
			System.err.println("Makefile should be a file in directory " + args[0]);
			return 1;
		} else if (fs.exists(pathToParsed)) {
			System.out.println("Makefile_parsed already exists, deleting it");
			fs.delete(pathToParsed, false);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(pathToMakefile)));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fs.create(pathToParsed, true)));
		
		String line;
		line = br.readLine();
		while (line != null) {
			if (line.contains(":") && !line.contains("#")) {
				// {The line is a target}
				line = line.trim();
				
				// Parsed string format:
				// TARGET:DEP1 DEP2 DEP3:COMMAND
				String parsedString = new String();
				
				// Parsing "TARGET"
				// 0 -> first occurence of ':' or ' '
				String firstWord = line.substring(0, Math.min(line.indexOf(":"), line.indexOf(" ")));
				parsedString = parsedString.concat(firstWord);
				
				// SEPARATOR ':'
				parsedString = parsedString.concat(":");
				
				// Parsing "DEPX"
				// first occurrence of ':' -> EOL
				
				// split according to whitespace (tab, space...)
				line = line.substring(line.indexOf(":"));
				line = line.substring(1);
				line = line.trim();
				String[] dependencies = line.split("\\s+");
				
				for (int i = 0; i < dependencies.length; i++) {
					parsedString = parsedString.concat(dependencies[i]);
					if (i != dependencies.length - 1) {
						// Add a separator whitespace
						parsedString = parsedString.concat(" ");
					}
				}
				
				// SEPARATOR ':'
				parsedString = parsedString.concat(":");
				
				// Parsing "COMMAND"
				// whole line without the engulfing whitespace
				line = br.readLine();
				parsedString = parsedString.concat(line.trim());
				
				
				// OUTPUT PARSED STRING TO TMP FILE
				bw.write(parsedString);
				System.out.println(parsedString);
				
				line = br.readLine();
				if (line != null) {
					bw.newLine();
				}
				
			} else {
				line = br.readLine();
			}
		}
		
		br.close();
		bw.close();
		
		// OVERWRITE MAKEFILE
		fs.delete(pathToMakefile, false);
		
		
		// DISPATCH JOB
        Job job = new Job(getConf());
		job.setJarByClass(MakeMapper.class);
		job.setJobName("wordcount");
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
        job.setMapperClass(MapClass.class);
        job.setReducerClass(Reduce.class);

		FileInputFormat.setInputPaths(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1; 
	}
	
	public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new MakeMapper(), args);
        System.exit(res);
	}

}