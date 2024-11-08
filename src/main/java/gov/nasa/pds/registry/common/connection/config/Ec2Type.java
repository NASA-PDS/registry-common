//
// This file was generated by the Eclipse Implementation of JAXB, v4.0.3 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
//


package gov.nasa.pds.registry.common.connection.config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         EC2 instances that have their IAM roles setup to match the cognito
 *         information when instantiated can use TCP/IP to share their credentials
 *         directly with their subprocess. They have no need to contact cognito
 *         nor the application gateway. They need only get the credentials from the
 *         EC2 instance.
 * 
 *         The value of this element should be the URL. For instance, these two
 *         URLs would normally resolve the same:
 * 
 *         https://localhost:54321/AWS_CONTAINER_CREDENTIALS_RELATIVE_URI
 *         https://127.0.0.1:54321/AWS_CONTAINER_CREDENTIALS_RELATIVE_URI
 *       
 * 
 * <p>Java class for ec2_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>{@code
 * <complexType name="ec2_type">
 *   <simpleContent>
 *     <extension base="<http://www.w3.org/2001/XMLSchema>normalizedString">
 *       <attribute name="endpoint" type="{http://www.w3.org/2001/XMLSchema}normalizedString" default="https://p5qmxrldysl1gy759hqf.us-west-2.aoss.amazonaws.com" />
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ec2_type", propOrder = {
    "value"
})
public class Ec2Type {

    @XmlValue
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String value;
    @XmlAttribute(name = "endpoint")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String endpoint;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the endpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpoint() {
        if (endpoint == null) {
            return "https://p5qmxrldysl1gy759hqf.us-west-2.aoss.amazonaws.com";
        } else {
            return endpoint;
        }
    }

    /**
     * Sets the value of the endpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
    }

}