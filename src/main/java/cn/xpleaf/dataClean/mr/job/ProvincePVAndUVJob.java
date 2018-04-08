package cn.xpleaf.dataClean.mr.job;

import cn.xpleaf.dataClean.mr.mapper.ProvincePVAndUVMapper;
import cn.xpleaf.dataClean.mr.reducer.ProvincePVAndUVReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * 统计每个省的pv和uv值
 * 输入：经过clean之后的access日志
 *      appid ip province city mid userid login_type request method request_url http_version status http_referer user_agent browser yyyy-MM-dd HH:mm:ss
 * 统计该标准化数据，产生结果
 * 省    pv      uv
 *
 * 分析：因为要统计的是每个省对应的pv和uv
 *      pv就是点击量，uv是独立访客量
 *      需要将省相同的数据拉取到一起，拉取到一块的这些数据每一条记录就代表了一次点击（pv + 1）
 *      这里面有同一个用户产生的数据（通过mid来唯一地标识是同一个浏览器，用mid进行去重，得到的就是uv）
 *      Mapper<LongWritable, Text, Text(Province), Text(mid)>
 *      Reducer<Text(Province), Text(mid), Text(Province), Text(pv + uv)>
 *
 *  输入参数：
 *  hdfs://ns1/output/data-clean/access hdfs://ns1/output/pv-uv
 */
public class ProvincePVAndUVJob {
    public static void main(String[] args) throws Exception {

        if (args == null || args.length < 2) {
            System.err.println("Parameter Errors! Usage <inputPath...> <outputPath>");
            System.exit(-1);
        }

        Path outputPath = new Path(args[args.length - 1]);

        Configuration conf = new Configuration();
        String jobName = ProvincePVAndUVJob.class.getSimpleName();
        Job job = Job.getInstance(conf, jobName);
        job.setJarByClass(ProvincePVAndUVJob.class);

        // 设置mr的输入参数
        for (int i = 0; i < args.length - 1; i++) {
            FileInputFormat.addInputPath(job, new Path(args[i]));
        }
        job.setInputFormatClass(TextInputFormat.class);
        job.setMapperClass(ProvincePVAndUVMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        // 设置mr的输出参数
        outputPath.getFileSystem(conf).delete(outputPath, true);    // 避免job在运行的时候出现输出目录已经存在的异常
        FileOutputFormat.setOutputPath(job, outputPath);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setReducerClass(ProvincePVAndUVReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1);

        job.waitForCompletion(true);
    }
}
