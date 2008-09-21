/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * This is the trivial map/reduce program that does absolutely nothing
 * other than use the framework to fragment and sort the input values.
 *
 * To run: bin/hadoop jar build/hadoop-examples.jar sort
 *            [-m <i>maps</i>] [-r <i>reduces</i>]
 *            [-inFormat <i>input format class</i>] 
 *            [-outFormat <i>output format class</i>] 
 *            [-outKey <i>output key class</i>] 
 *            [-outValue <i>output value class</i>] 
 *            <i>in-dir</i> <i>out-dir</i> 
 */
public class SortUnique2 extends Configured implements Tool {
	public static class SortMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
		IntWritable l = new IntWritable(1);
		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			output.collect(value, l);
		}
		
	}

	public static class SortReducer extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
		IntWritable l = new IntWritable();
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			int count=0;
			while (values.hasNext())
				count+=values.next().get();
			l.set(count);
			output.collect(key, l);
		}
	}

  /**
   * The main driver for sort program.
   * Invoke this method to submit the map/reduce job.
   * @throws IOException When there is communication problems with the 
   *                     job tracker.
   */
  public int run(String[] args) throws Exception {

    JobConf jobConf = new JobConf(getConf(), SortUnique2.class);
    jobConf.setJobName("sorter");

    jobConf.setMapperClass(SortMapper.class);        
    jobConf.setReducerClass(SortReducer.class);

    // Set user-supplied (possibly default) job configs
//    jobConf.setNumReduceTasks(1);

    jobConf.setInputFormat(TextInputFormat.class);
//    jobConf.setOutputFormat(outputFormatClass);

    jobConf.setOutputKeyClass(Text.class);
    jobConf.setOutputValueClass(IntWritable.class);

//    jobConf.setInputPath(new Path("sorted/"));
//    jobConf.setOutputPath(new Path("sortmerged/"));
	jobConf.set("mapred.child.java.opts", "-Xmx" + 3000 + "m");

    JobClient.runJob(jobConf);
    return 0;
  }



  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new Configuration(), new SortUnique(), args);
    System.exit(res);
  }

} 