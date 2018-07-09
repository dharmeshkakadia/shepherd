package com.devsda.platform.shephardcore.graphgenerator;

import com.devsda.platform.shephardcore.model.Graph;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DAGGeneratorTest {

    private static DAGGenerator dagGenerator;

    @BeforeClass
    public static void setUp() {
        dagGenerator = new DAGGenerator();
    }

    @AfterClass
    public static void tearDown() {

    }

    @Test
    public void generateDAGTest() throws Exception {

        Graph graph = dagGenerator.generate("src/test/java/com/devsda/platform/shephardcore/workflowrequests/sample_workflow.xml");

        System.out.println(graph);
    }

}
