# Huava-Compiler
Java-like compiler called **Huava**, created as a college project.

## Usage
- Install maven.
- Compile using maven.  
```mvn package```
- Execute  
 ```java -jar target/compiler-0.0.7.jar [ --encoding <name> ] [ --generate ] <inputfile(s)>```       
**or**  
```java -cp target/compiler-0.0.7.jar Compiler [ --encoding <name> ] [ --generate ] <inputfile(s)>```
- Deafult encoding of the input files is UTF-8.  
This can be easily changed, using the ```--encoding``` argument.  
- Using the ```--generate``` argument, huava class files can be generated, in folder ```huaclasses```.  
Output class type is ```.huaclass```.
- Input Files must be of type ```.huava```.  
The input files must be given to the compiler in the right order.  
For example, a class cannot be used before being defined.  
Therefore, the file containing the class definition must be given as a parameter, before a second file using that class.
- ```FinalTestFiles``` folder contains some files making up a program, in order to test the Compiler.

## Example
```java
java -jar target\compiler-0.0.7.jar FinalTestFiles/test1.huava FinalTestFiles/test2.huava FinalTestFiles/test3.huava
22:43:08.754 [main] INFO  org.hua.Compiler - Scanning file FinalTestFiles/test1.huava
22:43:08.854 [main] INFO  org.hua.Compiler - Constructed AST
22:43:08.854 [main] INFO  org.hua.Compiler - Building symbol table
22:43:08.859 [main] INFO  org.hua.Compiler - Semantic check
22:43:08.865 [main] INFO  org.hua.Compiler - Input:
class Test1 {
    String text;
    void print() {
        write(text);
    }
}

22:43:08.868 [main] INFO  org.hua.Compiler - Generating Byte Code for FinalTestFiles/test1.huava
// class version 49.0 (49)
// access flags 0x1
public class Test1 {

  // compiled from: FinalTestFiles/test1.huava

  // access flags 0x1
  public Ljava/lang/String; text

  // access flags 0x1
  public <init>()V
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x1
  public print()V
    ALOAD 0
    GETFIELD Test1.text : Ljava/lang/String;
    ASTORE 3
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 3
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    RETURN
    MAXSTACK = 2
    MAXLOCALS = 4
}
22:43:08.896 [main] INFO  org.hua.Compiler - Compilation done!
22:43:08.897 [main] INFO  org.hua.Compiler - Scanning file FinalTestFiles/test2.huava
22:43:08.901 [main] INFO  org.hua.Compiler - Constructed AST
22:43:08.901 [main] INFO  org.hua.Compiler - Building symbol table
22:43:08.902 [main] INFO  org.hua.Compiler - Semantic check
22:43:08.902 [main] INFO  org.hua.Compiler - Input:
class Test2 {
    Test1 m;
    void printMany(int count) {
        if(count > 0) {
            m.print();
            printMany(count - 1);
        }
    }
}

22:43:08.903 [main] INFO  org.hua.Compiler - Generating Byte Code for FinalTestFiles/test2.huava
// class version 49.0 (49)
// access flags 0x1
public class Test2 {

  // compiled from: FinalTestFiles/test2.huava

  // access flags 0x1
  public LTest1; m

  // access flags 0x1
  public <init>()V
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x1
  public printMany(I)V
    ILOAD 1
    LDC 0
    IF_ICMPLE L0
    ALOAD 0
    GETFIELD Test2.m : LTest1;
    INVOKEVIRTUAL Test1.print ()V
    ALOAD 0
    ILOAD 1
    LDC 1
    ISUB
    INVOKEVIRTUAL Test2.printMany (I)V
   L0
    RETURN
    MAXSTACK = 5
    MAXLOCALS = 3
}
22:43:08.905 [main] INFO  org.hua.Compiler - Compilation done!
22:43:08.905 [main] INFO  org.hua.Compiler - Scanning file FinalTestFiles/test3.huava
22:43:08.909 [main] INFO  org.hua.Compiler - Constructed AST
22:43:08.909 [main] INFO  org.hua.Compiler - Building symbol table
22:43:08.910 [main] INFO  org.hua.Compiler - Semantic check
22:43:08.912 [main] INFO  org.hua.Compiler - Input:
class Test3 {
    static float modOperation(float afterMod, int number) {
        return number % afterMod;
    }
    static void main() {
        int i;
        i = 0;
        while(i < 10) {
            float j;
            j = i + 1;
            write(j + "st time in the loop.");
            write("\n");
            i = i + 1;
            continue;
            write("I will never be executed...");
        }
        while(! i) {
            write("I will only be executed once...\n");
            break;
        }
        if(i != 10) {
            Test2 p;
            p = new Test2();
            p.m = new Test1();
            p.m.text = "Hello World ????? :'( \n";
            p.printMany(10);
        }
        else {
            Test2 p;
            p = new Test2();
            p.m = new Test1();
            p.m.text = "Hello World :) \n";
            p.printMany(1);
        }
        float f;
        f = 0.654;
        float final;
        write("Operating mod...\n");
        final = modOperation(f, i);
        write("The mod result is: ");
        write(final + "\n");
        String s;
        s = "Lets test null\n";
        write(s);
        s = null;
        write("Null worked!\n");
        write("Done!\n");
    }
}

22:43:08.933 [main] INFO  org.hua.Compiler - Generating Byte Code for FinalTestFiles/test3.huava
// class version 49.0 (49)
// access flags 0x1
public class Test3 {

  // compiled from: FinalTestFiles/test3.huava

  // access flags 0x1
  public <init>()V
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static modOperation(FI)F
    ILOAD 1
    FLOAD 0
    FSTORE 10
    I2F
    FLOAD 10
    FREM
    FRETURN
    MAXSTACK = 5
    MAXLOCALS = 11

  // access flags 0x9
  public static main([Ljava/lang/String;)V
    LDC 0
    ISTORE 2
   L0
    ILOAD 2
    LDC 10
    IF_ICMPGE L1
    ILOAD 2
    LDC 1
    IADD
    I2F
    FSTORE 3
    FLOAD 3
    LDC "st time in the loop."
    ASTORE 10
    INVOKESTATIC java/lang/Float.toString (F)Ljava/lang/String;
    ALOAD 10
    SWAP
    NEW java/lang/StringBuilder
    DUP
    INVOKESPECIAL java/lang/StringBuilder.<init> ()V
    SWAP
    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    SWAP
    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    LDC "\n"
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    ILOAD 2
    LDC 1
    IADD
    ISTORE 2
    GOTO L0
    LDC "I will never be executed..."
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    GOTO L0
   L1
   L2
    ILOAD 2
    IFEQ L3
    LDC "I will only be executed once...\n"
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    GOTO L4
    GOTO L2
   L3
   L4
    ILOAD 2
    LDC 10
    IF_ICMPEQ L5
    NEW Test2
    DUP
    INVOKESPECIAL Test2.<init> ()V
    ASTORE 4
    ALOAD 4
    NEW Test1
    DUP
    INVOKESPECIAL Test1.<init> ()V
    PUTFIELD Test2.m : LTest1;
    ALOAD 4
    GETFIELD Test2.m : LTest1;
    LDC "Hello World ????? :'( \n"
    PUTFIELD Test1.text : Ljava/lang/String;
    ALOAD 4
    LDC 10
    INVOKEVIRTUAL Test2.printMany (I)V
    GOTO L6
   L5
    NEW Test2
    DUP
    INVOKESPECIAL Test2.<init> ()V
    ASTORE 5
    ALOAD 5
    NEW Test1
    DUP
    INVOKESPECIAL Test1.<init> ()V
    PUTFIELD Test2.m : LTest1;
    ALOAD 5
    GETFIELD Test2.m : LTest1;
    LDC "Hello World :) \n"
    PUTFIELD Test1.text : Ljava/lang/String;
    ALOAD 5
    LDC 1
    INVOKEVIRTUAL Test2.printMany (I)V
   L7
   L6
    LDC 0.654
    FSTORE 6
    LDC "Operating mod...\n"
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    ALOAD 0
    FLOAD 6
    ILOAD 2
    INVOKESTATIC Test3.modOperation (FI)F
    FSTORE 7
    LDC "The mod result is: "
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    FLOAD 7
    LDC "\n"
    ASTORE 10
    INVOKESTATIC java/lang/Float.toString (F)Ljava/lang/String;
    ALOAD 10
    SWAP
    NEW java/lang/StringBuilder
    DUP
    INVOKESPECIAL java/lang/StringBuilder.<init> ()V
    SWAP
    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    SWAP
    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    LDC "Lets test null\n"
    ASTORE 8
    ALOAD 8
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    ACONST_NULL
    ASTORE 8
    LDC "Null worked!\n"
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    LDC "Done!\n"
    ASTORE 10
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ALOAD 10
    INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V
    RETURN
    MAXSTACK = 35
    MAXLOCALS = 11
}
22:43:08.942 [main] INFO  org.hua.Compiler - Compilation done!
22:43:08.942 [main] INFO  org.hua.Compiler - Loading class Test3.huaclass
22:43:08.942 [main] INFO  org.hua.Compiler - Test3.huaclass successfully loaded
22:43:08.942 [main] INFO  org.hua.Compiler - Running main function from Test3 huaclass using reflection:
1.0st time in the loop.
2.0st time in the loop.
3.0st time in the loop.
4.0st time in the loop.
5.0st time in the loop.
6.0st time in the loop.
7.0st time in the loop.
8.0st time in the loop.
9.0st time in the loop.
10.0st time in the loop.
I will only be executed once...
Hello World :) 
Operating mod...
The mod result is: 0.19000024
Lets test null
Null worked!
Done!
22:43:08.955 [main] INFO  org.hua.Compiler - Finished execution!
```
