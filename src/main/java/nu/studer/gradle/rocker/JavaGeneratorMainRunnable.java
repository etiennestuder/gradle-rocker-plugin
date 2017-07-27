package nu.studer.gradle.rocker;

import org.gradle.api.GradleException;

import java.lang.reflect.Method;

/**
 * This class is necessary as JavaGeneratorMain doesn't implement Runnable which is required by
 * Gradle Worker API.
 *
 * */
public class JavaGeneratorMainRunnable implements Runnable {
    @Override
    public void run() {
        try {
            Class<?> javaGeneratorClass = getClass().getClassLoader().loadClass("com.fizzed.rocker.compiler.JavaGeneratorMain");
            Method main = javaGeneratorClass.getMethod("main", String[].class);
            main.invoke(null, new Object[] {new String[0]});
        } catch (Exception e) {
            throw new GradleException("Rocker template compilation failed: " + e.getMessage(), e);
        }
    }
}