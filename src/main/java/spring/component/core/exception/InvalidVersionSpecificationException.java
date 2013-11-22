/**
 * @author XiongJie, Date: 13-9-3
 */
package spring.component.core.exception;

import spring.component.core.ComponentException;

/**
 * 版本错误异常
 */
public class InvalidVersionSpecificationException extends ComponentException {

    public InvalidVersionSpecificationException( String message )
    {
        super( message );
    }
}

