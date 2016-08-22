package org.eclipse.flux.core.handlers;

import org.eclipse.flux.client.MessageConstants;
import org.eclipse.flux.core.IRepositoryCallback;
import org.eclipse.flux.watcher.core.Resource;
import org.eclipse.flux.watcher.core.Resource.ResourceType;
import org.eclipse.flux.watcher.core.spi.Project;
import org.json.JSONObject;

public class ResourceRequestHandler extends AbstractMsgHandler {
    public ResourceRequestHandler(IRepositoryCallback repositoryCallback) {
        super(repositoryCallback, GET_RESOURCE_REQUEST);
    }

    @Override
    protected void onMessage(String type, JSONObject message) throws Exception {
        Project project = repositoryCallback.getProject(message.getString(MessageConstants.PROJECT_NAME));
        if (project == null)
            return;
        Resource resource = project.getResource(message.getString(MessageConstants.RESOURCE));
        if (resource == null || message.has("timestamp") && message.getLong("timestamp") != resource.timestamp())
            return;
        message.put(MessageConstants.TIMESTAMP, resource.timestamp());
        message.put(MessageConstants.HASH, resource.hash());
        message.put(MessageConstants.TYPE, resource.type().name().toLowerCase());
        if (resource.type() == ResourceType.FILE) {
            if(message.has("hash") && !message.getString("hash").equals(resource.hash()))
                return;
            message.put(MessageConstants.CONTENT, new String(resource.content()));
        }
        repositoryCallback.sendMessage(GET_RESOURCE_RESPONSE, message);
    }
}
