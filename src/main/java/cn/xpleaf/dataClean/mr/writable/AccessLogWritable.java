package cn.xpleaf.dataClean.mr.writable;

/**
 *  自定义的Writable，用于记录在操作过程中封装的access数据
 *   *  原始数据结构：
 *      appid ip mid userid login_type request status http_referer user_agent time --->10列内容
 *  清洗之后的结果：
 *      appid ip province city mid userid login_type request method request_url http_version status http_referer user_agent browser yyyy-MM-dd HH:mm:ss
 */
public class AccessLogWritable {
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

    public AccessLogWritable(String appid, String ip, String province, String city, String mid, String userId, String loginType, String request, String method, String requestUrl, String httpVersion, String status, String httpReferer, String userAgent, String browser, String time) {
        this.appid = appid;
        this.ip = ip;
        this.province = province;
        this.city = city;
        this.mid = mid;
        this.userId = userId;
        this.loginType = loginType;
        this.request = request;
        this.method = method;
        this.requestUrl = requestUrl;
        this.httpVersion = httpVersion;
        this.status = status;
        this.httpReferer = httpReferer;
        this.userAgent = userAgent;
        this.browser = browser;
        this.time = time;
    }

    @Override
    public String toString() {
        return appid + "\t" + ip + '\t' + province + '\t' + city + '\t' + mid + '\t' +
                userId + '\t' + loginType + "\t" + request + '\t' + method + '\t' +
                requestUrl + '\t' + httpVersion + '\t' + status + '\t'  + httpReferer + '\t' +
                userAgent + '\t' + browser + '\t' + time;
    }
}
