/**
 * @author XiongJie, Date: 13-9-13
 */
package net.happyonroad.component.core.support;

import junit.framework.TestCase;

/**
 * 测试Dependency对象
 */
public class DependencyTest extends TestCase {

    // ------------------------------------------------------------
    // 测试 Dependency.parse(String fullName)
    // ------------------------------------------------------------

    /**
     * 测试目的：
     *   测试不带后缀的 groupId.artifactId-version是否可以解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置
     * @throws Exception
     */
    public void testParseNameWithoutType() throws Exception {
        Dependency dependency = Dependency.parse("net.happyonroad.component.artifact-1.0.0");
        assertEquals("net.happyonroad.component", dependency.getGroupId());
        assertEquals("artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertNull(dependency.getType());
        assertNull(dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试带.jar后缀的 groupId.artifactId-version.jar 是否可以解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置，type也被正确设置
     * @throws Exception
     */
    public void testParseNameWithDotJar() throws Exception {
        Dependency dependency = Dependency.parse("net.happyonroad.component.artifact-1.0.0.jar");
        assertEquals("net.happyonroad.component", dependency.getGroupId());
        assertEquals("artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals("jar", dependency.getType());
        assertNull(dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试带.pom后缀的 groupId.artifactId-version.pom是否可以解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置，type没有被设置
     * @throws Exception
     */
    public void testParseNameWithDotPom() throws Exception {
        Dependency dependency = Dependency.parse("net.happyonroad.component.artifact-1.0.0.pom");
        assertEquals("net.happyonroad.component", dependency.getGroupId());
        assertEquals("artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals("pom", dependency.getType());
        assertNull(dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试带有Classifier后缀的 groupId.artifactId-Version.classifier.jar|pom.xml是否可以解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置，classifier也被正确设置
     * @throws Exception
     */
    public void testParseNameWithClassifier() throws Exception {
        Dependency dependency = Dependency.parse("net.happyonroad.component.artifact-1.0.0.RELEASE.pom");
        assertEquals("net.happyonroad.component", dependency.getGroupId());
        assertEquals("artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals("pom", dependency.getType());
        assertEquals("RELEASE", dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试带有Classifier后缀，且用-分割的 groupId.artifactId-Version-classifierPart1-classifierPart2.jar|pom.xml是否可以解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置，classifier也被正确设置
     * @throws Exception
     */
    public void testParseNameWithClassifierSeparatedBySlash() throws Exception {
        Dependency dependency = Dependency.parse("net.happyonroad.component.artifact-1.0.0-RELEASE-Rc1.pom");
        assertEquals("net.happyonroad.component", dependency.getGroupId());
        assertEquals("artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals("pom", dependency.getType());
        assertEquals("RELEASE-Rc1", dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试带有Classifier后缀，且用-分割的 groupId.artifactId-Version.classifierPart1-classifierPart2.jar|pom.xml是否可以解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置，classifier也被正确设置
     * @throws Exception
     */
    public void testParseNameWithClassifierSeparatedByDot() throws Exception {
        Dependency dependency = Dependency.parse("net.happyonroad.component.artifact-1.0.0.RELEASE-Rc1.pom");
        assertEquals("net.happyonroad.component", dependency.getGroupId());
        assertEquals("artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals("pom", dependency.getType());
        assertEquals("RELEASE-Rc1", dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试group，artifact id上面带有-号的名称可以正确解析
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置，classifier也被正确设置
     * @throws Exception
     */
    public void testParseNameWithSlashInGroupOrArtifactName() throws Exception {
        Dependency dependency = Dependency.parse("dnt-soft.new-artifact-1.0.0.RELEASE-Rc1.pom");
        assertEquals("dnt-soft", dependency.getGroupId());
        assertEquals("new-artifact", dependency.getArtifactId());
        assertEquals("1.0.0", dependency.getVersion());
        assertEquals("pom", dependency.getType());
        assertEquals("RELEASE-Rc1", dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试 dnt-soft.new-artifact-1.0.jar 这种具有四位长度的文件名称的能解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置
     * @throws Exception
     */
    public void testParseNameWithTwoDigits() throws Exception {
        Dependency dependency = Dependency.parse("dnt-soft.new-artifact-1.0.jar");
        assertEquals("dnt-soft", dependency.getGroupId());
        assertEquals("new-artifact", dependency.getArtifactId());
        assertEquals("1.0", dependency.getVersion());
        assertEquals("jar", dependency.getType());
        assertNull(dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试 dnt-soft.new-artifact-1.0.0.1.jar 这种具有四位长度的文件名称的能解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version被正确设置
     * @throws Exception
     */
    public void testParseNameWithFourDigits() throws Exception {
        Dependency dependency = Dependency.parse("dnt-soft.new-artifact-1.0.0.1.jar");
        assertEquals("dnt-soft", dependency.getGroupId());
        assertEquals("new-artifact", dependency.getArtifactId());
        assertEquals("1.0.0.1", dependency.getVersion());
        assertEquals("jar", dependency.getType());
        assertNull(dependency.getClassifier());
    }

    /**
     * 测试目的：
     *   测试 dnt-soft.new-artifact-1.2.4c.jar 这种具有最后一位数字后面带有classifier的依赖能解析成功
     * 验证方式：
     *   解析出来，groupId, artifactId, version, classifier被正确设置
     * @throws Exception
     */
    public void testParseNameWithClosingClassifier() throws Exception {
        Dependency dependency = Dependency.parse("dnt-soft.new-artifact-1.2.4c.jar");
        assertEquals("dnt-soft", dependency.getGroupId());
        assertEquals("new-artifact", dependency.getArtifactId());
        assertEquals("1.2.4", dependency.getVersion());
        assertEquals("c", dependency.getClassifier());
        assertEquals("jar", dependency.getType());
    }

    /**
     * 测试目的：
     *   测试 直接从pom文件以 new Dependency(group, artifact,version)方式构建时，version中带上了classifier时
     *   classifier能够正常的被分离出来
     * 验证方式：
     *   解析出来，groupId, artifactId, version, classifier被正确设置
     * @throws Exception
     */
     public void testNewDependencyByVersionWithClassifier()throws Exception{
         Dependency dependency = new Dependency("org.springframework", "spring-context", "3.2.4.RELEASE");
         assertEquals("org.springframework", dependency.getGroupId());
         assertEquals("spring-context", dependency.getArtifactId());
         assertEquals("3.2.4", dependency.getVersion());
         assertEquals("RELEASE", dependency.getClassifier());
     }

    /**
     * 测试目的：
     *   对于在artifact中也包含点的组件，能够将其artifactId中的信息转移到groupId中
     * 验证方式：
     *   解析出来，groupId, artifactId, version, classifier被正确设置
     * @throws Exception
     */
    public void testConvertArtifactWithDotAsNormal() throws Exception {
        Dependency dependency = new Dependency("javax.servlet", "javax.servlet-api", "3.1.0");
        assertEquals("javax.servlet.javax", dependency.getGroupId());
        assertEquals("servlet-api", dependency.getArtifactId());
        assertEquals("3.1.0", dependency.getVersion());
    }
}
