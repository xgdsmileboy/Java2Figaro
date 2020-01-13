package trans.staticUtils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Stmt extends StaticMsg {
    private Stmt _structure;
    private ASTNode _node;
    private ArrayList<Assign> _assignList = new ArrayList<>();
    private ArrayList<ControlExpression> _controlExprList = new ArrayList<>();
    private Map<String, VarNode> _useList = new HashMap<>();

    Stmt(ASTNode node, Stmt structure, int line, int column) {
        super(line, column);
        _node = node;
        _structure = structure;
    }

    ASTNode getNode() {
        return _node;
    }

    void addAssign(Assign assign) {
        _assignList.add(assign);
    }

    ArrayList<Assign> getAssignList() {
        return _assignList;
    }

    void addControlExpr(ControlExpression expr) {
        _controlExprList.add(expr);
    }

    ArrayList<ControlExpression> getControlExprList() {
        return _controlExprList;
    }

    public boolean isControlStmt() {
        return _controlExprList.size() > 0;
    }

    void addUse(VarNode use) {
        String varID = use.getID();
        if (!_useList.containsKey(varID))
            _useList.put(varID, use);
    }

    public ArrayList<VarNode> getUseList() {
        return new ArrayList<>(_useList.values());
    }

    public Stmt getStructure() {
        return _structure;
    }
}