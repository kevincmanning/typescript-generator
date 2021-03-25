
package cz.habarta.typescript.generator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


@SuppressWarnings("unused")
public class InputTest {

    @Test
    public void testScanner() {
        final ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages("cz.habarta").scan();
        final List<String> allClassNames = scanResult.getAllClasses().getNames();
        final List<String> testClassNames = Input.filterClassNames(allClassNames, Arrays.asList("cz.habarta.typescript.generator.**Test"));
        Assert.assertTrue("Typescript-generator must have at least 20 tests :-)", testClassNames.size() > 20);
    }

    @Test
    public void testMatches() {
        final List<String> result1 = Input.filterClassNames(
                Arrays.asList(
                        "com.example.Json",
                        "com.example.AAAJson",
                        "com.example.AAA",
                        "com.example.aaa$Json"
                ),
                Arrays.asList("**Json")
        );
        Assert.assertTrue(result1.contains("com.example.Json"));
        Assert.assertTrue(result1.contains("com.example.AAAJson"));
        Assert.assertTrue(!result1.contains("com.example.AAA"));
        Assert.assertTrue(result1.contains("com.example.aaa$Json"));

        final List<String> result2 = Input.filterClassNames(
                Arrays.asList(
                        "com.example.Json",
                        "cz.habarta.test.Json",
                        "cz.habarta.test.BBBJson",
                        "cz.habarta.test.aaa.BBBJson",
                        "cz.habarta.test.CCC$Json"
                ),
                Arrays.asList("cz.habarta.test.*")
        );
        Assert.assertTrue(!result2.contains("com.example.Json"));
        Assert.assertTrue(result2.contains("cz.habarta.test.Json"));
        Assert.assertTrue(result2.contains("cz.habarta.test.BBBJson"));
        Assert.assertTrue(!result2.contains("cz.habarta.test.aaa.BBBJson"));
        Assert.assertTrue(!result2.contains("cz.habarta.test.CCC$Json"));

        final List<String> result3 = Input.filterClassNames(
                Arrays.asList(
                        "cz.habarta.test.BBBJson",
                        "cz.habarta.ddd.CCC$Json",
                        "cz.habarta.CCC$Json"
                ),
                Arrays.asList("cz.habarta.*.*$*")
        );
        Assert.assertTrue(!result3.contains("cz.habarta.test.BBBJson"));
        Assert.assertTrue(result3.contains("cz.habarta.ddd.CCC$Json"));
        Assert.assertTrue(!result3.contains("cz.habarta.CCC$Json"));
    }

    @Test
    public void testClassesWithAnnotations() {
        final Input.Parameters parameters = new Input.Parameters();
        parameters.classesWithAnnotations = Arrays.asList(MyJsonClass.class.getName());
        parameters.scanningAcceptedPackages = Arrays.asList("cz.habarta");
        final String output = new TypeScriptGenerator(TestUtils.settings()).generateTypeScript(Input.from(parameters));
        Assert.assertTrue(output.contains("name: string;"));
    }

    @Test
    public void testClassesImplementingInterfaces() {
        final Input.Parameters parameters = new Input.Parameters();
        parameters.classesImplementingInterfaces = Arrays.asList(MyJsonInterface.class.getName());
        final String output = new TypeScriptGenerator(TestUtils.settings()).generateTypeScript(Input.from(parameters));
        Assert.assertTrue(output.contains("firstName: string;"));
        Assert.assertTrue(output.contains("lastName: string;"));
    }

    @Test
    public void testClassesExtendingClasses() {
        final Input.Parameters parameters = new Input.Parameters();
        parameters.classesExtendingClasses = Arrays.asList(MyJsonInterfaceImpl.class.getName());
        final String output = new TypeScriptGenerator(TestUtils.settings()).generateTypeScript(Input.from(parameters));
        Assert.assertTrue(output.contains("lastName: string;"));
    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface MyJsonClass {
    }

    private interface MyJsonInterface {
    }

    private static class MyJsonInterfaceImpl implements MyJsonInterface {
        public String firstName;
    }

    private static class MyJsonInterfaceSubclass extends MyJsonInterfaceImpl {
        public String lastName;
    }

    @MyJsonClass
    private static class MyData {
        public String name;
    }

}
