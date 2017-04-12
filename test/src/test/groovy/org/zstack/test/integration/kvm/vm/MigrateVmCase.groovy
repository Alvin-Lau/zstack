package org.zstack.test.integration.kvm.vm

import org.springframework.http.HttpEntity
import org.zstack.compute.vm.VmSystemTags
import org.zstack.header.vm.VmInstanceState
import org.zstack.sdk.VmInstanceInventory
import org.zstack.sdk.HostInventory
import org.zstack.sdk.MigrateVmAction
import org.zstack.sdk.PauseVmInstanceAction
import org.zstack.test.integration.kvm.Env
import org.zstack.kvm.KVMAgentCommands
import org.zstack.kvm.KVMSecurityGroupBackend
import org.zstack.kvm.KVMConstant
import org.zstack.test.integration.kvm.KvmTest
import org.zstack.testlib.EnvSpec
import org.zstack.testlib.HostSpec
import org.zstack.testlib.SubCase
import org.zstack.testlib.VmSpec
import org.zstack.utils.data.SizeUnit
import org.zstack.utils.gson.JSONObjectUtil


/**
 * Created by Lei Liu on April 1st 2017.
 */
class MigrateVmCase extends SubCase {
    EnvSpec env

    def DOC = """
This case mainly test migrating VM under various storages & VM states.

"""
    @Override
    void setup() {
        useSpring(KvmTest.springSpec)
        spring {
            securityGroup()
            kvm()
            ceph()
        }
    }
    @Override
    void environment() {
        env = Env.fourVmsFourPrimaryStorage()
    }

    @Override
    void test() {
        env.create {
            /*testMigrateVmLocalStorage() */
            testMigrateVmCeph()
        }
    }

    void testMigrateVm(VmInstanceInventory vm, HostInventory host1, HostInventory host2) {

        /**
        env.afterSimulator(KVMConstant.KVM_MIGRATE_VM_PATH) { rsp, HttpEntity<String> e ->
              cmd = JSONObjectUtil.toObjeck(e.body, KVMAgentCommands.MigrateVmCmd.class)
              return rsp
        }
        */

        println(host1.uuid)
        println(vm.hostUuid)
        println(host2.uuid)

        env.simulator(KVMSecurityGroupBackend.SECURITY_GROUP_CLEANUP_UNUSED_RULE_ON_HOST_PATH){
            return new KVMAgentCommands.CleanupUnusedRulesOnHostResponse()
        }

        MigrateVmAction migrateVmAction = new MigrateVmAction(
            sessionId: adminSession(),
            vmInstanceUuid: vm.uuid,  
            hostUuid: vm.hostUuid == host1.uuid ? host2.uuid : host1.uuid
        )

        MigrateVmAction.Result res = migrateVmAction.call()
        vm = res.value.inventory
        println(host1.uuid)
        println(vm.hostUuid)
        println(host2.uuid)
        /**
         * Vm should be successfully migrated.
         */
        assert null == migrateVmAction.call().error   
        assert vm.state == VmInstanceState.Running.toString()
        assert vm.lastHostUuid != vm.hostUuid

        /**
         * Pause Vm.
         */
        VmInstanceInventory inv = pauseVmInstance {
            uuid = vm.uuid
        }

        retryInSecs(2,2) {
            assert inv.state == VmInstanceState.Paused.toString()
        }
        assert null != migrateVmAction.call().error   

    }

    void testMigrateVmCeph() {
        VmSpec vmCeph = env.specByName("vm-ceph")
        HostSpec host1 = env.specByName("kvm-ceph")
        HostSpec host2 = env.specByName("host-ceph")

        testMigrateVm(vmCeph.inventory, host1.inventory, host2.inventory)
    }

    void testMigrateVmLocalStorage() {

        VmSpec vmLocalStorage = env.specByName("vm-ls")
        HostSpec host1 = env.specByName("kvm-ls1")
        HostSpec host2 = env.specByName("kvm-ls2")

        MigrateVmAction migrateVmAction = new MigrateVmAction(
            sessionId: adminSession(),
            vmInstanceUuid: vmLocalStorage.inventory.uuid,
            hostUuid: vmLocalStorage.inventory.hostUuid == host1.inventory.uuid ? host2.inventory.uuid : host1.inventory.uuid
        )

        /**
         * Vm should be running. Local Storage doesn't support live migrate VM.
         */
        assert null != migrateVmAction.call().error
        assert vmLocalStorage.inventory.state == VmInstanceState.Running.toString()
        assert vmLocalStorage.inventory.lastHostUuid == vmLocalStorage.inventory.hostUuid

        /**
         * Stopped Vm can not be migrated.
         */
        VmInstanceInventory inv = stopVmInstance {
            uuid = vmLocalStorage.inventory.uuid
        }
        assert inv.state == VmInstanceState.Stopped.toString()
        assert null != migrateVmAction.call().error
        assert vmLocalStorage.inventory.lastHostUuid == vmLocalStorage.inventory.hostUuid
    }


    @Override
    void clean() {
        env.delete()
    }
}
