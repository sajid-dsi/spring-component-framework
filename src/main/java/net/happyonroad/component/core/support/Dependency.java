/**
 * @author XiongJie, Date: 13-8-29
 */
package net.happyonroad.component.core.support;

import net.happyonroad.component.core.Component;
import net.happyonroad.component.core.Versionize;
import net.happyonroad.component.core.exception.InvalidComponentNameException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版本依赖
 * TODO 支持版本表达式，如 [0.1.0,2.1.0)
 */
public class Dependency implements Versionize{

    private static Pattern digitPattern = Pattern.compile("(^\\d+)(.+)?");
    private static Pattern typePattern = Pattern.compile("\\.(jar|rar|war|ear|pom)$");
    private static Pattern versionPattern = Pattern.compile("^\\d+(\\.\\d+\\w*)*");

    private String groupId;

    private String artifactId;

    private String version;//核心版本，如: 1.0.0

    private String type;//pom or jar, war, rar, and so on

    private String classifier;//snapshot, release, javadoc, source, java4, java5, java6, or other feature...

    private String scope;//See Component.SCOPE_XXX

    private Boolean optional;

    private List<Exclusion> exclusions;

    public Dependency() {
    }

    public Dependency(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        if(artifactId.indexOf(".") > 0 ){
            int position = artifactId.lastIndexOf('.');
            this.artifactId = artifactId.substring(position+1);
            this.groupId = groupId + "." + artifactId.substring(0, position);
        }
        setVersion(version);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    public boolean hasClassifier() {
        return classifier != null && !"".equalsIgnoreCase(classifier.trim());
    }

    public String getType() {
        return type;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setClassifier(String classifier) {
        if(classifier == null || classifier.length() == 0)
            this.classifier = null;
        else
            this.classifier = classifier;
    }

    public void setVersion(String version) {
        if(version != null ){
            String[] versionAndClassifier = splitClassifierFromVersion(version, new StringBuilder());
            this.version = versionAndClassifier[0];
            this.setClassifier(versionAndClassifier[1]);
        }
    }


    public boolean isOptional() {
        if(optional == null ) return false;
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isTest(){
        return Component.SCOPE_TEST.equalsIgnoreCase(getScope());
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isRuntime(){
        return Component.SCOPE_RUNTIME.equalsIgnoreCase(getScope());
    }

    public boolean isCompile(){
        return Component.SCOPE_COMPILE.equalsIgnoreCase(getScope()) || getScope() == null;
    }

    public List<Exclusion> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<Exclusion> exclusions) {
        this.exclusions = exclusions;
    }

    @Override
    public String toString() {
        String gs = groupId == null ? "<undefined>" : groupId;
        String as = artifactId == null ? "<undefined>" : artifactId;
        String vs = version != null && version.length() > 0 ? "-" + version : "-<*>";
        String cs = classifier != null && classifier.length() > 0 ? "-" + classifier : "";
        String ts = type != null && type.length() > 0 ? "." + type : "";
        return String.format("%s.%s%s%s%s", gs, as, vs, cs, ts);
    }

    /**
     * 根据文件全名称解析依赖信息
     *
     * @param fullName 全名称，形如： groupId.artifactId-version.type
     * @return 依赖信息
     */
    public static Dependency parse(String fullName) throws InvalidComponentNameException {
        Dependency dependency = new Dependency();
        String path;

        //假设给定的fullName为: org.apache.maven-1.0.0.R1-SNAPSHOT-PDM.pom
        //  处理后缀
        Matcher matcher = typePattern.matcher(fullName);
        if (matcher.find()) {
            dependency.setType(matcher.group(1));
            path = fullName.substring(0, fullName.length() - 4);
        } else if (fullName.endsWith(".pom")) {
            path = fullName.substring(0, fullName.length() - 8);
        } else {
            path = fullName;
        }
        //  -> path = org.apache.maven-1.0.0.R1-SNAPSHOT-PDM

        //  分离group.artifact与版本信息
        String[] nameAndVersion = path.split("-");
        if (nameAndVersion.length < 2) {
            throw new InvalidComponentNameException("The component full name [" + fullName + "] should be format as: " +
                                                            "$groupId.$artifactId-$version(.$classifier.$type)?");
        }
        int versionPos = 0;
        for(; versionPos < nameAndVersion.length; versionPos++){
            String string = nameAndVersion[versionPos];
            if(versionPattern.matcher(string).find())break;
        }
        if(versionPos == 0 || versionPos == nameAndVersion.length)
            throw new InvalidComponentNameException("The component full name [" + fullName + "] should be format as: " +
                                                            "$groupId.$artifactId-$version(.$classifier.$type)?");
        StringBuilder name = new StringBuilder();
        for(int i=0;i<versionPos;i++){
            name.append(nameAndVersion[i]).append("-");
        }
        removeLastChar(name);
        String version = nameAndVersion[versionPos];
        StringBuilder classifier = new StringBuilder();
        for (int i = versionPos+1; i < nameAndVersion.length; i++) {
            classifier.append(nameAndVersion[i]).append("-");
        }
        removeLastChar(classifier);
        //  -> name = org.apache.maven
        //  -> version = 1.0.0.SNAPSHOT.R1
        //  -> classifier = SNAPSHOT-PDM

        // 分离groupId与artifactId
        String[] groupAndArtifact = name.toString().split("\\.");
        if (groupAndArtifact.length < 2) {
            throw new InvalidComponentNameException("The component full name [" + fullName + "] should be format as: " +
                                                            "$groupId.$artifactId-$version(.$classifier.$type)?");
        }
        StringBuilder groupId = new StringBuilder();
        for (int i = 0; i < groupAndArtifact.length - 1; i++) {
            String pkg = groupAndArtifact[i];
            groupId.append(pkg).append(".");
        }
        groupId.deleteCharAt(groupId.length() - 1);
        dependency.setGroupId(groupId.toString());
        dependency.setArtifactId(groupAndArtifact[groupAndArtifact.length - 1]);
        //  -> groupId = org.apache
        //  -> artifactId = maven
        String[] versionAndClassifier = splitClassifierFromVersion(version, classifier);
        dependency.setVersion(versionAndClassifier[0]);
        dependency.setClassifier(versionAndClassifier[1]);
        //  -> pureVersion = 1.0.0
        //  -> classifier = R1-SNAPSHOT-PDM
        return dependency;
    }

    private static void removeLastChar(StringBuilder string) {
        if (string.length() > 0)
            string.deleteCharAt(string.length() - 1);
    }

    static String[] splitClassifierFromVersion(String version, StringBuilder classifier) {
        // 分离version中的classifier，注意，version并不总是 1.0.0这样，可能：
        //   1.1 -> 1.1.0
        //   甚至1.1.2.3这种形式
        //   我认为，暂时不应该有简化到只有一个数字的版本号，如：1
        String[] versions = version.split("\\.");
        StringBuilder pureVersion = new StringBuilder();
        int i = 0;
        for (; i < versions.length; i++) {
            String v = versions[i];
            Matcher dm = digitPattern.matcher(v);
            if (dm.matches()) {
                pureVersion.append(dm.group(1)).append(".");
                String closingClassifier = dm.group(2);
                if(closingClassifier != null){
                    if(closingClassifier.startsWith("-")){
                        closingClassifier = closingClassifier.substring(1, closingClassifier.length());
                    }
                    if(classifier.length() > 0 ){
                        classifier.insert(0, closingClassifier + "-");
                    }else{
                        classifier.append(closingClassifier);
                    }
                    i = versions.length;
                    break;
                }
            } else {
                break;
            }
        }
        removeLastChar(pureVersion);
        // pureVersion = 1.0.0
        StringBuilder classifierInVersion = new StringBuilder();
        for (; i < versions.length; i++) {
            String c = versions[i];
            classifierInVersion.append(c).append(".");
        }
        removeLastChar(classifierInVersion);
        if (classifierInVersion.length() > 0) {
            boolean separate = (classifier.length() > 0);
            classifier.insert(0, classifierInVersion);
            if (separate)
                classifier.insert(classifierInVersion.length(), "-");
        }
        return new String[]{pureVersion.toString(), classifier.toString()};
    }

    /**
     * 判断某个组件是否满足特定依赖信息
     * <pre>
     * 如， dependency1 = {groupId: dnt, artifactId: component}
     *     dependency2 = {groupId: dnt, artifactId: component, version: 1.0.0}
     *     那么: dependency1.accept(dependency2) = true
     *     反之: dependency2.accept(dependency1) = false
     * </pre>
     * @param componentOrDependency 被判断的组件或者其他依赖
     * @return 是否依赖
     */
    public boolean accept(Versionize componentOrDependency) {
        boolean accept = getGroupId().equals(componentOrDependency.getGroupId()) && getArtifactId().equals(componentOrDependency.getArtifactId());
        if (!accept) return false;
        if (getVersion() != null) {
            //有version需要，暂时不知道是否需要支持version的表达式，maven自身好像没有这个机制
            accept = getVersion().equals(componentOrDependency.getVersion());
            if (!accept) return false;
        }
        if( getClassifier() != null ){
            accept = getClassifier().equals(componentOrDependency.getClassifier());
            if (!accept) return false;
        }
/*
        if( getType() != null ){
            accept = getType().equals(componentOrDependency.getType());
            if (!accept) return false;
        }
*/
        return true;
    }

    public boolean exclude(Versionize componentOrDependency){
        if( exclusions == null || exclusions.isEmpty()) return false;
        for (Exclusion exclusion : exclusions) {
            if(exclusion.cover(componentOrDependency)) return true;
        }
        return false;
    }

    public boolean conflict(Dependency dependency){
        return getGroupId().equals(dependency.getGroupId()) &&
                getArtifactId().equals(dependency.getArtifactId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Dependency)) {
            return false;
        }

        Dependency that = (Dependency) o;

        if (!artifactId.equals(that.artifactId)) {
            return false;
        }
        if (!groupId.equals(that.groupId)) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    /**
     * 为某个依赖补齐信息
     * @param parentDefined 父组件中定义的依赖信息
     */
    public void merge(Dependency parentDefined) {
        if(optional == null ){
            setOptional(parentDefined.isOptional());
        }
        if(version == null){
            setVersion(parentDefined.getVersion());
        }
        if(classifier == null ){
            setClassifier(parentDefined.getClassifier());
        }
        if(scope == null ){
            setScope(parentDefined.getScope());
        }
        if(exclusions == null ){
            setExclusions(parentDefined.getExclusions());
        }
    }

    /**
     * 根据组件的上下文，替换依赖中的信息
     * @param component 所属的组件
     */
    public void interpolate(DefaultComponent component) {
        if(groupId != null) this.groupId = component.interpolate(groupId);
        if(artifactId != null) this.artifactId = component.interpolate(artifactId);
        //暂时主要处理一些启动相关的关键属性，如version
        if(version != null ) this.setVersion(component.interpolate(version)) ;
        if(classifier != null ) this.classifier = component.interpolate(classifier);
        if(scope != null ) this.scope = component.interpolate(scope);
    }

    public void reform() {
        if(artifactId.indexOf(".") > 0 ){
            String badArtifactId = this.artifactId;
            int position = badArtifactId.lastIndexOf('.');
            this.artifactId = badArtifactId.substring(position+1);
            this.groupId = groupId + "." + badArtifactId.substring(0, position);
        }
    }
}
