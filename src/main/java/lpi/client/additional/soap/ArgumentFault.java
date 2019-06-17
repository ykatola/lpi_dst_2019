
package lpi.client.additional.soap;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "ArgumentFault", targetNamespace = "http://soap.server.lpi/")
public class ArgumentFault
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private ArgumentException faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public ArgumentFault(String message, ArgumentException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public ArgumentFault(String message, ArgumentException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: lpi.server.soap.ArgumentException
     */
    public ArgumentException getFaultInfo() {
        return faultInfo;
    }

}
