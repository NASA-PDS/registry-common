//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.3 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package gov.nasa.pds.registry.common.cfg;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         Describe the prefix of the filename to replace with new content.
 * 
 *         @replacePrefix: the string to find in the filename starting with the
 *                         first character of the filename (no regex or globbing)
 *         @with: the replacement content
 * 
 *         Example:
 *            filename = /home/username/pdsfiles/someproduct.xml
 *            replacePrefix = /home/username/pdsfiles
 *            with = https://public.com/pdsfiles/root
 * 
 *            After this fileRef is used, filename would be:
 *               https://public.com/pdsfiles/root/someproduct.xml
 *       
 * 
 * <p>Java class for file_ref_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>{@code
 * <complexType name="file_ref_type">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="replacePrefix" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *       <attribute name="with" use="required" type="{http://www.w3.org/2001/XMLSchema}normalizedString" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "file_ref_type")
public class FileRefType {

    @XmlAttribute(name = "replacePrefix", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String replacePrefix;
    @XmlAttribute(name = "with", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String with;

    /**
     * Gets the value of the replacePrefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplacePrefix() {
        return replacePrefix;
    }

    /**
     * Sets the value of the replacePrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplacePrefix(String value) {
        this.replacePrefix = value;
    }

    /**
     * Gets the value of the with property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWith() {
        return with;
    }

    /**
     * Sets the value of the with property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWith(String value) {
        this.with = value;
    }

}
