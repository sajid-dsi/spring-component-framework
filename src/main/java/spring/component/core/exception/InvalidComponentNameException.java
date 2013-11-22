/**
 * @author XiongJie, Date: 13-9-3
 */
package spring.component.core.exception;

import spring.component.core.ComponentException;

/**
 * 组件名称错误
 */
public class InvalidComponentNameException extends ComponentException {
    public InvalidComponentNameException(String s) {
        super(s);
    }
}
