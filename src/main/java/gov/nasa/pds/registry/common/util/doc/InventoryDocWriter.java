package gov.nasa.pds.registry.common.util.doc;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.stream.JsonWriter;

import gov.nasa.pds.registry.common.util.LidVidUtils;
import gov.nasa.pds.registry.common.util.json.NJsonDocUtils;


/**
 * Interface to write product references extracted from PDS4 collection inventory files.
 * 
 * @author karpenko
 */
public class InventoryDocWriter implements Closeable
{
    private Logger log;
    private List<String> data;


    public InventoryDocWriter()
    {
        log = LogManager.getLogger(this.getClass());
        data = new ArrayList<>();
    }
    
    
    public List<String> getData()
    {
        return data;
    }
    
    
    public void clearData()
    {
        data.clear();
    }

    
    public void writeBatch(String collectionLidvid, ProdRefsBatch batch, RefType refType, String jobId) throws Exception
    {
        if(collectionLidvid == null) return;
        int idx = collectionLidvid.indexOf("::");
        if(idx <= 0) return;
        
        String collectionLid = collectionLidvid.substring(0, idx);
        String collectionVid = collectionLidvid.substring(idx+2);
        
        String docId = collectionLidvid + "::" + refType.getId() + batch.batchNum;

        // First line: primary key 
        String pkJson = NJsonDocUtils.createPKJson(docId);
        data.add(pkJson);

        // Second line: main document
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        
        jw.beginObject();
        // Batch info
        NJsonDocUtils.writeField(jw, "batch_id", batch.batchNum);
        NJsonDocUtils.writeField(jw, "batch_size", batch.size);

        // Reference type
        NJsonDocUtils.writeField(jw, "reference_type", refType.getLabel());

        // Collection ids
        NJsonDocUtils.writeField(jw, "collection_lidvid", collectionLidvid);
        NJsonDocUtils.writeField(jw, "collection_lid", collectionLid);
        NJsonDocUtils.writeField(jw, "collection_vid", collectionVid);
        
        // Product refs
        NJsonDocUtils.writeArray(jw, "product_lidvid", batch.lidvids);
        
        // Convert lidvids to lids
        Set<String> lids = LidVidUtils.lidvidToLid(batch.lidvids);
        lids = LidVidUtils.add(lids, batch.lids);
        NJsonDocUtils.writeArray(jw, "product_lid", lids);
        
        // Job ID
        NJsonDocUtils.writeField(jw, "_package_id", jobId);
        jw.endObject();
        
        jw.close();
        
        String dataJson = sw.getBuffer().toString();
        data.add(dataJson);
    }


    @Override
    public void close() throws IOException
    {
    }
    
}
