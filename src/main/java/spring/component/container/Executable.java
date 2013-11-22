/**
 * @author XiongJie, Date: 13-11-4
 */

package spring.component.container;

/** 可以执行特定命令的服务进程接口 */
public interface Executable {
    void start() throws Exception;

    void exit();

    void reload();
}
