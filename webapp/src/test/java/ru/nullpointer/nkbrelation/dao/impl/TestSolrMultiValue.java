package ru.nullpointer.nkbrelation.dao.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Yastrebov
 */
public class TestSolrMultiValue {

    private final Logger logger = LoggerFactory.getLogger(TestSolrMultiValue.class);

    //
    @Test
    @Ignore
    public void testMultiValueAdd() throws IOException, SolrServerException {

        Map<String, Object> m = new HashMap<String, Object>();

        m.put("id", "1");
        m.put("value", "4113522");
        m.put("valuesort", "4113522660");

        SolrServer server = new CommonsHttpSolrServer("http://localhost:8983/solr/phone/");

        SolrInputDocument d = fromMap(m);

        server.add(d);

        server.commit();

    }

    private SolrInputDocument fromMap(Map<String, Object> m) {
        SolrInputDocument result = new SolrInputDocument();
        String id = m.get("id").toString();
        for (Map.Entry<String, Object> e : m.entrySet()) {
            String key = e.getKey();
            if (key.equals("type") || key.equals("id")) {
                continue;
            }
            result.addField(key, e.getValue());
        }
        result.addField("id", id);
        return result;
    }
}
