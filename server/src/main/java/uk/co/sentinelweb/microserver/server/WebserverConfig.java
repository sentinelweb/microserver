package uk.co.sentinelweb.microserver.server;

import java.io.File;
import java.util.HashMap;

import uk.co.sentinelweb.microserver.server.cp.CommandProcessor;

/**
 * Config for server
 *
 * Created by robert on 23/02/2017.
 */
public class WebServerConfig {
    private int port = C.SERVERPORT_DEF;
    private File webRoot;
    private int priority = C.WEB_SVR_PRIORITY;
    private final HashMap<String, CommandProcessor> commandProcessorMap = new HashMap<>();
    private String name = C.DEFAULT_NAME;
    private int _cacheTimeSecs;

    public HashMap<String, CommandProcessor> getCommandProcessorMap() {
        return commandProcessorMap;
    }

    public int getPort() {
        return port;
    }

    public int getPriority() {
        return priority;
    }

    public File getWebRoot() {
        return webRoot;
    }

    public String getName() {
        return name;
    }

    public int getCacheTimeSecs() {

        return _cacheTimeSecs;
    }

    public void setCacheTimeSecs(final int cacheTimeSecs) {
        _cacheTimeSecs = cacheTimeSecs;
    }

    public static class Builder {
        final WebServerConfig c = new WebServerConfig();

        public WebServerConfig.Builder withPort(final int port) {
            c.port = port;
            return this;
        }

        public WebServerConfig.Builder withWebRoot(final File webRoot) {
            c.webRoot = webRoot;
            return this;
        }

        public WebServerConfig.Builder withPriority(final int priority) {
            c.priority = priority;
            return this;
        }

        public WebServerConfig.Builder withCacheTimeSecs(final int cacheTimeSecs) {
            c._cacheTimeSecs = cacheTimeSecs;
            return this;
        }

        public WebServerConfig.Builder withName(final String name) {
            c.name = name;
            return this;
        }

        public WebServerConfig.Builder addProcessor(final String path, final CommandProcessor cp ) {
            c.commandProcessorMap.put(path, cp);
            return this;
        }

        public WebServerConfig build() {
            return c;
        }
    }
}
