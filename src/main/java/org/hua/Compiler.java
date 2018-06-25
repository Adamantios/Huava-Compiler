package org.hua;

import org.hua.ast.ASTNode;
import org.hua.ast.ASTVisitor;
import org.hua.ast.ASTVisitorException;
import org.hua.helpers.ReloadingClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

public class Compiler {

    /**
     * A Logger for our compiler messages.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    /**
     * The index of the first file's position.
     */
    private static int firstFilePos;

    /**
     * The encoding of the input files.
     */
    private static String encodingName;

    /**
     * Flag which shows if the user wants to produce the huaclasses in files.
     */
    private static boolean produceClasses;

    /**
     * An argument's name without extension.
     */
    private static String argNameWithoutExtension;

    /**
     * A Lexer scanner.
     */
    private static Lexer scanner;

    /**
     * The Compound Unit of the program.
     */
    private static ASTNode compUnit;

    /**
     * A class Loader.
     */
    private static ReloadingClassLoader reloadingClassLoader;

    /**
     * The name of the class which contains the main function.
     */
    private static String className;

    /**
     * The main function.
     */
    private static Method main;

    /**
     * The arguments which will be passed to the user's program.
     * <br>
     * NOTE: This compiler's version does not support program arguments, so this will be null.
     */
    @SuppressWarnings("unused")
    private static String[] params;

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        if (args.length == 0)
            // noinspection SpellCheckingInspection
            LOGGER.info("Usage : java Compiler [ --encoding <name> ] [ --generate ] <inputfile(s)>");

        else {
            encodingName = "UTF-8";
            if (args.length > 1) {
                if (!parseForEncoding(args[0], args[1]))
                    return;

                parseForGenerate(args[firstFilePos]);
            }

            // Create a class loader.
            reloadingClassLoader = new ReloadingClassLoader(ClassLoader.getSystemClassLoader());

            // For every input file:
            for (int i = firstFilePos; i < args.length; i++) {
                try {
                    checkFileExtension(args[i]);
                    scanFile(args[i], encodingName);
                    parseFile();
                    setRegistryRoot();
                    buildSymbolTableAndLocalVariables();
                    constructTypes(args.length > i + 1);
                    printProgram();
                    generateByteCode();
                } catch (java.io.FileNotFoundException e) {
                    LOGGER.error("File not found : \"" + args[i] + "\"");
                } catch (java.io.IOException e) {
                    LOGGER.error("IO error scanning file \"" + args[i] + "\"");
                    LOGGER.error(e.toString());
                } catch (HuavaException e) {
                    LOGGER.error(e.getMessage());
                } catch (ASTVisitorException e) {
                    LOGGER.error(e.getMessage());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }

            try {
                loadClass();
            } catch (ClassNotFoundException e) {
                LOGGER.error("One or more of your program's classes was not found!");
            } catch (NoSuchMethodException e) {
                LOGGER.error("'main' function was not found in your program.");
            }

            runMainFunction();
        }
    }

    /**
     * Parses the arguments and returns the encoding choice.
     * <br>
     * If the user has not given it, the default value is UTF-8.
     * <br>
     * If encoding name is invalid, it returns null.
     *
     * @param arg1 the first argument.
     * @param arg2 the second argument.
     */
    private static boolean parseForEncoding(String arg1, String arg2) {
        firstFilePos = 0;

        if (arg1.equals("--encoding")) {
            firstFilePos = 2;
            encodingName = arg2;
            try {
                java.nio.charset.Charset.forName(encodingName); // Side-effect: is encodingName valid?
            } catch (Exception e) {
                LOGGER.error("Invalid encoding '" + encodingName + "'");
                return false;
            }
        }
        return true;
    }

    /**
     * Parses the argument and returns if the classes are going to be generated.
     * <br>
     * The default value is false.
     *
     * @param arg the argument.
     */
    private static void parseForGenerate(String arg) {
        if (arg.equals("--generate")) {
            firstFilePos++;
            produceClasses = true;
        } else
            produceClasses = false;
    }

    /**
     * Checks the current file's extension. If it is not '.huava', an Exception is being thrown.
     *
     * @param fileName the file's name.
     * @throws HuavaException
     */
    private static void checkFileExtension(String fileName) throws HuavaException {
        // Split filename.
        String[] argNameSeparated = fileName.split("\\.");
        argNameWithoutExtension = argNameSeparated[0];
        if (argNameSeparated.length > 1) {
            String argExtension = argNameSeparated[1];

            // Check file's extension.
            if (!argExtension.equals("huava"))
                throw new HuavaException("Unknown extension found in file "
                        + argNameWithoutExtension + ": Try '.huava' instead.");
        }
    }

    /**
     * Scans the current file.
     *
     * @param fileName     the file's name.
     * @param encodingName the encoding of the file.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    private static void scanFile(String fileName, String encodingName) throws FileNotFoundException, UnsupportedEncodingException {
        java.io.FileInputStream stream = new java.io.FileInputStream(fileName);
        LOGGER.info("Scanning file " + fileName);
        java.io.Reader reader = new java.io.InputStreamReader(stream, encodingName);
        scanner = new Lexer(reader);
    }

    /**
     * Parses the current file.
     *
     * @throws Exception
     */
    private static void parseFile() throws Exception {
        parser p = new parser(scanner);
        compUnit = (ASTNode) p.parse().value;
        LOGGER.info("Constructed AST");
    }

    /**
     * Keeps the global instance of the compound unit.
     */
    private static void setRegistryRoot() {
        Registry.getInstance().setRoot(compUnit);
    }

    /**
     * Builds the symbol table and the local variables.
     *
     * @throws ASTVisitorException
     */
    private static void buildSymbolTableAndLocalVariables() throws ASTVisitorException {
        // Build symbol table.
        LOGGER.info("Building symbol table");
        compUnit.accept(new SymTableBuilderASTVisitor());
        // Build local variables index.
        LOGGER.debug("Building local variables index");
        compUnit.accept(new LocalIndexBuilderASTVisitor());
    }

    /**
     * Collects the symbols an the types of the code.
     * <br>
     * Also makes all the necessary symbol and type checking.
     *
     * @param moreFilesExpected flag which shows if there are more files expected as input.
     * @throws ASTVisitorException
     */
    private static void constructTypes(boolean moreFilesExpected) throws ASTVisitorException {
        LOGGER.info("Semantic check");
        compUnit.accept(new CollectSymbolsASTVisitor());
        compUnit.accept(new CollectTypesASTVisitor(moreFilesExpected));
    }

    /**
     * Prints the user's code formatted and with colors.
     *
     * @throws ASTVisitorException
     */
    private static void printProgram() throws ASTVisitorException {
        LOGGER.info("Input:");
        ASTVisitor printVisitor = new PrintASTVisitor();
        compUnit.accept(printVisitor);
    }

    /**
     * Generates the byte code, prints it, produces the classes and registers them to the Class Loader.
     *
     * @throws IOException
     * @throws ASTVisitorException
     */
    private static void generateByteCode() throws IOException, ASTVisitorException {
        // Print Byte code.
        LOGGER.info("Generating Byte Code for " + argNameWithoutExtension + ".huava");
        ByteCodeGeneratorASTVisitor byteCodeGeneratorASTVisitor =
                new ByteCodeGeneratorASTVisitor(argNameWithoutExtension);
        compUnit.accept(byteCodeGeneratorASTVisitor);

        LOGGER.info("Compilation done!");

        // Get all the class' bytes with their names.
        TreeMap<String, byte[]> classBytesWithIds = byteCodeGeneratorASTVisitor.getClassBytes();

        // For each class of the file:
        for (Map.Entry<String, byte[]> pair : classBytesWithIds.entrySet()) {
            if (produceClasses)
                generateClasses(pair);
            registerClass(pair);
        }
    }

    /**
     * Registers a class to the Class Loader.
     *
     * @param pair a pair containing the class name with its bytes.
     */
    private static void registerClass(Map.Entry<String, byte[]> pair) {
        reloadingClassLoader.register(pair.getKey(), pair.getValue());
    }

    /**
     * Generates the given class in a folder called huaclasses. If the folder does not exist it creates it.
     *
     * @param pair a pair containing the class name with its bytes.
     * @throws IOException
     */
    private static void generateClasses(Map.Entry<String, byte[]> pair) throws IOException {
        LOGGER.info("Generating class " + pair.getKey() + ".huaclass");
        File f = new File("huaclasses");
        //noinspection ResultOfMethodCallIgnored
        f.mkdirs();
        FileOutputStream fos = new FileOutputStream(f + "/" + pair.getKey() + ".huaclass");
        fos.write(pair.getValue());
        fos.close();
        LOGGER.info(pair.getKey() + ".huaclass successfully generated");
    }

    /**
     * Loads the Class which has the main function.
     */
    private static void loadClass() throws ClassNotFoundException, NoSuchMethodException {
        className = ByteCodeGeneratorASTVisitor.getClassWithMain();
        if (className != null) {
            LOGGER.info("Loading class " + className + ".huaclass");
            Class<?> c = reloadingClassLoader.loadClass(className);
            main = c.getMethod("main", String[].class);
            LOGGER.info(className + ".huaclass successfully loaded");
        }
    }

    /**
     * Runs the main function.
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static void runMainFunction() throws InvocationTargetException, IllegalAccessException {
        if (className != null && main != null) {
            LOGGER.info("Running main function from "
                    + className + " huaclass using reflection:");
            main.invoke(null, (Object) params);
            LOGGER.info("Finished execution!");
        } else
            LOGGER.error("Could not find and run main function!");
    }

    /**
     * An Exception thrown by CheckFileExtension when the extension is not .huava.
     */
    private static class HuavaException extends Exception {
        private HuavaException(String message) { super(message); }
    }
}
