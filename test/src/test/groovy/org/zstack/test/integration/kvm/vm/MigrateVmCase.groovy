package org.zstack.test.integration.kvm.vm

import org.zstack.compute.vm.VmSystemTags
import org.zstack.sdk.VmInstanceInventory
import org.zstack.sdk.MigrateVmAction
import org.zstack.test.integration.kvm.Env
import org.zstack.test.integration.kvm.KvmTest
import org.zstack.testlib.EnvSpec
import org.zstack.testlib.HostSpec
import org.zstack.testlib.SubCase
import org.zstack.testlib.VmSpec
import org.zstack.utils.data.SizeUnit


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
    }
    @Override
    void environment() {
        env = Env.fourVmsFourPrimaryStorage
    }

    @Override
    void test() {
        env.create {
            testMigrateVmLocalStorage()
        }
    }

    void testMigrateVmLocalStorage() {
        VmSpec vm = specByName("vm-ls")
        HostSpec host1 = specByName("kvm-ls1")
        HostSpec host2 = specByName("kvm-ls2")

        vmHostUuid = vm.hostUuid
        MigrateVmAction migrateVmAction = new MigrateVmAction(
            sessionId: Test.currentEnvSpec.session.uuid,
            vmInstanceUuid: vm.uuid,
            hostUuid: vm.hostUuid == host1.uuid ? host2.uuid : host1.uuid
        )

        assert vm.state == VmInstanceState.Running.toString()
        assert vm.hostUuid != vmHostUuid
    }

    @Override
    void clean() {
        env.delete()
    }
}
