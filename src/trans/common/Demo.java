package trans.common;

import trans.patchsim.PatchSimParser;

import java.io.File;
import java.util.ArrayList;

public class Demo {

    public static void main(String[] args) {
//        runJsonFile();
        runProject1();
    }

    private final static String DATA_BASE = "/Users/Jack/Code/IntelliJ/Java2Figaro/Java2FigaroData";

    private static void runPatchSimByFiles() {
        String workPath = DATA_BASE;
        System.out.println(PatchSimParser.analyzeByFiles(new File(workPath)));
    }

    private static void runJsonFile() {
        String workPath = DATA_BASE;
        Transverse.runByJsonFile(new File(workPath));
    }

    private static void runFinalResultGenerate() {
        String resultFilePath = DATA_BASE + "/Output.txt";
        Transverse.genMultiTestResultByFiles(new File(resultFilePath));
    }

    private static void runProject1() {
        String originProjectPath = DATA_BASE + "/tmp";
        String copyProjectPath = DATA_BASE + "/copy";
        String srcFilePath = "/src/main/java/introclassJava/smallest_af81ffd4_000.java";
        String testName = "introclassJava.smallest_af81ffd4_000BlackboxTest#test5";
        String methodName = "exec";

        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();
        exEntry.add("scanner");
        exEntry.add("output");
        exRet.add("output");

        Transverse.TestCaseResult testCaseResult = Transverse.runProjectTestCase(new File(originProjectPath), new File(copyProjectPath), srcFilePath, testName, methodName, exEntry, exRet);
        System.out.println(String.format("%.4f", testCaseResult.getProbability() * 100.0));
    }

    public static void runProject2() {
        File projectDir = new File(DATA_BASE + "/digits_d5059e2b_000");
        File mutantFile = new File(projectDir, "mutants/digits_d5059e2b_000_CapGen_12.java");
        String srcFilePath = "/src/main/java/introclassJava/digits_d5059e2b_000.java";
        String testName = "digits_d5059e2b_000BlackboxTest#test4";
        File originProjectDir = new File(projectDir, "source");
        String copyProjectPath = DATA_BASE + "/copy";
        String methodName = "exec";

        ArrayList<String> exEntry = new ArrayList<>();
        ArrayList<String> exRet = new ArrayList<>();
        exEntry.add("scanner");
        exEntry.add("output");
        exRet.add("output");

        Transverse.TestCaseResult testCaseResult = Transverse.runMutantTestCase(originProjectDir, new File(copyProjectPath), srcFilePath, mutantFile, testName, methodName, exEntry, exRet);
        System.out.println(String.format("%.4f", testCaseResult.getProbability() * 100.0));
    }

    public static void parseResult() {
        String resultPath = "D:\\program\\workspace\\CapGen\\CapGenOutput";
        IntroClassScript.parseResult(resultPath);
    }

    public static void crawlIntroClassS3Patch(String[] args) {
        String IntroClassPath = "D:\\program\\workspace\\IntroClassJavaCopy";
        String mutationPath = "D:\\program\\workspace\\FSE2017-S3-SyntaxSemanticRepairData-master";
        IntroClassScript.crawl(IntroClassPath, mutationPath);
    }
}
