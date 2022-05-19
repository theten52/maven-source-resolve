package org.apache.maven;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven source code debug entrance.
 *
 * @author wangjin
 */
public class MavenStarter
{
    private static final Logger LOG = LoggerFactory.getLogger(MavenStarter.class);

    public static void main( String[] args )
    {
        LOG.info("===================== Program Arguments ========================");
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = bean.getInputArguments();

        for (String arg : arguments) {
            LOG.info(arg);
        }
        LOG.info("===================== Program Arguments ========================");
        org.codehaus.plexus.classworlds.launcher.Launcher.main(args);
    }
}
