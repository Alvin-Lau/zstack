package org.zstack.test.core;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.encrypt.EncryptRSA;
import org.zstack.header.core.encrypt.DECRYPT;
import org.zstack.header.core.encrypt.ENCRYPT;
import org.zstack.kvm.KVMHostVO;
import org.zstack.test.BeanConstructor;
import org.zstack.test.deployer.Deployer;

/**
 * Created by mingjian.deng on 16/11/2.
 */
public class TestEncrypt {
    private String password;
    ComponentLoader loader;
    EncryptRSA rsa;
    DatabaseFacade dbf;
    Deployer deployer;


    @Before
    public void setUp() throws Exception {
        BeanConstructor con = new BeanConstructor();
        loader = con.build();
        rsa = loader.getComponent(EncryptRSA.class);

        deployer = new Deployer("deployerXml/mevoco/TestMevoco.xml", con);
        deployer.addSpringConfig("mevocoRelated.xml");
        deployer.addSpringConfig("billing.xml");
        deployer.load();

        loader = deployer.getComponentLoader();
        //bus = loader.getComponent(CloudBus.class);
        dbf = loader.getComponent(DatabaseFacade.class);

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ENCRYPT
    public void setString(String password){
        this.password = password;
    }

    @DECRYPT
    public String getString(){
        return password;
    }

    @Test
    public void test(){
        setString("pwd");
        Assert.assertNotSame("if encrypt successful, this couldn't be same.", "pwd", getPassword());
        String decreptPassword = getString();
        Assert.assertNotNull(decreptPassword);
        Assert.assertEquals("pwd", getString());
        Assert.assertTrue("pwd".equals(decreptPassword));

        setPassword("test_update");
        Assert.assertEquals("test_update", getString());

        KVMHostVO kvmHostVO = new KVMHostVO();
        kvmHostVO.setUsername("test");
        kvmHostVO.setPassword("password");
        dbf.persist(kvmHostVO);
    }
}
