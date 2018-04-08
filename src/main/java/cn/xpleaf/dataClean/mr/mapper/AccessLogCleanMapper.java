package cn.xpleaf.dataClean.mr.mapper;

import cn.xpleaf.dataClean.mr.writable.AccessLogWritable;
import cn.xpleaf.dataClean.utils.JedisUtil;
import cn.xpleaf.dataClean.utils.UserAgent;
import cn.xpleaf.dataClean.utils.UserAgentUtil;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * access日志清洗的主要mapper实现类
 * 原始数据结构：
 * appid ip mid userid login_tpe request status http_referer user_agent time ---> 10列内容
 * 清洗之后的结果：
 * appid ip province city mid userid login_type request method request_url http_version status http_referer user_agent browser yyyy-MM-dd HH:mm:ss
 */
public class AccessLogCleanMapper extends Mapper<LongWritable, Text, NullWritable, Text> {

    private Logger logger;
    private String[] fields;

    private String appid;      //数据来源 web:1000,android:1001,ios:1002,ipad:1003
    private String ip;
    //通过ip来衍生出来的字段 province和city
    private String province;
    private String city;

    private String mid;      //mid:唯一的id此id第一次会种在浏览器的cookie里。如果存在则不再种。作为浏览器唯一标示。移动端或者pad直接取机器码。
    private String userId;     //用户id
    private String loginType; //登录状态，0未登录、1：登录用户
    private String request; //类似于此种 "GET userList HTTP/1.1"
    //通过request 衍生出来的字段 method request_url http_version
    private String method;
    private String requestUrl;
    private String httpVersion;

    private String status;          //请求的状态主要有：200 ok、/404 not found、408 Request Timeout、500 Internal Server Error、504 Gateway Timeout等
    private String httpReferer; //请求该url的上一个url地址。
    private String userAgent;   //览器的信息，例如："Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36"
    //通过userAgent来获取对应的浏览器
    private String browser;

    //private long time; //action对应的时间戳
    private String time;//action对应的格式化时间yyyy-MM-dd HH:mm:ss

    private DateFormat df;
    private Jedis jedis;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        logger = Logger.getLogger(AccessLogCleanMapper.class);
        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jedis = JedisUtil.getJedis();
    }

    /**
     * appid ip mid userid login_tpe request status http_referer user_agent time ---> 10列内容
     * ||
     * ||
     * appid ip province city mid userid login_type request method request_url http_version status http_referer user_agent browser yyyy-MM-dd HH:mm:ss
     */
    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        fields = value.toString().split("\t");
        if (fields == null || fields.length != 10) { // 有异常数据
            return;
        }
        // 因为所有的字段没有进行特殊操作，只是文本的输出，所以没有必要设置特定类型，全部设置为字符串即可，
        // 这样在做下面的操作时就可以省去类型的转换，但是如果对数据的合法性有严格的验证的话，则要保持类型的一致
        appid = fields[0];
        ip = fields[1];
        // 解析IP
        if (ip != null) {
            String ipInfo = jedis.hget("ip_info", ip);
            province = ipInfo.split("\t")[0];
            city = ipInfo.split("\t")[1];
        }

        mid = fields[2];
        userId = fields[3];
        loginType = fields[4];
        request = fields[5];
        method = request.split(" ")[0];
        requestUrl = request.split(" ")[1];
        httpVersion = request.split(" ")[2];

        status = fields[6];
        httpReferer = fields[7];
        userAgent = fields[8];
        if (userAgent != null) {
            UserAgent uAgent = UserAgentUtil.getUserAgent(userAgent);
            if (uAgent != null) {
                browser = uAgent.getBrowserType();
            }
        }
        try { // 转换有可能出现异常
            time = df.format(new Date(Long.parseLong(fields[9])));
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
        }
        AccessLogWritable access = new AccessLogWritable(appid, ip, province, city, mid,
                userId, loginType, request, method, requestUrl,
                httpVersion, status, httpReferer, this.userAgent, browser, time);
        context.write(NullWritable.get(), new Text(access.toString()));
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // 资源释放
        logger = null;
        df = null;
        JedisUtil.returnJedis(jedis);
    }
}
