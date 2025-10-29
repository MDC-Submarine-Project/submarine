package dev.senj.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;

public class ObjectListenerTest {

    // A test class that is not Position
    static class TestObject implements Serializable {
        public int value;
        public String name;
        
        public TestObject() {
            this.value = 0;
            this.name = "default";
        }
        
        @Override
        public String toString() {
            return "TestObject{value=" + value + ", name='" + name + "'}";
        }
    }
    
    @Test
    public void testCopyFields() throws Exception {
        // Get the ObjectListener instance
        ObjectListener listener = ObjectListener.getInstance();
        
        // Create a test object
        TestObject source = new TestObject();
        source.value = 42;
        source.name = "test";
        
        // Create a destination object
        TestObject destination = new TestObject();
        
        // Use reflection to access the private copyFields method
        java.lang.reflect.Method copyFieldsMethod = ObjectListener.class.getDeclaredMethod("copyFields", Object.class, Object.class);
        copyFieldsMethod.setAccessible(true);
        
        // Call the copyFields method
        boolean result = (boolean) copyFieldsMethod.invoke(listener, source, destination);
        
        // Verify the result
        assertTrue(result, "copyFields should return true");
        assertEquals(42, destination.value, "value should be copied");
        assertEquals("test", destination.name, "name should be copied");
        
        System.out.println("[DEBUG_LOG] copyFields test passed");
    }
    
    @Test
    public void testCreateInstance() throws Exception {
        // Get the ObjectListener instance
        ObjectListener listener = ObjectListener.getInstance();
        
        // Use reflection to access the private createInstance method
        java.lang.reflect.Method createInstanceMethod = ObjectListener.class.getDeclaredMethod("createInstance", Class.class);
        createInstanceMethod.setAccessible(true);
        
        // Call the createInstance method
        Object instance = createInstanceMethod.invoke(listener, TestObject.class);
        
        // Verify the result
        assertNotNull(instance, "createInstance should return a non-null object");
        assertTrue(instance instanceof TestObject, "instance should be a TestObject");
        
        System.out.println("[DEBUG_LOG] createInstance test passed");
    }
    
    @Test
    public void testPositionUpdate() throws Exception {
        // Get the ObjectListener instance
        ObjectListener listener = ObjectListener.getInstance();
        
        // Create a Position object
        Position source = new Position();
        source.x = 1.0f;
        source.y = 2.0f;
        source.z = 3.0f;
        
        // Create a destination Position object
        Position destination = new Position();
        
        // Use reflection to access the private copyFields method
        java.lang.reflect.Method copyFieldsMethod = ObjectListener.class.getDeclaredMethod("copyFields", Object.class, Object.class);
        copyFieldsMethod.setAccessible(true);
        
        // Call the copyFields method
        boolean result = (boolean) copyFieldsMethod.invoke(listener, source, destination);
        
        // Verify the result
        assertTrue(result, "copyFields should return true");
        assertEquals(1.0f, destination.x, 0.001f, "x should be copied");
        assertEquals(2.0f, destination.y, 0.001f, "y should be copied");
        assertEquals(3.0f, destination.z, 0.001f, "z should be copied");
        
        System.out.println("[DEBUG_LOG] Position update test passed");
    }
}