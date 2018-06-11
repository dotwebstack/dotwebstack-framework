package org.dotwebstack.framework.frontend.soap.wsdlreader;

import java.util.Set;
import javax.xml.namespace.QName;

/**
 * @author Tom Bujok
 * @since 1.0.0
 */
public interface SoapMultiValuesProvider {

  Set<String> getMultiValues(QName name);

}
