# dukescript.eclipse
compiling dukescript with tycho
"mvn clean verify" should compile with two errors in the generated sources:

1. AllModel.java produces AllData.java with:
```java 
 private AllData applyBindings() {
    throw new IllegalStateException("Please specify targetId=\"\" in your @Model annotation");
  }
```
expected:
```java
 public AllData applyBindings() {
    proto.applyBindings();
    return this;
  }
```
2.  AllModel.java produces AllData.java with:
```java
public int getComputedProp() {
    org.csstudio.java2html.scan.java.util.List<SomeArrayData> arg1 = getSomeArray();
    try {
      proto.acquireLock("computedProp");
      return org.csstudio.java2html.scan.AllModel.computedProp(arg1);
    } finally {
      proto.releaseLock();
    }
```
expected:
```java
  public int getComputedProp() {
    java.util.List<SomeArrayData> arg1 = getSomeArray();
    try {
      proto.acquireLock("computedProp");
      return org.csstudio.java2html.scan.AllModel.computedProp(arg1);
    } finally {
      proto.releaseLock();
    }
  }
```

