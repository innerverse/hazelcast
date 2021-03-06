package com.hazelcast.partition.impl;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.Address;
import com.hazelcast.partition.NoDataMemberInClusterException;
import com.hazelcast.test.AssertTask;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class InternalPartitionServiceLiteMemberTest
        extends HazelcastTestSupport {

    private final Config liteMemberConfig = new Config().setLiteMember(true);

    /**
     * PARTITION ASSIGNMENT
     **/

    @Test
    public void test_partitionsNotAssigned_withLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance instance = factory.newHazelcastInstance(liteMemberConfig);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
        partitionService.firstArrangement();

        for (int i = 0; i < partitionService.getPartitionCount(); i++) {
            assertNull(partitionService.getPartition(i).getOwnerOrNull());
        }
    }

    @Test
    public void test_partitionsAreAssigned_afterDataMemberJoins() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance liteInstance = factory.newHazelcastInstance(liteMemberConfig);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(liteInstance);
        partitionService.firstArrangement();

        final HazelcastInstance dataInstance = factory.newHazelcastInstance();
        warmUpPartitions(liteInstance, dataInstance);

        for (int i = 0; i < partitionService.getPartitionCount(); i++) {
            assertEquals(getNode(dataInstance).getThisAddress(), partitionService.getPartition(i).getOwnerOrNull());
        }
    }

    /**
     * GET PARTITION
     **/

    @Test
    public void test_getPartition_nullPartitionOwnerOnMasterLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance instance = factory.newHazelcastInstance(liteMemberConfig);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
        assertNull(partitionService.getPartition(0).getOwnerOrNull());
    }

    @Test
    public void test_getPartition_nullPartitionOwnerOnNonMasterLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, master);
        assertClusterSizeEventually(2, other);

        for (HazelcastInstance instance : asList(master, other)) {
            final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);

            for (int partitionId = 0; partitionId < partitionService.getPartitionCount(); partitionId++) {
                assertNull(partitionService.getPartition(0).getOwnerOrNull());
            }
        }
    }

    @Test
    public void test_getPartition_afterLiteMemberLeavesTheCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance dataInstance = factory.newHazelcastInstance();
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        warmUpPartitions(dataInstance, lite);

        lite.getLifecycleService().shutdown();

        assertClusterSizeEventually(1, dataInstance);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(dataInstance);

        for (int i = 0; i < partitionService.getPartitionCount(); i++) {
            assertEquals(getNode(dataInstance).getThisAddress(), partitionService.getPartition(i).getOwnerOrNull());
        }
    }

    @Test
    public void test_getPartition_afterDataMemberLeavesTheCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(3);
        final HazelcastInstance master = factory.newHazelcastInstance();
        final HazelcastInstance dataInstance = factory.newHazelcastInstance();
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        warmUpPartitions(lite);

        dataInstance.getLifecycleService().shutdown();

        for (HazelcastInstance instance : asList(master, lite)) {
            final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);

            assertTrueEventually(new AssertTask() {
                @Override
                public void run()
                        throws Exception {
                    for (int i = 0; i < partitionService.getPartitionCount(); i++) {
                        assertEquals(getNode(master).getThisAddress(), partitionService.getPartition(i).getOwnerOrNull());
                    }
                }
            });
        }
    }

    /**
     * GET PARTITION OWNER
     **/

    @Test
    public void test_getPartitionOwner_onMasterLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance instance = factory.newHazelcastInstance(liteMemberConfig);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
        assertNull(partitionService.getPartitionOwner(0));
    }

    @Test
    public void test_getPartitionOwner_onNonMasterLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, master);
        assertClusterSizeEventually(2, other);

        for (HazelcastInstance instance : asList(master, other)) {
            final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
            assertNull(partitionService.getPartitionOwner(0));
        }
    }

    @Test(expected = NoDataMemberInClusterException.class)
    public void test_getPartitionOwnerOrWait_onMasterLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance instance = factory.newHazelcastInstance(liteMemberConfig);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
        assertNull(partitionService.getPartitionOwnerOrWait(0));
    }

    @Test(expected = NoDataMemberInClusterException.class)
    public void test_getPartitionOwnerOrWait_onNonMasterLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, master);
        assertClusterSizeEventually(2, other);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(other);
        assertNull(partitionService.getPartitionOwnerOrWait(0));
    }

    @Test(expected = NoDataMemberInClusterException.class)
    public void test_getPartitionOwnerOrWait_onLiteMemberAfterDataMemberTerminates() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance();
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        warmUpPartitions(master, lite);

        master.getLifecycleService().terminate();

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(lite);
        assertNull(partitionService.getPartitionOwnerOrWait(0));
    }

    @Test(expected = NoDataMemberInClusterException.class)
    public void test_getPartitionOwnerOrWait_onLiteMemberAfterDataMemberShutsDown() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance();
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, master);
        assertClusterSizeEventually(2, lite);
        warmUpPartitions(master, lite);

        master.getLifecycleService().shutdown();

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(lite);
        assertNull(partitionService.getPartitionOwnerOrWait(0));
    }

    /**
     * GRACEFUL SHUTDOWN
     **/

    @Test(timeout = 30000)
    public void test_liteMemberCanShutdownSafely_withClusterSize1() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        lite.getLifecycleService().shutdown();
    }

    @Test(timeout = 30000)
    public void test_liteMemberCanShutdownSafely_withClusterSize2() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite1 = factory.newHazelcastInstance(liteMemberConfig);
        factory.newHazelcastInstance(liteMemberConfig);
        lite1.getLifecycleService().shutdown();
    }

    @Test(timeout = 30000)
    public void test_liteMemberCanShutdownSafely_whenDataMemberExistsInCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();

        warmUpPartitions(lite, other);

        lite.getLifecycleService().shutdown();
    }

    @Test(timeout = 30000)
    public void test_dataMemberCanShutdownSafely_withClusterSize1() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance master = factory.newHazelcastInstance();

        master.getLifecycleService().shutdown();
    }

    @Test(timeout = 30000)
    public void test_dataMemberCanShutdownSafely_whenOnlyLiteMemberExistsInCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance();
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        warmUpPartitions(master, lite);

        master.getLifecycleService().shutdown();
    }

    /**
     * TERMINATE
     **/

    @Test(timeout = 30000)
    public void test_liteMemberCanTerminate_withClusterSize1() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        lite.getLifecycleService().terminate();
    }

    @Test(timeout = 30000)
    public void test_liteMemberCanTerminate_withClusterSize2() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite1 = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance lite2 = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, lite1);
        assertClusterSizeEventually(2, lite2);

        lite1.getLifecycleService().terminate();
    }

    @Test(timeout = 30000)
    public void test_liteMemberCanTerminate_whenDataMemberExistsInCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();

        warmUpPartitions(lite, other);

        lite.getLifecycleService().terminate();
    }

    @Test(timeout = 30000)
    public void test_dataMemberCanTerminate_withClusterSize1() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance master = factory.newHazelcastInstance();

        master.getLifecycleService().terminate();
    }

    @Test(timeout = 30000)
    public void test_dataMemberCanTerminate_whenOnlyLiteMemberExistsInCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance master = factory.newHazelcastInstance();
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        warmUpPartitions(master, lite);

        master.getLifecycleService().terminate();
    }

    /**
     * GET MEMBER PARTITIONS MAP
     **/

    @Test(expected = NoDataMemberInClusterException.class)
    public void test_getMemberPartitionsMap_withOnlyLiteMembers() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(lite);
        partitionService.getMemberPartitionsMap();
    }

    @Test
    public void test_getMemberPartitionsMap_withLiteAndDataMembers() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance dataInstance = factory.newHazelcastInstance();

        warmUpPartitions(lite, dataInstance);

        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(lite);
        final Map<Address, List<Integer>> partitionsMap = partitionService.getMemberPartitionsMap();

        assertEquals(1, partitionsMap.size());
        final List<Integer> partitions = partitionsMap.get(getAddress(dataInstance));
        assertNotNull(partitions);
        assertFalse(partitions.isEmpty());
    }

    /**
     * MEMBER GROUP SIZE
     **/

    @Test
    public void test_memberGroupSize_withSingleLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        assertMemberGroupsSize(lite, 0);
    }

    @Test
    public void test_memberGroupSize_withMultipleLiteMembers() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance lite2 = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, lite);
        assertClusterSizeEventually(2, lite2);

        for (HazelcastInstance instance : asList(lite, lite2)) {
            assertMemberGroupsSize(instance, 0);
        }
    }

    @Test
    public void test_memberGroupSize_withOneLiteMemberAndOneDataMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();

        assertClusterSizeEventually(2, lite);
        assertClusterSizeEventually(2, other);

        for (HazelcastInstance instance : asList(lite, other)) {
            assertMemberGroupsSize(instance, 1);
        }
    }

    @Test
    public void test_memberGroupSize_afterDataMemberLeavesTheCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();

        assertClusterSizeEventually(2, lite);
        assertClusterSizeEventually(2, other);

        for (HazelcastInstance instance : asList(lite, other)) {
            assertMemberGroupsSize(instance, 1);
        }

        other.getLifecycleService().shutdown();
        assertClusterSizeEventually(1, lite);
        assertMemberGroupsSize(lite, 0);
    }

    @Test
    public void test_memberGroupSize_afterLiteMemberLeavesTheCluster() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();

        assertClusterSizeEventually(2, lite);
        assertClusterSizeEventually(2, other);

        for (HazelcastInstance instance : asList(lite, other)) {
            assertMemberGroupsSize(instance, 1);
        }

        lite.getLifecycleService().shutdown();
        assertClusterSizeEventually(1, other);
        assertMemberGroupsSize(other, 1);
    }

    private void assertMemberGroupsSize(final HazelcastInstance instance, final int memberGroupSize) {
        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
        assertEquals(memberGroupSize, partitionService.getMemberGroupsSize());
    }

    /**
     * MAX BACKUP COUNT
     **/

    @Test
    public void test_maxBackupCount_withSingleLiteMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(1);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);

        assertMaxBackupCount(lite, 0);
    }

    @Test
    public void test_maxBackupCount_withTwoLiteMembers() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance lite2 = factory.newHazelcastInstance(liteMemberConfig);

        assertClusterSizeEventually(2, lite);
        assertClusterSizeEventually(2, lite2);

        for (HazelcastInstance instance : asList(lite, lite2)) {
            assertMaxBackupCount(instance, 0);
        }
    }

    @Test
    public void test_maxBackupCount_withOneLiteMemberAndOneDataMember() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(2);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();

        assertClusterSizeEventually(2, lite);
        assertClusterSizeEventually(2, other);

        for (HazelcastInstance instance : asList(lite, other)) {
            assertMaxBackupCount(instance, 0);
        }
    }

    @Test
    public void test_maxBackupCount_withOneLiteMemberAndTwoDataMembers() {
        final TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(3);
        final HazelcastInstance lite = factory.newHazelcastInstance(liteMemberConfig);
        final HazelcastInstance other = factory.newHazelcastInstance();
        final HazelcastInstance other2 = factory.newHazelcastInstance();

        assertClusterSizeEventually(3, lite);
        assertClusterSizeEventually(3, other);
        assertClusterSizeEventually(3, other2);

        for (HazelcastInstance instance : asList(lite, other, other2)) {
            assertMaxBackupCount(instance, 1);
        }
    }

    private void assertMaxBackupCount(final HazelcastInstance instance, final int maxBackupCount) {
        final InternalPartitionServiceImpl partitionService = getInternalPartitionServiceImpl(instance);
        assertEquals(maxBackupCount, partitionService.getMaxBackupCount());
    }

    private InternalPartitionServiceImpl getInternalPartitionServiceImpl(HazelcastInstance instance) {
        return (InternalPartitionServiceImpl) getNode(instance).getPartitionService();
    }

}
