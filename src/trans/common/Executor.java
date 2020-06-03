/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package trans.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Jiajun
 * @date: 2020/6/3
 */
public class Executor {

    public static List<String> execute(String[] cmd) {
        ProcessBuilder builder = getProcessBuilder(cmd, Util.JAVA_HOME);
        Process process = null;
        final List<String> results = new ArrayList<String>();
        try {
            builder.redirectErrorStream(true);
            process = builder.start();
            final InputStream inputStream = process.getInputStream();

            Thread processReader = new Thread(){
                public void run() {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    try {
                        while((line = reader.readLine()) != null) {
                            results.add(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            processReader.start();
            try {
                processReader.join();
                process.waitFor();
            } catch (InterruptedException e) {
                LevelLogger.error("ExecuteCommand#execute Process interrupted !");
                return results;
            }
        } catch (IOException e) {
            LevelLogger.error("ExecuteCommand#execute Process output redirect exception !");
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        for (String s : results) {
            LevelLogger.debug(s);
        }
        return results;
    }

    private static ProcessBuilder getProcessBuilder(String[] command, String jhome) {
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> evn = builder.environment();
        evn.put("JAVA_HOME", jhome);
        evn.put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF8");
        evn.put("PATH", jhome + "/bin:" + evn.get("PATH"));
        return builder;
    }

    private static ProcessBuilder getProcessBuilder(String[] command, String jhome, String d4jhome) {
        ProcessBuilder builder = new ProcessBuilder(command);
        Map<String, String> evn = builder.environment();
        evn.put("JAVA_HOME", jhome);
        evn.put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF8");
        evn.put("DEFECTS4J_HOME", d4jhome);
        evn.put("PATH", jhome + "/bin:" + evn.get("PATH"));
        return builder;
    }
}
