package org.failuretest.failurecore.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;


public class TemplateEngineTest {

    @Test
    public void testNetworkToScript() {
        Map<String, Object> data = new HashMap<>();
        data.put("operator", "loss");
        data.put("params", "500ms");
        data.put("ipList", new String[] {"127.0.0.1", "127.0.0.3"});
        data.put("timeInSec", 60);
        String result = TemplateEngine.loadTemplate("scripts/network_to_script.sh", data);
        assertNotEquals(result, "");
    }
}
