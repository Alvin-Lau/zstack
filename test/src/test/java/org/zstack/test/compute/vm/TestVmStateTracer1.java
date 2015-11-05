package org.zstack.test.compute.vm;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.compute.host.HostGlobalConfig;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.cloudbus.EventCallback;
import org.zstack.core.cloudbus.EventFacade;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.core.config.GlobalConfigFacade;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.db.SimpleQuery;
import org.zstack.core.db.SimpleQuery.Op;
import org.zstack.header.allocator.HostCapacityVO;
import org.zstack.header.host.HostVO;
import org.zstack.header.vm.*;
import org.zstack.header.vm.VmTracerCanonicalEvents.VmStateChangedData;
import org.zstack.simulator.SimulatorController;
import org.zstack.test.Api;
import org.zstack.test.ApiSenderException;
import org.zstack.test.DBUtil;
import org.zstack.test.deployer.Deployer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 1. create a vm
 * 2. disable host auto-reconnect
 * 3. make the host disconnected
 *
 * confirm the vm becomes Unknown
 * confirm the host capacity not changed
 * confirm VmStateChangedData issued
 *
 * 4. reconnect the host
 *
 * confirm the vm becomes running
 * confirm VmStateChangedData issued
 *
 */
public class TestVmStateTracer1 {
    Deployer deployer;
    Api api;
    ComponentLoader loader;
    CloudBus bus;
    DatabaseFacade dbf;
    GlobalConfigFacade gcf;
    SimulatorController sctrl;
    EventFacade evtf;
    boolean success1 = false;
    boolean success2 = false;

    @Before
    public void setUp() throws Exception {
        DBUtil.reDeployDB();
        deployer = new Deployer("deployerXml/vm/TestVmStateTracer.xml");
        loader = deployer.getComponentLoader();
        gcf = loader.getComponent(GlobalConfigFacade.class);
        sctrl = loader.getComponent(SimulatorController.class);
        HostGlobalConfig.PING_HOST_INTERVAL.updateValue(1);
        bus = loader.getComponent(CloudBus.class);
        dbf = loader.getComponent(DatabaseFacade.class);
        evtf = loader.getComponent(EventFacade.class);
        
        deployer.build();
        api = deployer.getApi();
    }
    
    @Test
    public void test() throws InterruptedException, ApiSenderException {
        HostGlobalConfig.AUTO_RECONNECT_ON_ERROR.updateValue(false);
        SimpleQuery<VmInstanceVO> q = dbf.createQuery(VmInstanceVO.class);
        q.add(VmInstanceVO_.name, Op.EQ, "TestVm1");
        VmInstanceVO vm1 = q.find();
        final String vmUuid = vm1.getUuid();
        final String hostUuid = vm1.getHostUuid();

        evtf.on(VmTracerCanonicalEvents.VM_STATE_CHANGED_PATH, new EventCallback() {
            @Override
            public void run(Map tokens, Object data) {
                VmStateChangedData d = (VmStateChangedData) data;
                if (d.getVmUuid().equals(vmUuid) && d.getTo() == VmInstanceState.Unknown
                        && d.getOriginalHostUuid().equals(hostUuid) && d.getCurrentHostUuid().equals(hostUuid)) {
                    success1 = true;
                } else if (d.getVmUuid().equals(vmUuid) && d.getTo() == VmInstanceState.Running &&
                        d.getOriginalHostUuid().equals(hostUuid) && d.getCurrentHostUuid().equals(hostUuid)) {
                    // abnormally started
                    success2 = true;
                }
            }
        });

        HostCapacityVO cap1 = dbf.findByUuid(hostUuid, HostCapacityVO.class);
        sctrl.setSimulatorHostConnectionState(hostUuid, true);
        TimeUnit.SECONDS.sleep(3);
        vm1 = q.find();

        Assert.assertTrue(success1);
        Assert.assertFalse(success2);

        Assert.assertEquals(VmInstanceState.Unknown, vm1.getState());
        Assert.assertEquals(hostUuid, vm1.getHostUuid());
        HostCapacityVO cap2 = dbf.findByUuid(hostUuid, HostCapacityVO.class);
        Assert.assertEquals(cap1.getAvailableMemory(), cap2.getAvailableMemory());
        Assert.assertEquals(cap1.getAvailableCpu(), cap2.getAvailableCpu());

        sctrl.setSimulatorHostConnectionState(hostUuid, false);
        api.reconnectHost(hostUuid);
        TimeUnit.SECONDS.sleep(3);
        vm1 = q.find();
        Assert.assertEquals(VmInstanceState.Running, vm1.getState());
        Assert.assertTrue(success2);
    }
}
