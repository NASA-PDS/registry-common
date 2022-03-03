package gov.nasa.pds.registry.common.mq.msg;

import java.util.List;
import java.util.Set;

/**
 * Harvest job message sent to RabbitMQ.
 * @author karpenko
 *
 */
public class JobMessage
{
    /**
     * Harvest job ID. Autogenerated UUID.
     */
    public String jobId;
    
    /**
     * PDS node name, such as "PDS_ENG".
     */
    public String nodeName;
    
    /**
     * List of directories to process
     */
    public List<String> dirs;
    
    /**
     * List of manifest files (file lists) to process
     */
    public List<String> manifests;

    /**
     * Product class filter. List of classes to include.
     */
    public Set<String> prodClassInclude;
    
    /**
     * Product class filter. List of classes to exclude.
     */
    public Set<String> prodClassExclude;
    
    /**
     * List of rules to generate file references.
     */
    public List<String> fileRefs;

    /**
     * Overwrite existing products in the Registry (Elasticsearch) if set to true.
     */
    public boolean overwrite = false;
}
