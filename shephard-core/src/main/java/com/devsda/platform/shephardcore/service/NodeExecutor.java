package com.devsda.platform.shephardcore.service;

import com.devsda.platform.shephardcore.dao.WorkflowOperationDao;
import com.devsda.platform.shephardcore.util.DateUtil;
import com.devsda.platform.shepherd.constants.NodeState;
import com.devsda.platform.shepherd.exception.ClientNodeFailureException;
import com.devsda.platform.shepherd.exception.NodeFailureException;
import com.devsda.platform.shepherd.factory.ObjectFactory;
import com.devsda.platform.shepherd.model.Node;
import com.devsda.platform.shepherd.model.NodeConfiguration;
import com.devsda.platform.shephardcore.model.NodeResponse;
import com.devsda.platform.shepherd.model.ServerDetails;
import com.devsda.utils.httputils.constants.Protocol;
import com.devsda.utils.httputils.methods.HttpPostMethod;
import com.google.inject.Inject;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class NodeExecutor implements Callable<NodeResponse> {

    private static final Logger log = LoggerFactory.getLogger(NodeExecutor.class);

    private NodeConfiguration nodeConfiguration;
    private ServerDetails serverDetails;
    private Integer executionId;

    @Inject
    private static WorkflowOperationDao workflowOperationDao;

    public NodeExecutor(Integer executionId, NodeConfiguration nodeConfiguration, ServerDetails serverDetails) {
        this.executionId = executionId;
        this.nodeConfiguration = nodeConfiguration;
        this.serverDetails = serverDetails;
    }

    @Override
    public NodeResponse call() throws Exception {


        log.info(String.format("Executing node : %s. NodeConfiguration : %s, Server details : %s", nodeConfiguration.getName(), nodeConfiguration, serverDetails));

        String response = null;
        Node node = null;

        try {

            // 1. Create entry in Node table.
            node = ObjectFactory.createNode(this.nodeConfiguration.getName(), null, this.executionId, NodeState.PROCESSING, null);
            workflowOperationDao.createNode(node);

            // 2. Execute Node.
            response = new HttpPostMethod().call(Protocol.HTTPS, serverDetails.getHostName(), serverDetails.getPort(),
                    nodeConfiguration.getURI(), null, nodeConfiguration.getHeaders(),
                    new StringEntity(""), String.class);

            log.info(String.format("Response of Node : %s is %s", nodeConfiguration.getName(), response));

            // 3. Update Node status as Completed in Node table.
            node.setNodeState(NodeState.COMPLETED);
            node.setUpdatedAt(DateUtil.currentDate());
            workflowOperationDao.updateNode(node);

            return new NodeResponse(nodeConfiguration.getName(), response);

        } catch(HttpResponseException e) {

            log.error(String.format("Node : {} failed at client side.", this.nodeConfiguration.getName()));
            node.setNodeState(NodeState.FAILED);
            node.setUpdatedAt(DateUtil.currentDate());
            workflowOperationDao.updateNode(node);
            throw new ClientNodeFailureException(String.format("Node : {} failed at client side.", this.nodeConfiguration.getName()));

        } catch(Exception e) {

            log.error(String.format("Node : {} failed internally.", this.nodeConfiguration.getName()));
            node.setNodeState(NodeState.FAILED);
            node.setUpdatedAt(DateUtil.currentDate());
            workflowOperationDao.updateNode(node);
            throw new NodeFailureException(String.format("Node : {} failed at client side.", this.nodeConfiguration.getName()));

        }
    }
}
