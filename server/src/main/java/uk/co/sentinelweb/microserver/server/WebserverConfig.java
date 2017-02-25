package uk.co.sentinelweb.microserver.server;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
    private final List< CommandProcessor> commandProcessorMap = new LinkedList<>();
    private final HashMap< String, String> forwards = new HashMap<>();
    private final HashMap< String, String> redirects = new HashMap<>();
    private String name = C.DEFAULT_NAME;
    private int _cacheTimeSecs = -1;

    public List< CommandProcessor> getCommandProcessorMap() {
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

    public HashMap<String, String> getForwards() {
        return forwards;
    }

    public HashMap<String, String> getRedirects() {
        return redirects;
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

        public WebServerConfig.Builder addProcessor(final CommandProcessor cp ) {
            c.commandProcessorMap.add(cp);
            return this;
        }

        public WebServerConfig.Builder addForward(final String src, final String tgt) {
            c.forwards.put(src, tgt);
            return this;
        }

        public WebServerConfig.Builder addRedirect(final String src, final String tgt) {
            c.redirects.put(src, tgt);
            return this;
        }


        public WebServerConfig build() {
            return c;
        }
    }
}
