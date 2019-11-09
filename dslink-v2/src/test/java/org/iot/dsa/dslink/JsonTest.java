package org.iot.dsa.dslink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class JsonTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    @Test
    public void theTest() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DSIWriter out = Json.writer(baos);
        out.beginList();
        {
            out.beginMap()
               .key("first").value("first")
               .key("second").value(true)
               .key("third").value(3)
               .key("fourth");
            {
                out.beginList();
                {
                    out.beginMap()
                       .key("first").value("first")
                       .key("second").value(true)
                       .key("third").value(3)
                       .endMap();
                    out.value(true).value(3.0d).endList();
                }
            }
            out.key("fifth")
               .beginMap()
               .key("first").value("first")
               .key("second").value(true)
               .key("third").value(3)
               .endMap();
            out.key("sixth").value("somebytes".getBytes());
            out.endMap();
        }
        out.endList();
        out.close();
        byte[] encoded = baos.toByteArray();
        DSIReader parser = Json.reader(new ByteArrayInputStream(encoded));
        DSList decoded = parser.getElement().toList();
        parser.close();
        parser = Json.reader(new ByteArrayInputStream(encoded));
        DSElement tmp = parser.getElement();
        parser.close();
        Assert.assertTrue(decoded.equals(tmp));
        Assert.assertTrue(decoded.isList());
        Assert.assertTrue(decoded.size() == 1);
        Assert.assertTrue(decoded.get(0).isMap());
        Assert.assertTrue(decoded.get(0).toMap().size() == 6);
        Assert.assertTrue(decoded.get(0).toMap().get("sixth").isBytes());
    }

// Inner Classes
// -------------
//

}
