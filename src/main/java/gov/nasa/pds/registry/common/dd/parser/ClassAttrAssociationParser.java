package gov.nasa.pds.registry.common.dd.parser;

import java.io.File;
import com.google.gson.stream.JsonToken;


/**
 * PDS LDD JSON file parser. 
 * Parses "dataDictionary" -&gt; "classDictionary" subtree and extracts attribute associations 
 * ("class" -&gt; "association" -&gt; "isAttribute" == true).
 * For each "attributeId" a callback method is called.
 * 
 * @author karpenko
 */
public class ClassAttrAssociationParser extends BaseLddParser
{
    /**
     * Callback interface 
     * @author karpenko
     */
    public static interface Callback
    {
        /**
         * This method is called for each "attributeId" from class attribute association
         * ("class" -&gt; "association" -&gt; "isAttribute" == true).
         * @param classNs class namespace
         * @param className class name
         * @param attrId attribute ID
         * @throws Exception an exception
         */
        public void onAssociation(String classNs, String className, String attrId) throws Exception;
    }
    
    ////////////////////////////////////////////////////////////////////////
    
    
    private Callback cb;
    private int itemCount;

    private String classNs;
    private String className;
    
    
    /**
     * Constructor
     * @param file PDS LDD JSON file
     * @param cb Callback
     * @throws Exception an exception
     */
    public ClassAttrAssociationParser(File file, Callback cb) throws Exception
    {
        super(file);
        this.cb = cb;
    }

    
    @Override
    protected void parseClassDictionary() throws Exception
    {
        jsonReader.beginArray();
        
        while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_ARRAY)
        {
            jsonReader.beginObject();

            while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT)
            {
                String name = jsonReader.nextName();
                if("class".equals(name))
                {
                    parseClass();
                }
                else
                {
                    jsonReader.skipValue();
                }
            }
            
            jsonReader.endObject();
        }
        
        jsonReader.endArray();
    }


    private void parseClass() throws Exception
    {
        itemCount++;
        classNs = null;
        className = null;
        
        jsonReader.beginObject();
        
        while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT)
        {
            String name = jsonReader.nextName();
            if("identifier".equals(name))
            {
                String id = jsonReader.nextString();
                
                String tokens[] = id.split("\\.");
                if(tokens.length >= 3)
                {
                    classNs = tokens[tokens.length-2];
                    className = tokens[tokens.length-1];
                }
                else
                {
                    throw new Exception("Could not parse class identifier " + id);
                }
            }
            else if("associationList".equals(name))
            {
                parseAssocList();
            }
            else
            {
                jsonReader.skipValue();
            }
        }
        
        jsonReader.endObject();
        
        if(className == null)
        {
            String msg = "Missing identifier in class definition. Index = " + itemCount;
            throw new Exception(msg);
        }
    }


    private void parseAssocList() throws Exception
    {
        jsonReader.beginArray();
        
        while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_ARRAY)
        {
            jsonReader.beginObject();

            while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT)
            {
                String name = jsonReader.nextName();
                if("association".equals(name))
                {
                    parseAssoc();
                }
                else
                {
                    jsonReader.skipValue();
                }
            }
            
            jsonReader.endObject();
        }
        
        jsonReader.endArray();
    }

    
    private void parseAssoc() throws Exception
    {
        // The LDD JSON format uses "identifier" (a string) and "isAttribute" (a string "true"/"false")
        // as sibling fields on each association object. Both must be read before we can decide
        // whether to emit a callback, so we buffer them and act after the object is fully consumed.
        String attrId = null;
        boolean isAttribute = false;

        jsonReader.beginObject();

        while(jsonReader.hasNext() && jsonReader.peek() != JsonToken.END_OBJECT)
        {
            String name = jsonReader.nextName();
            if("identifier".equals(name))
            {
                attrId = jsonReader.nextString();
            }
            else if("isAttribute".equals(name))
            {
                isAttribute = "true".equals(jsonReader.nextString());
            }
            else
            {
                jsonReader.skipValue();
            }
        }

        jsonReader.endObject();

        if(isAttribute && attrId != null)
        {
            cb.onAssociation(classNs, className, attrId);
        }
    }
    
}
