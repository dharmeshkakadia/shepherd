package com.devsda.platform.shepherd.factory;

import com.devsda.platform.shepherd.constants.NodeState;
import com.devsda.platform.shepherd.model.Node;

public class ObjectFactory {

    public static Node createNode(String nodeName, Integer nodeId, Integer executionId, NodeState status, String errorMessage) {

        Node node = new Node();
        node.setName(nodeName);
        node.setNodeId(nodeId);
        node.setExecutionId(executionId);
        node.setNodeState(status);
        node.setErrorMessage(errorMessage);

        return node;
    }
}
