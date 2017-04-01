package com.zhuanglide.micrboot.test;

import com.zhuanglide.micrboot.Server;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by wwj on 17/3/6.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:api_batch.xml"})
public class TestBatchRunServer {
    @Resource(name="batchServer")
    private Server server;
    @Test
    public void main(){
        server.start();
    }
}
