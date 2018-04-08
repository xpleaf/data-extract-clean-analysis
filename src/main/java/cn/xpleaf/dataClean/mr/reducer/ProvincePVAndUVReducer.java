package cn.xpleaf.dataClean.mr.reducer;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 统计该标准化数据，产生结果
 * 省    pv      uv
 * 这里面有同一个用户产生的数|据（通过mid来唯一地标识是同一个浏览器，用mid进行去重，得到的就是uv）
 * Mapper<LongWritable, Text, Text(Province), Text(mid)>
 * Reducer<Text(Province), Text(mid), Text(Province), Text(pv + uv)>
 */
public class ProvincePVAndUVReducer extends Reducer<Text, Text, Text, Text> {

    private Set<String> uvSet = new HashSet<>();

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        long pv = 0;
        uvSet.clear();
        for(Text mid : values) {
            pv++;
            uvSet.add(mid.toString());
        }
        long uv = uvSet.size();
        String pvAndUv = pv + "\t" + uv;
        context.write(key, new Text(pvAndUv));
    }
}
