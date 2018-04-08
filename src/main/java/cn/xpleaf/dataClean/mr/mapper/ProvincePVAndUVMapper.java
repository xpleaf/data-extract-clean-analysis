package cn.xpleaf.dataClean.mr.mapper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Mapper<LongWritable, Text, Text(Province), Text(mid)>
 * Reducer<Text(Province), Text(mid), Text(Province), Text(pv + uv)>
 */
public class ProvincePVAndUVMapper extends Mapper<LongWritable, Text, Text, Text> {
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();
        String[] fields = line.split("\t");
        if(fields == null || fields.length != 16) {
            return;
        }
        String province = fields[2];
        String mid = fields[4];
        context.write(new Text(province), new Text(mid));
    }
}
