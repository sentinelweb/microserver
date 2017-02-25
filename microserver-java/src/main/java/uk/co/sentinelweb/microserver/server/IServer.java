package uk.co.sentinelweb.microserver.server;

import uk.co.sentinelweb.microserver.server.util.HeaderUtils;

/**
 * Interface to separate server from CommandProcessor
 * Created by robert on 25/02/2017.
 */
public interface IServer {
    HeaderUtils getHeaderUtils();
    WebServerConfig getConfig();
}
