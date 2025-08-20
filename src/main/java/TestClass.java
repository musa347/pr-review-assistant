public class TestClass {
    public void testMethod() {
        String unused = "test"; // This should trigger checkstyle
        System.out.println("Hello World");
    }
}
