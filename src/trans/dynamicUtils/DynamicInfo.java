package trans.dynamicUtils;

import trans.staticUtils.*;
import trans.demo.LevelLogger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.util.Pair;
import trans.strategy.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicInfo {
    private StaticInfo _stcInfo;
    private JSONObject _json;

    DynamicInfo(StaticInfo stcInfo, JSONObject json) {
        _stcInfo = stcInfo;
        _json = json;
    }
    
    /***************Dynamic Inform Parse*********************/
    private ArrayList<DynamicMsg> _msgList = new ArrayList<>();
    private Map<String, DynamicStmt> _structureMap = new HashMap<>();
    private Map<String, Integer> _varDefTime = new HashMap<>();
    private Map<String, Integer> _stmtDefTime = new HashMap<>();
    private Map<String, Integer> _controlDefTime = new HashMap<>();
    private ArrayList<DynamicCtrlExpr> _controlList = new ArrayList<>();
    private ArrayList<Pair<String, Double>> _varObservation = new ArrayList<>();

    void parse() {
        JSONArray data = (JSONArray) _json.get("data");
        for (Object obj : data) {
            JSONObject jsonMsg = (JSONObject) obj;
            Long line = (Long) jsonMsg.get("line");
            Long column = (Long) jsonMsg.get("column");
            String type = (String) jsonMsg.get("type");
            Object value = jsonMsg.get("value");
            addMessage(line.intValue(), column.intValue(), type, value);
        }

        for (DynamicMsg msg : _msgList)
            msg.parse();

        Strategy stgGather = new Strategy();
        stgGather.init();
        _varObservation = stgGather.parse(_msgList);
    }

    private void addMessage(int line, int column, String type, Object value) {
        switch (type) {
            case "ASSIGN":
                Assign assign = _stcInfo.getAssign(line, column);
                if (assign == null)
                    LevelLogger.error("JSON INFORMATION ASSIGN NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION ASSIGN VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicAssign(this, line, column, assign, (String)value));
                break;
            case "ENTRY":
                Assign entry = _stcInfo.getAssign(line, column);
                if (entry == null)
                    LevelLogger.error("JSON INFORMATION ENTRY NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION ENTRY VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicEntry(this, line, column, entry, (String)value));
                break;
            case "RET":
                Stmt ret = _stcInfo.getStmt(line, column);
                if (ret == null)
                    LevelLogger.error("JSON INFORMATION RET NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof String))
                    LevelLogger.error("JSON INFORMATION ASSIGN VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicRet(this, line, column, ret, (String)value));
                break;
            case "CONTROL":
                ControlExpression ctrl = _stcInfo.getCtrlExpr(line, column);
                if (ctrl == null)
                    LevelLogger.error("JSON INFORMATION CONTROL EXPRESSION NOT FOUND : line " + line + ", column " + column);
                if (!(value instanceof Boolean))
                    LevelLogger.error("JSON INFORMATION CONTROL EXPRESSION VALUE ERROR : line " + line + ", column " + column);
                _msgList.add(new DynamicCtrlExpr(this, line, column, ctrl, (boolean)value));
                break;
            case "STMT":
                Stmt stmt = _stcInfo.getStmt(line, column);
                if (stmt == null)
                    LevelLogger.error("JSON INFORMATION STMT NOT FOUND : line " + line + ", column " + column);
                _msgList.add(new DynamicStmt(this, line, column, stmt));
                break;
            default:
                LevelLogger.error("JSON INFORMATION TYPE ERROR : line " + line + ", column " + column);
        }
    }

    String genVarFigaroID(VarNode var, boolean isDef) {
        String varName = var.getName();

        if (_varDefTime.containsKey(varName)) {
            int varTime = _varDefTime.get(varName);
            if (isDef) {
                varTime = varTime + 1;
                _varDefTime.put(varName, varTime);
            }
            return "Var_" + varName + (varTime == 0 ? "" : "_" + varTime);
        }

        if (!isDef)
            LevelLogger.error("ERROR : Use Without Define : " + varName);
        _varDefTime.put(varName, 0);
        //if (!isDef) _source.append(genVarDeclaration("Var_" + varName));
        //_varDefTime.put(varName, 0);
        return "Var_" + varName;
    }

    DynamicStmt getStructure(Stmt stmt) {
        return _structureMap.get(stmt.getKey());
    }

    void setStructure(Stmt stmt, DynamicStmt dycStmt) {
        _structureMap.put(stmt.getKey(), dycStmt);
    }

    void addCtrlExpr(DynamicCtrlExpr ctrl) {
        _controlList.add(ctrl);
    }

    ArrayList<DynamicCtrlExpr> getCtrlList() {
        ArrayList<DynamicCtrlExpr> newList = new ArrayList<>(_controlList);
        _controlList.clear();
        return newList;
    }

    String genStmtFigaroID(DynamicStmt dycStmt) {
        String key = dycStmt.getKey();
        if (_stmtDefTime.containsKey(key)) {
            int ctrlTime = _stmtDefTime.get(key) + 1;
            _stmtDefTime.put(key, ctrlTime);
            return "Stmt_" + key + "_" + ctrlTime;
        }
        _stmtDefTime.put(key, 0);
        return "Stmt_" + key;
    }

    String genCtrlFigaroID(DynamicCtrlExpr dycCtrl) {
        String key = dycCtrl.getKey();
        if (_controlDefTime.containsKey(key)) {
            int ctrlTime = _controlDefTime.get(key) + 1;
            _controlDefTime.put(key, ctrlTime);
            return "Control_" + key + "_" + ctrlTime;
        }
        _controlDefTime.put(key, 0);
        return "Control_" + key;
    }

    /***************Figaro Source Generate*********************/

    public String genFigaroSource() {
        StringBuilder _source = new StringBuilder("");

        _source.append("import com.cra.figaro.algorithm.factored.VariableElimination\n");
        _source.append("import com.cra.figaro.language._\n");
        _source.append("import com.cra.figaro.library.compound._\n\n");

        _source.append("object patch {\n");
        _source.append("  def main(args: Array[String]): Unit = {\n");

        _source.append("    //-------------Semantic--------------\n");
        for (DynamicMsg msg : _msgList)
            _source.append(msg.genSource());
        _source.append("\n");

        _source.append("    //-------------Observation--------------\n");
        for (DynamicMsg msg : _msgList)
            if (msg instanceof DynamicEntry)
                _source.append("    ").append(msg.getFigaroID()).append(".observe(true)\n");
        _source.append("\n");

        _source.append("    //-------------Constraint--------------\n");
        for (Pair<String, Double> it : _varObservation) {
            _source.append("    ");
            _source.append(it.getKey());
            _source.append(".addConstraint((b: Boolean) => if (b) ");
            _source.append(it.getValue());
            _source.append(" else ");
            _source.append(1 - it.getValue());
            _source.append(")\n");
        }
        _source.append("\n");

        _source.append("    //-------------Sampling--------------\n");
        _source.append("    val samplePatchValid = VariableElimination(Ret)\n");
        _source.append("    samplePatchValid.start()\n");
        _source.append("    println(\"Probability of test:\" + samplePatchValid.probability(Ret, true))\n");
        _source.append("    samplePatchValid.kill()\n");
        _source.append("  }\n");
        _source.append("}\n");

        return _source.toString();
    }
}