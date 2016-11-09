package org.nlpcn.jcoder.run.java;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class ClassFileManager extends
        ForwardingJavaFileManager {
 
    private List<JavaClassObject> javaClassObjectList;
 
    public ClassFileManager(StandardJavaFileManager
            standardManager) {
        super(standardManager);
        this.javaClassObjectList = new ArrayList<JavaClassObject>();
    }
 
    public JavaClassObject getMainJavaClassObject() {
        if (this.javaClassObjectList != null && this.javaClassObjectList.size() > 0) {
            int size = this.javaClassObjectList.size();
            return this.javaClassObjectList.get((size - 1));
        }
        return null;
    }
 
    public List<JavaClassObject> getInnerClassJavaClassObject() {
        if (this.javaClassObjectList != null && this.javaClassObjectList.size() > 0) {
            int size = this.javaClassObjectList.size();
            if (size == 1) {
                return null;
            }
            return this.javaClassObjectList.subList(0, size - 1);
        }
        return null;
    }
 
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className, JavaFileObject.Kind kind, FileObject sibling)
            throws IOException {
        JavaClassObject jclassObject = new JavaClassObject(className, kind);
        this.javaClassObjectList.add(jclassObject);
        return jclassObject;
    }
}