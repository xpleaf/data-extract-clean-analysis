package cn.xpleaf.dataClean.utils;

import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *将在计算过程中需要到的ip地址存放到redis中
 * string
 * list
 * hash
 * set
 * zset
 */
public class Redis4IpOps {
    public static void main(String[] args) throws Exception {
        Jedis jedis = JedisUtil.getJedis();
        Map<String, String> ipMap = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("D:/opt/data/ip/ip.data"));
        String line = null;
        while((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            ipMap.put(fields[0], fields[1] + "\t" + fields[2]);
        }
        jedis.hmset("ip_info", ipMap);
        JedisUtil.returnJedis(jedis);
    }
}
