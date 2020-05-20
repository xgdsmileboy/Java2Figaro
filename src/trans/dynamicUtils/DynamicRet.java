package trans.dynamicUtils;

import trans.common.LevelLogger;
import trans.common.Util;
import trans.staticUtils.Stmt;
import trans.staticUtils.VarNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DynamicRet extends DynamicMsg {
    private String _value = null;
    private DynamicStmt _structure = null;
    private Map<String, String> _useIDMap = new HashMap<>();
    private ArrayList<FieldRelation> _relationList = new ArrayList<>();
    private ArrayList<String> _exRet;

    DynamicRet(DynamicInfo info, int line, int column, Stmt stmt, String value, ArrayList<String> exRet) {
        super(info, line, column, stmt);
        _value = value;
        _figaroID = "Ret";
        _exRet = exRet;
    }

    DynamicRet(DynamicInfo info, ArrayList<String> exRet) {
        super(info, 0, 0, null);
        _figaroID = "Ret";
        _exRet = exRet;
    }

    private DynamicStmt getStructure(Stmt stmt) {
        Stmt structure = stmt.getStructure();
        if (structure != null) {
            DynamicStmt dycStructure = _info.getStructure(structure);
            if (dycStructure == null)
                LevelLogger.error("ERROR : Structure Not Found : line " + getLine() + " , column " + getColumn());
            return dycStructure;
        }
        return null;
    }

    void parse() {
        if (_msg != null) {
            Stmt stmt = (Stmt) _msg;
            for (VarNode var : stmt.getUseList()) {
                FieldRelation relation = _info.genFieldRelation(var);
                if (relation != null) _relationList.add(relation);

                String varFigaroID = _info.genVarFigaroID(var, false);
                if (varFigaroID != null)
                    _useIDMap.put(var.getID(), varFigaroID);
            }
            _structure = getStructure(stmt);
        }

        for (String varID : _exRet) {
            VarNode var = new VarNode(varID);
            FieldRelation relation = _info.genFieldRelation(var);
            if (relation != null) _relationList.add(relation);

            String varFigaroID = _info.genVarFigaroID(var, false);
            if (varFigaroID != null)
                _useIDMap.put(var.getID(), varFigaroID);
        }
    }

    public ArrayList<String> getUseList() {
        ArrayList<String> useList = new ArrayList<>(_useIDMap.values());
        if (_structure != null)
            useList.add(_structure.getFigaroID());
        useList.addAll(_info.getConstraintList(_figaroID));
        return useList;
    }

    String genSource() {
        StringBuilder source = new StringBuilder();
        for (FieldRelation relation : _relationList)
            source.append(DynamicInfo.genDefinitionSource(relation.getDef(), relation.getUse(), Util.SEMANTIC_LOW_PROBABILITY));
        source.append(DynamicInfo.genDefinitionSource(_figaroID, getUseList(), Util.SEMANTIC_LOW_PROBABILITY));
        return source.toString();
    }
}
