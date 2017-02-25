package uk.co.sentinelweb.microserver.server;

import org.junit.Test;

import java.io.File;

import uk.co.sentinelweb.microserver.server.util.FileUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by robert on 23/02/2017.
 */
public class FileUtilsTest {
    @Test
    public void testCheckParent() throws Exception {
        final FileUtils fileUtils = new FileUtils();
        assertTrue(fileUtils.checkParent(new File("/"),new File("/tmp")));
        assertFalse(fileUtils.checkParent(new File("/tmp"),new File("/tmp/../file")));
        assertTrue(fileUtils.checkParent(new File("/tmp"),new File("/tmp/file")));
        assertFalse(fileUtils.checkParent(new File("/tmp"),new File("/tmp/../../file")));
        assertTrue(fileUtils.checkParent(new File("/tmp"),new File("/tmp/d1/d2/../../file")));
        assertTrue(fileUtils.checkParent(new File("/tmp"),new File("/tmp/d1/d2/../file")));
        assertTrue(fileUtils.checkParent(new File("/tmp"),new File("/tmp/d1/../d2/../file")));
        assertFalse(fileUtils.checkParent(new File("/tmp"),new File("/tmp/d1/../../d2/../file")));
    }

}