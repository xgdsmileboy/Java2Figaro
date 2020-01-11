package Trans.demo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

public class JavaParser {

    private static String readFileToString(String srcFile) {
        if (srcFile == null) {
            LevelLogger.error("#readFileToString Illegal input file path : null.");
            return "";
        }

        File file = new File(srcFile);
        if (!file.exists() || !file.isFile()) {
            LevelLogger.error("#readFileToString Illegal input file path : " + srcFile);
            return "";
        }

        StringBuffer stringBuffer = new StringBuffer();
        InputStream in = null;
        InputStreamReader inputStreamReader = null;
        try {
            in = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            char[] ch = new char[1024];
            int readCount = 0;
            while ((readCount = inputStreamReader.read(ch)) != -1) {
                stringBuffer.append(ch, 0, readCount);
            }
            inputStreamReader.close();
            in.close();

        } catch (Exception e) {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e1) {
                    return "";
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    return "";
                }
            }
        }
        return stringBuffer.toString();
    }

    public static void Analyze(String srcFile) {
        String source = readFileToString(srcFile);
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        astParser.setSource(source.toCharArray());
        CompilationUnit srcUnit = (CompilationUnit) astParser.createAST(null);

        MethodDeclCollector methodDeclCollector = new MethodDeclCollector();
        methodDeclCollector.init();
        srcUnit.accept(methodDeclCollector);
        List<MethodDeclaration> srcMethods = methodDeclCollector.getAllMethDecl();

        for (MethodDeclaration sm : srcMethods) {
            TmpASTVisitor parser = new TmpASTVisitor(srcUnit, srcFile);
            SourceInform inform = parser.process(sm);
            inform.build();
            printResult(inform);
        }
    }

    static void printResult(SourceInform inform) {
        inform.printAnalyzeInform();
        //System.out.println(inform.genFigaroSource());
    }

    static class MethodDeclCollector extends ASTVisitor {

        List<MethodDeclaration> methodDeclarations;

        public MethodDeclCollector() {
        }

        public void init() {
            methodDeclarations = new LinkedList<>();
        }

        public List<MethodDeclaration> getAllMethDecl() {
            return methodDeclarations;
        }

        public boolean visit(MethodDeclaration md) {
            methodDeclarations.add(md);
            return true;
        }
    }
}
