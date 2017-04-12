package org.zstack.test.integration.kvm

import org.zstack.header.network.service.NetworkServiceType
import org.zstack.network.securitygroup.SecurityGroupConstant
import org.zstack.network.service.virtualrouter.VirtualRouterConstant
import org.zstack.testlib.EnvSpec
import org.zstack.testlib.Test
import org.zstack.utils.data.SizeUnit

/**
 * Created by xing5 on 2017/2/22.
 */
class Env {
    def DOC = """
use:
1. sftp backup storage
2. local primary storage
3. virtual router provider
4. l2 novlan network
5. security group
"""

    static EnvSpec oneVmBasicEnv() {
        return Test.makeEnv {
            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(8)
                cpu = 4
            }

            diskOffering {
                name = "diskOffering"
                diskSize = SizeUnit.GIGABYTE.toByte(20)
            }

            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                hostname = "localhost"

                image {
                    name = "image1"
                    url = "http://zstack.org/download/test.qcow2"
                }

                image {
                    name = "vr"
                    url = "http://zstack.org/download/vr.qcow2"
                }
            }

            zone {
                name = "zone"
                description = "test"

                cluster {
                    name = "cluster"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm"
                        managementIp = "localhost"
                        username = "root"
                        password = "password"
                    }

                    attachPrimaryStorage("local")
                    attachL2Network("l2")
                }

                localPrimaryStorage {
                    name = "local"
                    url = "/local_ps"
                }

                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "l3"

                        service {
                            provider = VirtualRouterConstant.PROVIDER_TYPE
                            types = [NetworkServiceType.DHCP.toString(), NetworkServiceType.DNS.toString()]
                        }

                        service {
                            provider = SecurityGroupConstant.SECURITY_GROUP_PROVIDER_TYPE
                            types = [SecurityGroupConstant.SECURITY_GROUP_NETWORK_SERVICE_TYPE]
                        }

                        ip {
                            startIp = "192.168.100.10"
                            endIp = "192.168.100.100"
                            netmask = "255.255.255.0"
                            gateway = "192.168.100.1"
                        }
                    }

                    l3Network {
                        name = "pubL3"

                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr")
                }

                attachBackupStorage("sftp")
            }

            vm {
                name = "vm"
                useInstanceOffering("instanceOffering")
                useImage("image1")
                useL3Networks("l3")
            }
        }
    }

    static EnvSpec noHostBasicEnv() {
        return Test.makeEnv {
            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(8)
                cpu = 4
            }

            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                hostname = "localhost"

                image {
                    name = "image1"
                    url = "http://zstack.org/download/test.qcow2"
                }

                image {
                    name = "vr"
                    url = "http://zstack.org/download/vr.qcow2"
                }
            }

            zone {
                name = "zone"
                description = "test"

                cluster {
                    name = "cluster"
                    hypervisorType = "KVM"

                    attachPrimaryStorage("local")
                    attachL2Network("l2")
                }

                localPrimaryStorage {
                    name = "local"
                    url = "/local_ps"
                }

                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "l3"

                        service {
                            provider = VirtualRouterConstant.PROVIDER_TYPE
                            types = [NetworkServiceType.DHCP.toString(), NetworkServiceType.DNS.toString()]
                        }

                        service {
                            provider = SecurityGroupConstant.SECURITY_GROUP_PROVIDER_TYPE
                            types = [SecurityGroupConstant.SECURITY_GROUP_NETWORK_SERVICE_TYPE]
                        }

                        ip {
                            startIp = "192.168.100.10"
                            endIp = "192.168.100.100"
                            netmask = "255.255.255.0"
                            gateway = "192.168.100.1"
                        }
                    }

                    l3Network {
                        name = "pubL3"

                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr")
                }

                attachBackupStorage("sftp")
            }
        }
    }

    static EnvSpec noVmEnv() {
        return Test.makeEnv {
            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(8)
                cpu = 4
            }

            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                hostname = "localhost"

                image {
                    name = "image1"
                    url = "http://zstack.org/download/test.qcow2"
                }

                image {
                    name = "vr-image"
                    url = "http://zstack.org/download/vr.qcow2"
                }
            }

            zone {
                name = "zone"
                description = "test"

                cluster {
                    name = "cluster"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm"
                        managementIp = "localhost"
                        username = "root"
                        password = "password"
                        totalCpu = 40
                        totalMem = SizeUnit.GIGABYTE.toByte(320)
                    }

                    attachPrimaryStorage("local")
                    attachL2Network("l2")
                }

                localPrimaryStorage {
                    name = "local"
                    url = "/local_ps"
                }

                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "l3"

                        service {
                            provider = VirtualRouterConstant.PROVIDER_TYPE
                            types = [NetworkServiceType.DHCP.toString(), NetworkServiceType.DNS.toString()]
                        }

                        service {
                            provider = SecurityGroupConstant.SECURITY_GROUP_PROVIDER_TYPE
                            types = [SecurityGroupConstant.SECURITY_GROUP_NETWORK_SERVICE_TYPE]
                        }

                        ip {
                            startIp = "192.168.100.10"
                            endIp = "192.168.100.100"
                            netmask = "255.255.255.0"
                            gateway = "192.168.100.1"
                        }
                    }

                    l3Network {
                        name = "pubL3"

                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr-image")
                }

                attachBackupStorage("sftp")
            }

        }
    }

    static EnvSpec noVm4Cpu8GHostEnv() {
        return Test.makeEnv {
            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(8)
                cpu = 4
            }

            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                hostname = "localhost"

                image {
                    name = "image1"
                    url = "http://zstack.org/download/test.qcow2"
                }

                image {
                    name = "vr-image"
                    url = "http://zstack.org/download/vr.qcow2"
                }
            }

            zone {
                name = "zone"
                description = "test"

                cluster {
                    name = "cluster"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm"
                        managementIp = "localhost"
                        username = "root"
                        password = "password"
                        totalCpu = 40
                        totalMem = SizeUnit.GIGABYTE.toByte(8)
                    }

                    attachPrimaryStorage("local")
                    attachL2Network("l2")
                }

                localPrimaryStorage {
                    name = "local"
                    url = "/local_ps"
                }

                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "l3"

                        service {
                            provider = VirtualRouterConstant.PROVIDER_TYPE
                            types = [NetworkServiceType.DHCP.toString(), NetworkServiceType.DNS.toString()]
                        }

                        service {
                            provider = SecurityGroupConstant.SECURITY_GROUP_PROVIDER_TYPE
                            types = [SecurityGroupConstant.SECURITY_GROUP_NETWORK_SERVICE_TYPE]
                        }

                        ip {
                            startIp = "192.168.100.10"
                            endIp = "192.168.100.100"
                            netmask = "255.255.255.0"
                            gateway = "192.168.100.1"
                        }
                    }

                    l3Network {
                        name = "pubL3"

                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr-image")
                }

                attachBackupStorage("sftp")
            }

        }
    }
    static EnvSpec fourVmsFourPrimaryStorage() {
        return Test.makeEnv {
            instanceOffering {
                name = "instanceOffering"
                memory = SizeUnit.GIGABYTE.toByte(1)
                cpu = 1 
            }   
            diskOffering {
                name = "diskOffering"
                diskSize = SizeUnit.GIGABYTE.toByte(20)
            }   
            sftpBackupStorage {
                name = "sftp"
                url = "/sftp"
                username = "root"
                password = "password"
                image {
                    name = "image-sftp"
                    url = "https://zstack.io/downloads/test.qcow2"
                }   

                image {
                    name = "vr-image"
                    url = "https://zstack.io/downloads/vr.qcow2"
                }   
            }   

            cephBackupStorage {
                name = "ceph-bk"
                description = "Test Ceph BK"
                totalCapacity = SizeUnit.GIGABYTE.toByte(100)
                availableCapacity = SizeUnit.GIGABYTE.toByte(100)
                url = "/cephbk"
                fsid = "7ff218d9-f525-435f-8a40-3618d1772a64"
                monUrls = ["root:password@localhost/?monPort=7777"]

                image {
                    name = "test-ceph-iso"
                    url = "https://zstack.io/downloads/test.io"
                }   
                image {
                    name = "test-ceph-image"
                    url = "https://zstack.io/downloads/image.qcow2"
                }   
            }   

            zone {
                name = "zone"
                description = "zone with 4 cluster"

                cluster {
                    name = "cluster-ls"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm-ls1"
                        managementIp = "127.0.0.2"
                        username = "root"
                        password = "password"
                    }   

                    kvm {
                        name = "kvm-ls2"
                        managementIp = "127.0.0.3"
                        username = "root"
                        password = "password"
                    }   

                    attachPrimaryStorage("local")
                    attachL2Network("l2")
                }   
                localPrimaryStorage {
                    name = "local"
                    url = "/local_ps"
                }   
                cluster {
                    name = "cluster-ceph"
                    hypervisorType = "KVM"

                    kvm {
                        name = "kvm-ceph"
                        username = "root"
                        managementIp = "127.0.0.4"
                        password = "password"
                        totalCpu = 20
                    }
                    kvm {
                        name = "host-ceph"
                        username = "root"
                        managementIp = "127.0.0.5"
                        password = "password"
                        totalCpu = 20
                    }
                    attachPrimaryStorage("ceph")
                    attachL2Network("l2")
                }
                cephPrimaryStorage {
                    name = "ceph"
                    description = "Test"
                    totalCapacity = SizeUnit.GIGABYTE.toByte(100)
                    availableCapacity = SizeUnit.GIGABYTE.toByte(100)
                    url = "ceph://pri"
                    fsid = "7ff218d9-f525-435f-8a40-3618d1772a64"
                    monUrls = ["root:password@127.0.0.10/?monPort=7777"]
                }

                cluster {
                    name = "cluster-nfs"
                    description = "Test"
                    hypervisorType = "KVM"
                    kvm {
                        name = "kvm-nfs"
                        managementIp = "127.0.0.6"
                        username = "root"
                        totalCpu = 40
                        totalMem = SizeUnit.GIGABYTE.toByte(320)
                    }
                    kvm {
                        name = "host-nfs"
                        managementIp = "127.0.0.7"
                        username = "root"
                        totalCpu = 40
                        totalMem = SizeUnit.GIGABYTE.toByte(320)
                    }
                    attachPrimaryStorage("nfs")
                    attachL2Network("l2")
                }
                nfsPrimaryStorage {
                    name = "nfs"
                    url = "localhost:/nfs"
                }

                cluster {
                    name = "cluster-smp"
                    description = "Test"
                    hypervisorType = "KVM"
                    kvm {
                        name = "kvm-smp"
                        managementIp = "127.0.0.8"
                        username = "root"
                        password = "password"
                    }
                    kvm {
                        name = "host-smp"
                        managementIp = "127.0.0.9"
                        username = "root"
                        password = "password"
                    }
                    attachPrimaryStorage("smp")
                    attachL2Network("l2")
                }
                smpPrimaryStorage {
                    name = "smp"
                    url = "/test"
                }
                l2NoVlanNetwork {
                    name = "l2"
                    physicalInterface = "eth0"

                    l3Network {
                        name = "pubL3"
                        ip {
                            startIp = "12.16.10.10"
                            endIp = "12.16.10.100"
                            netmask = "255.255.255.0"
                            gateway = "12.16.10.1"
                        }
                        service {
                            provider = SecurityGroupConstant.SECURITY_GROUP_PROVIDER_TYPE
                            types = [SecurityGroupConstant.SECURITY_GROUP_NETWORK_SERVICE_TYPE]
                        }
                    }
                }

                virtualRouterOffering {
                    name = "vr"
                    memory = SizeUnit.MEGABYTE.toByte(512)
                    cpu = 2
                    useManagementL3Network("pubL3")
                    usePublicL3Network("pubL3")
                    useImage("vr-image")
                }
                attachBackupStorage("sftp")
                attachBackupStorage("ceph-bk")
            }

            vm {
                name = "vm-ls"
                useInstanceOffering("instanceOffering")
                useImage("image-sftp")
                useCluster("cluster-ls")
                useL3Networks("pubL3")
            }
            vm {
                name = "vm-ceph"
                useInstanceOffering("instanceOffering")
                useImage("test-ceph-image")
                useCluster("cluster-ceph")
                useL3Networks("pubL3")
            }
            vm {
                name = "vm-nfs"
                useInstanceOffering("instanceOffering")
                useImage("image-sftp")
                useCluster("cluster-nfs")
                useL3Networks("pubL3")
            }
            vm {
                name = "vm-smp"
                useInstanceOffering("instanceOffering")
                useImage("image-sftp")
                useCluster("cluster-smp")
                useL3Networks("pubL3")
            }
        }
    }
}
