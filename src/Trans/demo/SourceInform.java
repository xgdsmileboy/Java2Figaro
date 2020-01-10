package Trans.demo;

import javafx.util.Pair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SourceInform {
    private static int _patchNum = 0;
    private int _patchID;
    private ArrayList<VarNode> _entry = new ArrayList<>();;
    private ArrayList<Stmt> _statements = new ArrayList<>();
    private Map<String, VarNode> _vars = new HashMap<String, VarNode>();
    private Map<String, VarNode> _ret = new HashMap<String, VarNode>();

    SourceInform() {
        _patchID = ++_patchNum;
    }

    VarNode addVar(String varID) {
        String varName = VarNode.transID2Name(varID);
        if (_vars.containsKey(varName))
            return _vars.get(varName);
        VarNode var = new VarNode(varName, this);
        _vars.put(varName, var);
        return var;
    }

    public VarNode getVar(String varID) {
        String varName = VarNode.transID2Name(varID);
        if (_vars.containsKey(varName))
            return _vars.get(varName);
        return null;
    }

    void buildEntryList() {
        for(Stmt stmt : _statements)
            if (stmt.getNode() instanceof MethodDeclaration)
                for (Assign assign : stmt.getAssign())
                    _entry.add(assign.getDef());
    }

    ArrayList<VarNode> getEntryList() {
        return _entry;
    }

    void addReturn(VarNode var) {
        String varName = var.getName();
        if (!_ret.containsKey(varName))
            _ret.put(varName, var);
    }

    void buildReturnList() {
        for(Stmt stmt : _statements)
            if (stmt.getNode() instanceof ReturnStatement)
                for(VarNode var : stmt.getUse())
                    addReturn(var);
    }

    ArrayList<VarNode> getReturnList() {
        return new ArrayList<>(_ret.values());
    }

    void addStatement(Stmt stmt) {
        _statements.add(stmt);
    }

    ArrayList<Stmt> getStatementList() {
        return _statements;
    }

    public int getPatchID() {
        return _patchID;
    }


    /***************Figaro Source Generate*********************/
    private ArrayList<Pair<String, Double>> _varObservation = new ArrayList<>();
    private Map<String, Integer> _varDefTime = new HashMap<>();
    StringBuilder _source = null;



    String genFigaroSource() {
        _varDefTime.clear();
        _source = new StringBuilder("");
        _varObservation.add(new Pair<>("Var_this_even", 0.8));
        _varObservation.add(new Pair<>("Var_this_size", 0.9));

        genImport();
        genMethodDeclaration();
        genEntryDeclaration();
        genSemanticRelation();
        genEntryObservation();
        genVarObservation();
        genSampling();
        genMethodDeclarationEnd();
        return _source.toString();
    }

    private String genVarFigaroID(VarNode var, boolean isDef, boolean needInit) {
        String varName = var.getName();
        //System.out.println(varName + " " + isDef);

        if (_varDefTime.containsKey(varName)) {
            int varTime = _varDefTime.get(varName);
            if (isDef) {
                varTime = varTime + 1;
                _varDefTime.put(varName, varTime);
            }
            return "Var_" + varName + ((needInit) || (varTime == 0) ? "" : "_" + varTime);
        }

        if (!isDef) _source.append(genVarDeclaration("Var_" + varName));
        _varDefTime.put(varName, 0);
        return "Var_" + varName;
    }

    private String genVarDeclaration(String varFigaroName) {
        return "    val " + varFigaroName + " = Flip(0.5)\n";
    }

    private String genVarDefinition(String def, ArrayList<String> useList) {
        int size = useList.size();

        if (size == 0)
            return genVarDeclaration(def);

        StringBuilder source = new StringBuilder("    val " + def + "= RichCPD(");
        for (String use : useList)
            source.append(use).append(", ");
        source.append("\n");

        source.append("      (" + "OneOf(true)");
        for (int i = 1; i < size; i++)
            source.append(", OneOf(true)");
        source.append(") -> Flip(0.95),\n");

        source.append("      (" + "*");
        for (int i = 1; i < size; i++)
            source.append(", *");
        source.append(") -> Flip(0.05))\n");

        return source.toString();
    }

    private void genSampling() {
        _source.append("    //-------------Sampling--------------\n");
        _source.append("    val samplePatchValid = VariableElimination(Ret)\n");
        _source.append("    samplePatchValid.start()\n");
        _source.append("    println(\"Probability of test:\" + samplePatchValid.probability(Ret, true))\n");
        _source.append("    samplePatchValid.kill()\n");
    }

    private void genVarObservation() {
        _source.append("    //-------------Constraint--------------\n");

        StringBuilder source = new StringBuilder();
        for (Pair<String, Double> it : _varObservation) {
            source.append("    ");
            source.append(it.getKey());
            source.append(".addConstraint((b: Boolean) => if (b) ");
            source.append(it.getValue());
            source.append(" else ");
            source.append(1 - it.getValue());
            source.append(")\n");
        }

        _source.append(source);
        _source.append("\n");
    }

    private void genEntryObservation() {
        _source.append("    //-------------Observation--------------\n");

        StringBuilder source = new StringBuilder();
        for (VarNode var : _entry)
            source.append("    ").append(genVarFigaroID(var, false, true)).append(".observe(true)\n");

        _source.append(source);
        _source.append("\n");
    }

    private ArrayList<String> getVarFigaroNameList(ArrayList<VarNode> varList, boolean isDef) {
        ArrayList<String> nameList = new ArrayList<>();
        for (VarNode var : varList)
            nameList.add(genVarFigaroID(var, isDef, false));
        return nameList;
    }

    private void genSemanticRelation() {
        _source.append("    //-------------Semantic--------------\n");

        StringBuilder source = new StringBuilder();
        for (Stmt stmt : _statements)
            if (!(stmt.getNode() instanceof MethodDeclaration))
                for (Assign assign : stmt.getAssign()) {
                    ArrayList<String> use = getVarFigaroNameList(assign.getUse(), false);
                    String def = genVarFigaroID(assign.getDef(), true, false);
                    source.append(genVarDefinition(def, use));
                }

        ArrayList<String> retList = getVarFigaroNameList(new ArrayList<>(_ret.values()), false);
        source.append(genVarDefinition("Ret", retList));
        _source.append(source);
        _source.append("\n");
    }

    private void genEntryDeclaration() {
        _source.append("    //-------------Entry--------------\n");
        StringBuilder source = new StringBuilder();

        for (VarNode var : _entry)
            source.append(genVarDeclaration(genVarFigaroID(var, true, false)));
        _source.append(source);
        _source.append("\n");
    }

    private void genMethodDeclarationEnd() {
        _source.append("  }\n");
        _source.append("}\n");
    }

    private void genMethodDeclaration() {
        _source.append("object patch {\n");
        _source.append("  def main(args: Array[String]): Unit = {\n");
    }

    private void genImport() {
        _source.append("import com.cra.figaro.algorithm.factored.VariableElimination\n");
        _source.append("import com.cra.figaro.language._\n");
        _source.append("import com.cra.figaro.library.compound._\n\n");
    }
}
