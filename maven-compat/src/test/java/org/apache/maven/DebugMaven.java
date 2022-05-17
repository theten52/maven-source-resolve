package org.apache.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.codehaus.plexus.PlexusTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * 用来启动Maven源码的辅助类。
 *
 * @author wangjin
 * @date 2022/5/17
 */

public class DebugMaven extends PlexusTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(DebugMaven.class);

    public static void main(String[] args) {
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
